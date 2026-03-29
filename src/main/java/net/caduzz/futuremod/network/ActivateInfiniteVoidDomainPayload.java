package net.caduzz.futuremod.network;

import org.jetbrains.annotations.NotNull;

import net.caduzz.futuremod.FutureMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Requisicao cliente->servidor para ativar infinite_void_domain. */
public record ActivateInfiniteVoidDomainPayload() implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "activate_infinite_void_domain");
    public static final CustomPacketPayload.Type<ActivateInfiniteVoidDomainPayload> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ActivateInfiniteVoidDomainPayload> STREAM_CODEC =
            StreamCodec.unit(new ActivateInfiniteVoidDomainPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
