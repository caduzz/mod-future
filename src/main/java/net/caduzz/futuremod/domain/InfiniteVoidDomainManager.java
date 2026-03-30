package net.caduzz.futuremod.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.caduzz.futuremod.Config;
import net.caduzz.futuremod.block.ModBlocks;
import net.caduzz.futuremod.item.ModItems;
import net.caduzz.futuremod.relic.RelicSlotAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * infinite_void_domain: servidor autoritativo.
 * Domínio ancorado na posição inicial; freeze custom (sem poções de controle); dano + life steal.
 */
public final class InfiniteVoidDomainManager {

    private record DomainShell(BlockPos center, int radius, int floorY, Map<BlockPos, BlockState> previousStates) {}

    private static final Map<UUID, DomainShell> ACTIVE_SHELLS = new HashMap<>();

    /** Snapshot no Pre do tick; restaurado no Post — ServerTick.Post corre depois do movimento, entao so zerar delta nao segura players. */
    private record DomainPlayerFreezeAnchor(Vec3 position, float yRot, float xRot) {}

    private static final Map<UUID, DomainPlayerFreezeAnchor> DOMAIN_PLAYER_FREEZE_ANCHORS = new HashMap<>();
    private static final int DAMAGE_INTERVAL_TICKS = 10;
    private static final float DOMAIN_DAMAGE_MAX_HEALTH_RATIO = 0.20f;
    private static final float LIFE_STEAL_RATIO = 0.50f;
    private static final boolean NON_LETHAL_DOMAIN_DAMAGE = true;

    public enum ActivateResult {
        ACTIVATED,
        ALREADY_ACTIVE,
        ON_COOLDOWN,
        /** Precisa da Relíquia da Regeneração no slot de relíquia do mod. */
        REQUIRES_REGENERATION_RELIC
    }

    public enum CancelResult {
        CANCELLED,
        NOT_ACTIVE
    }

    private InfiniteVoidDomainManager() {
    }

    public static InfiniteVoidDomainData data(ServerPlayer player) {
        return player.getData(InfiniteVoidDomainAttachment.INFINITE_VOID_DOMAIN.get());
    }

    private static boolean hasRegenerationRelicEquipped(ServerPlayer player) {
        ItemStack stack = player.getData(RelicSlotAttachment.RELIC_SLOT.get()).getStackInSlot(0);
        return !stack.isEmpty() && stack.is(ModItems.REGENERATION_RELIC.get());
    }

