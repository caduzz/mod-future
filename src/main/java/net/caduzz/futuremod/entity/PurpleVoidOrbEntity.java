package net.caduzz.futuremod.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import net.caduzz.futuremod.Config;
import net.caduzz.futuremod.ModDamageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Esfera destrutiva roxa: movimento linear, dano letal (um golpe), puxão contínua tipo gravidade na zona.
 */
public class PurpleVoidOrbEntity extends Entity {

    private static final Vector3f PURPLE_BRIGHT = new Vector3f(0.72f, 0.35f, 1.0f);
    private static final Vector3f PURPLE_CORE = new Vector3f(0.45f, 0.08f, 0.85f);

    private int ticksAlive;
    private int maxLifeTicks;
    private float effectRadius;
    @Nullable
    private UUID ownerUUID;
    @Nullable
    private LivingEntity ownerCached;
    private final Set<UUID> damagedThisOrb = new HashSet<>();

    /** Casca esférica de {@link Display.BlockDisplay}; só servidor (dois invólucros = bola). */
    @Nullable
    private List<Display.BlockDisplay> purpleBlockShell;
    private int shellSurfacePoints;
    private int shellInnerSpherePoints;

    public PurpleVoidOrbEntity(EntityType<? extends PurpleVoidOrbEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.ticksAlive = 0;
        this.maxLifeTicks = Config.PURPLE_VOID_ORB_MAX_LIFE_TICKS.get();
        this.effectRadius = (float) (double) Config.PURPLE_VOID_ORB_RADIUS.get();
    }

    public PurpleVoidOrbEntity(EntityType<? extends PurpleVoidOrbEntity> type, Level level, LivingEntity owner) {
        this(type, level);
        this.ownerUUID = owner.getUUID();
        this.ownerCached = owner;
        Vec3 look = owner.getLookAngle();
        double spawn = 0.6;
        setPos(
                owner.getX() + look.x * spawn,
                owner.getEyeY() - 0.1 + look.y * spawn,
                owner.getZ() + look.z * spawn);
        double speed = Config.PURPLE_VOID_ORB_SPEED.get();
        setDeltaMovement(look.scale(speed));
    }

    /** Raio lógico / visual (cliente usa no renderer da bola de blocos). */
    public float getEffectRadius() {
        return effectRadius;
    }

    @Nullable
    public LivingEntity getOwner() {
        if (ownerCached != null && ownerCached.isAlive()) {
            return ownerCached;
        }
        if (ownerUUID != null && level() instanceof ServerLevel sl) {
            Entity e = sl.getEntity(ownerUUID);
            if (e instanceof LivingEntity le) {
                ownerCached = le;
                return le;
            }
        }
        return null;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            clientTrail();
            return;
        }
        if (ticksAlive == 0) {
            level().playSound(
                    null,
                    getX(),
                    getY(),
                    getZ(),
                    SoundEvents.SCULK_CATALYST_BLOOM,
                    SoundSource.PLAYERS,
                    0.45f,
                    0.28f);
        }
        ticksAlive++;
        if (ticksAlive > maxLifeTicks) {
            explodeVisualAndDiscard();
            return;
        }

        ensurePurpleBlockShell();
        updatePurpleBlockShellPositions();

        Vec3 motion = getDeltaMovement();
        Vec3 start = position();
        Vec3 end = start.add(motion);

        BlockHitResult blockHit = level().clip(
                new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() != HitResult.Type.MISS) {
            double hitDist = blockHit.getLocation().distanceToSqr(start);
            if (hitDist < motion.lengthSqr() + 0.01) {
                setPos(blockHit.getLocation().x, blockHit.getLocation().y, blockHit.getLocation().z);
                updatePurpleBlockShellPositions();
                affectZone((ServerLevel) level());
                explodeVisualAndDiscard();
                return;
            }
        }

        Vec3 before = position();
        move(MoverType.SELF, motion);
        affectZone((ServerLevel) level());

        // Antes: movimento < 25% do previsto explodia no 1º tick (raspar bloco / colisão) e a casca nem era criada.
        // Só considerar preso se não houve deslocamento efetivo.
        if (motion.lengthSqr() > 1e-6 && before.distanceToSqr(position()) < 1e-8) {
            explodeVisualAndDiscard();
            return;
        }

