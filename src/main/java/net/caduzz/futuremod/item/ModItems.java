package net.caduzz.futuremod.item;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import net.caduzz.futuremod.FutureMod;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.caduzz.futuremod.entity.ModEntities;
import net.caduzz.futuremod.item.CigaretteItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.food.Foods;

import net.minecraft.world.item.component.Unbreakable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FutureMod.MOD_ID);

  private static ItemAttributeModifiers createBismuthChestplateAttributes() {
    return ItemAttributeModifiers.builder()

        .add(Attributes.ARMOR,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_chestplate_armor"),
                17.0,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.CHEST
        )

        .add(Attributes.ARMOR_TOUGHNESS,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_chestplate_toughness"),
                5.0,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.CHEST
        )
        .add(Attributes.ENTITY_INTERACTION_RANGE,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_chestplate_interaction_range"),
                1.0,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.CHEST
        )
        .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_chestplate_explosion_knockback"),
                0.75,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.CHEST
        )
        .add(Attributes.BURNING_TIME,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_chestplate_burning_time"),
                -0.6,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.CHEST
        )

        .add(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_chestplate_attack"),
                7.0,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.CHEST
        )

        .add(Attributes.ATTACK_SPEED,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_chestplate_speed"),
                2.0,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.CHEST
        )

        .add(Attributes.MAX_HEALTH,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_chestplate_health"),
                8.0,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.CHEST
        )
        .add(Attributes.KNOCKBACK_RESISTANCE,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_chestplate_knockback"),
                0.2,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.CHEST
        )

        .build();
  }

  private static ItemAttributeModifiers createBismuthHelmetAttributes() {
    return ItemAttributeModifiers.builder()
        .add(Attributes.ARMOR,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_helmet_armor"),
                3.0,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.HEAD
        )
        .add(Attributes.ARMOR_TOUGHNESS,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_helmet_toughness"),
                1.0,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.HEAD
        )
        .add(Attributes.MAX_HEALTH,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_helmet_health"),
                4.0,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.HEAD
        )
        .add(Attributes.KNOCKBACK_RESISTANCE,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_helmet_knockback"),
                0.1,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.HEAD
        )
        .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_helmet_explosion_knockback"),
                0.2,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.HEAD
        )
        .add(Attributes.BURNING_TIME,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_helmet_burning_time"),
                -0.15,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.HEAD
        )
        .build();
  }

  private static ItemAttributeModifiers createBismuthLeggingsAttributes() {
    return ItemAttributeModifiers.builder()
        .add(Attributes.ARMOR,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_leggings_armor"),
                6.0,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.LEGS
        )
        .add(Attributes.ARMOR_TOUGHNESS,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_leggings_toughness"),
                2.0,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.LEGS
        )
        .add(Attributes.MAX_HEALTH,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_leggings_health"),
                8.0,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.LEGS
        )
        .add(Attributes.KNOCKBACK_RESISTANCE,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_leggings_knockback"),
                0.15,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.LEGS
        )
        .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_leggings_explosion_knockback"),
                0.2,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.LEGS
        )
        .add(Attributes.BURNING_TIME,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_leggings_burning_time"),
                -0.15,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.LEGS
        )
        .build();
  }

  private static ItemAttributeModifiers createBismuthBootsAttributes() {
    return ItemAttributeModifiers.builder()
        .add(Attributes.ARMOR,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_boots_armor"),
                3.0,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.FEET
        )
        .add(Attributes.ARMOR_TOUGHNESS,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_boots_toughness"),
                1.0,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.FEET
        )
        .add(Attributes.MAX_HEALTH,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_boots_health"),
                8.0,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.FEET
        )
        .add(Attributes.MOVEMENT_SPEED,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_boots_speed"),
                0.2,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.FEET
        )
        .add(Attributes.JUMP_STRENGTH,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_boots_jump_strength"),
                0.2,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.FEET
        )
        .add(Attributes.KNOCKBACK_RESISTANCE,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_boots_knockback"),
                0.1,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.FEET
        )
        .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_boots_explosion_knockback"),
                0.2,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.FEET
        )
        .add(Attributes.BURNING_TIME,
            new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "bismuth_boots_burning_time"),
                -0.15,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.FEET
        )
        .build();
  }

  public static final DeferredItem<Item> INGOT_BISMUTH = ITEMS.register("ingot_bismuth",
      () -> new Item(new Item.Properties()));

  public static final DeferredItem<Item> RAW_BISMUTH = ITEMS.register("raw_bismuth",
      () -> new Item(new Item.Properties()));

  public static final DeferredItem<ArmorItem> JUJU_GLASS = ITEMS.register("juju_glass",
      () -> new ArmorItem(
          ModArmorMaterials.GLASSES,
          ArmorItem.Type.HELMET,
          new Item.Properties().stacksTo(1)));

  private static Item.Properties kadouArmorProperties(ItemAttributeModifiers attributes) {
    return new Item.Properties()
        .stacksTo(1)
        .attributes(attributes)
        .rarity(net.caduzz.futuremod.ModEnumParams.RED_RARITY_PROXY.getValue())
        .component(DataComponents.UNBREAKABLE, new Unbreakable(true));
  }

  public static final DeferredItem<ArmorItem> BISMUTH_HELMET = ITEMS.register("bismuth_helmet",
      () -> new BismuthAnvilOnlyArmorItem(
          ModArmorMaterials.BISMUTH,
          ArmorItem.Type.HELMET,
          kadouArmorProperties(createBismuthHelmetAttributes())));

  public static final DeferredItem<ArmorItem> BISMUTH_CHESTPLATE =
      ITEMS.register("bismuth_chestplate",
          () -> new BismuthAnvilOnlyArmorItem(
              ModArmorMaterials.BISMUTH,
              ArmorItem.Type.CHESTPLATE,
              kadouArmorProperties(createBismuthChestplateAttributes())));

  public static final DeferredItem<ArmorItem> BISMUTH_LEGGINGS = ITEMS.register("bismuth_leggings",
      () -> new BismuthAnvilOnlyArmorItem(
          ModArmorMaterials.BISMUTH,
          ArmorItem.Type.LEGGINGS,
          kadouArmorProperties(createBismuthLeggingsAttributes())));

  public static final DeferredItem<ArmorItem> BISMUTH_BOOTS = ITEMS.register("bismuth_boots",
      () -> new BismuthAnvilOnlyArmorItem(
          ModArmorMaterials.BISMUTH,
          ArmorItem.Type.BOOTS,
          kadouArmorProperties(createBismuthBootsAttributes())));

  public static final DeferredItem<ArmorItem> JETPACK = ITEMS.register("jetpack",
      () -> new BismuthAnvilOnlyArmorItem(
          ModArmorMaterials.JETPACK,
          ArmorItem.Type.CHESTPLATE,
          new Item.Properties().stacksTo(1)));

  public static final DeferredItem<Item> BISMUTH_WARDEN_SPAWN_EGG = ITEMS.register("bismuth_warden_spawn_egg",
      () -> new SpawnEggItem(
          ModEntities.BISMUTH_WARDEN.get(),
          0x2d4a3e,
          0x5a7a6e,
          new Item.Properties()));

  /** Relíquia que substitui os efeitos de regeneração/resistência da peitoral de bismuto. Equipa no slot Curios (charm). */
  public static final DeferredItem<Item> REGENERATION_RELIC = ITEMS.register("regeneration_relic",
      () -> new Item(new Item.Properties().stacksTo(1).rarity(net.caduzz.futuremod.ModEnumParams.RED_RARITY_PROXY.getValue())));

  /** Cigarro: fumar teleporta para a Creative Realm (ou de volta ao Overworld). */
  public static final DeferredItem<Item> CIGARETTE = ITEMS.register("cigarette",
      () -> new CigaretteItem(new Item.Properties()));

  /** Berries do proprio mod, para textura e controle independentes. */
  public static final DeferredItem<Item> FUTURE_GLOW_BERRIES = ITEMS.register("future_glow_berries",
      () -> new Item(new Item.Properties().food(Foods.GLOW_BERRIES)));

  public static final DeferredItem<Item> FUTURE_SWEET_BERRIES = ITEMS.register("future_sweet_berries",
      () -> new Item(new Item.Properties().food(Foods.SWEET_BERRIES)));

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