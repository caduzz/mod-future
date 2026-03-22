package net.caduzz.futuremod.client;

import com.mojang.blaze3d.systems.RenderSystem;

import net.caduzz.futuremod.FutureMod;
import net.caduzz.futuremod.dimension.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * Overlay de tela inteira na Creative Realm.
 * Filtro rosa pastel muito suave – sem distorção, sem efeito psicodélico.
 */
@EventBusSubscriber(modid = FutureMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CreativeRealmOverlay {

    /** Rosa pastel muito suave, ~8% opacidade – apenas coloração leve. */
    private static final int TINT_COLOR = (0x14 << 24) | 0xFFE4EC;

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
            ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "creative_realm_tint"),
            new LayeredDraw.Layer() {
                @Override
                public void render(GuiGraphics gui, DeltaTracker delta) {
                    var mc = Minecraft.getInstance();
                    if (mc.player == null || mc.level == null) return;
                    if (mc.level.dimension() != ModDimensions.CREATIVE_REALM_LEVEL) return;
                    if (mc.options.hideGui) return;

                    int w = mc.getWindow().getGuiScaledWidth();
                    int h = mc.getWindow().getGuiScaledHeight();

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.disableDepthTest();

                    gui.fill(0, 0, w, h, TINT_COLOR);

                    RenderSystem.enableDepthTest();
                    RenderSystem.disableBlend();
                }
            }
        );
    }
}
