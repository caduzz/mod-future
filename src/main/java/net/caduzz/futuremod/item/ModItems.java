package net.caduzz.futuremod.item;

import javax.print.attribute.AttributeSet;

import net.caduzz.futuremod.FutureMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FutureMod.MOD_ID);

  public static final DeferredItem<Item> INGOT_BISMUTH = ITEMS.register("ingot_bismuth",
      () -> new Item(new Item.Properties()));

  public static final DeferredItem<Item> RAW_BISMUTH = ITEMS.register("raw_bismuth",
      () -> new Item(new Item.Properties()));

  public static final DeferredItem<ArmorItem> JUJU_GLASS = ITEMS.register("juju_glass",
      () -> new ArmorItem(
          ModArmorMaterials.GLASSES,
          ArmorItem.Type.HELMET,
          new Item.Properties().stacksTo(1)));

  public static final DeferredItem<ArmorItem> BISMUTH_HELMET = ITEMS.register("bismuth_helmet",
      () -> new ArmorItem(
          ModArmorMaterials.BISMUTH,
          ArmorItem.Type.HELMET,
          new Item.Properties().stacksTo(1)));

  public static final DeferredItem<ArmorItem> BISMUTH_CHESTPLATE = ITEMS.register("bismuth_chestplate",
      () -> new ArmorItem(
          ModArmorMaterials.BISMUTH,
          ArmorItem.Type.CHESTPLATE,
          new Item.Properties().stacksTo(1)));

  public static final DeferredItem<ArmorItem> BISMUTH_LEGGINGS = ITEMS.register("bismuth_leggings",
      () -> new ArmorItem(
          ModArmorMaterials.BISMUTH,
          ArmorItem.Type.LEGGINGS,
          new Item.Properties().stacksTo(1)));

  public static final DeferredItem<ArmorItem> BISMUTH_BOOTS = ITEMS.register("bismuth_boots",
      () -> new ArmorItem(
          ModArmorMaterials.BISMUTH,
          ArmorItem.Type.BOOTS,
          new Item.Properties().stacksTo(1)));

  public static final DeferredItem<SwordItem> BISMUTH_SWORD = ITEMS.register("bismuth_sword",
      () -> new SwordItem(
          ModToolMaterials.BISMUTH,
          new Item.Properties()
              .stacksTo(1)
              .attributes(
                  ItemAttributeModifiers.builder()
                      .add(
                          Attributes.ATTACK_DAMAGE,
                          new AttributeModifier(
                              ResourceLocation.fromNamespaceAndPath(
                                  FutureMod.MOD_ID, "bismuth_sword_damage"),
                              20,
                              AttributeModifier.Operation.ADD_VALUE),
                          EquipmentSlotGroup.MAINHAND)
                      .add(
                          Attributes.ATTACK_SPEED,
                          new AttributeModifier(
                              ResourceLocation.fromNamespaceAndPath(
                                  FutureMod.MOD_ID, "bismuth_sword_speed"),
                             4,
                              AttributeModifier.Operation.ADD_VALUE),
                          EquipmentSlotGroup.MAINHAND)
                      .build())));

  public static final DeferredItem<AxeItem> BISMUTH_AXE = ITEMS.register("bismuth_axe",
      () -> new AxeItem(
          ModToolMaterials.BISMUTH,
          new Item.Properties()
              .stacksTo(1)
              .attributes(
                  ItemAttributeModifiers.builder()
                      .add(
                          Attributes.ATTACK_DAMAGE,
                          new AttributeModifier(
                              ResourceLocation.fromNamespaceAndPath(
                                  FutureMod.MOD_ID, "bismuth_axe_damage"),
                              9.0,
                              AttributeModifier.Operation.ADD_VALUE),
                          EquipmentSlotGroup.MAINHAND)
                      .add(
                          Attributes.ATTACK_SPEED,
                          new AttributeModifier(
                              ResourceLocation.fromNamespaceAndPath(
                                  FutureMod.MOD_ID, "bismuth_axe_speed"),
                              -3.1,
                              AttributeModifier.Operation.ADD_VALUE),
                          EquipmentSlotGroup.MAINHAND)
                      .build())));

    public static final DeferredItem<PickaxeItem> BISMUTH_PICKAXE =
      ITEMS.register("bismuth_pickaxe",
          () -> new PickaxeItem(
              ModTiers.BISMUTH,
              new Item.Properties().stacksTo(1)
          )
      );

  public static final DeferredItem<HoeItem> BISMUTH_HOE = ITEMS.register("bismuth_hoe",
      () -> new HoeItem(
          ModToolMaterials.BISMUTH,
          new Item.Properties()
              .stacksTo(1)
              .attributes(
                  ItemAttributeModifiers.builder()
                      .add(
                          Attributes.ATTACK_DAMAGE,
                          new AttributeModifier(
                              ResourceLocation.fromNamespaceAndPath(
                                  FutureMod.MOD_ID, "bismuth_hoe_damage"),
                              1.0,
                              AttributeModifier.Operation.ADD_VALUE),
                          EquipmentSlotGroup.MAINHAND)
                      .add(
                          Attributes.ATTACK_SPEED,
                          new AttributeModifier(
                              ResourceLocation.fromNamespaceAndPath(
                                  FutureMod.MOD_ID, "bismuth_hoe_speed"),
                              -1.0,
                              AttributeModifier.Operation.ADD_VALUE),
                          EquipmentSlotGroup.MAINHAND)
                      .build())));

  public static void register(IEventBus eventBus) {
    ITEMS.register(eventBus);
  }
}