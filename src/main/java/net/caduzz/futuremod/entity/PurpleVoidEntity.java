package net.caduzz.futuremod.entity;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.caduzz.futuremod.Config;
import net.caduzz.futuremod.purplevoid.PurpleVoidPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

/**
 * Purple bolt after blue/red fusion: short fusion pop, then linear motion that erases blocks and discards entities.
 */
public class PurpleVoidEntity extends Entity {

    private static final EntityDataAccessor<Integer> DATA_TICKS_ALIVE =
            SynchedEntityData.defineId(PurpleVoidEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_VISUAL_RADIUS =
            SynchedEntityData.defineId(PurpleVoidEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Byte> DATA_PHASE =
            SynchedEntityData.defineId(PurpleVoidEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_POP_TICKS_MAX =
            SynchedEntityData.defineId(PurpleVoidEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_CHARGED =
            SynchedEntityData.defineId(PurpleVoidEntity.class, EntityDataSerializers.BYTE);

    private int ticksAlive;
    /** Bolt phase only; fusion pop ticks are not counted against this. */
    private int projectileMaxTicks;
    private float visualRadius;
    /** Bolt phase: block/entity erasure radius (can differ from visual mesh). */
    private float tunnelRadius;
    private int popTicksMax;
    @Nullable
    private UUID ownerUUID;
    @Nullable
    private LivingEntity ownerCached;
    private boolean charged;
    /** Normalized flight direction; fixed at spawn so the bolt does not depend on entity yaw sync. */
    private Vec3 boltDirection;

    public PurpleVoidEntity(EntityType<? extends PurpleVoidEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
        setNoGravity(true);
        this.projectileMaxTicks = Math.max(1, Config.PURPLE_VOID_ORB_MAX_LIFE_TICKS.get());
        this.popTicksMax = Config.PURPLE_VOID_FUSION_POP_TICKS.get();
        this.visualRadius = (float) (double) Config.PURPLE_VOID_VISUAL_RADIUS.get();
        this.tunnelRadius = (float) (double) Config.PURPLE_VOID_TUNNEL_ERASE_RADIUS.get();
        this.boltDirection = new Vec3(0.0, 0.0, -1.0);
    }

    public PurpleVoidEntity(
            EntityType<? extends PurpleVoidEntity> type,
            ServerLevel level,
            LivingEntity owner,
            Vec3 pos,
            float yRot,
            float xRot,
            boolean charged) {
        this(type, level);
        this.ownerUUID = owner.getUUID();
        this.ownerCached = owner;
        this.charged = charged;
        this.popTicksMax = Config.PURPLE_VOID_FUSION_POP_TICKS.get();
        float baseR = (float) (double) Config.PURPLE_VOID_VISUAL_RADIUS.get();
        float baseTunnel = (float) (double) Config.PURPLE_VOID_TUNNEL_ERASE_RADIUS.get();
        double scale = charged ? Config.PURPLE_VOID_CHARGED_SCALE.get() : 1.0;
        this.visualRadius = (float) (baseR * scale);
        this.tunnelRadius = (float) (baseTunnel * scale);
        this.projectileMaxTicks = Math.max(1, Config.PURPLE_VOID_ORB_MAX_LIFE_TICKS.get());
        Vec3 look = owner.getLookAngle();
        double ll = look.length();
        this.boltDirection = ll > 1e-5 ? look.scale(1.0 / ll) : new Vec3(0.0, 0.0, -1.0);
        setPos(pos.x, pos.y, pos.z);
        setYRot(yRot);
        setXRot(xRot);
        entityData.set(DATA_VISUAL_RADIUS, visualRadius);
        entityData.set(DATA_POP_TICKS_MAX, popTicksMax);
        entityData.set(DATA_CHARGED, (byte) (charged ? 1 : 0));
        entityData.set(DATA_PHASE, PurpleVoidPhase.FUSION_POP.id());
        refreshDimensions();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TICKS_ALIVE, 0);
        builder.define(DATA_VISUAL_RADIUS, 1.5f);
        builder.define(DATA_PHASE, PurpleVoidPhase.FUSION_POP.id());
        builder.define(DATA_POP_TICKS_MAX, 6);
        builder.define(DATA_CHARGED, (byte) 0);
    }

    public int getSyncedTicksAlive() {
        return entityData.get(DATA_TICKS_ALIVE);
    }

    public float getSyncedVisualRadius() {
        return entityData.get(DATA_VISUAL_RADIUS);
    }

    public PurpleVoidPhase getPhase() {
        return PurpleVoidPhase.fromId(entityData.get(DATA_PHASE));
    }

    public int getPopTicksMax() {
        return Math.max(0, entityData.get(DATA_POP_TICKS_MAX));
    }

    public boolean isCharged() {
        return entityData.get(DATA_CHARGED) != 0;
    }

    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Nullable
    public LivingEntity getOwner() {
        LivingEntity resolved = VoidOwnerResolver.resolve(this, ownerUUID, ownerCached);
        ownerCached = resolved;
        return resolved;
    }

    private double moveSpeed() {
        double v = Config.PURPLE_VOID_ORB_SPEED.get();
        if (charged) {
            v *= Config.PURPLE_VOID_CHARGED_SCALE.get();
        }
        return v;
    }

    @Override
    public void tick() {
        setDeltaMovement(Vec3.ZERO);
        super.tick();
        if (level().isClientSide) {
            clientParticles();
            return;
        }

        ticksAlive++;
        entityData.set(DATA_TICKS_ALIVE, ticksAlive);

        PurpleVoidPhase phase =
                ticksAlive <= popTicksMax ? PurpleVoidPhase.FUSION_POP : PurpleVoidPhase.BOLT;
        entityData.set(DATA_PHASE, phase.id());

        ServerLevel sl = (ServerLevel) level();
        LivingEntity owner = getOwner();
        if (ownerUUID != null && owner == null) {
            discard();
            return;
        }

        if (ticksAlive == 1) {
            sl.playSound(null, getX(), getY(), getZ(), SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 0.55f, 0.35f);
        }

        if (phase == PurpleVoidPhase.BOLT) {
            if ((ticksAlive - popTicksMax) % 14 == 0) {
                sl.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.22f, 1.65f);
            }

            Vec3 dir = boltDirection;
            double speed = moveSpeed();
            Vec3 before = position();
            Vec3 after = before.add(dir.scale(speed));
            eraseSegment(sl, before, after, owner);
            setPos(after.x, after.y, after.z);
        }

        int maxTotalTicks = popTicksMax + projectileMaxTicks;
        if (ticksAlive > maxTotalTicks) {
            discard();
        }
    }

    private void eraseSegment(ServerLevel level, Vec3 from, Vec3 to, LivingEntity owner) {
        double r = tunnelRadius;
        Vec3 seg = to.subtract(from);
        double len = seg.length();
        if (len < 1e-6) {
            eraseSphere(level, to, r, owner);
            voidEntities(level, to, r, owner);
            return;
        }
        Vec3 n = seg.scale(1.0 / len);
        double step = Math.max(0.35, r * 0.65);
        for (double d = 0; d <= len; d += step) {
            Vec3 p = from.add(n.scale(Math.min(d, len)));
            eraseSphere(level, p, r, owner);
            voidEntities(level, p, r, owner);
        }
    }

    private void voidEntities(ServerLevel level, Vec3 center, double radius, LivingEntity owner) {
        AABB box = new AABB(center, center).inflate(radius + 0.25);
        List<Entity> list = level.getEntities(this, box, e -> e.isAlive() && e != this);
        for (Entity e : list) {
            if (owner != null && e.getUUID().equals(owner.getUUID())) {
                continue;
            }
            if (e instanceof Player p && p.isSpectator()) {
                continue;
            }
            if (e.position().distanceToSqr(center) > radius * radius) {
                continue;
            }
            e.discard();
        }
    }

    private void eraseSphere(ServerLevel level, Vec3 center, double radius, @Nullable LivingEntity owner) {
        float maxBreak = (float) (double) Config.PURPLE_VOID_BREAK_MAX_DESTROY_SPEED.get();
        if (maxBreak <= 0f) {
            return;
        }
        int ri = Mth.ceil(radius);
        double rsq = radius * radius;
        BlockPos.MutableBlockPos mb = new BlockPos.MutableBlockPos();
        BlockPos o = BlockPos.containing(center);
        Entity breaker = owner;
        for (int dx = -ri; dx <= ri; dx++) {
            for (int dy = -ri; dy <= ri; dy++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    mb.set(o.getX() + dx, o.getY() + dy, o.getZ() + dz);
                    if (center.distanceToSqr(mb.getX() + 0.5, mb.getY() + 0.5, mb.getZ() + 0.5) > rsq) {
                        continue;
                    }
                    BlockState state = level.getBlockState(mb);
                    if (state.isAir()) {
                        continue;
                    }
                    float ds = state.getDestroySpeed(level, mb);
                    if (ds < 0 || ds > maxBreak) {
                        continue;
                    }
                    if (!level.destroyBlock(mb, false, breaker) && !level.removeBlock(mb, false)) {
                        level.setBlock(mb, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private void clientParticles() {
        Level level = level();
        float r = getSyncedVisualRadius();
        int tick = tickCount;
        int id = getId();
        Vec3 core = position();

        Vector3f purple = new Vector3f(0.62f, 0.22f, 0.95f);
        Vector3f black = new Vector3f(0.08f, 0.08f, 0.1f);

        int count = isCharged() ? 14 : 10;
        for (int i = 0; i < count; i++) {
            double u1 = fractSinSeed(i * 104729 + tick * 7919 + id);
            double u2 = fractSinSeed(i * 224737 + tick * 1091 + id * 17);
            double theta = u1 * Mth.TWO_PI;
            double cosPhi = 2.0 * u2 - 1.0;
            double phi = Math.acos(Mth.clamp(cosPhi, -1.0, 1.0));
            double sp = Mth.sin((float) phi);
            double sx = r * sp * Mth.cos((float) theta);
            double sy = r * Mth.cos((float) phi);
            double sz = r * sp * Mth.sin((float) theta);
            double px = core.x + sx;
            double py = core.y + sy;
            double pz = core.z + sz;
            boolean dark = (i & 1) == 0;
            level.addParticle(
                    new DustParticleOptions(dark ? black : purple, dark ? 0.55f : 0.85f),
                    px,
                    py,
                    pz,
                    0.0,
                    0.0,
                    0.0);
        }
        if (tick % 3 == 0) {
            level.addParticle(ParticleTypes.SMOKE, core.x, core.y, core.z, 0.0, 0.02, 0.0);
        }
    }

    /** Deterministic [0,1) from integer seed — no {@link java.util.Random} per tick. */
    private static double fractSinSeed(int seed) {
        double x = Math.sin(seed * 12.9898) * 43758.5453;
        return x - Math.floor(x);
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        ticksAlive = tag.getInt("ticks_alive");
        projectileMaxTicks = Math.max(
                1,
                tag.contains("projectile_max_ticks") ? tag.getInt("projectile_max_ticks") : tag.getInt("max_life"));
        visualRadius = tag.getFloat("visual_radius");
        tunnelRadius = tag.contains("tunnel_radius") ? tag.getFloat("tunnel_radius") : visualRadius * 0.92f;
        popTicksMax = tag.getInt("pop_ticks_max");
        charged = tag.getBoolean("charged");
        if (tag.contains("bolt_dx")) {
            boltDirection = new Vec3(tag.getDouble("bolt_dx"), tag.getDouble("bolt_dy"), tag.getDouble("bolt_dz"));
            if (boltDirection.lengthSqr() < 1e-8) {
                boltDirection = new Vec3(0.0, 0.0, -1.0);
            } else {
                boltDirection = boltDirection.normalize();
            }
        }
        if (tag.hasUUID("owner")) {
            ownerUUID = tag.getUUID("owner");
        }
        entityData.set(DATA_TICKS_ALIVE, ticksAlive);
        entityData.set(DATA_VISUAL_RADIUS, visualRadius);
        entityData.set(DATA_POP_TICKS_MAX, popTicksMax);
        entityData.set(DATA_CHARGED, (byte) (charged ? 1 : 0));
        PurpleVoidPhase ph = ticksAlive <= popTicksMax ? PurpleVoidPhase.FUSION_POP : PurpleVoidPhase.BOLT;
        entityData.set(DATA_PHASE, ph.id());
        refreshDimensions();
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        tag.putInt("ticks_alive", ticksAlive);
        tag.putInt("projectile_max_ticks", projectileMaxTicks);
        tag.putFloat("visual_radius", visualRadius);
        tag.putFloat("tunnel_radius", tunnelRadius);
        tag.putInt("pop_ticks_max", popTicksMax);
        tag.putBoolean("charged", charged);
        tag.putDouble("bolt_dx", boltDirection.x);
        tag.putDouble("bolt_dy", boltDirection.y);
        tag.putDouble("bolt_dz", boltDirection.z);
        if (ownerUUID != null) {
            tag.putUUID("owner", ownerUUID);
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSq) {
        // Vanilla small custom entities get ~20 block range (BB * 64); use client tracking in blocks².
        int chunks = getType().clientTrackingRange();
        double maxBlocks = chunks * 16.0;
        return distanceSq < maxBlocks * maxBlocks;
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
    public EntityDimensions getDimensions(Pose pose) {
        float r = level().isClientSide ? getSyncedVisualRadius() : visualRadius;
        float d = Math.max(0.6f, r * 2.1f);
        return EntityDimensions.scalable(d, d);
    }
}
