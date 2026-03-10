package net.caduzz.futuremod.entity;

import net.caduzz.futuremod.FutureMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, FutureMod.MOD_ID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>, EntityType<BismuthWarden>> BISMUTH_WARDEN =
        ENTITY_TYPES.register("bismuth_warden",
            () -> EntityType.Builder.<BismuthWarden>of(BismuthWarden::new, MobCategory.MONSTER)
                .sized(0.9f, 2.9f)
                .eyeHeight(0.45f)
                .clientTrackingRange(16)
                .build("bismuth_warden"));

    public static void register(net.neoforged.bus.api.IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
