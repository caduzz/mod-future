package net.caduzz.futuremod.dimension;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Coloca blocos de luz fixos na superfície da Creative Realm,
 * simulando vaga-lumes que emitem luz real.
 * Posições determinísticas por chunk - não se movem.
 */
@EventBusSubscriber(modid = "futuremod")
public final class CreativeRealmFireflyLights {

    private static final int TICK_INTERVAL = 10;
    private static final int CHUNK_RADIUS = 5;
    private static final int LIGHT_LEVEL = 9;

    private static final Map<UUID, Set<BlockPos>> PLAYER_LIGHTS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;
        if (level.dimension() != ModDimensions.CREATIVE_REALM_LEVEL) return;

        var serverLevel = (ServerLevel) level;
        long gameTime = serverLevel.getGameTime();
        if (gameTime % TICK_INTERVAL != 0) return;

        for (ServerPlayer player : serverLevel.players()) {
            var set = PLAYER_LIGHTS.computeIfAbsent(player.getUUID(), u -> new HashSet<>());
            int playerChunkX = SectionPos.blockToSectionCoord(player.blockPosition().getX());
            int playerChunkZ = SectionPos.blockToSectionCoord(player.blockPosition().getZ());

            for (int dcx = -CHUNK_RADIUS; dcx <= CHUNK_RADIUS; dcx++) {
                for (int dcz = -CHUNK_RADIUS; dcz <= CHUNK_RADIUS; dcz++) {
                    int cx = playerChunkX + dcx;
                    int cz = playerChunkZ + dcz;
                    try {
                        LevelChunk chunk = serverLevel.getChunk(cx, cz);
                        placeFixedLightsForChunk(serverLevel, chunk, set);
                    } catch (Exception ignored) {}
                }
            }

            set.removeIf(pos -> {
                int distChunks = Math.max(
                        Math.abs(SectionPos.blockToSectionCoord(pos.getX()) - playerChunkX),
                        Math.abs(SectionPos.blockToSectionCoord(pos.getZ()) - playerChunkZ));
                if (distChunks > CHUNK_RADIUS + 1) {
                    if (serverLevel.getBlockState(pos).is(Blocks.LIGHT)) {
                        serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                    return true;
                }
                return false;
            });
        }
    }

    private static void placeFixedLightsForChunk(ServerLevel level, LevelChunk chunk, Set<BlockPos> placed) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        var positions = CreativeRealmFireflyPositions.getFixedPositionsForChunk(chunkX, chunkZ);

        for (BlockPos basePos : positions) {
            int floorY = CreativeRealmFireflyPositions.findFloorY(level, basePos.getX(), basePos.getZ());
            if (floorY <= level.getMinBuildHeight()) continue;
            BlockPos pos = new BlockPos(basePos.getX(), floorY, basePos.getZ()).above();

            if (placed.contains(pos)) continue;
            if (!level.getBlockState(pos).isAir() && !level.getBlockState(pos).canBeReplaced()) continue;

            var lightState = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, LIGHT_LEVEL);
            if (level.setBlock(pos, lightState, 3)) {
                placed.add(pos.immutable());
            }
        }
    }

    private static void removeAllLightsForPlayer(ServerPlayer player) {
        var list = PLAYER_LIGHTS.remove(player.getUUID());
        if (list != null && player.level() instanceof ServerLevel level) {
            for (BlockPos pos : list) {
                if (level.getBlockState(pos).is(Blocks.LIGHT)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            removeAllLightsForPlayer(player);
        }
    }
}
