package net.caduzz.futuremod.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

/**
 * Armadura que só pode ser encantada/reparada na bigorna de bismuto do mod.
 * A bigorna e a mesa de encantamentos vanilla não aceitam estes itens.
 */
public class BismuthAnvilOnlyArmorItem extends ArmorItem {

    public BismuthAnvilOnlyArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }
}
