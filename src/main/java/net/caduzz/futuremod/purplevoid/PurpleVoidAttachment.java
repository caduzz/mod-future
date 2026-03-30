package net.caduzz.futuremod.purplevoid;

import java.util.function.Supplier;

import net.caduzz.futuremod.FutureMod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class PurpleVoidAttachment {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.ATTACHMENT_TYPES, FutureMod.MOD_ID);

    public static final Supplier<AttachmentType<PurpleVoidData>> PURPLE_VOID = ATTACHMENT_TYPES.register(
            "purple_void",
            () -> AttachmentType.serializable(PurpleVoidData::new).build());

    private PurpleVoidAttachment() {
    }
}
