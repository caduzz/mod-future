package net.caduzz.futuremod.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public class ModToolMaterials {

    public static final Tier BISMUTH = new SimpleTier(
        BlockTags.MINEABLE_WITH_PICKAXE, // blocos que pode minerar
        1500, // durabilidade
        8.0f, // velocidade de mineração
        3.0f, // dano base
        18,   // enchantability
        () -> Ingredient.of(ModItems.INGOT_BISMUTH.get())
    );
}