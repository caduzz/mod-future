package net.caduzz.futuremod.dimension;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Posições fixas e determinísticas para vaga-lumes na Creative Realm.
 * Usado por luzes (server) e partículas (client) para manter sincronia.
 */
public final class CreativeRealmFireflyPositions {

    private static final int LIGHTS_PER_CHUNK = 4;
    private static final long SEED = 0x9E3779B97F4A7C15L;

    private CreativeRealmFireflyPositions() {}

    /** Retorna as posições base (x,0,z) fixas para um chunk. */
    public static List<BlockPos> getFixedPositionsForChunk(int chunkX, int chunkZ) {
        var rng = new java.util.Random(SEED ^ ((long) chunkX << 32 | chunkZ & 0xFFFFFFFFL));
        var positions = new ArrayList<BlockPos>(LIGHTS_PER_CHUNK);
        for (int i = 0; i < LIGHTS_PER_CHUNK; i++) {
            int x = chunkX * 16 + rng.nextInt(16);
            int z = chunkZ * 16 + rng.nextInt(16);
            positions.add(new BlockPos(x, 0, z));
        }
        return positions;
    }

    /**
     * Encontra o chão (superfície de grama/terra) em dimensões com teto.
     * O heightmap retorna o teto (bedrock), então varremos de cima para baixo.
     */
    public static int findFloorY(Level level, int x, int z) {
        int maxY = level.getMaxBuildHeight() - 1;
        int minY = level.getMinBuildHeight();
        boolean foundCeiling = false;

        for (int y = maxY; y >= minY; y--) {
            BlockState state = level.getBlockState(new BlockPos(x, y, z));
            boolean solid = !state.isAir() && state.blocksMotion();

            if (solid && !foundCeiling) {
                foundCeiling = true;
            } else if (solid && foundCeiling) {
                return y;
            }
        }
        return minY;
    }
}
