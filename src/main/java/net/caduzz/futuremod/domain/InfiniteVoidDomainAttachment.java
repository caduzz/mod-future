package net.caduzz.futuremod.domain;

import java.util.function.Supplier;

import net.caduzz.futuremod.FutureMod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class InfiniteVoidDomainAttachment {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.ATTACHMENT_TYPES, FutureMod.MOD_ID);

    public static final Supplier<AttachmentType<InfiniteVoidDomainData>> INFINITE_VOID_DOMAIN = ATTACHMENT_TYPES.register(
            "infinite_void_domain",
            () -> AttachmentType.serializable(InfiniteVoidDomainData::new).build());

    private InfiniteVoidDomainAttachment() {
    }
}
