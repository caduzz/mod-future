package net.caduzz.futuremod.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.caduzz.futuremod.block.ModBlocks;

/**
 * Coloca a moldura do portal da dimensão criativa no spawn do overworld (uma vez por mundo).
 */
public final class SpawnPortalStructure {

    private static final BlockState FRAME = ModBlocks.CREATIVE_PORTAL_FRAME.get().defaultBlockState();

    /** Distância em X e Z do ponto de spawn para a base da moldura (evita spawn dentro do portal). */
    private static final int OFFSET_FROM_SPAWN = 3;

    private SpawnPortalStructure() {}

    /**
     * Tenta colocar a estrutura no spawn. Só coloca se ainda não existir moldura perto do spawn.
     */
    public static void tryCreateAtSpawn(ServerLevel level) {
        if (level.dimension() != net.minecraft.world.level.Level.OVERWORLD) return;

        BlockPos spawn = level.getSharedSpawnPos();
        if (alreadyHasStructure(level, spawn)) return;

        int x = spawn.getX() + OFFSET_FROM_SPAWN;
        int z = spawn.getZ();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        BlockPos base = new BlockPos(x, y, z);
        buildFrame3x3(level, base);
    }

    private static boolean alreadyHasStructure(ServerLevel level, BlockPos spawn) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = 0; dy <= 4; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    if (level.getBlockState(spawn.offset(dx, dy, dz)).is(ModBlocks.CREATIVE_PORTAL_FRAME.get())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Moldura 3x3 no plano X-Y (Z fixo): 8 blocos de frame, 1 de abertura. */
    private static void buildFrame3x3(ServerLevel level, BlockPos base) {
        int x0 = base.getX(), y0 = base.getY(), z0 = base.getZ();
        setFrame(level, x0,     y0,     z0);
        setFrame(level, x0 + 1, y0,     z0);
        setFrame(level, x0 + 2, y0,     z0);
        setFrame(level, x0,     y0 + 1, z0);
        setFrame(level, x0 + 2, y0 + 1, z0);
        setFrame(level, x0,     y0 + 2, z0);
        setFrame(level, x0 + 1, y0 + 2, z0);
        setFrame(level, x0 + 2, y0 + 2, z0);
    }

    private static void setFrame(ServerLevel level, int x, int y, int z) {
        level.setBlock(new BlockPos(x, y, z), FRAME, 3);
    }
}
