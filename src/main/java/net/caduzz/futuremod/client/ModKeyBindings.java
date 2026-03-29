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
    /** Abre o menu do slot de relíquia (equipar Relíquia da Regeneração). */
    public static final KeyMapping RELIC_SLOT_KEY = new KeyMapping(
        "key.futuremod.relic_slot",
        InputConstants.KEY_H,
        "key.categories.futuremod"
    );

    /** Ativa o dominio infinite_void_domain. */
    public static final KeyMapping INFINITE_VOID_DOMAIN_KEY = new KeyMapping(
        "key.futuremod.infinite_void_domain",
        InputConstants.KEY_V,
        "key.categories.futuremod"
    );

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
      event.register(RELIC_SLOT_KEY);
      event.register(INFINITE_VOID_DOMAIN_KEY);
    }
}
