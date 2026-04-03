package net.caduzz.futuremod.domain;

import org.joml.Vector3f;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * Sistema cinematográfico de ativação do Infinite Void Domain em 4 fases.
 * 
 * Fases:
 * 1. CHARGING (0-40 ticks) - Carregamento com partículas giratórias
 * 2. COLLAPSING (40-65 ticks) - Colapso do espaço com aceleração
 * 3. BURST (65-70 ticks) - Explosão silenciosa com flash
 * 4. ACTIVE (70+ ticks) - Domínio operacional (delegado ao InfiniteVoidDomainManager)
 */
public class VoidActivationSequence {

    public enum ActivationPhase {
        CHARGING(0, 40),
        COLLAPSING(40, 65),
        BURST(65, 70),
        ACTIVE(70, Integer.MAX_VALUE);

        final int startTick;
        final int endTick;

        ActivationPhase(int start, int end) {
            this.startTick = start;
            this.endTick = end;
        }

        public static ActivationPhase fromTicks(int ticks) {
            for (ActivationPhase phase : values()) {
                if (ticks >= phase.startTick && ticks < phase.endTick) {
                    return phase;
                }
            }
            return ACTIVE;
        }
    }

    public static void tickActivation(ServerPlayer player, int activationTicks) {
        ServerLevel level = player.serverLevel();
        if (level == null) return;

        ActivationPhase phase = ActivationPhase.fromTicks(activationTicks);
        Vec3 playerPos = player.position();
        double cx = playerPos.x;
        double cy = playerPos.y + player.getBbHeight() * 0.5;
        double cz = playerPos.z;

        switch (phase) {
            case CHARGING:
                tickCharging(level, player, cx, cy, cz, activationTicks);
                break;
            case COLLAPSING:
                tickCollapsing(level, player, cx, cy, cz, activationTicks);
                break;
            case BURST:
                tickBurst(level, player, cx, cy, cz, activationTicks);
                break;
            case ACTIVE:
                // Deleg ao InfiniteVoidDomainManager com partículas normais
                break;
        }
    }

    /**
     * Fase 1: CHARGING (0-40 ticks, ~2 segundos)
     * - Partículas roxas e azuis girando lentamente
     * - Som crescente (volume progressivo)
     * - Leve escurecimento visual (não implementado sem shader)
     */
    private static void tickCharging(ServerLevel level, ServerPlayer player, double cx, double cy, double cz, int ticks) {
        int relTicks = ticks;

        // Partículas roxas e azuis em espiral
        double spinAngle = relTicks * 4.5;
        int points = 16;
        float radius = 4.5f + (relTicks / 40f) * 0.5f;

        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points + spinAngle;
            double x = cx + Math.cos(angle) * radius;
            double z = cz + Math.sin(angle) * radius;
            double y = cy + Math.sin(angle * 0.5) * 0.8;

            // Roxo brilhante
            Vector3f purpleBright = new Vector3f(0.72f, 0.35f, 1.0f);
            level.sendParticles(new DustParticleOptions(purpleBright, 0.8f), x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }

        // Partículas azuis internas
        for (int i = 0; i < 8; i++) {
            double angle = (2 * Math.PI * i) / 8 - spinAngle * 0.5;
            double x = cx + Math.cos(angle) * (radius * 0.5);
            double z = cz + Math.sin(angle) * (radius * 0.5);
            double y = cy + (Math.random() - 0.5) * 1.0;

            Vector3f azureBlue = new Vector3f(0.3f, 0.6f, 1.0f);
            level.sendParticles(new DustParticleOptions(azureBlue, 0.6f), x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }

        // Sons progressivos (crescendo)
        if (relTicks % 8 == 0) {
            float pitch = 0.55f + (relTicks / 40f) * 0.25f;
            float volume = 0.4f + (relTicks / 40f) * 0.6f;
            level.playSound(null, cx, cy, cz, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, volume, pitch);
        }

        // Tremor visual (modificar yRot ligeiramente, cliente lado)
        // Nota: Seria melhor com shader ou packet, mas isso requer sincronização
    }

    /**
     * Fase 2: COLLAPSING (40-65 ticks, ~1.25 segundos)
     * - Partículas acceleram em direção ao centro
     * - Efeito de "compressão"
     * - Silêncio progressivo
     */
    private static void tickCollapsing(ServerLevel level, ServerPlayer player, double cx, double cy, double cz, int ticks) {
        int relTicks = ticks - 40;
        int phaseDuration = 25;

        // Partículas roxas sendo puxadas para o centro
        int points = 20 + (relTicks / 2);
        float radius = 5.0f * (1.0f - (relTicks / (float) phaseDuration) * 0.7f);

        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points + relTicks;
            double distFromCenter = radius + (Math.random() - 0.5) * 0.8;
            double x = cx + Math.cos(angle) * distFromCenter;
            double z = cz + Math.sin(angle) * distFromCenter;
            double y = cy + (Math.random() - 0.5) * 2.0;

            // Roxo mais escuro (core)
            Vector3f purpleCore = new Vector3f(0.45f, 0.08f, 0.85f);
            level.sendParticles(new DustParticleOptions(purpleCore, 1.0f), x, y, z, 1, 0.0, 0.0, 0.0, 0.0);

            // Velocidade em direção ao centro
            Vec3 dir = new Vec3(cx - x, cy - y, cz - z).normalize();
            double speed = 0.15 + (relTicks / (float) phaseDuration) * 0.25;

            level.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, dir.x * speed, dir.y * speed, dir.z * speed, 0.1);
        }

