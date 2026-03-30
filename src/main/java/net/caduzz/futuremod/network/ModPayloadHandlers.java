package net.caduzz.futuremod.network;

import net.caduzz.futuremod.client.DomainFreezeClientState;
import net.caduzz.futuremod.client.InfiniteVoidClientState;
import net.caduzz.futuremod.client.PurpleVoidClientState;
import net.caduzz.futuremod.domain.InfiniteVoidDomainManager;
import net.caduzz.futuremod.purplevoid.PurpleVoidManager;
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
        registrar.playToServer(
                ActivatePurpleVoidPayload.TYPE,
                ActivatePurpleVoidPayload.STREAM_CODEC,
                ModPayloadHandlers::handleActivatePurpleVoidOnServer);
        registrar.playToClient(
                SyncInfiniteVoidHudPayload.TYPE,
                SyncInfiniteVoidHudPayload.STREAM_CODEC,
                ModPayloadHandlers::handleSyncInfiniteVoidHudOnClient);
        registrar.playToClient(
                SyncDomainFreezePayload.TYPE,
                SyncDomainFreezePayload.STREAM_CODEC,
                ModPayloadHandlers::handleSyncDomainFreezeOnClient);
        registrar.playToClient(
                SyncPurpleVoidHudPayload.TYPE,
                SyncPurpleVoidHudPayload.STREAM_CODEC,
                ModPayloadHandlers::handleSyncPurpleVoidHudOnClient);
    }

    private static void handleActivatePurpleVoidOnServer(ActivatePurpleVoidPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            PurpleVoidManager.ActivateResult result = PurpleVoidManager.tryActivate(serverPlayer);
            PurpleVoidManager.syncHud(serverPlayer);
            switch (result) {
                case ACTIVATED -> serverPlayer.sendSystemMessage(
                        Component.translatable("message.futuremod.purple_void.activated"), true);
                case ON_COOLDOWN -> {
                    int t = PurpleVoidManager.data(serverPlayer).getCooldownTicks();
                    int sec = Math.max(1, (t + 19) / 20);
                    serverPlayer.sendSystemMessage(
                            Component.translatable("message.futuremod.purple_void.cooldown", sec), true);
                }
                case REQUIRES_PURPLE_RELIC -> serverPlayer.sendSystemMessage(
                        Component.translatable("message.futuremod.purple_void.requires_relic"), true);
            }
        });
    }

    private static void handleSyncPurpleVoidHudOnClient(SyncPurpleVoidHudPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> PurpleVoidClientState.sync(payload.cooldownTicks()));
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

            var data = InfiniteVoidDomainManager.data(serverPlayer);
            if (data.isActive()) {
                InfiniteVoidDomainManager.CancelResult cancel = InfiniteVoidDomainManager.tryCancelEarly(serverPlayer);
                data = InfiniteVoidDomainManager.data(serverPlayer);
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                        serverPlayer,
                        new SyncInfiniteVoidHudPayload(data.getActiveTicks(), data.getCooldownTicks()));
                if (cancel == InfiniteVoidDomainManager.CancelResult.CANCELLED) {
                    serverPlayer.sendSystemMessage(
                            Component.translatable("message.futuremod.infinite_void_domain.cancelled"), true);
                }
                return;
            }

            InfiniteVoidDomainManager.ActivateResult result = InfiniteVoidDomainManager.tryActivate(serverPlayer);
            data = InfiniteVoidDomainManager.data(serverPlayer);
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
                case REQUIRES_REGENERATION_RELIC -> serverPlayer.sendSystemMessage(
                        Component.translatable("message.futuremod.infinite_void_domain.requires_relic"), true);
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
