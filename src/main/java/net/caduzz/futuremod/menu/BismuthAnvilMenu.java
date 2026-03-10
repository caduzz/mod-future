package net.caduzz.futuremod.menu;

import net.caduzz.futuremod.block.ModBlocks;
import net.caduzz.futuremod.item.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BismuthAnvilMenu extends AnvilMenu {

    private final ContainerLevelAccess access;

    /** Construtor para o servidor (abre o menu com nível e posição do bloco). */
    public BismuthAnvilMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(containerId, playerInventory, access);
        this.access = access;
        replaceFirstSlot();
    }

    /** Construtor para o cliente (dados extras vêm do buffer). */
    public BismuthAnvilMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, ContainerLevelAccess.create(playerInventory.player.level(), extraData.readBlockPos()));
    }

    /**
     * Aceita armaduras do mod (bismuto, jetpack) e qualquer item que a bigorna vanilla aceitaria.
     */
    private static boolean isValidBismuthAnvilInput(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (isModEnchantableArmor(stack)) return true;
        return stack.isEnchantable() || stack.isRepairable();
    }

    private static boolean isModEnchantableArmor(ItemStack stack) {
        return stack.is(ModItems.BISMUTH_HELMET.get())
                || stack.is(ModItems.BISMUTH_CHESTPLATE.get())
                || stack.is(ModItems.BISMUTH_LEGGINGS.get())
                || stack.is(ModItems.BISMUTH_BOOTS.get())
                || stack.is(ModItems.JETPACK.get());
    }

    private void replaceFirstSlot() {
        if (this.slots.size() > 0 && this.inputSlots != null) {
            Slot customSlot = new Slot(this.inputSlots, 0, 27, 47) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return isValidBismuthAnvilInput(stack);
                }
            };
            this.slots.set(0, customSlot);
        }
    }

    /**
     * Valida em relação à bigorna de bismuto; o AnvilMenu vanilla verifica só a bigorna vanilla,
     * o que fazia o menu fechar na hora.
     */
    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.BISMUTH_ANVIL.get());
    }
}
