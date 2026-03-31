package net.caduzz.futuremod.block;

import java.util.function.Supplier;

import net.caduzz.futuremod.FutureMod;
import net.caduzz.futuremod.item.ModItems;
import net.caduzz.futuremod.block.BismuthAnvilBlock;
import net.caduzz.futuremod.block.VoidBlackBlock;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FutureMod.MOD_ID);

  public static final DeferredBlock<Block> BISMUTH_BLOCK = registerBlock("bismuth_block",
      () -> new Block(
          BlockBehaviour.Properties.of().strength(4f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));

  public static final DeferredBlock<Block> BISMUTH_ORE = registerBlock("bismuth_ore",
      () -> new DropExperienceBlock(
          UniformInt.of(2, 4),
          BlockBehaviour.Properties.of()
              .strength(3f)
              .requiresCorrectToolForDrops()
              .sound(SoundType.AMETHYST_CLUSTER)));

  public static final DeferredBlock<Block> BISMUTH_DEEPSLATE_ORE = registerBlock("bismuth_deepslate_ore",
      () -> new DropExperienceBlock(
          UniformInt.of(3, 4),
          BlockBehaviour.Properties.of()
              .strength(3f)
              .requiresCorrectToolForDrops()
              .sound(SoundType.AMETHYST_CLUSTER)));

  public static final DeferredBlock<Block> BISMUTH_ANVIL = registerBlock("bismuth_anvil",
      () -> new BismuthAnvilBlock(
          BlockBehaviour.Properties.of()
              .strength(5.0F, 1200.0F)
              .requiresCorrectToolForDrops()
              .sound(SoundType.ANVIL)));

  /** Musgo roxo do FutureMod - mesma textura do moss block vanilla, cor roxa. */
  public static final DeferredBlock<Block> FUTURE_MOSS_BLOCK = registerBlock("future_moss_block",
      () -> new Block(
          BlockBehaviour.Properties.of()
              .mapColor(MapColor.COLOR_PURPLE)
              .strength(0.1F)
              .sound(SoundType.MOSS)));

  /** Tapete de musgo roxo do FutureMod. */
  public static final DeferredBlock<Block> FUTURE_MOSS_CARPET = registerBlock("future_moss_carpet",
      () -> new CarpetBlock(
          BlockBehaviour.Properties.of()
              .mapColor(MapColor.COLOR_PURPLE)
              .strength(0.1F)
              .sound(SoundType.MOSS)));

  /** Bloco de grama roxa do FutureMod. */
  public static final DeferredBlock<Block> FUTURE_GRASS_BLOCK = registerBlock("future_grass_block",
      () -> new Block(
          BlockBehaviour.Properties.of()
              .mapColor(MapColor.COLOR_PURPLE)
              .strength(0.6F)
              .sound(SoundType.GRASS)));

  /** Flor roxa da Creative Realm com brilho suave. */
  public static final DeferredBlock<Block> FUTURE_GLOW_FLOWER = registerBlock("future_glow_flower",
      () -> new Block(
          BlockBehaviour.Properties.of()
              .mapColor(MapColor.COLOR_PURPLE)
              .noCollission()
              .instabreak()
              .sound(SoundType.GRASS)
              .lightLevel(state -> 10)));

  /** Vines customizadas da Creative Realm para geracao no teto. */
  public static final DeferredBlock<Block> FUTURE_CAVE_VINES = registerBlock("future_cave_vines",
      () -> new FutureCaveVinesBlock(
          BlockBehaviour.Properties.of()
              .mapColor(MapColor.COLOR_PURPLE)
              .noCollission()
              .instabreak()
              .sound(SoundType.CAVE_VINES)));

  /** Variante luminosa das vines da Creative Realm. */
  public static final DeferredBlock<Block> FUTURE_CAVE_VINES_LIT = registerBlock("future_cave_vines_lit",
      () -> new FutureCaveVinesBlock(
          BlockBehaviour.Properties.of()
              .mapColor(MapColor.COLOR_PURPLE)
              .noCollission()
              .instabreak()
              .sound(SoundType.CAVE_VINES)
              .lightLevel(state -> 12)));

  /** Bloco de vazio absoluto: textura preta e sem resposta visual a luz. */
  public static final DeferredBlock<Block> VOID_BLACK_BLOCK = registerBlock("void_black_block",
      () -> new VoidBlackBlock(
          BlockBehaviour.Properties.of()
              .mapColor(MapColor.COLOR_BLACK)
              .strength(-1.0F, 3600000.0F)
              .requiresCorrectToolForDrops()
              .sound(SoundType.BASALT)));

  // ============ Blocos do Bioma Azul Bebê ============

  /** Musgo azul bebê do bioma etéreo. */
  public static final DeferredBlock<Block> AZURE_MOSS_BLOCK = registerBlock("azure_moss_block",
      () -> new Block(
          BlockBehaviour.Properties.of()
              .mapColor(MapColor.COLOR_BLUE)
              .strength(0.1F)
              .sound(SoundType.MOSS)));

  /** Tapete de musgo azul bebê. */
  public static final DeferredBlock<Block> AZURE_MOSS_CARPET = registerBlock("azure_moss_carpet",
      () -> new CarpetBlock(
          BlockBehaviour.Properties.of()
              .mapColor(MapColor.COLOR_BLUE)
              .strength(0.1F)
              .sound(SoundType.MOSS)));

  /** Bloco de grama azul bebê. */
  public static final DeferredBlock<Block> AZURE_GRASS_BLOCK = registerBlock("azure_grass_block",
      () -> new Block(
          BlockBehaviour.Properties.of()
              .mapColor(MapColor.COLOR_CYAN)
              .strength(0.6F)
              .sound(SoundType.GRASS)));

  /** Flor azul bebê com brilho suave etéreo. */
  public static final DeferredBlock<Block> AZURE_GLOW_FLOWER = registerBlock("azure_glow_flower",
      () -> new Block(
          BlockBehaviour.Properties.of()
              .mapColor(MapColor.COLOR_BLUE)
              .noCollission()
              .instabreak()
              .sound(SoundType.GRASS)
              .lightLevel(state -> 4)));

  /** Vinhas azuis da Creative Realm para geração no teto. */
  public static final DeferredBlock<Block> AZURE_CAVE_VINES = registerBlock("azure_cave_vines",
      () -> new FutureCaveVinesBlock(
          BlockBehaviour.Properties.of()
              .mapColor(MapColor.COLOR_CYAN)
              .noCollission()
              .instabreak()
              .sound(SoundType.CAVE_VINES)));

  /** Variante luminosa das vinhas azuis. */
  public static final DeferredBlock<Block> AZURE_CAVE_VINES_LIT = registerBlock("azure_cave_vines_lit",
      () -> new FutureCaveVinesBlock(
          BlockBehaviour.Properties.of()
              .mapColor(MapColor.COLOR_CYAN)
              .noCollission()
              .instabreak()
              .sound(SoundType.CAVE_VINES)
              .lightLevel(state -> 10)));

  /** Bloco de solo azul bebê - versão sólida para camadas profundas. */
  public static final DeferredBlock<Block> AZURE_SOIL_BLOCK = registerBlock("azure_soil_block",
      () -> new Block(
          BlockBehaviour.Properties.of()
              .mapColor(MapColor.COLOR_LIGHT_BLUE)
              .strength(0.5F)
              .sound(SoundType.ROOTED_DIRT)));

  private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
    DeferredBlock<T> toReturn = BLOCKS.register(name, block);
    registerBlockItem(name, toReturn);
    return toReturn;
  }

  private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
    ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
  }

  public static void register(IEventBus eventBus) {
    BLOCKS.register(eventBus);
  }
}
