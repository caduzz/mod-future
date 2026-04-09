package net.caduzz.futuremod.worldgen;

import net.caduzz.futuremod.FutureMod;
import net.caduzz.futuremod.worldgen.structure.CaveCrystalStructurePiece;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModStructurePieces {
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(BuiltInRegistries.STRUCTURE_PIECE, FutureMod.MOD_ID);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> CAVE_CRYSTAL_PIECE =
            STRUCTURE_PIECES.register("cave_crystal_piece", () -> CaveCrystalStructurePiece::new);

    private ModStructurePieces() {
    }

    public static void register(IEventBus eventBus) {
        STRUCTURE_PIECES.register(eventBus);
    }
}
