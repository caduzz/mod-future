package net.caduzz.futuremod.item;

import java.util.function.Supplier;

import net.caduzz.futuremod.FutureMod;
import net.caduzz.futuremod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeModeTabs {
  public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FutureMod.MOD_ID);

  public static final Supplier<CreativeModeTab> BISMUTH_ITEMS_TABS = CREATIVE_MODE_TAB.register("bismuth_items_tab", 
    () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.INGOT_BISMUTH.get()))
      .title(Component.translatable("creativetab.futuremod.futuremod_items"))
      .displayItems(
        (itemDisplayParamenters, output) -> {
          output.accept(ModItems.INGOT_BISMUTH);   
          output.accept(ModItems.RAW_BISMUTH);
          output.accept(ModItems.FUTURE_GLOW_BERRIES);
          output.accept(ModItems.FUTURE_SWEET_BERRIES);
        }
      )
      .build()
  );
  
  public static final Supplier<CreativeModeTab> BISMUTH_BLOCKS_TABS = CREATIVE_MODE_TAB.register("bismuth_blocks_tab", 
    () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.BISMUTH_BLOCK.get()))
      .withTabsBefore(ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_items_tab"))
      .title(Component.translatable("creativetab.futuremod.bismuth_blocks"))
      .displayItems(
        (itemDisplayParamenters, output) -> {
          output.accept(ModBlocks.BISMUTH_BLOCK);   
          output.accept(ModBlocks.BISMUTH_ORE);
          output.accept(ModBlocks.BISMUTH_DEEPSLATE_ORE);
          output.accept(ModBlocks.BISMUTH_ANVIL);
          output.accept(ModBlocks.FUTURE_MOSS_BLOCK);
          output.accept(ModBlocks.FUTURE_MOSS_CARPET);
          output.accept(ModBlocks.FUTURE_GRASS_BLOCK);
          output.accept(ModBlocks.FUTURE_GLOW_FLOWER);
          output.accept(ModBlocks.FUTURE_CAVE_VINES);
          output.accept(ModBlocks.FUTURE_CAVE_VINES_LIT);
          output.accept(ModBlocks.VOID_BLACK_BLOCK);
          output.accept(ModBlocks.CHECKERS_BLOCK);
          output.accept(ModBlocks.CHESS_BLOCK);
        }
      )
      .build()
  );

    public static final Supplier<CreativeModeTab> BISMUTH_EQUIPMENTS_TABS = CREATIVE_MODE_TAB.register("bismuth_equipments_tab", 
    () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BISMUTH_HELMET.get()))
      .title(Component.translatable("creativetab.futuremod.futuremod_equipments"))
      .displayItems(
        (itemDisplayParamenters, output) -> {
          output.accept(ModItems.BISMUTH_HELMET);
          output.accept(ModItems.BISMUTH_CHESTPLATE);
          output.accept(ModItems.BISMUTH_LEGGINGS);
          output.accept(ModItems.BISMUTH_BOOTS);
          output.accept(ModItems.JETPACK);
          output.accept(ModItems.BISMUTH_WARDEN_SPAWN_EGG);
          output.accept(ModItems.REGENERATION_RELIC);
          output.accept(ModItems.PURPLE_VOID_RELIC);
          output.accept(ModItems.CIGARETTE);
          output.accept(ModItems.BISMUTH_SWORD);
          output.accept(ModItems.BISMUTH_AXE);
          output.accept(ModItems.BISMUTH_PICKAXE);
          output.accept(ModItems.BISMUTH_HOE);
        }
      )
      .build()
  );
  

  public static void register(IEventBus eventBus) {
    CREATIVE_MODE_TAB.register(eventBus);
  }
}
