package net.caduzz.futuremod.dimension;

import net.caduzz.futuremod.FutureMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Armazena inventários por jogador e dimensão. Persistido no disco com o mundo (Overworld).
 * Usado para não perder itens ao trocar entre Overworld e Creative Realm.
 */
public final class DimensionInventorySavedData extends SavedData {

    private static final String DATA_ID = FutureMod.MOD_ID + "_dimension_inventories";

    /** Estrutura: playerUuid -> (dimensionKey -> inventory CompoundTag com Main, Armor, Offhand) */
    private final CompoundTag storage = new CompoundTag();

    public DimensionInventorySavedData() {
        super();
    }

    public static SavedData.Factory<DimensionInventorySavedData> factory() {
        return new SavedData.Factory<>(
                DimensionInventorySavedData::new,
                (tag, lookup) -> {
                    DimensionInventorySavedData data = new DimensionInventorySavedData();
                    data.storage.merge(tag);
                    return data;
                },
                null
        );
    }

    public static DimensionInventorySavedData get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(factory(), DATA_ID);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup) {
        tag.merge(storage.copy());
        return tag;
    }

    /** Retorna o CompoundTag do jogador (cria se não existir). Chave: dimensionKey -> inventory. */
    public CompoundTag getOrCreatePlayerData(String playerUuid) {
        if (!storage.contains(playerUuid)) {
            storage.put(playerUuid, new CompoundTag());
        }
        return storage.getCompound(playerUuid);
    }

    /** Marca os dados como alterados para persistir no disco. */
    public void markDirty() {
        setDirty();
    }
}
