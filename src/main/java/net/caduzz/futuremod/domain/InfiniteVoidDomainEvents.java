package net.caduzz.futuremod.domain;

import net.caduzz.futuremod.FutureMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = FutureMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class InfiniteVoidDomainEvents {

    private InfiniteVoidDomainEvents() {
    }

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        InfiniteVoidDomainManager.tickActiveDomains(event.getServer());
    }

    /**
     * LOWEST Pre = snapshot imediatamente antes do tick vanilla (apos outros mods).
     * LOWEST Post = restaura apos o tick (incl. tickOwner), para anular movimento no servidor.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTickPreDomainFreeze(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            InfiniteVoidDomainManager.captureDomainFreezePlayerAnchor(serverPlayer);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTickPostDomainFreeze(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (!InfiniteVoidDomainManager.isForeignDomainVictim(serverPlayer)) {
                InfiniteVoidDomainManager.clearDomainFreezePlayerAnchor(serverPlayer);
                return;
            }
            InfiniteVoidDomainManager.snapDomainFreezePlayerToAnchor(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            InfiniteVoidDomainManager.tickOwner(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            InfiniteVoidDomainManager.cleanupFor(serverPlayer);
        }
    }
}
