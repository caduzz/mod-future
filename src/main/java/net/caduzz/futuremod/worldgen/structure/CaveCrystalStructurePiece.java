package net.caduzz.futuremod.worldgen.structure;

import net.caduzz.futuremod.FutureMod;
import net.caduzz.futuremod.block.ModBlocks;
import net.caduzz.futuremod.worldgen.ModStructurePieces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class CaveCrystalStructurePiece extends StructurePiece {
    private final BlockPos center;
    private final int radius;
    private final int halfHeight;

    public CaveCrystalStructurePiece(BlockPos center, int radius, int halfHeight) {
        super(ModStructurePieces.CAVE_CRYSTAL_PIECE.get(), 0, makeBox(center, radius, halfHeight));
        this.center = center;
        this.radius = radius;
        this.halfHeight = halfHeight;
    }

    public CaveCrystalStructurePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructurePieces.CAVE_CRYSTAL_PIECE.get(), tag);
        this.center = new BlockPos(tag.getInt("CenterX"), tag.getInt("CenterY"), tag.getInt("CenterZ"));
        this.radius = tag.getInt("Radius");
        this.halfHeight = tag.getInt("HalfHeight");
    }

    private static BoundingBox makeBox(BlockPos center, int radius, int halfHeight) {
        return new BoundingBox(
                center.getX() - radius,
                center.getY() - halfHeight,
                center.getZ() - radius,
                center.getX() + radius,
                center.getY() + halfHeight,
                center.getZ() + radius
        );
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CenterX", this.center.getX());
        tag.putInt("CenterY", this.center.getY());
        tag.putInt("CenterZ", this.center.getZ());
        tag.putInt("Radius", this.radius);
        tag.putInt("HalfHeight", this.halfHeight);
    }

    @Override
    public void postProcess(
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator chunkGenerator,
            RandomSource random,
            BoundingBox chunkBox,
            ChunkPos chunkPos,
            BlockPos pivot
    ) {
        int attempts = 0;
        int placed = 0;
        int blocked = 0;

        for (int x = this.center.getX() - this.radius; x <= this.center.getX() + this.radius; x++) {
            for (int y = this.center.getY() - this.halfHeight; y <= this.center.getY() + this.halfHeight; y++) {
                for (int z = this.center.getZ() - this.radius; z <= this.center.getZ() + this.radius; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!chunkBox.isInside(pos)) {
                        continue;
                    }

                    attempts++;
                    if (!isInsideCrystalShape(pos)) {
                        continue;
                    }

                    BlockState current = level.getBlockState(pos);
                    if (!current.isAir()) {
                        blocked++;
                        continue;
                    }

                    BlockState target = choosePalette(level, random, pos);
                    this.placeBlock(level, target, x, y, z, chunkBox);
                    placed++;
                }
            }
        }

        // Adapta ao terreno irregular: cria pilares verticais onde houver "ilhas" da estrutura no ar.
        createSupportPillars(level, chunkBox);

        if (placed == 0) {
            FutureMod.LOGGER.debug(
                    "Cave structure failed at {} (space check failed, blocked={} attempts={})",
                    this.center,
                    blocked,
                    attempts
            );
        } else {
            FutureMod.LOGGER.debug(
                    "Cave structure generated at {} (placed={} blocked={} attempts={})",
                    this.center,
                    placed,
                    blocked,
                    attempts
            );
        }
    }

    private boolean isInsideCrystalShape(BlockPos pos) {
        int dx = pos.getX() - this.center.getX();
        int dy = pos.getY() - this.center.getY();
        int dz = pos.getZ() - this.center.getZ();
        double horizontal = (double) (dx * dx + dz * dz) / (double) (this.radius * this.radius);
        double vertical = (double) (dy * dy) / (double) (this.halfHeight * this.halfHeight);
        return horizontal + vertical <= 1.0D;
    }

    private BlockState choosePalette(ServerLevelAccessor level, RandomSource random, BlockPos pos) {
        if (random.nextFloat() < 0.08F) {
            return ModBlocks.FUTURE_GLOW_FLOWER.get().defaultBlockState();
        }
        if (isTouchingSolid(level, pos) && random.nextFloat() < 0.20F) {
            return ModBlocks.FUTURE_MOSS_BLOCK.get().defaultBlockState();
        }
        return Blocks.AMETHYST_BLOCK.defaultBlockState();
    }

    private boolean isTouchingSolid(ServerLevelAccessor level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).isSolid()) {
                return true;
            }
        }
        return false;
    }

    private void createSupportPillars(WorldGenLevel level, BoundingBox chunkBox) {
        for (int x = this.center.getX() - this.radius; x <= this.center.getX() + this.radius; x += 2) {
            for (int z = this.center.getZ() - this.radius; z <= this.center.getZ() + this.radius; z += 2) {
                BlockPos top = new BlockPos(x, this.center.getY() - this.halfHeight, z);
                if (!chunkBox.isInside(top)) {
                    continue;
                }

                BlockState topState = level.getBlockState(top);
                if (!isGeneratedStructureBlock(topState.getBlock())) {
                    continue;
                }

                BlockPos.MutableBlockPos cursor = top.below().mutable();
                int depth = 0;
                while (chunkBox.isInside(cursor) && depth < 24 && level.getBlockState(cursor).isAir()) {
                    this.placeBlock(level, ModBlocks.FUTURE_MOSS_BLOCK.get().defaultBlockState(), cursor.getX(), cursor.getY(), cursor.getZ(), chunkBox);
                    cursor.move(Direction.DOWN);
                    depth++;
                }
            }
        }
    }

    private boolean isGeneratedStructureBlock(Block block) {
        return block == Blocks.AMETHYST_BLOCK
                || block == ModBlocks.FUTURE_MOSS_BLOCK.get()
                || block == ModBlocks.FUTURE_GLOW_FLOWER.get();
    }
}
