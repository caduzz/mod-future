package net.caduzz.futuremod.network;

import net.caduzz.futuremod.FutureMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** Payload enviado do cliente para o servidor para abrir o menu do slot de relíquia. */
public record OpenRelicMenuPayload() implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "open_relic_menu");
    public static final CustomPacketPayload.Type<OpenRelicMenuPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, OpenRelicMenuPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenRelicMenuPayload());

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}