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
            ENTITY_TYPES.register(
                    "bismuth_warden",
                    () -> EntityType.Builder.<BismuthWarden>of(BismuthWarden::new, MobCategory.MONSTER)
                            .sized(0.9f, 2.9f)
                            .eyeHeight(0.45f)
                            .clientTrackingRange(16)
                            .build("bismuth_warden"));

    public static final DeferredHolder<EntityType<?>, EntityType<BlueVoidOrbEntity>> BLUE_VOID_ORB =
            ENTITY_TYPES.register(
                    "blue_void_orb",
                    () -> EntityType.Builder.<BlueVoidOrbEntity>of(BlueVoidOrbEntity::new, MobCategory.MISC)
                            .sized(0.35f, 0.35f)
                            .clientTrackingRange(48)
                            .updateInterval(1)
                            .build("blue_void_orb"));

    public static final DeferredHolder<EntityType<?>, EntityType<RedVoidOrbEntity>> RED_VOID_ORB =
            ENTITY_TYPES.register(
                    "red_void_orb",
                    () -> EntityType.Builder.<RedVoidOrbEntity>of(RedVoidOrbEntity::new, MobCategory.MISC)
                            .sized(0.35f, 0.35f)
                            .clientTrackingRange(48)
                            .updateInterval(1)
                            .build("red_void_orb"));

    public static final DeferredHolder<EntityType<?>, EntityType<PurpleVoidEntity>> PURPLE_VOID =
            ENTITY_TYPES.register(
                    "purple_void",
                    () -> EntityType.Builder.<PurpleVoidEntity>of(PurpleVoidEntity::new, MobCategory.MISC)
                            .sized(3.0f, 3.0f)
                            .clientTrackingRange(72)
                            .updateInterval(1)
                            .build("purple_void"));

    public static void register(net.neoforged.bus.api.IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
