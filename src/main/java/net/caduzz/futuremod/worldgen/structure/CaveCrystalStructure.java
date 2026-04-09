package net.caduzz.futuremod.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.caduzz.futuremod.FutureMod;
import net.caduzz.futuremod.worldgen.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class CaveCrystalStructure extends Structure {
    public static final MapCodec<CaveCrystalStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            HeightProvider.CODEC.fieldOf("height").forGetter(structure -> structure.heightProvider),
            IntProvider.codec(3, 16).fieldOf("radius").forGetter(structure -> structure.radiusProvider),
            IntProvider.codec(2, 12).fieldOf("half_height").forGetter(structure -> structure.halfHeightProvider)
    ).apply(instance, CaveCrystalStructure::new));

    private final HeightProvider heightProvider;
    private final IntProvider radiusProvider;
    private final IntProvider halfHeightProvider;

    public CaveCrystalStructure(StructureSettings settings, HeightProvider heightProvider, IntProvider radiusProvider, IntProvider halfHeightProvider) {
        super(settings);
        this.heightProvider = heightProvider;
        this.radiusProvider = radiusProvider;
        this.halfHeightProvider = halfHeightProvider;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        RandomSource random = context.random();
        ChunkPos chunkPos = context.chunkPos();
        WorldGenerationContext worldGenerationContext = new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor());

        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int y = this.heightProvider.sample(random, worldGenerationContext);

        int radius = this.radiusProvider.sample(random);
        int halfHeight = this.halfHeightProvider.sample(random);

        BlockPos center = new BlockPos(x, y, z);
        FutureMod.LOGGER.debug(
                "Trying cave structure '{}' at {} radius={} halfHeight={}",
                "futuremod:cave_crystal_structure",
                center,
                radius,
                halfHeight
        );

        return Optional.of(new GenerationStub(center, builder ->
                builder.addPiece(new CaveCrystalStructurePiece(center, radius, halfHeight))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.CAVE_CRYSTAL_STRUCTURE.get();
    }
}
