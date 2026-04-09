package net.caduzz.futuremod.worldgen;

import net.caduzz.futuremod.FutureMod;
import net.caduzz.futuremod.worldgen.structure.CaveCrystalStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, FutureMod.MOD_ID);

    public static final DeferredHolder<StructureType<?>, StructureType<CaveCrystalStructure>> CAVE_CRYSTAL_STRUCTURE =
            STRUCTURE_TYPES.register("cave_crystal_structure", () -> () -> CaveCrystalStructure.CODEC);

    private ModStructures() {
    }

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
    }
}
