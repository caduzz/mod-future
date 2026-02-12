package net.caduzz.futuremod.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public enum ModTiers implements Tier {

    BISMUTH(
        2,      // nível (2 = ferro)
        800,    // durabilidade
        7.0F,   // velocidade
        2.5F,   // dano bônus
        18,     // encantabilidade
        BlockTags.NEEDS_IRON_TOOL,
        () -> Ingredient.of(ModItems.INGOT_BISMUTH.get())
    );

    private final int level;
    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int enchantmentValue;
    private final TagKey<Block> incorrectBlocksForDrops;
    private final java.util.function.Supplier<Ingredient> repairIngredient;

    ModTiers(
        int level,
        int uses,
        float speed,
        float attackDamageBonus,
        int enchantmentValue,
        TagKey<Block> incorrectBlocksForDrops,
        java.util.function.Supplier<Ingredient> repairIngredient
    ) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantmentValue = enchantmentValue;
        this.incorrectBlocksForDrops = incorrectBlocksForDrops;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return attackDamageBonus;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return incorrectBlocksForDrops;
    }
}
