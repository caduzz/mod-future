package net.caduzz.futuremod.purplevoid;

import java.util.UUID;

import net.caduzz.futuremod.Config;
import net.caduzz.futuremod.entity.BlueVoidOrbEntity;
import net.caduzz.futuremod.entity.ModEntities;
import net.caduzz.futuremod.entity.PurpleVoidEntity;
import net.caduzz.futuremod.entity.RedVoidOrbEntity;
import net.caduzz.futuremod.item.ModItems;
import net.caduzz.futuremod.network.SyncPurpleVoidHudPayload;
import net.caduzz.futuremod.relic.PurpleSlotAttachment;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PurpleVoidManager {

    public enum ActivateResult {
        ACTIVATED,
        ON_COOLDOWN,
        REQUIRES_PURPLE_RELIC
    }

    private PurpleVoidManager() {
    }

    public static PurpleVoidData data(ServerPlayer player) {
        return player.getData(PurpleVoidAttachment.PURPLE_VOID.get());
    }

    public static boolean hasPurpleRelicEquipped(Player player) {
        ItemStack stack = player.getData(PurpleSlotAttachment.PURPLE_SLOT.get()).getStackInSlot(0);
        return !stack.isEmpty() && stack.is(ModItems.PURPLE_VOID_RELIC.get());
    }

    public static void syncHud(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SyncPurpleVoidHudPayload(data(player).getCooldownTicks()));
    }

    public static ActivateResult tryActivate(ServerPlayer player) {
        PurpleVoidData voidData = data(player);
        if (!voidData.canActivate()) {
            return ActivateResult.ON_COOLDOWN;
        }
        if (!hasPurpleRelicEquipped(player)) {
            return ActivateResult.REQUIRES_PURPLE_RELIC;
        }
        ServerLevel level = player.serverLevel();
        discardFusionOrbs(level, player);

        boolean charged = player.isShiftKeyDown();
        Vec3 anchor = PurpleVoidSpawnUtil.fusionTarget(player);
        Vec3 right = PurpleVoidSpawnUtil.horizontalRight(player);
        double side = Config.PURPLE_VOID_SIDE_OFFSET.get();
        Vec3 bluePos = anchor.add(right.scale(-side));
        Vec3 redPos = anchor.add(right.scale(side));

        BlueVoidOrbEntity blue = new BlueVoidOrbEntity(ModEntities.BLUE_VOID_ORB.get(), level, player, bluePos, charged);
        RedVoidOrbEntity red = new RedVoidOrbEntity(ModEntities.RED_VOID_ORB.get(), level, player, redPos);
        blue.bindFusionPartner(red.getUUID());
        red.bindFusionPartner(blue.getUUID());
        level.addFreshEntity(blue);
        level.addFreshEntity(red);

        voidData.setCooldownTicks(Config.PURPLE_VOID_COOLDOWN_SECONDS.get() * 20);
        return ActivateResult.ACTIVATED;
    }

    private static void discardFusionOrbs(ServerLevel level, ServerPlayer player) {
        UUID ownerId = player.getUUID();
        AABB box = player.getBoundingBox().inflate(160.0);
        for (BlueVoidOrbEntity e : level.getEntitiesOfClass(BlueVoidOrbEntity.class, box, en -> ownerId.equals(en.getOwnerUUID()))) {
            e.discard();
        }
        for (RedVoidOrbEntity e : level.getEntitiesOfClass(RedVoidOrbEntity.class, box, en -> ownerId.equals(en.getOwnerUUID()))) {
            e.discard();
        }
        for (PurpleVoidEntity e : level.getEntitiesOfClass(PurpleVoidEntity.class, box, en -> ownerId.equals(en.getOwnerUUID()))) {
            e.discard();
        }
    }

    /** Clears void tech left in a dimension the player just left (or stale worlds on login). */
    public static void discardOwnedVoidTechInLevel(ServerLevel level, UUID ownerId) {
        int y0 = level.getMinBuildHeight();
        int y1 = level.getMaxBuildHeight();
        AABB box = new AABB(-30_000_000, y0, -30_000_000, 30_000_000, y1, 30_000_000);
        for (BlueVoidOrbEntity e : level.getEntitiesOfClass(BlueVoidOrbEntity.class, box, en -> ownerId.equals(en.getOwnerUUID()))) {
            e.discard();
        }
        for (RedVoidOrbEntity e : level.getEntitiesOfClass(RedVoidOrbEntity.class, box, en -> ownerId.equals(en.getOwnerUUID()))) {
            e.discard();
        }
        for (PurpleVoidEntity e : level.getEntitiesOfClass(PurpleVoidEntity.class, box, en -> ownerId.equals(en.getOwnerUUID()))) {
            e.discard();
        }
    }

    public static void discardOwnedVoidTechLeavingDimension(ServerPlayer player, ResourceKey<Level> dimensionLeft) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        ServerLevel left = server.getLevel(dimensionLeft);
        if (left != null) {
            discardOwnedVoidTechInLevel(left, player.getUUID());
        }
    }

    /** After login/respawn: remove void entities stuck in other dimensions from a previous session. */
    public static void discardOwnedVoidInOtherLevels(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        ServerLevel current = player.serverLevel();
        UUID id = player.getUUID();
        for (ServerLevel level : server.getAllLevels()) {
            if (level != current) {
                discardOwnedVoidTechInLevel(level, id);
            }
        }
    }

    public static void tickPlayer(ServerPlayer player) {
        PurpleVoidData voidData = data(player);
        int before = voidData.getCooldownTicks();
        voidData.tick();
        if (before > 0 && voidData.getCooldownTicks() == 0) {
            syncHud(player);
        }
    }
}
