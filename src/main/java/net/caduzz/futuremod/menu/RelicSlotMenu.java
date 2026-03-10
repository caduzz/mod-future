package net.caduzz.futuremod.menu;

import net.caduzz.futuremod.item.ModItems;
import net.caduzz.futuremod.relic.RelicSlotAttachment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class RelicSlotMenu extends AbstractContainerMenu {

    /** Construtor no cliente: inventário dummy para sincronizar. */
    public RelicSlotMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, new ItemStackHandler(1));
    }

    /** Construtor no servidor: usa o slot de relíquia do jogador. */
    public RelicSlotMenu(int containerId, Inventory playerInv, Player player) {
        this(containerId, playerInv, player.getData(RelicSlotAttachment.RELIC_SLOT.get()));
    }

    private RelicSlotMenu(int containerId, Inventory playerInv, IItemHandler relicHandler) {
        super(ModMenuTypes.RELIC_SLOT.get(), containerId);

        // Slot de equipar (relíquia) em cima, no centro — como um baú de 1 linha
        this.addSlot(new SlotItemHandler(relicHandler, 0, 80, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && stack.is(ModItems.REGENERATION_RELIC.get());
            }
        });

        // Inventário do jogador em baixo (como ao abrir um baú)
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
            if (index == 0) {
                if (!this.moveItemStackTo(stackInSlot, 1, 37, true)) return ItemStack.EMPTY;
            } else {
                if (stackInSlot.is(ModItems.REGENERATION_RELIC.get()) && !this.moveItemStackTo(stackInSlot, 0, 1, false))
                    return ItemStack.EMPTY;
                if (index < 28) {
                    if (!this.moveItemStackTo(stackInSlot, 28, 37, false)) return ItemStack.EMPTY;
                } else if (!this.moveItemStackTo(stackInSlot, 1, 28, false)) return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return stack;
    }
}
