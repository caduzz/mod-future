package net.caduzz.futuremod.menu;

import net.caduzz.futuremod.item.ModItems;
import net.caduzz.futuremod.relic.PurpleSlotAttachment;
import net.caduzz.futuremod.relic.RelicSlotAttachment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Dois slots do mod: Regeneração (0) e Purple Void (1), mais inventário do jogador. */
public class RelicSlotMenu extends AbstractContainerMenu {

    private static final int REGEN_SLOT_INDEX = 0;
    private static final int PURPLE_SLOT_INDEX = 1;
    private static final int PLAYER_FIRST_SLOT = 2;
    private static final int PLAYER_LAST_PLUS_ONE = 38;

    /** Cliente: handlers dummy para sincronizar. */
    public RelicSlotMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, new ItemStackHandler(1), new ItemStackHandler(1));
    }

    /** Servidor: anexos reais do jogador. */
    public RelicSlotMenu(int containerId, Inventory playerInv, Player player) {
        this(
                containerId,
                playerInv,
                player.getData(RelicSlotAttachment.RELIC_SLOT.get()),
                player.getData(PurpleSlotAttachment.PURPLE_SLOT.get()));
    }

    private RelicSlotMenu(int containerId, Inventory playerInv, IItemHandler regenHandler, IItemHandler purpleHandler) {
        super(ModMenuTypes.RELIC_SLOT.get(), containerId);

        this.addSlot(new SlotItemHandler(regenHandler, 0, 62, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && stack.is(ModItems.REGENERATION_RELIC.get());
            }
        });
        this.addSlot(new SlotItemHandler(purpleHandler, 0, 98, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && stack.is(ModItems.PURPLE_VOID_RELIC.get());
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 50 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 108));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            stack = stackInSlot.copy();
            if (index == REGEN_SLOT_INDEX || index == PURPLE_SLOT_INDEX) {
                if (!this.moveItemStackTo(stackInSlot, PLAYER_FIRST_SLOT, PLAYER_LAST_PLUS_ONE, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (stackInSlot.is(ModItems.REGENERATION_RELIC.get())) {
                    this.moveItemStackTo(stackInSlot, REGEN_SLOT_INDEX, REGEN_SLOT_INDEX + 1, false);
                }
                if (!stackInSlot.isEmpty() && stackInSlot.is(ModItems.PURPLE_VOID_RELIC.get())) {
                    this.moveItemStackTo(stackInSlot, PURPLE_SLOT_INDEX, PURPLE_SLOT_INDEX + 1, false);
                }
                if (!stackInSlot.isEmpty()) {
                    if (index < 29) {
                        if (!this.moveItemStackTo(stackInSlot, 29, PLAYER_LAST_PLUS_ONE, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(stackInSlot, PLAYER_FIRST_SLOT, 29, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return stack;
    }
}
