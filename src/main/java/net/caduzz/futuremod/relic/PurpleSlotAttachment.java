package net.caduzz.futuremod.relic;

import java.util.function.Supplier;

import net.caduzz.futuremod.FutureMod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Slot dedicado à Purple Void Relic (tecla H). Persiste no jogador. */
public final class PurpleSlotAttachment {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.ATTACHMENT_TYPES, FutureMod.MOD_ID);

    public static final Supplier<AttachmentType<ItemStackHandler>> PURPLE_SLOT = ATTACHMENT_TYPES.register(
            "purple_slot",
            () -> AttachmentType.serializable(() -> new ItemStackHandler(1)).build());

    private PurpleSlotAttachment() {
    }
}
