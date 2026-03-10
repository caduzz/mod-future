package net.caduzz.futuremod.network;

import net.caduzz.futuremod.FutureMod;
import net.caduzz.futuremod.menu.ModMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModPayloadHandlers {

    private static final Component RELIC_SLOT_TITLE = Component.translatable("container.futuremod.relic_slot");

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModPayloadHandlers::onRegisterPayloads);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(
                OpenRelicMenuPayload.TYPE,
                OpenRelicMenuPayload.STREAM_CODEC,
                ModPayloadHandlers::handleOpenRelicMenuOnServer);
    }

    private static void handleOpenRelicMenuOnServer(OpenRelicMenuPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (containerId, playerInv, player) -> new net.caduzz.futuremod.menu.RelicSlotMenu(containerId, playerInv, player),
                        RELIC_SLOT_TITLE));
            }
        });
    }
}
