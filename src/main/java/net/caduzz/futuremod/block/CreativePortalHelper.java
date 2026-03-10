package net.caduzz.futuremod.block;

import com.mojang.logging.LogUtils;
import net.caduzz.futuremod.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Acende o portal: coloca blocos de creative_portal no interior de uma moldura
 * retangular de creative_portal_frame. Moldura mínima 3x3 (abertura 1x1).
 * Aceita qualquer tamanho (3x3 até 8x8) e espessura 1, 2 ou 3 blocos.
 * Funciona ao clicar em qualquer bloco da moldura, no chão dentro do vão, ou quando fogo é colocado dentro.
 * Quando criado no Overworld, cria portal espelho no creative_realm nas mesmas coordenadas.
 */
public final class CreativePortalHelper {

    /** Bounds do portal: min/max em cada eixo e eixo fino (0=X, 1=Y, 2=Z). */
    public record PortalBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int thinAxis) {}

    private static final Logger LOG = LogUtils.getLogger();
    private static final Block FRAME = ModBlocks.CREATIVE_PORTAL_FRAME.get();
    private static final BlockState PORTAL_STATE = ModBlocks.CREATIVE_PORTAL.get().defaultBlockState();
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS;

    private CreativePortalHelper() {}

    /**
     * Tenta acender o portal. Chamado com o bloco clicado (moldura ou vizinho, ex. chão).
     * @return true se o portal foi criado
     */
    public static boolean tryLightPortal(Level level, BlockPos clickedPos, Direction face) {
        BlockPos seed = findFrameSeed(level, clickedPos);
        LOG.info("[FutureMod DEBUG] tryLightPortal: clickedPos={} seed={} blocoClicado={}", clickedPos, seed, level.getBlockState(clickedPos).getBlock().getDescriptionId());
        if (seed == null) {
            LOG.info("[FutureMod DEBUG] tryLightPortal: seed null, abortando");
            return false;
        }
        Optional<PortalBounds> bounds;
        // Tenta fallback primeiro (seed em qualquer borda, tamanhos 3x3 até 8x8) - mais robusto
        bounds = tryFill3x3Around(level, seed);
        if (bounds.isEmpty()) {
            bounds = tryFillFromFloodFill(level, seed);
        }
        if (bounds.isEmpty()) {
            LOG.info("[FutureMod DEBUG] tryLightPortal: ambos falharam");
            return false;
        }
        LOG.info("[FutureMod DEBUG] tryLightPortal: portal criado!");
        // Cria portal espelho no creative_realm quando criado no Overworld
        if (level instanceof ServerLevel sl && sl.dimension() == Level.OVERWORLD) {
            ServerLevel creativeRealm = ModDimensions.getOrCreateCreativeRealm(sl.getServer());
            if (creativeRealm != null) {
                createPortalStructureInLevel(creativeRealm, bounds.get());
                LOG.info("[FutureMod DEBUG] tryLightPortal: portal espelho criado no creative_realm");
            }
        }
        return true;
    }

    private static BlockPos findFrameSeed(Level level, BlockPos clickedPos) {
        if (isFrame(level, clickedPos)) return clickedPos;
        for (Direction d : Direction.values()) {
            BlockPos n = clickedPos.relative(d);
            if (isFrame(level, n)) return n;
        }
        return null;
    }

    private static boolean isFrame(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(FRAME);
    }

    /** Tenta preencher usando flood-fill + bounding box (moldura 1 ou 2 blocos de espessura). */
    private static Optional<PortalBounds> tryFillFromFloodFill(Level level, BlockPos seed) {
        Set<BlockPos> frameBlocks = floodFillFrame(level, seed);
        if (frameBlocks.size() < 8) return Optional.empty();

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos p : frameBlocks) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
            maxZ = Math.max(maxZ, p.getZ());
        }
        int dx = maxX - minX + 1, dy = maxY - minY + 1, dz = maxZ - minZ + 1;

        int thinAxis = -1;
        int size = Math.min(dx, Math.min(dy, dz));
        if (size < 1 || size > 3) return Optional.empty(); // espessura 1, 2 ou 3
        if (dx == size) thinAxis = 0;
        else if (dy == size) thinAxis = 1;
        else if (dz == size) thinAxis = 2;
        if (thinAxis < 0) return Optional.empty();

        int w1, w2;
        if (thinAxis == 0) {
            w1 = dy; w2 = dz;
            if (w1 < 3 || w2 < 3) return Optional.empty();
            for (int layer = 0; layer < size; layer++) {
                int x = layer == 0 ? minX : maxX;
                if (fillPortalInYZ(level, frameBlocks, x, minY, minZ, maxY, maxZ))
                    return Optional.of(new PortalBounds(minX, minY, minZ, maxX, maxY, maxZ, thinAxis));
            }
        } else if (thinAxis == 1) {
            w1 = dx; w2 = dz;
            if (w1 < 3 || w2 < 3) return Optional.empty();
            for (int layer = 0; layer < size; layer++) {
                int y = layer == 0 ? minY : maxY;
                if (fillPortalInXZ(level, frameBlocks, y, minX, minZ, maxX, maxZ))
                    return Optional.of(new PortalBounds(minX, minY, minZ, maxX, maxY, maxZ, thinAxis));
            }
        } else {
            w1 = dx; w2 = dy;
            if (w1 < 3 || w2 < 3) return Optional.empty();
            for (int layer = 0; layer < size; layer++) {
                int z = layer == 0 ? minZ : maxZ;
                if (fillPortalInXY(level, frameBlocks, z, minX, minY, maxX, maxY))
                    return Optional.of(new PortalBounds(minX, minY, minZ, maxX, maxY, maxZ, thinAxis));
            }
        }
        return Optional.empty();
    }

    /** Fallback: procura um retângulo onde o seed está em QUALQUER ponto do perímetro (não só cantos). Tamanhos 3x3 até 8x8. */
    private static Optional<PortalBounds> tryFill3x3Around(Level level, BlockPos seed) {
        int sx = seed.getX(), sy = seed.getY(), sz = seed.getZ();
        Optional<PortalBounds> result;
        // Plano XY (z fixo): thinAxis=2, minZ=maxZ=sz
        for (int w = 3; w <= 8; w++) {
            for (int h = 3; h <= 8; h++) {
                for (int minX = sx - w + 1; minX <= sx; minX++) {
                    for (int minY = sy - h + 1; minY <= sy; minY++) {
                        int maxX = minX + w - 1, maxY = minY + h - 1;
                        if (sx < minX || sx > maxX || sy < minY || sy > maxY) continue;
                        boolean onEdge = (sx == minX || sx == maxX || sy == minY || sy == maxY);
                        if (!onEdge) continue;
                        result = tryFillRectXY(level, minX, minY, maxX, maxY, sz);
                        if (result.isPresent()) return result;
                    }
                }
            }
        }
        // Plano XZ (y fixo): thinAxis=1, minY=maxY=sy
        for (int w = 3; w <= 8; w++) {
            for (int h = 3; h <= 8; h++) {
                for (int minX = sx - w + 1; minX <= sx; minX++) {
                    for (int minZ = sz - h + 1; minZ <= sz; minZ++) {
                        int maxX = minX + w - 1, maxZ = minZ + h - 1;
                        if (sx < minX || sx > maxX || sz < minZ || sz > maxZ) continue;
                        boolean onEdge = (sx == minX || sx == maxX || sz == minZ || sz == maxZ);
                        if (!onEdge) continue;
                        result = tryFillRectXZ(level, minX, minZ, maxX, maxZ, sy);
                        if (result.isPresent()) return result;
                    }
                }
            }
        }
        // Plano YZ (x fixo): thinAxis=0, minX=maxX=sx
        for (int w = 3; w <= 8; w++) {
            for (int h = 3; h <= 8; h++) {
                for (int minY = sy - w + 1; minY <= sy; minY++) {
                    for (int minZ = sz - h + 1; minZ <= sz; minZ++) {
                        int maxY = minY + w - 1, maxZ = minZ + h - 1;
                        if (sy < minY || sy > maxY || sz < minZ || sz > maxZ) continue;
                        boolean onEdge = (sy == minY || sy == maxY || sz == minZ || sz == maxZ);
                        if (!onEdge) continue;
                        result = tryFillRectYZ(level, sx, minY, minZ, maxY, maxZ);
                        if (result.isPresent()) return result;
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<PortalBounds> tryFillRectYZ(Level level, int x, int minY, int minZ, int maxY, int maxZ) {
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos p = new BlockPos(x, y, z);
                boolean edge = (y == minY || y == maxY || z == minZ || z == maxZ);
                if (edge && !isFrame(level, p)) return Optional.empty();
            }
        }
        for (int y = minY + 1; y < maxY; y++)
            for (int z = minZ + 1; z < maxZ; z++)
                level.setBlock(new BlockPos(x, y, z), PORTAL_STATE, FLAGS);
        return Optional.of(new PortalBounds(x, minY, minZ, x, maxY, maxZ, 0));
    }

    private static Optional<PortalBounds> tryFillRectXZ(Level level, int minX, int minZ, int maxX, int maxZ, int y) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos p = new BlockPos(x, y, z);
                boolean edge = (x == minX || x == maxX || z == minZ || z == maxZ);
                if (edge && !isFrame(level, p)) return Optional.empty();
            }
        }
        for (int x = minX + 1; x < maxX; x++)
            for (int z = minZ + 1; z < maxZ; z++)
                level.setBlock(new BlockPos(x, y, z), PORTAL_STATE, FLAGS);
        return Optional.of(new PortalBounds(minX, y, minZ, maxX, y, maxZ, 1));
    }

    private static Optional<PortalBounds> tryFillRectXY(Level level, int minX, int minY, int maxX, int maxY, int z) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                BlockPos p = new BlockPos(x, y, z);
                boolean edge = (x == minX || x == maxX || y == minY || y == maxY);
                if (edge && !isFrame(level, p)) return Optional.empty();
            }
        }
        for (int x = minX + 1; x < maxX; x++)
            for (int y = minY + 1; y < maxY; y++)
                level.setBlock(new BlockPos(x, y, z), PORTAL_STATE, FLAGS);
        return Optional.of(new PortalBounds(minX, minY, z, maxX, maxY, z, 2));
    }

    private static Set<BlockPos> floodFillFrame(Level level, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            BlockPos p = queue.pollFirst();
            for (Direction d : Direction.values()) {
                BlockPos next = p.relative(d);
                if (visited.add(next) && isFrame(level, next)) queue.add(next);
            }
        }
        return visited;
    }

    private static boolean fillPortalInYZ(Level level, Set<BlockPos> frameBlocks, int x, int minY, int minZ, int maxY, int maxZ) {
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos p = new BlockPos(x, y, z);
                boolean edge = (y == minY || y == maxY || z == minZ || z == maxZ);
                if (edge) {
                    if (!frameBlocks.contains(p)) return false;
                } else {
                    level.setBlock(p, PORTAL_STATE, FLAGS);
                }
            }
        }
        return true;
    }

    private static boolean fillPortalInXZ(Level level, Set<BlockPos> frameBlocks, int y, int minX, int minZ, int maxX, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos p = new BlockPos(x, y, z);
                boolean edge = (x == minX || x == maxX || z == minZ || z == maxZ);
                if (edge) {
                    if (!frameBlocks.contains(p)) return false;
                } else {
                    level.setBlock(p, PORTAL_STATE, FLAGS);
                }
            }
        }
        return true;
    }

    private static boolean fillPortalInXY(Level level, Set<BlockPos> frameBlocks, int z, int minX, int minY, int maxX, int maxY) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                BlockPos p = new BlockPos(x, y, z);
                boolean edge = (x == minX || x == maxX || y == minY || y == maxY);
                if (edge) {
                    if (!frameBlocks.contains(p)) return false;
                } else {
                    level.setBlock(p, PORTAL_STATE, FLAGS);
                }
            }
        }
        return true;
    }

    public static boolean tryCreatePortal(Level level, BlockPos start, Direction face) {
        if (!isFrame(level, start)) return false;
        return tryLightPortal(level, start, face);
    }

    public static boolean tryCreatePortal(Level level, BlockPos start, Direction.Axis axis) {
        if (!isFrame(level, start)) return false;
        return tryLightPortal(level, start, axis == Direction.Axis.X ? Direction.EAST : Direction.NORTH);
    }

    public static boolean tryCreatePortalFromInside(Level level, BlockPos clickedPos) {
        return tryLightPortal(level, clickedPos, null);
    }

    /**
     * Cria a estrutura completa do portal (moldura + interior) em um nível nas coordenadas dos bounds.
     * Usado para criar o portal espelho no creative_realm.
     */
    private static void createPortalStructureInLevel(Level level, PortalBounds b) {
        int size = (b.thinAxis() == 0) ? (b.maxX() - b.minX() + 1)
                : (b.thinAxis() == 1) ? (b.maxY() - b.minY() + 1)
                : (b.maxZ() - b.minZ() + 1);
        for (int layer = 0; layer < size; layer++) {
            if (b.thinAxis() == 0) {
                int x = layer == 0 ? b.minX() : b.maxX();
                placeFrameAndPortalYZ(level, x, b.minY(), b.minZ(), b.maxY(), b.maxZ());
            } else if (b.thinAxis() == 1) {
                int y = layer == 0 ? b.minY() : b.maxY();
                placeFrameAndPortalXZ(level, y, b.minX(), b.minZ(), b.maxX(), b.maxZ());
            } else {
                int z = layer == 0 ? b.minZ() : b.maxZ();
                placeFrameAndPortalXY(level, z, b.minX(), b.minY(), b.maxX(), b.maxY());
            }
        }
    }

    private static void placeFrameAndPortalXY(Level level, int z, int minX, int minY, int maxX, int maxY) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                BlockPos p = new BlockPos(x, y, z);
                boolean edge = (x == minX || x == maxX || y == minY || y == maxY);
                level.setBlock(p, edge ? FRAME.defaultBlockState() : PORTAL_STATE, FLAGS);
            }
        }
    }

    private static void placeFrameAndPortalXZ(Level level, int y, int minX, int minZ, int maxX, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos p = new BlockPos(x, y, z);
                boolean edge = (x == minX || x == maxX || z == minZ || z == maxZ);
                level.setBlock(p, edge ? FRAME.defaultBlockState() : PORTAL_STATE, FLAGS);
            }
        }
    }

    private static void placeFrameAndPortalYZ(Level level, int x, int minY, int minZ, int maxY, int maxZ) {
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos p = new BlockPos(x, y, z);
                boolean edge = (y == minY || y == maxY || z == minZ || z == maxZ);
                level.setBlock(p, edge ? FRAME.defaultBlockState() : PORTAL_STATE, FLAGS);
            }
        }
    }

    /**
     * Remove todos os blocos de portal quando qualquer parte da moldura ou do portal for quebrada.
     * Usa flood-fill em moldura E portal para obter a estrutura completa (evita planos desconectados).
     * Depois limpa todos os blocos de portal dentro do bounding box.
     */
    public static void removePortalAt(Level level, BlockPos brokenPos) {
        BlockPos seed = null;
        if (level.getBlockState(brokenPos).is(FRAME) || level.getBlockState(brokenPos).is(ModBlocks.CREATIVE_PORTAL.get())) {
            seed = brokenPos;
        } else {
            for (Direction d : Direction.values()) {
                BlockPos n = brokenPos.relative(d);
                if (level.getBlockState(n).is(FRAME) || level.getBlockState(n).is(ModBlocks.CREATIVE_PORTAL.get())) {
                    seed = n;
                    break;
                }
            }
        }
        if (seed == null) return;

        // Flood-fill em moldura E portal: pega a estrutura completa mesmo com planos desconectados
        Set<BlockPos> structureBlocks = floodFillFrameAndPortal(level, seed);
        if (structureBlocks.isEmpty()) return;

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos p : structureBlocks) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
            maxZ = Math.max(maxZ, p.getZ());
        }

        // Limpa TODOS os blocos de portal dentro do bounding box (mais robusto)
        Block portalBlock = ModBlocks.CREATIVE_PORTAL.get();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (level.getBlockState(p).is(portalBlock)) {
                        level.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), FLAGS);
                    }
                }
            }
        }
    }

    /** Flood-fill que inclui blocos de moldura E portal (estrutura conectada). */
    private static Set<BlockPos> floodFillFrameAndPortal(Level level, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        Block portalBlock = ModBlocks.CREATIVE_PORTAL.get();
        while (!queue.isEmpty()) {
            BlockPos p = queue.pollFirst();
            for (Direction d : Direction.values()) {
                BlockPos next = p.relative(d);
                if (visited.add(next)) {
                    var state = level.getBlockState(next);
                    if (state.is(FRAME) || state.is(portalBlock)) {
                        queue.add(next);
                    }
                }
            }
        }
        return visited;
    }
}
