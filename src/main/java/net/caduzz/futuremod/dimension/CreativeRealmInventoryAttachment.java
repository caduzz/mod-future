package net.caduzz.futuremod.dimension;

import net.caduzz.futuremod.FutureMod;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Armazena inventários por dimensão para não compartilhar itens entre overworld e dimensão criativa.
 * Usa SavedData do Overworld + cache em memória para garantir restauração correta.
 */
public final class CreativeRealmInventoryAttachment {

    /** Cache em memória: playerUuid -> (dimensionKey -> dimData). Garante dados durante a sessão. */
    private static final Map<UUID, Map<String, CompoundTag>> MEMORY_CACHE = new ConcurrentHashMap<>();

    private CreativeRealmInventoryAttachment() {}

    /**
     * Salva o inventário do survival (overworld) e carrega o inventário da dimensão criativa (vazio).
     * Os itens do survival ficam salvos e voltam quando o jogador retorna ao overworld.
     */
    public static void saveAndSwapToCreativeRealm(ServerPlayer player) {
        ServerLevel overworld = player.getServer().overworld();
        if (overworld == null) return;

        DimensionInventorySavedData data = DimensionInventorySavedData.get(overworld);
        saveCurrentDimension(player, data);
        loadDimensionEmpty(player);
        data.markDirty();
    }

    /**
     * Salva o inventário da dimensão criativa e restaura o inventário da dimensão de destino.
     */
    public static void saveCreativeRealmAndRestore(ServerPlayer player, ResourceKey<Level> targetDimension) {
        ServerLevel overworld = player.getServer().overworld();
        if (overworld == null) return;

        DimensionInventorySavedData data = DimensionInventorySavedData.get(overworld);
        saveCurrentDimension(player, data);
        loadDimension(player, data, targetDimension);
        data.markDirty();
    }

    private static String key(ResourceKey<Level> dimension) {
        return dimension.location().toString();
    }

    /** Chaves alternativas para o overworld (varia entre versões do Minecraft). */
    private static final String OVERWORLD_KEY = Level.OVERWORLD.location().toString();
    private static final String OVERWORLD_KEY_ALT = "minecraft:the_overworld";

    private static void saveCurrentDimension(Player player, DimensionInventorySavedData data) {
        CompoundTag playerData = data.getOrCreatePlayerData(player.getUUID().toString());
        String k = key(player.level().dimension());

        ListTag list = new ListTag();
        NonNullList<ItemStack> main = player.getInventory().items;
        for (int i = 0; i < main.size(); i++) {
            ItemStack stack = main.get(i);
            if (stack.isEmpty()) continue;
            CompoundTag slot = new CompoundTag();
            stack.save(player.registryAccess(), slot);
            slot.putInt("Slot", i);
            list.add(slot);
        }
        ListTag armor = new ListTag();
        for (int i = 0; i < 4; i++) {
            ItemStack stack = player.getInventory().armor.get(i);
            if (stack.isEmpty()) continue;
            CompoundTag slot = new CompoundTag();
            stack.save(player.registryAccess(), slot);
            slot.putInt("Slot", i);
            armor.add(slot);
        }
        ListTag offhand = new ListTag();
        ItemStack offStack = player.getInventory().offhand.get(0);
        if (!offStack.isEmpty()) {
            CompoundTag off = new CompoundTag();
            offStack.save(player.registryAccess(), off);
            off.putInt("Slot", 0);
            offhand.add(off);
        }

        CompoundTag dimData = new CompoundTag();
        dimData.put("Main", list);
        dimData.put("Armor", armor);
        dimData.put("Offhand", offhand);
        playerData.put(k, dimData);
        // Salva também sob a chave canônica do overworld para garantir consistência ao restaurar
        if (k.equals(OVERWORLD_KEY) || k.equals(OVERWORLD_KEY_ALT)) {
            playerData.put(OVERWORLD_KEY, dimData.copy());
        }
        // Atualiza cache em memória (fallback confiável durante a sessão)
        MEMORY_CACHE.computeIfAbsent(player.getUUID(), u -> new ConcurrentHashMap<>()).put(k, dimData.copy());
        if (k.equals(OVERWORLD_KEY) || k.equals(OVERWORLD_KEY_ALT)) {
            MEMORY_CACHE.get(player.getUUID()).put(OVERWORLD_KEY, dimData.copy());
        }
    }

    private static void loadDimension(Player player, DimensionInventorySavedData data, ResourceKey<Level> dimension) {
        CompoundTag playerData = data.getOrCreatePlayerData(player.getUUID().toString());
        String k = key(dimension);

        for (int i = 0; i < player.getInventory().items.size(); i++) {
            player.getInventory().items.set(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            player.getInventory().armor.set(i, ItemStack.EMPTY);
        }
        player.getInventory().offhand.set(0, ItemStack.EMPTY);

        // Prioridade 1: cache em memória (mais confiável durante a sessão)
        CompoundTag dimData = null;
        Map<String, CompoundTag> cache = MEMORY_CACHE.get(player.getUUID());
        if (cache != null) {
            if (cache.containsKey(k)) {
                dimData = cache.get(k);
            } else if (k.equals(OVERWORLD_KEY) || k.equals(OVERWORLD_KEY_ALT)) {
                dimData = cache.get(OVERWORLD_KEY);
                if (dimData == null) dimData = cache.get(OVERWORLD_KEY_ALT);
            }
        }
        // Prioridade 2: SavedData
        if (dimData == null && playerData.contains(k, Tag.TAG_COMPOUND)) {
            dimData = playerData.getCompound(k);
        } else if (dimData == null && (dimension.equals(Level.OVERWORLD) || k.equals(OVERWORLD_KEY) || k.equals(OVERWORLD_KEY_ALT))) {
            if (playerData.contains(OVERWORLD_KEY, Tag.TAG_COMPOUND)) {
                dimData = playerData.getCompound(OVERWORLD_KEY);
            } else if (playerData.contains(OVERWORLD_KEY_ALT, Tag.TAG_COMPOUND)) {
                dimData = playerData.getCompound(OVERWORLD_KEY_ALT);
            }
        }

        if (dimData != null) {
            loadList(dimData.getList("Main", Tag.TAG_COMPOUND), player.getInventory().items, player);
            loadList(dimData.getList("Armor", Tag.TAG_COMPOUND), player.getInventory().armor, player);
            ListTag offhandList = dimData.getList("Offhand", Tag.TAG_COMPOUND);
            if (!offhandList.isEmpty()) {
                CompoundTag offTag = offhandList.getCompound(0);
                if (!offTag.isEmpty()) {
                    ItemStack stack = ItemStack.parse(player.registryAccess(), offTag).orElse(ItemStack.EMPTY);
                    player.getInventory().offhand.set(0, stack);
                }
            }
        }
    }

    private static void loadDimensionEmpty(Player player) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            player.getInventory().items.set(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            player.getInventory().armor.set(i, ItemStack.EMPTY);
        }
        player.getInventory().offhand.set(0, ItemStack.EMPTY);
    }

    private static void loadList(ListTag list, NonNullList<ItemStack> target, Player player) {
        for (int i = 0; i < target.size(); i++) {
            target.set(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < list.size(); i++) {
            CompoundTag slot = list.getCompound(i);
            int idx = slot.getInt("Slot");
            if (idx >= 0 && idx < target.size()) {
                target.set(idx, ItemStack.parse(player.registryAccess(), slot).orElse(ItemStack.EMPTY));
            }
        }
    }
}
