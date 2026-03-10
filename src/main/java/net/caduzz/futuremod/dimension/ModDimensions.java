package net.caduzz.futuremod.dimension;

import net.caduzz.futuremod.FutureMod;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;

/**
 * Dimensão alternativa onde o jogador entra em criativo e tem inventário separado.
 */
public final class ModDimensions {

    public static final ResourceKey<Level> CREATIVE_REALM_LEVEL =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "creative_realm"));

    private ModDimensions() {}

    public static ServerLevel getOrCreateCreativeRealm(MinecraftServer server) {
        return server.getLevel(CREATIVE_REALM_LEVEL);
    }

    /**
     * Teleporta o jogador para a dimensão alvo, aplicando modo criativo e troca de inventário.
     * Itens do survival ficam salvos e voltam ao retornar; itens do criativo não vão para o survival.
     */
    public static void teleportToDimension(ServerPlayer player, ServerLevel targetLevel) {
        boolean enteringCreativeRealm = targetLevel.dimension() == CREATIVE_REALM_LEVEL;

        if (enteringCreativeRealm) {
            CreativeRealmInventoryAttachment.saveAndSwapToCreativeRealm(player);
            player.setGameMode(GameType.CREATIVE);
        } else {
            CreativeRealmInventoryAttachment.saveCreativeRealmAndRestore(player, targetLevel.dimension());
            player.setGameMode(GameType.SURVIVAL);
        }

        player.teleportTo(targetLevel, player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        player.inventoryMenu.broadcastChanges();
        player.onUpdateAbilities();
    }
}
