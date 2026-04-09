package net.caduzz.futuremod.client;

import net.caduzz.futuremod.FutureMod;
import net.caduzz.futuremod.dimension.ModDimensions;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Aproxima a névoa de distância na Creative Realm (menos “fumaça” longe do jogador).
 */
@EventBusSubscriber(modid = FutureMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class CreativeRealmFogEffect {

    /** &lt; 1 aproxima o fim da névoa (menos “fumaça” no horizonte). */
    private static final float FOG_FAR_SCALE = 0.58f;

    private CreativeRealmFogEffect() {
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        if (!mc.level.dimension().equals(ModDimensions.CREATIVE_REALM_LEVEL)) {
            return;
        }

        event.scaleFarPlaneDistance(FOG_FAR_SCALE);
    }
}
