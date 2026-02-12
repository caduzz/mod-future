package net.caduzz.futuremod.item;

import java.util.List;
import java.util.Map;

import net.caduzz.futuremod.FutureMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister
            .create(net.minecraft.core.registries.Registries.ARMOR_MATERIAL, FutureMod.MOD_ID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> BISMUTH =
        ARMOR_MATERIALS.register("bismuth", () ->
            new ArmorMaterial(
                Map.of(
                    ArmorItem.Type.HELMET, 3,
                    ArmorItem.Type.CHESTPLATE, 8,
                    ArmorItem.Type.LEGGINGS, 6,
                    ArmorItem.Type.BOOTS, 3
                ),
                40,
                SoundEvents.ARMOR_EQUIP_DIAMOND,
                () -> Ingredient.of(ModItems.INGOT_BISMUTH.get()),
                List.of(
                    new ArmorMaterial.Layer(
                        ResourceLocation.fromNamespaceAndPath(
                            FutureMod.MOD_ID, "bismuth"
                        )
                    )
                ),
                1.0f,
                0.0f
            )
        );

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GLASSES = ARMOR_MATERIALS.register("glasses",
            () -> new ArmorMaterial(
                    Map.of(
                            ArmorItem.Type.HELMET, 3),
                    20,
                    SoundEvents.ARMOR_EQUIP_IRON,
                    () -> Ingredient.of(ModItems.INGOT_BISMUTH.get()),
                    List.of(
                            new ArmorMaterial.Layer(
                                    ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "glasses"))),
                    2.0f,
                    0.0f)

    );

}