package net.caduzz.futuremod.client;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import com.mojang.blaze3d.platform.InputConstants;

@EventBusSubscriber(
    modid = "futuremod",
    value = Dist.CLIENT,
    bus = EventBusSubscriber.Bus.MOD
)
public class ModKeyBindings {

    public static final KeyMapping ZOOM_KEY = new KeyMapping(
        "key.futuremod.zoom",
        InputConstants.KEY_Z,
        "key.categories.futuremod"
    );

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
      event.register(ZOOM_KEY);
    }
}
