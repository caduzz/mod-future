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
    /** Abre o menu dos slots de relíquia do mod (Regeneração + Purple Void). */
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

    /** Dispara a habilidade Purple Void (com relíquia no slot do menu de relíquias). */
    public static final KeyMapping PURPLE_VOID_KEY = new KeyMapping(
        "key.futuremod.purple_void",
        InputConstants.KEY_J,
        "key.categories.futuremod"
    );

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
      event.register(RELIC_SLOT_KEY);
      event.register(INFINITE_VOID_DOMAIN_KEY);
      event.register(PURPLE_VOID_KEY);
    }
}
