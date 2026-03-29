package net.caduzz.futuremod.network;

import org.jetbrains.annotations.NotNull;

import net.caduzz.futuremod.FutureMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Atualiza o HUD local com estado atual de ativo/cooldown do dominio. */
public record SyncInfiniteVoidHudPayload(int activeTicks, int cooldownTicks) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "sync_infinite_void_hud");
    public static final CustomPacketPayload.Type<SyncInfiniteVoidHudPayload> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SyncInfiniteVoidHudPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.activeTicks());
                buf.writeVarInt(payload.cooldownTicks());
            },
            buf -> new SyncInfiniteVoidHudPayload(buf.readVarInt(), buf.readVarInt()));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
