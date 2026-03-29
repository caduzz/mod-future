package net.caduzz.futuremod.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.caduzz.futuremod.network.SyncDomainFreezePayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/** Envia mudancas de freeze apenas quando o estado muda (evita spam de pacotes). */
public final class DomainFreezeSync {

    private static final Map<UUID, Boolean> LAST_SENT = new HashMap<>();

    private DomainFreezeSync() {
    }

    public static void sync(ServerPlayer player, boolean frozen) {
        Boolean prev = LAST_SENT.get(player.getUUID());
        if (prev != null && prev == frozen) {
            return;
        }
        LAST_SENT.put(player.getUUID(), frozen);
        PacketDistributor.sendToPlayer(player, new SyncDomainFreezePayload(frozen));
    }

    public static void clear(ServerPlayer player) {
        LAST_SENT.remove(player.getUUID());
        PacketDistributor.sendToPlayer(player, new SyncDomainFreezePayload(false));
    }
}
