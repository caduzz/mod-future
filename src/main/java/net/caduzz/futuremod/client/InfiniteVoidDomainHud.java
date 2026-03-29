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

/** HUD com icone de habilidade ativa e icone de cooldown. */
@EventBusSubscriber(modid = FutureMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class InfiniteVoidDomainHud {

    private static final ItemStack ACTIVE_ICON = new ItemStack(Items.ENDER_EYE);
    private static final ItemStack COOLDOWN_ICON = new ItemStack(Items.CLOCK);

    private InfiniteVoidDomainHud() {
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "infinite_void_domain_hud"),
                new LayeredDraw.Layer() {
                    @Override
                    public void render(GuiGraphics gui, DeltaTracker deltaTracker) {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player == null || mc.options.hideGui) return;
                        if (!InfiniteVoidClientState.isActive() && !InfiniteVoidClientState.isOnCooldown()) return;

                        int x = 10;
                        int y = 10;

                        if (InfiniteVoidClientState.isActive()) {
                            drawIconFrame(gui, x, y, 0xA0000018);
                            gui.renderItem(ACTIVE_ICON, x + 2, y + 2);
                            gui.drawString(mc.font, "VOID " + InfiniteVoidClientState.activeSeconds() + "s", x + 24, y + 7, 0x9CC5FF, true);
                            y += 24;
                        }

                        if (InfiniteVoidClientState.isOnCooldown()) {
                            drawIconFrame(gui, x, y, 0x90000000);
                            gui.renderItem(COOLDOWN_ICON, x + 2, y + 2);
                            gui.drawString(mc.font, "CD " + InfiniteVoidClientState.cooldownSeconds() + "s", x + 24, y + 7, 0xB0B0B0, true);
                        }
                    }
                });
    }

    private static void drawIconFrame(GuiGraphics gui, int x, int y, int bgColor) {
        RenderSystem.enableBlend();
        gui.fill(x, y, x + 20, y + 20, bgColor);
        gui.fill(x, y, x + 20, y + 1, 0xAA6AA2FF);
        gui.fill(x, y + 19, x + 20, y + 20, 0xAA223566);
        gui.fill(x, y, x + 1, y + 20, 0xAA6AA2FF);
        gui.fill(x + 19, y, x + 20, y + 20, 0xAA223566);
        RenderSystem.disableBlend();
    }
}