        updatePurpleBlockShellPositions();
    }

    private void ensurePurpleBlockShell() {
        if (purpleBlockShell != null) {
            return;
        }
        ServerLevel sl = (ServerLevel) level();
        shellSurfacePoints = Mth.clamp((int) (effectRadius * 14), 32, 52);
        shellInnerSpherePoints = Mth.clamp((int) (effectRadius * 9), 18, 34);
        int n = shellSurfacePoints + shellInnerSpherePoints;
        purpleBlockShell = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Display.BlockDisplay d = EntityType.BLOCK_DISPLAY.create(sl);
            if (d == null) {
                continue;
            }
            d.setBlockState(Blocks.PURPLE_CONCRETE.defaultBlockState());
            // Display vem com width/height 0: frustum / Sodium entity-culling tratam como sem volume e somem.
            d.setWidth(1.0f);
            d.setHeight(1.0f);
            d.setBrightnessOverride(Brightness.FULL_BRIGHT);
            d.setGlowColorOverride(0xFFB866FF);
            d.setNoGravity(true);
            d.setInvulnerable(true);
            sl.addFreshEntity(d);
            purpleBlockShell.add(d);
        }
        updatePurpleBlockShellPositions();
    }

    /** Duas cascas esféricas (Fibonacci), rotação lenta para ler volume tipo bola. */
    private void updatePurpleBlockShellPositions() {
        if (purpleBlockShell == null || purpleBlockShell.isEmpty()) {
            return;
        }
        float cy = getBbHeight() * 0.5f;
        float rOut = effectRadius * 0.9f;
        float rIn = effectRadius * 0.52f;
        double spinOut = tickCount * 0.065;
        double spinIn = -tickCount * 0.05;

        List<Display.BlockDisplay> list = purpleBlockShell;
        int idx = 0;
        for (int i = 0; i < shellSurfacePoints && idx < list.size(); i++) {
            Vec3 off = fibonacciSphereOffset(i, shellSurfacePoints, rOut, spinOut);
            setShellDisplayAt(list.get(idx++), cy, off);
        }
        for (int i = 0; i < shellInnerSpherePoints && idx < list.size(); i++) {
            Vec3 off = fibonacciSphereOffset(i, shellInnerSpherePoints, rIn, spinIn);
            setShellDisplayAt(list.get(idx++), cy, off);
        }
        while (idx < list.size()) {
            Display.BlockDisplay d = list.get(idx++);
            if (d.isAlive()) {
                d.setPos(getX(), getY() + cy, getZ());
            }
        }
    }

    private void setShellDisplayAt(Display.BlockDisplay d, double cy, Vec3 off) {
        if (!d.isAlive()) {
            return;
        }
        d.setPos(getX() + off.x, getY() + cy + off.y, getZ() + off.z);
    }

    /**
     * Ponto na superfície de uma esfera (distribuição Fibonacci), com rotação em torno do eixo Y.
     */
    private static Vec3 fibonacciSphereOffset(int index, int count, float radius, double yawSpin) {
        if (count <= 1) {
            return Vec3.ZERO;
        }
        double t = (double) index / (count - 1);
        double ny = 1.0 - 2.0 * t;
        double ringR = Math.sqrt(Mth.clamp(1.0 - ny * ny, 0.0, 1.0));
        double theta = index * Math.PI * (3.0 - Math.sqrt(5.0)) + yawSpin;
        double x = Mth.cos((float) theta) * ringR * radius;
        double z = Mth.sin((float) theta) * ringR * radius;
        return new Vec3(x, ny * radius, z);
    }

    private void discardPurpleBlockShell() {
        if (purpleBlockShell == null) {
            return;
        }
        for (Display.BlockDisplay d : purpleBlockShell) {
            if (d != null && d.isAlive()) {
                d.discard();
            }
        }
        purpleBlockShell = null;
        shellSurfacePoints = 0;
        shellInnerSpherePoints = 0;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!level().isClientSide) {
            discardPurpleBlockShell();
        }
        super.remove(reason);
    }

    private void affectZone(ServerLevel serverLevel) {
        LivingEntity owner = getOwner();
        Vec3 center = position();
        double r = effectRadius;
        AABB box = new AABB(center, center).inflate(r);
        List<Entity> entities = serverLevel.getEntities(this, box, e -> e.isAlive() && e != this);
        DamageSource src = damageSource(serverLevel, owner);

        for (Entity entity : entities) {
            if (entity instanceof Display.BlockDisplay) {
                continue;
            }
            if (owner != null && entity.getUUID().equals(owner.getUUID())) {
                continue;
            }
            if (entity.position().distanceToSqr(center) > r * r) {
                continue;
            }

            if (entity instanceof Projectile proj) {
                if (owner != null && proj.getOwner() != null && proj.getOwner().getUUID().equals(owner.getUUID())) {
                    continue;
                }
                proj.setDeltaMovement(Vec3.ZERO);
                proj.hasImpulse = true;
                proj.discard();
                continue;
            }

            if (entity instanceof LivingEntity living) {
                if (living instanceof Player p && p.isSpectator()) {
                    continue;
                }
                applyVoidPull(living, center, getDeltaMovement(), r);
                if (damagedThisOrb.add(living.getUUID())) {
                    applyVoidLethalHit(living, src, owner);
                }
            }
        }

        float maxBreak = (float) (double) Config.PURPLE_VOID_BREAK_MAX_DESTROY_SPEED.get();
        if (maxBreak > 0f) {
            double breakR = r * Config.PURPLE_VOID_BREAK_RADIUS_SCALE.get();
            double breakRSq = breakR * breakR;
            BlockPos.MutableBlockPos mb = new BlockPos.MutableBlockPos();
            int ri = Mth.ceil(breakR);
            BlockPos centerPos = BlockPos.containing(center);
            for (int dx = -ri; dx <= ri; dx++) {
                for (int dy = -ri; dy <= ri; dy++) {
                    for (int dz = -ri; dz <= ri; dz++) {
                        mb.set(centerPos.getX() + dx, centerPos.getY() + dy, centerPos.getZ() + dz);
                        if (center.distanceToSqr(mb.getX() + 0.5, mb.getY() + 0.5, mb.getZ() + 0.5) > breakRSq) {
                            continue;
                        }
                        BlockState state = serverLevel.getBlockState(mb);
                        if (state.isAir()) {
                            continue;
                        }
                        float ds = state.getDestroySpeed(serverLevel, mb);
                        if (ds < 0 || ds > maxBreak) {
                            continue;
                        }
                        serverLevel.destroyBlock(mb, true, owner instanceof ServerPlayer se ? se : null);
                    }
                }
            }
        }
    }

    private static DamageSource damageSource(ServerLevel level, @Nullable LivingEntity owner) {
        if (owner != null) {
            return level.damageSources().source(ModDamageTypes.PURPLE_VOID, owner);
        }
        return level.damageSources().source(ModDamageTypes.PURPLE_VOID, null);
    }

    /** Um golpe: mata na prática qualquer alvo (vida + absorção + margem), inclusive criativo (tags do damage type). */
    private void applyVoidLethalHit(LivingEntity target, DamageSource src, @Nullable LivingEntity owner) {
        float pool = target.getHealth() + target.getAbsorptionAmount();
        float kill = Math.max(pool + 64f, target.getMaxHealth() * 50f + 64f);
        float ratio = (float) (double) Config.PURPLE_VOID_LIFE_STEAL_RATIO.get();
        if (!target.hurt(src, kill)) {
            return;
        }
        if (owner instanceof ServerPlayer sp && ratio > 0f && pool > 0f) {
            float heal = Math.min(pool * ratio, sp.getMaxHealth() * 2f);
            if (heal > 0f) {
                sp.heal(heal);
            }
        }
    }

    /** Puxão estilo gravidade a cada tick dentro da esfera; mais forte perto do centro. */
    private void applyVoidPull(LivingEntity living, Vec3 orbCenter, Vec3 orbMotion, double radius) {
        Vec3 v = living.position().add(0, living.getBbHeight() * 0.35, 0);
        Vec3 toCenter = orbCenter.subtract(v);
        double dist = toCenter.length();
        if (dist < 1e-4) {
            toCenter = new Vec3(0, 0.12, 0);
            dist = 0.12;
        }
        double pullScale = Config.PURPLE_VOID_PULL_STRENGTH.get();
        double edge = Mth.clamp(1.0 - dist / Math.max(radius, 0.01), 0.18, 1.0);
        Vec3 inward = toCenter.scale(1.0 / dist).scale(pullScale * (0.32 + 0.68 * edge));

        Vec3 out = orbMotion.lengthSqr() > 1e-6
                ? orbMotion.normalize().scale(0.36)
                : inward.scale(-0.18);

        Vec3 motion = living.getDeltaMovement().scale(0.9);
        Vec3 dv = motion.add(inward).add(out);
        living.setDeltaMovement(dv.x, Math.max(0.1, dv.y + 0.14), dv.z);
        living.hasImpulse = true;
        living.hurtMarked = true;
    }

    private void explodeVisualAndDiscard() {
        if (level() instanceof ServerLevel sl) {
            Vec3 p = position();
            float re = effectRadius * 1.1f;
            sl.playSound(null, p.x, p.y, p.z, SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.PLAYERS, 2.2f, 0.35f);
            double cy = getBbHeight() * 0.5;
            Vec3 c = new Vec3(p.x, p.y + cy, p.z);
            spawnPurpleSphereParticles(sl, c, re * 0.92f, 52, 0.0);
            spawnPurpleSphereParticles(sl, c, re * 0.5f, 30, tickCount * 0.08);
        }
        discard();
    }

    /** Poeira roxa na superfície de uma esfera (Fibonacci). */
    private static void spawnPurpleSphereParticles(ServerLevel sl, Vec3 center, float radius, int points, double yawSpin) {
        for (int i = 0; i < points; i++) {
            Vec3 off = fibonacciSphereOffset(i, points, radius, yawSpin);
            double x = center.x + off.x;
            double y = center.y + off.y;
            double z = center.z + off.z;
            Vector3f col = (i & 1) == 0 ? PURPLE_BRIGHT : PURPLE_CORE;
            sl.sendParticles(new DustParticleOptions(col, 1.35f), x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /** Rastro: duas cascas esféricas + alguns pontos no volume (bola densa). */
    private void clientTrail() {
        Vec3 p = position();
        double cx = p.x;
        double cy = p.y + getBbHeight() * 0.5;
        double cz = p.z;
        float r = effectRadius;
        double spinOut = tickCount * 0.065;
        double spinIn = -tickCount * 0.048;

        int nOut = Mth.clamp((int) (r * 14), 40, 72);
        for (int i = 0; i < nOut; i++) {
            Vec3 off = fibonacciSphereOffset(i, nOut, r * 0.9f, spinOut);
            Vector3f col = (i & 1) == 0 ? PURPLE_BRIGHT : PURPLE_CORE;
            level().addParticle(new DustParticleOptions(col, 1.3f), cx + off.x, cy + off.y, cz + off.z, 0, 0, 0);
        }
        int nIn = Mth.clamp((int) (r * 9), 22, 40);
        for (int i = 0; i < nIn; i++) {
            Vec3 off = fibonacciSphereOffset(i, nIn, r * 0.52f, spinIn);
            level().addParticle(new DustParticleOptions(PURPLE_CORE, 1.25f), cx + off.x, cy + off.y, cz + off.z, 0, 0, 0);
        }
        int fill = Mth.clamp((int) (r * 5), 10, 22);
        for (int i = 0; i < fill; i++) {
            Vec3 off = randomPointInBall(random, r * 0.45f);
            level().addParticle(new DustParticleOptions(PURPLE_BRIGHT, 1.0f), cx + off.x, cy + off.y, cz + off.z, 0, 0, 0);
        }
    }

    /** Ponto uniforme dentro da bola de raio {@code radius} (rejeição no cubo). */
    private static Vec3 randomPointInBall(net.minecraft.util.RandomSource random, float radius) {
        for (int k = 0; k < 12; k++) {
            double x = (random.nextDouble() * 2 - 1) * radius;
            double y = (random.nextDouble() * 2 - 1) * radius;
            double z = (random.nextDouble() * 2 - 1) * radius;
            if (x * x + y * y + z * z <= radius * radius) {
                return new Vec3(x, y, z);
            }
        }
        return Vec3.ZERO;
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        ticksAlive = tag.getInt("ticks_alive");
        maxLifeTicks = tag.getInt("max_life");
        effectRadius = tag.getFloat("radius");
        if (tag.hasUUID("owner")) {
            ownerUUID = tag.getUUID("owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        tag.putInt("ticks_alive", ticksAlive);
        tag.putInt("max_life", maxLifeTicks);
        tag.putFloat("radius", effectRadius);
        if (ownerUUID != null) {
            tag.putUUID("owner", ownerUUID);
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }
}