    public static ActivateResult tryActivate(ServerPlayer player) {
        InfiniteVoidDomainData domainData = data(player);
        if (domainData.isActive()) return ActivateResult.ALREADY_ACTIVE;
        if (!domainData.canActivate()) return ActivateResult.ON_COOLDOWN;
        if (!hasRegenerationRelicEquipped(player)) {
            return ActivateResult.REQUIRES_REGENERATION_RELIC;
        }

        domainData.activate(
                Config.INFINITE_VOID_DURATION_SECONDS.get() * 20,
                Config.INFINITE_VOID_COOLDOWN_SECONDS.get() * 20);
        createDomainShell(player);
        ServerLevel level = player.serverLevel();
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.RESPAWN_ANCHOR_CHARGE,
                SoundSource.PLAYERS,
                1.35f,
                0.55f);
        return ActivateResult.ACTIVATED;
    }

    /** Mesma tecla (V): encerra dominio, restaura mundo e aplica cooldown. */
    public static CancelResult tryCancelEarly(ServerPlayer player) {
        InfiniteVoidDomainData domainData = data(player);
        if (!domainData.isActive()) {
            return CancelResult.NOT_ACTIVE;
        }
        clearDomainShell(player);
        domainData.endEarlyWithCooldown(Config.INFINITE_VOID_COOLDOWN_SECONDS.get() * 20);
        MinecraftServer server = player.getServer();
        if (server != null) {
            tickActiveDomains(server);
        }
        ServerLevel level = player.serverLevel();
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.PORTAL_TRIGGER,
                SoundSource.PLAYERS,
                0.6f,
                1.4f);
        return CancelResult.CANCELLED;
    }

    /**
     * Congela vitimas e projéteis em todos os domínios ativos; sincroniza freeze nos clientes.
     * Sincroniza freeze; movimento de players e corrigido em PlayerTick Pre/Post (ancora).
     */
    public static void tickActiveDomains(MinecraftServer server) {
        if (ACTIVE_SHELLS.isEmpty()) {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                DomainFreezeSync.sync(p, false);
                clearDomainFreezePlayerAnchor(p);
            }
            return;
        }

        double effectRadius = Config.INFINITE_VOID_RADIUS_BLOCKS.get();
        double radiusSq = effectRadius * effectRadius;

        for (Map.Entry<UUID, DomainShell> entry : ACTIVE_SHELLS.entrySet()) {
            UUID ownerId = entry.getKey();
            DomainShell shell = entry.getValue();
            ServerPlayer owner = server.getPlayerList().getPlayer(ownerId);
            if (owner == null) {
                continue;
            }
            ServerLevel level = owner.serverLevel();
            Vec3 center = domainCenterFromShell(shell);
            AABB area = new AABB(center, center).inflate(effectRadius);

            List<Entity> inDomain = level.getEntities(
                    (Entity) null,
                    area,
                    entity -> entity.isAlive()
                            && entity != owner
                            && entity.position().distanceToSqr(center) <= radiusSq);

            for (Entity entity : inDomain) {
                if (entity instanceof ServerPlayer victim) {
                    DomainFreezeSync.sync(victim, true);
                } else if (entity instanceof LivingEntity living) {
                    applyFreezeToVictim(living);
                } else if (entity instanceof Projectile projectile) {
                    if (projectile.getOwner() != null && projectile.getOwner().getUUID().equals(ownerId)) {
                        continue;
                    }
                    projectile.setDeltaMovement(Vec3.ZERO);
                    projectile.hasImpulse = true;
                }
            }
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!isInsideAnyForeignDomain(player, radiusSq)) {
                DomainFreezeSync.sync(player, false);
                clearDomainFreezePlayerAnchor(player);
            }
        }
    }

    public static boolean isForeignDomainVictim(ServerPlayer player) {
        if (ACTIVE_SHELLS.isEmpty()) {
            return false;
        }
        double r = Config.INFINITE_VOID_RADIUS_BLOCKS.get();
        return isInsideAnyForeignDomain(player, r * r);
    }

    public static void captureDomainFreezePlayerAnchor(ServerPlayer player) {
        if (!isForeignDomainVictim(player)) {
            return;
        }
        DOMAIN_PLAYER_FREEZE_ANCHORS.put(
                player.getUUID(),
                new DomainPlayerFreezeAnchor(player.position(), player.getYRot(), player.getXRot()));
    }

    public static void snapDomainFreezePlayerToAnchor(ServerPlayer player) {
        if (!isForeignDomainVictim(player)) {
            return;
        }
        DomainPlayerFreezeAnchor anchor = DOMAIN_PLAYER_FREEZE_ANCHORS.get(player.getUUID());
        if (anchor == null) {
            return;
        }
        player.moveTo(anchor.position.x, anchor.position.y, anchor.position.z, anchor.yRot, anchor.xRot);
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
    }

    public static void clearDomainFreezePlayerAnchor(ServerPlayer player) {
        DOMAIN_PLAYER_FREEZE_ANCHORS.remove(player.getUUID());
    }

    private static boolean isInsideAnyForeignDomain(ServerPlayer player, double radiusSq) {
        for (Map.Entry<UUID, DomainShell> entry : ACTIVE_SHELLS.entrySet()) {
            if (entry.getKey().equals(player.getUUID())) {
                continue;
            }
            Vec3 c = domainCenterFromShell(entry.getValue());
            if (player.position().distanceToSqr(c) <= radiusSq) {
                return true;
            }
        }
        return false;
    }

    private static Vec3 domainCenterFromShell(DomainShell shell) {
        return new Vec3(
                shell.center().getX() + 0.5,
                shell.center().getY(),
                shell.center().getZ() + 0.5);
    }

    private static void applyFreezeToVictim(LivingEntity entity) {
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setYRot(entity.yRotO);
        entity.setXRot(entity.xRotO);
        entity.hurtMarked = true;
        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
            mob.setTarget(null);
        }
    }

    public static void tickOwner(ServerPlayer owner) {
        InfiniteVoidDomainData domainData = data(owner);
        domainData.tick();
        if (!domainData.isActive()) {
            clearDomainShell(owner);
            return;
        }

        int elapsed = (Config.INFINITE_VOID_DURATION_SECONDS.get() * 20) - domainData.getActiveTicks();
        double radius = Config.INFINITE_VOID_RADIUS_BLOCKS.get();
        ServerLevel level = owner.serverLevel();
        Vec3 domainCenter = getDomainCenter(owner);
        double radiusSq = radius * radius;
        AABB domainArea = new AABB(domainCenter, domainCenter).inflate(radius);
        for (LivingEntity victim : level.getEntitiesOfClass(
                LivingEntity.class,
                domainArea,
                entity -> entity.isAlive()
                        && entity != owner
                        && entity.position().distanceToSqr(domainCenter) <= radiusSq)) {
            victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, 25, 0, false, false, true));
        }

        owner.removeEffect(MobEffects.BLINDNESS);
        owner.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        owner.removeEffect(MobEffects.DIG_SLOWDOWN);

        applyDomainDamage(level, owner, domainCenter, radius, elapsed);
        spawnParticles(level, domainCenter, radius);
        spawnPressureParticles(level, domainCenter, radius);
        playAmbientSound(level, domainCenter, elapsed);
    }

    public static void cleanupFor(ServerPlayer player) {
        clearDomainShell(player);
        clearDomainFreezePlayerAnchor(player);
        DomainFreezeSync.clear(player);
    }

    private static Vec3 getDomainCenter(ServerPlayer owner) {
        DomainShell shell = ACTIVE_SHELLS.get(owner.getUUID());
        if (shell == null) {
            return owner.position();
        }
        return domainCenterFromShell(shell);
    }

    private static void spawnParticles(ServerLevel level, Vec3 center, double radius) {
        var rng = level.random;
        Vec3 sphereCenter = center.add(0.0, 1.0, 0.0);
        double shellRadius = Math.max(2.5, radius * 0.55);

        for (int i = 0; i < 24; i++) {
            double theta = rng.nextDouble() * Math.PI * 2.0;
            double phi = Math.acos((rng.nextDouble() * 2.0) - 1.0);
            double r = shellRadius * (0.90 + rng.nextDouble() * 0.10);
            double x = sphereCenter.x + r * Math.sin(phi) * Math.cos(theta);
            double y = sphereCenter.y + r * Math.cos(phi);
            double z = sphereCenter.z + r * Math.sin(phi) * Math.sin(theta);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SQUID_INK, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
            if (rng.nextFloat() < 0.35f) {
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, x, y, z, 1, 0.02, 0.02, 0.02, 0.0);
            }
        }

        for (int i = 0; i < 14; i++) {
            double angle = (level.getGameTime() * 0.12) + ((Math.PI * 2.0) / 14.0) * i;
            double ringRadius = shellRadius * 1.02;
            double x = sphereCenter.x + Math.cos(angle) * ringRadius;
            double y = sphereCenter.y + (Math.sin(angle * 1.6) * 0.9);
            double z = sphereCenter.z + Math.sin(angle) * ringRadius;
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, x, y, z, 2, 0.04, 0.04, 0.04, 0.0);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, x, y, z, 1, 0.0, 0.0, 0.0, 0.01);
        }

        for (int i = 0; i < 16; i++) {
            double angle = (level.getGameTime() * 0.16) + ((Math.PI * 2.0) / 16.0) * i;
            double ringRadius = shellRadius * 1.15;
            double x = sphereCenter.x + Math.cos(angle) * ringRadius;
            double y = sphereCenter.y + (Math.cos(angle * 1.8) * 1.1);
            double z = sphereCenter.z + Math.sin(angle) * ringRadius;
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.WHITE_ASH, x, y, z, 2, 0.03, 0.03, 0.03, 0.0);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, x, y, z, 1, 0.0, 0.0, 0.0, 0.005);
        }
    }

    /** Pressão visual constante na área. */
    private static void spawnPressureParticles(ServerLevel level, Vec3 center, double radius) {
        var rng = level.random;
        for (int i = 0; i < 8; i++) {
            double ox = (rng.nextDouble() - 0.5) * 2.0 * radius;
            double oy = (rng.nextDouble() - 0.5) * radius;
            double oz = (rng.nextDouble() - 0.5) * 2.0 * radius;
            if (ox * ox + oy * oy + oz * oz > radius * radius) {
                continue;
            }
            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.SMOKE,
                    center.x + ox,
                    center.y + 1.0 + oy,
                    center.z + oz,
                    1,
                    0.05,
                    0.05,
                    0.05,
                    0.0);
        }
    }

    private static void playAmbientSound(ServerLevel level, Vec3 center, int elapsedTicks) {
        // Menos frequente + som baixo: sensação de espaço vazio (antes: âncora de respawn).
        if (elapsedTicks % 40 != 0) {
            return;
        }
        float pitch = 0.32f + level.random.nextFloat() * 0.08f;
        level.playSound(
                null,
                center.x,
                center.y,
                center.z,
                SoundEvents.END_PORTAL_FRAME_FILL,
                SoundSource.PLAYERS,
                0.22f,
                pitch);
    }

    private static void createDomainShell(ServerPlayer owner) {
        UUID ownerId = owner.getUUID();
        if (ACTIVE_SHELLS.containsKey(ownerId)) {
            return;
        }

        ServerLevel level = owner.serverLevel();
        int radius = Math.max(3, (int) Math.round(Config.INFINITE_VOID_RADIUS_BLOCKS.get()));
        BlockPos center = owner.blockPosition().immutable();
        Map<BlockPos, BlockState> previousStates = new HashMap<>();
        int floorY = getLowestEntityY(level, owner, center, radius);

        double shellMin = (radius - 0.85) * (radius - 0.85);
        double shellMax = (radius + 0.35) * (radius + 0.35);
        double innerMax = shellMin;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double distSq = (dx * dx) + (dy * dy) + (dz * dz);
                    BlockPos pos = center.offset(dx, dy, dz).immutable();
                    if (distSq >= shellMin && distSq <= shellMax) {
                        replaceWithBackup(level, pos, ModBlocks.VOID_BLACK_BLOCK.get().defaultBlockState(), previousStates);
                    } else if (distSq < innerMax) {
                        replaceWithBackup(level, pos, Blocks.AIR.defaultBlockState(), previousStates);
                    }
                }
            }
        }

        createInvisibleFloor(level, center, radius, floorY, previousStates);
        ACTIVE_SHELLS.put(ownerId, new DomainShell(center, radius, floorY, previousStates));
    }

    private static void clearDomainShell(ServerPlayer owner) {
        DomainShell shell = ACTIVE_SHELLS.remove(owner.getUUID());
        if (shell == null) {
            return;
        }
        ServerLevel level = owner.serverLevel();
        for (Map.Entry<BlockPos, BlockState> entry : shell.previousStates().entrySet()) {
            level.setBlock(entry.getKey(), entry.getValue(), 3);
        }
        Vec3 c = domainCenterFromShell(shell);
        int scan = shell.radius() + 4;
        List<ServerPlayer> nearby = level.getEntitiesOfClass(
                ServerPlayer.class,
                new AABB(c, c).inflate(scan),
                Entity::isAlive);
        for (ServerPlayer p : nearby) {
            nudgePlayerOutOfSolidBlocks(p, level);
        }
    }

    /**
     * Apos restaurar blocos do domínio, sobe o jogador até o AABB não colidir (evita nascer dentro de void_black / pedra).
     */
    private static void nudgePlayerOutOfSolidBlocks(ServerPlayer player, ServerLevel level) {
        if (level.noCollision(player, player.getBoundingBox())) {
            return;
        }
        double x = player.getX();
        double z = player.getZ();
        double baseY = player.getY();
        float yRot = player.getYRot();
        float xRot = player.getXRot();
        for (int step = 1; step <= 64; step++) {
            double y = baseY + step;
            player.moveTo(x, y, z, yRot, xRot);
            if (level.noCollision(player, player.getBoundingBox())) {
                player.setDeltaMovement(Vec3.ZERO);
                player.fallDistance = 0.0f;
                player.hurtMarked = true;
                return;
            }
        }
    }

    private static void replaceWithBackup(ServerLevel level, BlockPos pos, BlockState targetState, Map<BlockPos, BlockState> backup) {
        BlockState oldState = level.getBlockState(pos);
        if (oldState.equals(targetState)) {
            return;
        }
        backup.putIfAbsent(pos, oldState);
        level.setBlock(pos, targetState, 3);
    }

    /**
     * Altura do disco BARRIER: bloco sob os pés (floor(minY) - 1), alinhado ao topo do chão de suporte.
     * Menor valor entre LivingEntity no raio; calculado uma vez na ativação.
     */
    private static int getLowestEntityY(ServerLevel level, ServerPlayer owner, BlockPos center, int radius) {
        AABB area = new AABB(center).inflate(radius);
        double centerX = center.getX() + 0.5;
        double centerY = center.getY();
        double centerZ = center.getZ() + 0.5;
        int radiusSq = radius * radius;

        List<Entity> entities = level.getEntities(
                (Entity) null,
                area,
                entity -> entity.isAlive()
                        && entity instanceof LivingEntity
                        && entity.position().distanceToSqr(centerX, centerY, centerZ) <= radiusSq);

        double minY = Double.POSITIVE_INFINITY;
        for (Entity entity : entities) {
            minY = Math.min(minY, entity.getBoundingBox().minY);
        }
        if (minY == Double.POSITIVE_INFINITY) {
            minY = owner.getBoundingBox().minY;
        }
        // Feet sit on top of block (y-1); floor(minY) is the air cell index — barrier there is one block too high.
        int supportY = Mth.floor(minY) - 1;
        return Math.max(level.getMinBuildHeight(), supportY);
    }

    private static void createInvisibleFloor(
            ServerLevel level,
            BlockPos center,
            int radius,
            int floorY,
            Map<BlockPos, BlockState> previousStates) {
        int radiusSq = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            int dxSq = dx * dx;
            for (int dz = -radius; dz <= radius; dz++) {
                if (dxSq + (dz * dz) > radiusSq) {
                    continue;
                }
                BlockPos pos = new BlockPos(center.getX() + dx, floorY, center.getZ() + dz);
                BlockState current = level.getBlockState(pos);
                if (current.is(ModBlocks.VOID_BLACK_BLOCK.get())) {
                    continue;
                }
                if (current.is(Blocks.BARRIER)) {
                    continue;
                }
                replaceWithBackup(level, pos, Blocks.BARRIER.defaultBlockState(), previousStates);
            }
        }
    }

    private static void applyDomainDamage(ServerLevel level, ServerPlayer owner, Vec3 center, double radius, int elapsedTicks) {
        if (elapsedTicks % DAMAGE_INTERVAL_TICKS != 0) {
            return;
        }

        AABB area = new AABB(center, center).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity.isAlive()
                        && entity != owner
                        && entity.position().distanceToSqr(center) <= radius * radius);

        for (LivingEntity entity : entities) {
            float baseDamage = calculatePercentageDamage(entity);
            float dealtDamage = applySilentDamage(entity, level, baseDamage);
            applyLifeSteal(owner, dealtDamage);

            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.WHITE_ASH,
                    entity.getX(),
                    entity.getEyeY(),
                    entity.getZ(),
                    4,
                    0.2,
                    0.2,
                    0.2,
                    0.0);
            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.END_ROD,
                    entity.getX(),
                    entity.getEyeY(),
                    entity.getZ(),
                    2,
                    0.15,
                    0.15,
                    0.15,
                    0.01);
            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.SMOKE,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    3,
                    0.2,
                    0.1,
                    0.2,
                    0.0);
        }
    }

    private static float calculatePercentageDamage(LivingEntity entity) {
        return Math.max(1.0f, entity.getMaxHealth() * DOMAIN_DAMAGE_MAX_HEALTH_RATIO);
    }

    private static float applySilentDamage(LivingEntity entity, ServerLevel level, float amount) {
        float healthBefore = entity.getHealth();
        if (healthBefore <= 0.0f) {
            return 0.0f;
        }

        float clampedDamage = amount;
        if (NON_LETHAL_DOMAIN_DAMAGE) {
            clampedDamage = Math.min(amount, Math.max(0.0f, healthBefore - 1.0f));
        }
        if (clampedDamage <= 0.0f) {
            return 0.0f;
        }

        boolean wasSilent = entity.isSilent();
        entity.setSilent(true);
        boolean damaged = entity.hurt(level.damageSources().magic(), clampedDamage);
        entity.setSilent(wasSilent);

        if (!damaged) {
            return 0.0f;
        }
        return Math.max(0.0f, healthBefore - entity.getHealth());
    }

    private static void applyLifeSteal(ServerPlayer player, float damageDealt) {
        if (damageDealt > 0.0f) {
            player.heal(damageDealt * LIFE_STEAL_RATIO);
        }
    }
}
