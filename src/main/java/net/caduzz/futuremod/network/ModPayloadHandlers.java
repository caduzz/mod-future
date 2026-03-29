package net.caduzz.futuremod.network;

import net.caduzz.futuremod.client.DomainFreezeClientState;
import net.caduzz.futuremod.client.InfiniteVoidClientState;
import net.caduzz.futuremod.domain.InfiniteVoidDomainManager;
import net.caduzz.futuremod.menu.ModMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.bus.api.IEventBus;
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
        registrar.playToServer(
                ActivateInfiniteVoidDomainPayload.TYPE,
                ActivateInfiniteVoidDomainPayload.STREAM_CODEC,
                ModPayloadHandlers::handleActivateInfiniteVoidOnServer);
        registrar.playToClient(
                SyncInfiniteVoidHudPayload.TYPE,
                SyncInfiniteVoidHudPayload.STREAM_CODEC,
                ModPayloadHandlers::handleSyncInfiniteVoidHudOnClient);
        registrar.playToClient(
                SyncDomainFreezePayload.TYPE,
                SyncDomainFreezePayload.STREAM_CODEC,
                ModPayloadHandlers::handleSyncDomainFreezeOnClient);
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

    private static void handleActivateInfiniteVoidOnServer(ActivateInfiniteVoidDomainPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            InfiniteVoidDomainManager.ActivateResult result = InfiniteVoidDomainManager.tryActivate(serverPlayer);
            var data = InfiniteVoidDomainManager.data(serverPlayer);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                    serverPlayer,
                    new SyncInfiniteVoidHudPayload(data.getActiveTicks(), data.getCooldownTicks()));

            switch (result) {
                case ACTIVATED -> serverPlayer.sendSystemMessage(
                        Component.translatable("message.futuremod.infinite_void_domain.activated"), true);
                case ALREADY_ACTIVE -> serverPlayer.sendSystemMessage(
                        Component.translatable("message.futuremod.infinite_void_domain.already_active"), true);
                case ON_COOLDOWN -> serverPlayer.sendSystemMessage(
                        Component.translatable(
                                "message.futuremod.infinite_void_domain.cooldown",
                                Math.max(1, data.getCooldownTicks() / 20)),
                        true);
            }
        });
    }

    private static void handleSyncInfiniteVoidHudOnClient(SyncInfiniteVoidHudPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> InfiniteVoidClientState.sync(payload.activeTicks(), payload.cooldownTicks()));
    }

    private static void handleSyncDomainFreezeOnClient(SyncDomainFreezePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> DomainFreezeClientState.setFrozen(payload.frozen()));
    }
}
