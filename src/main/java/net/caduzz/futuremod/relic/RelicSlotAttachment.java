package net.caduzz.futuremod.relic;

import net.caduzz.futuremod.FutureMod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.function.Supplier;

/** Slot dedicado à relíquia no jogador (funciona sem Curios). */
public final class RelicSlotAttachment {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, FutureMod.MOD_ID);

    /** Um slot de item no jogador só para a Relíquia da Regeneração. Persiste e copia na morte. */
    public static final Supplier<AttachmentType<ItemStackHandler>> RELIC_SLOT = ATTACHMENT_TYPES.register(
            "relic_slot",
            () -> AttachmentType.serializable(() -> new ItemStackHandler(1)).build());
}
