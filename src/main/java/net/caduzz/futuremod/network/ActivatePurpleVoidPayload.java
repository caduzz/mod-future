package net.caduzz.futuremod.network;

import org.jetbrains.annotations.NotNull;

import net.caduzz.futuremod.FutureMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ActivatePurpleVoidPayload() implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "activate_purple_void");
    public static final CustomPacketPayload.Type<ActivatePurpleVoidPayload> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ActivatePurpleVoidPayload> STREAM_CODEC =
            StreamCodec.unit(new ActivatePurpleVoidPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
