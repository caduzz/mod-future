package net.caduzz.futuremod.network;

import org.jetbrains.annotations.NotNull;

import net.caduzz.futuremod.FutureMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncPurpleVoidHudPayload(int cooldownTicks) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "sync_purple_void_hud");
    public static final CustomPacketPayload.Type<SyncPurpleVoidHudPayload> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SyncPurpleVoidHudPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> buf.writeVarInt(p.cooldownTicks),
            buf -> new SyncPurpleVoidHudPayload(buf.readVarInt()));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
