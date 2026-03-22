package net.caduzz.futuremod.client;

import java.util.ArrayList;
import java.util.List;

import net.caduzz.futuremod.dimension.CreativeRealmFireflyPositions;
import net.caduzz.futuremod.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Spawna particulas de cherry em posicoes fixas na Creative Realm.
 * Usa as mesmas posicoes deterministicas das luzes para simular
 * petalas com emissao de luz real no ambiente.
 */
@EventBusSubscriber(modid = "futuremod", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class CreativeRealmFireflyParticles {

    private static final int TICK_INTERVAL = 2;
    private static final int CHUNK_RADIUS = 4;
    private static final int PARTICLES_PER_CYCLE = 12;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.level.dimension() != ModDimensions.CREATIVE_REALM_LEVEL) return;
        if (mc.isPaused()) return;

        if (mc.player.tickCount % TICK_INTERVAL != 0) return;

        var level = mc.level;
        var player = mc.player;
        var rng = player.getRandom();

        int playerChunkX = SectionPos.blockToSectionCoord(player.blockPosition().getX());
        int playerChunkZ = SectionPos.blockToSectionCoord(player.blockPosition().getZ());

        var allPositions = new ArrayList<BlockPos>();
        for (int dcx = -CHUNK_RADIUS; dcx <= CHUNK_RADIUS; dcx++) {
            for (int dcz = -CHUNK_RADIUS; dcz <= CHUNK_RADIUS; dcz++) {
                var positions = CreativeRealmFireflyPositions.getFixedPositionsForChunk(
                        playerChunkX + dcx, playerChunkZ + dcz);
                allPositions.addAll(positions);
            }
        }

        for (int i = 0; i < PARTICLES_PER_CYCLE && !allPositions.isEmpty(); i++) {
            BlockPos basePos = allPositions.get(rng.nextInt(allPositions.size()));

            int floorY = CreativeRealmFireflyPositions.findFloorY(level, basePos.getX(), basePos.getZ());
            if (floorY <= level.getMinBuildHeight()) continue;

            double px = basePos.getX() + 0.5 + (rng.nextDouble() - 0.5) * 0.5;
            double py = floorY + 0.2 + rng.nextDouble() * 0.8;
            double pz = basePos.getZ() + 0.5 + (rng.nextDouble() - 0.5) * 0.5;

            var particle = ParticleTypes.CHERRY_LEAVES;

            // Leve deriva lateral e queda suave para parecer petalas no ar.
            double dx = (rng.nextDouble() - 0.5) * 0.008;
            double dy = -0.01 - rng.nextDouble() * 0.01;
            double dz = (rng.nextDouble() - 0.5) * 0.008;

            level.addParticle(particle, px, py, pz, dx, dy, dz);
        }
    }
}
