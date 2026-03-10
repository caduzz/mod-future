package net.caduzz.futuremod.menu;

import net.caduzz.futuremod.FutureMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, FutureMod.MOD_ID);

    /** Factory para o cliente: lê BlockPos do buffer. */
    public static final Supplier<MenuType<BismuthAnvilMenu>> BISMUTH_ANVIL = MENU_TYPES.register("bismuth_anvil",
            () -> IMenuTypeExtension.create(BismuthAnvilMenu::new));

    /** Menu do slot de relíquia (aberto por tecla). Cliente: (id, inv); servidor: (id, inv, player). */
    public static final Supplier<MenuType<RelicSlotMenu>> RELIC_SLOT = MENU_TYPES.register("relic_slot",
            () -> new MenuType<>(RelicSlotMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
