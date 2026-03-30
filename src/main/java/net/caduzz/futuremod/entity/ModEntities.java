package net.caduzz.futuremod.entity;

import net.caduzz.futuremod.FutureMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, FutureMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<BismuthWarden>> BISMUTH_WARDEN =
        ENTITY_TYPES.register("bismuth_warden",
            () -> EntityType.Builder.<BismuthWarden>of(BismuthWarden::new, MobCategory.MONSTER)
                .sized(0.9f, 2.9f)
                .eyeHeight(0.45f)
                .clientTrackingRange(16)
                .build("bismuth_warden"));

    public static final DeferredHolder<EntityType<?>, EntityType<PurpleVoidOrbEntity>> PURPLE_VOID_ORB =
            ENTITY_TYPES.register(
                    "purple_void_orb",
                    () -> EntityType.Builder.<PurpleVoidOrbEntity>of(PurpleVoidOrbEntity::new, MobCategory.MISC)
                            .sized(0.45f, 0.45f)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build("purple_void_orb"));

    public static void register(net.neoforged.bus.api.IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
