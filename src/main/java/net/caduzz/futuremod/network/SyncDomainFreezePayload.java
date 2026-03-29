package net.caduzz.futuremod.network;

import org.jetbrains.annotations.NotNull;

import net.caduzz.futuremod.FutureMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Sincroniza freeze do dominio no cliente (input lock). */
public record SyncDomainFreezePayload(boolean frozen) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "sync_domain_freeze");
    public static final CustomPacketPayload.Type<SyncDomainFreezePayload> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SyncDomainFreezePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeBoolean(payload.frozen()),
            buf -> new SyncDomainFreezePayload(buf.readBoolean()));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
