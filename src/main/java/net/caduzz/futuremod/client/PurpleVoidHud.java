package net.caduzz.futuremod.client;

import com.mojang.blaze3d.systems.RenderSystem;

import net.caduzz.futuremod.FutureMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid = FutureMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class PurpleVoidHud {

    private static final ItemStack COOLDOWN_ICON = new ItemStack(Items.AMETHYST_SHARD);

    private PurpleVoidHud() {
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "purple_void_hud"),
                new LayeredDraw.Layer() {
                    @Override
                    public void render(GuiGraphics gui, DeltaTracker deltaTracker) {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player == null || mc.options.hideGui) {
                            return;
                        }
                        if (!PurpleVoidClientState.isOnCooldown()) {
                            return;
                        }
                        int x = 100;
                        int y = 10;
                        drawIconFrame(gui, x, y, 0xA0401040);
                        gui.renderItem(COOLDOWN_ICON, x + 2, y + 2);
                        gui.drawString(mc.font, "P.VOID CD " + PurpleVoidClientState.cooldownSeconds() + "s", x + 24, y + 7, 0xD090FF, true);
                    }
                });
    }

    private static void drawIconFrame(GuiGraphics gui, int x, int y, int bgColor) {
        RenderSystem.enableBlend();
        gui.fill(x, y, x + 20, y + 20, bgColor);
        gui.fill(x, y, x + 20, y + 1, 0xAAAA66FF);
        gui.fill(x, y + 19, x + 20, y + 20, 0xAA442288);
        gui.fill(x, y, x + 1, y + 20, 0xAAAA66FF);
        gui.fill(x + 19, y, x + 20, y + 20, 0xAA442288);
        RenderSystem.disableBlend();
    }
}