        // Som decrescente (silêncio antes do pico)
        if (relTicks % 10 == 0 && relTicks < phaseDuration - 5) {
            float volume = 0.8f * (1.0f - relTicks / (float) phaseDuration);
            float pitch = 0.7f - (relTicks / (float) phaseDuration) * 0.2f;
            level.playSound(null, cx, cy, cz, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, volume, pitch);
        }
    }

    /**
     * Fase 3: BURST (65-70 ticks, ~0.25 segundos)
     * - Flash roxo/azulado (sem som tradicional)
     * - Expansão rápida de partículas
     * - Transição imediata
     */
    private static void tickBurst(ServerLevel level, ServerPlayer player, double cx, double cy, double cz, int ticks) {
        int relTicks = ticks - 65;
        float progress = relTicks / 5f; // 0 -> 1

        // Flash expansivo de partículas
        int burstPoints = 40;
        float expandRadius = 1.0f + progress * 8.0f;

        for (int i = 0; i < burstPoints; i++) {
            double angle = (2 * Math.PI * i) / burstPoints;
            double x = cx + Math.cos(angle) * expandRadius;
            double z = cz + Math.sin(angle) * expandRadius;
            double y = cy + (Math.random() - 0.5) * (expandRadius * 0.5);

            // Brilho roxo/azul
            Vector3f purpleBright = new Vector3f(0.72f, 0.35f, 1.0f);
            Vector3f azureBlue = new Vector3f(0.3f, 0.6f, 1.0f);
            Vector3f color = i % 2 == 0 ? purpleBright : azureBlue;

            level.sendParticles(new DustParticleOptions(color, 1.2f), x, y, z, 1, 0.0, 0.0, 0.0, 0.0);

            // Partículas END_ROD para mais brilho
            level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.0, 0.0, 0.0, 0.1);
        }

        // Som único de transição (acorde etéreo, não explosão)
        if (relTicks == 0) {
            level.playSound(null, cx, cy, cz, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.2f, 0.8f);
        }
    }

    /**
     * Tipo de suporte para partículas customizadas com velocidade direcionada
     */
    public static void spawnDirectedParticles(ServerLevel level, double x, double y, double z,
            net.minecraft.core.particles.ParticleOptions options,
            Vec3 direction, double speed, int count) {
        for (int i = 0; i < count; i++) {
            Vec3 scatter = direction.normalize().scale(speed).add(
                    (Math.random() - 0.5) * 0.2,
                    (Math.random() - 0.5) * 0.2,
                    (Math.random() - 0.5) * 0.2);
            level.sendParticles(options, x, y, z, 1, scatter.x, scatter.y, scatter.z, 0.0);
        }
    }
}
