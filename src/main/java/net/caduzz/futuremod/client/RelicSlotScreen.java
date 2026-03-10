package net.caduzz.futuremod.client;

import net.caduzz.futuremod.FutureMod;
import net.caduzz.futuremod.menu.RelicSlotMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Baú pequeno: um slot de equipar em cima e o inventário em baixo. */
public class RelicSlotScreen extends AbstractContainerScreen<RelicSlotMenu> {

    /** Texture da GUI: coloque sua imagem em assets/futuremod/textures/gui/relic_slot.png (176×132 px). */
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "textures/gui/relic_slot.png");

    public RelicSlotScreen(RelicSlotMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 132;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
