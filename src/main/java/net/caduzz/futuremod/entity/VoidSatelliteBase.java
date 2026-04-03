package net.caduzz.futuremod.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import net.caduzz.futuremod.Config;
import net.caduzz.futuremod.purplevoid.PurpleVoidSpawnUtil;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Aproximação fixa (blocos/tick) ao peito do dono ({@code +0, 1.5, 0}), sem física vanilla.
 * Apenas {@link BlueVoidOrbEntity} inicia a fusão com {@link RedVoidOrbEntity}.
 */
abstract class VoidSatelliteBase extends Entity {

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Nullable
    protected UUID ownerUUID;
    @Nullable
    protected UUID partnerUUID;
    @Nullable
    protected LivingEntity ownerCached;
    protected int approachTicks;
    private int partnerMissingTicks;

    protected VoidSatelliteBase(EntityType<? extends VoidSatelliteBase> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.setNoGravity(true);
    }

    protected VoidSatelliteBase(EntityType<? extends VoidSatelliteBase> type, Level level, LivingEntity owner, Vec3 spawnPos) {
        this(type, level);
        this.ownerUUID = owner.getUUID();
        this.ownerCached = owner;
        this.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        this.setDeltaMovement(Vec3.ZERO);
    }

    /** Se true, esta entidade procura o par e chama a fusão (apenas azul). */
    protected abstract boolean initiatesMerge();

    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void bindFusionPartner(UUID partner) {
        this.partnerUUID = partner;
    }

    @Nullable
    public LivingEntity getOwner() {
        LivingEntity resolved = VoidOwnerResolver.resolve(this, ownerUUID, ownerCached);
        ownerCached = resolved;
        return resolved;
    }

    @Override
    public void tick() {
        setDeltaMovement(Vec3.ZERO);
        super.tick();
        if (level().isClientSide) {
            return;
        }

        ServerLevel sl = (ServerLevel) level();

        LivingEntity owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            discard();
            return;
        }

        approachTicks++;
        int maxApproach = Config.PURPLE_VOID_FUSION_ORB_MAX_TICKS.get();
        if (approachTicks > maxApproach) {
            discard();
            return;
        }

        PurpleVoidSpawnUtil.stepTowardFusionCenter(this, owner);

        if (!initiatesMerge() && partnerUUID != null) {
            Entity pr = sl.getEntity(partnerUUID);
            if (!(pr instanceof BlueVoidOrbEntity) || !pr.isAlive()) {
                partnerMissingTicks++;
                if (partnerMissingTicks > 80) {
                    discard();
                }
            } else {
                partnerMissingTicks = 0;
            }
        }

        if (initiatesMerge()) {
            tryMergeWithRed(sl, owner);
        }
    }

    private void tryMergeWithRed(ServerLevel sl, LivingEntity owner) {
        if (!(this instanceof BlueVoidOrbEntity blue)) {
            return;
        }
        if (partnerUUID == null) {
            return;
        }
        Entity p = sl.getEntity(partnerUUID);
        if (!(p instanceof RedVoidOrbEntity partner) || !partner.isAlive()) {
            return;
        }
        double mergeDist = Config.PURPLE_VOID_MERGE_DISTANCE.get();
        double mergeSq = mergeDist * mergeDist;
        Vec3 core = PurpleVoidSpawnUtil.fusionTarget(owner);
        boolean orbsClose = blue.distanceToSqr(partner) <= mergeSq;
        boolean bothNearCore = blue.distanceToSqr(core) <= mergeSq && partner.distanceToSqr(core) <= mergeSq;
        if (!orbsClose && !bothNearCore) {
            return;
        }

        Vec3 mid = blue.position().add(partner.position()).scale(0.5);
        sl.playSound(null, mid.x, mid.y, mid.z, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.PLAYERS, 0.7f, 0.45f);
        boolean charged = blue.isChargedSpawn();
        PurpleVoidEntity purple =
                new PurpleVoidEntity(
                        ModEntities.PURPLE_VOID.get(),
                        sl,
                        owner,
                        mid,
                        owner.getYRot(),
                        owner.getXRot(),
                        charged);
        sl.addFreshEntity(purple);
        partner.discard();
        blue.discard();
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        approachTicks = tag.getInt("approach_ticks");
        if (tag.hasUUID("owner")) {
            ownerUUID = tag.getUUID("owner");
        }
        if (tag.hasUUID("partner")) {
            partnerUUID = tag.getUUID("partner");
        }
        partnerMissingTicks = tag.getInt("partner_missing_ticks");
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        tag.putInt("approach_ticks", approachTicks);
        tag.putInt("partner_missing_ticks", partnerMissingTicks);
        if (ownerUUID != null) {
            tag.putUUID("owner", ownerUUID);
        }
        if (partnerUUID != null) {
            tag.putUUID("partner", partnerUUID);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSq) {
        int chunks = getType().clientTrackingRange();
        double maxBlocks = chunks * 16.0;
        return distanceSq < maxBlocks * maxBlocks;
    }

}
