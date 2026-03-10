package net.caduzz.futuremod;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.caduzz.futuremod.block.CreativePortalHelper;
import net.caduzz.futuremod.block.ModBlocks;
import net.caduzz.futuremod.world.SpawnPortalStructure;
import net.caduzz.futuremod.client.ModKeyBindings;
import net.caduzz.futuremod.command.ModCommands;
import net.caduzz.futuremod.menu.ModMenuTypes;
import net.caduzz.futuremod.network.OpenRelicMenuPayload;
import net.caduzz.futuremod.network.ModPayloadHandlers;
import net.caduzz.futuremod.relic.RelicSlotAttachment;
import net.caduzz.futuremod.entity.BismuthWarden;
import net.caduzz.futuremod.entity.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.entity.WardenRenderer;
import net.caduzz.futuremod.item.ModArmorMaterials;
import net.caduzz.futuremod.item.ModCreativeModeTabs;
import net.caduzz.futuremod.integration.CuriosHelper;
import net.caduzz.futuremod.item.ModItems;
import net.caduzz.futuremod.client.RelicSlotScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(FutureMod.MOD_ID)
public class FutureMod {
    public static final String MOD_ID = "futuremod";
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public FutureMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        
        NeoForge.EVENT_BUS.register(this);

        ModCreativeModeTabs.register(modEventBus);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModEntities.register(modEventBus);
        ModArmorMaterials.ARMOR_MATERIALS.register(modEventBus);
        RelicSlotAttachment.ATTACHMENT_TYPES.register(modEventBus);
        ModPayloadHandlers.register(modEventBus);
        
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::onEntityAttributeCreation);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.BISMUTH_WARDEN.get(), BismuthWarden.createAttributes().build());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    /** Quando o overworld carrega, coloca a moldura do portal no spawn (só se ainda não existir). */
    @SubscribeEvent
    public void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) {
            SpawnPortalStructure.tryCreateAtSpawn(level);
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    /** Esqueiro na moldura ou no vão: tenta acender o portal (uma única lógica em CreativePortalHelper). */
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (!event.getItemStack().is(net.minecraft.world.item.Items.FLINT_AND_STEEL)) return;
        LOGGER.info("[FutureMod DEBUG] RightClickBlock: esqueiro em pos {} face {}", event.getPos(), event.getFace());
        boolean ok = CreativePortalHelper.tryLightPortal(event.getLevel(), event.getPos(), event.getFace());
        LOGGER.info("[FutureMod DEBUG] RightClickBlock: tryLightPortal = {}", ok);
        if (!ok) return;
        event.setCanceled(true);
        if (event.getEntity() instanceof ServerPlayer player && !player.getAbilities().instabuild) {
            event.getItemStack().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
    }

    @SubscribeEvent
    public void onUseItemOnBlock(UseItemOnBlockEvent event) {
        var ctx = event.getUseOnContext();
        if (ctx.getLevel().isClientSide()) return;
        if (!ctx.getItemInHand().is(net.minecraft.world.item.Items.FLINT_AND_STEEL)) return;
        LOGGER.info("[FutureMod DEBUG] UseItemOnBlock: esqueiro em pos {} face {}", ctx.getClickedPos(), event.getFace());
        boolean ok = CreativePortalHelper.tryLightPortal(ctx.getLevel(), ctx.getClickedPos(), event.getFace());
        LOGGER.info("[FutureMod DEBUG] UseItemOnBlock: tryLightPortal = {}", ok);
        if (!ok) return;
        event.cancelWithResult(net.minecraft.world.ItemInteractionResult.SUCCESS);
        if (!ctx.getPlayer().getAbilities().instabuild) {
            ctx.getItemInHand().hurtAndBreak(1, ctx.getPlayer(), EquipmentSlot.MAINHAND);
        }
    }

    /** DEBUG: Detecta quando fogo é colocado (para ver se o jogo dispara o evento). */
    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof Level level)) return;
        var state = event.getPlacedBlock();
        if (state.is(net.minecraft.world.level.block.Blocks.FIRE)) {
            var pos = event.getPos();
            boolean nearFrame = false;
            for (var d : net.minecraft.core.Direction.values()) {
                if (level.getBlockState(pos.relative(d)).is(ModBlocks.CREATIVE_PORTAL_FRAME.get())) {
                    nearFrame = true;
                    break;
                }
            }
            LOGGER.info("[FutureMod DEBUG] EntityPlaceEvent: FOGO colocado em {} perto da moldura? {}", pos, nearFrame);
            if (nearFrame) {
                boolean ok = CreativePortalHelper.tryLightPortal(level, pos, null);
                LOGGER.info("[FutureMod DEBUG] EntityPlaceEvent: tryLightPortal = {}", ok);
            }
        }
    }

    /** Remove o portal quando qualquer bloco da moldura ou do portal for quebrado. */
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof Level level)) return;
        var pos = event.getPos();
        var state = event.getState();
        if (state.is(ModBlocks.CREATIVE_PORTAL_FRAME.get()) || state.is(ModBlocks.CREATIVE_PORTAL.get())) {
            CreativePortalHelper.removePortalAt(level, pos);
        }
    }

    /** Reage quando um bloco da moldura recebe atualização de vizinho (ex.: fogo colocado ao lado). */
    @SubscribeEvent
    public void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel().isClientSide()) return;
        var pos = event.getPos();
        var state = event.getState();
        if (!state.is(ModBlocks.CREATIVE_PORTAL_FRAME.get())) return;
        // Verifica se algum vizinho é fogo
        boolean hasFire = false;
        for (var d : net.minecraft.core.Direction.values()) {
            if (event.getLevel().getBlockState(pos.relative(d)).is(net.minecraft.world.level.block.Blocks.FIRE)) {
                hasFire = true;
                break;
            }
        }
        LOGGER.info("[FutureMod DEBUG] NeighborNotify: moldura em {} tem fogo vizinho? {}", pos, hasFire);
        if (!hasFire) return;
        if (!(event.getLevel() instanceof Level level)) return;
        boolean ok = CreativePortalHelper.tryLightPortal(level, pos, null);
        LOGGER.info("[FutureMod DEBUG] NeighborNotify: tryLightPortal = {}", ok);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        event.getEntity().sendSystemMessage(
            Component.literal("Bem-vindo ao Futuro!")
        );
    }

    @EventBusSubscriber(
        modid = "futuremod",
        value = Dist.CLIENT
    )
    public class ClientEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            if (ModKeyBindings.RELIC_SLOT_KEY.consumeClick()) {
                PacketDistributor.sendToServer(new OpenRelicMenuPayload());
            }
        }
}

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = FutureMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    static class ClientModEvents {
        @SubscribeEvent
        static void onClientSetup(FMLClientSetupEvent event) {
        }

        @SubscribeEvent
        static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.BISMUTH_ANVIL.get(), AnvilScreen::new);
            event.register(ModMenuTypes.RELIC_SLOT.get(), RelicSlotScreen::new);
        }

        @SubscribeEvent
        static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.BISMUTH_WARDEN.get(), WardenRenderer::new);
        }
    }

    @EventBusSubscriber(modid = FutureMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class KadouTooltipEvents {
        private static final Component KADOU_LORE = Component.literal("Um artefato que perteceu a カドゥ...").withStyle(ChatFormatting.RED);

        private static final Component RELIC_SLOT_LORE = Component.literal("Uma reliquia que perteceu a カドゥ").withStyle(ChatFormatting.RED);

        /** Linha "Slot: [nome]" no estilo Curios (Slot: em dourado, nome do slot em amarelo). */
        private static void addSlotTooltip(java.util.List<Component> tooltip, String slotKey) {
            Component line = Component.literal("Slot: ").withStyle(ChatFormatting.GOLD)
                    .append(Component.translatable(slotKey).withStyle(ChatFormatting.YELLOW));
            if (!tooltip.stream().anyMatch(c -> c.getString().startsWith("Slot:"))) {
                tooltip.add(1, line);
            }
        }

        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            if (stack.is(ModItems.BISMUTH_HELMET.get()) || stack.is(ModItems.BISMUTH_CHESTPLATE.get())
                    || stack.is(ModItems.BISMUTH_LEGGINGS.get()) || stack.is(ModItems.BISMUTH_BOOTS.get())) {
                if (!event.getToolTip().contains(KADOU_LORE)) {
                    event.getToolTip().add(1, KADOU_LORE);
                }
            }
            if (stack.is(ModItems.REGENERATION_RELIC.get())) {
                if (!event.getToolTip().contains(RELIC_SLOT_LORE)) {
                    event.getToolTip().add(1, RELIC_SLOT_LORE);
                }
                addSlotTooltip(event.getToolTip(), "tooltip.futuremod.slot.relic");
                if (!event.getToolTip().stream().anyMatch(c -> c.getString().contains("When equipped") || c.getString().contains("Equipado"))) {
                    event.getToolTip().add(Component.translatable("tooltip.futuremod.relic.when_equipped").withStyle(ChatFormatting.DARK_AQUA));
                    event.getToolTip().add(Component.translatable("tooltip.futuremod.relic.effect_attack_speed").withStyle(ChatFormatting.BLUE));
                    event.getToolTip().add(Component.translatable("tooltip.futuremod.relic.effect_armor").withStyle(ChatFormatting.BLUE));
                    event.getToolTip().add(Component.translatable("tooltip.futuremod.relic.effect_attack_damage").withStyle(ChatFormatting.BLUE));
                    event.getToolTip().add(Component.translatable("tooltip.futuremod.relic.effect_armor_toughness").withStyle(ChatFormatting.BLUE));
                    event.getToolTip().add(Component.translatable("tooltip.futuremod.relic.effect_knockback_resistance").withStyle(ChatFormatting.BLUE));
                    event.getToolTip().add(Component.translatable("tooltip.futuremod.relic.effect_speed").withStyle(ChatFormatting.BLUE));
                }
            }
            if (stack.is(ModItems.BISMUTH_HELMET.get())) addSlotTooltip(event.getToolTip(), "tooltip.futuremod.slot.helmet");
            if (stack.is(ModItems.BISMUTH_CHESTPLATE.get())) addSlotTooltip(event.getToolTip(), "tooltip.futuremod.slot.chestplate");
            if (stack.is(ModItems.BISMUTH_LEGGINGS.get())) addSlotTooltip(event.getToolTip(), "tooltip.futuremod.slot.leggings");
            if (stack.is(ModItems.BISMUTH_BOOTS.get())) addSlotTooltip(event.getToolTip(), "tooltip.futuremod.slot.boots");
            if (stack.is(ModItems.JETPACK.get())) addSlotTooltip(event.getToolTip(), "tooltip.futuremod.slot.chestplate");
        }
    }

    @EventBusSubscriber(modid = FutureMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public class ModArmorEvents {

        private static final int SATURATION_TICK_INTERVAL = 20;
        private static final float SATURATION_BONUS_PER_TICK = 0.5f;
        private static final float MAX_SATURATION = 20.0f;

        /** Bônus em % do valor atual do jogador (relíquia/peitoral). ADD_MULTIPLIED_TOTAL = percentual sobre o total. */
        private static final double PERCENT_ATTACK_SPEED = 0.20;
        private static final double PERCENT_ARMOR = 0.20;
        private static final double PERCENT_ATTACK_DAMAGE = 0.20;
        private static final double PERCENT_ARMOR_TOUGHNESS = 0.20;
        private static final double PERCENT_KNOCKBACK_RESISTANCE = 0.20;
        private static final double PERCENT_SPEED = 0.40;

        /** Aplica ou remove bônus % por fonte (relic / chestplate). IDs diferentes = os % stackam. */
        private static void applyOrRemovePercentModifiers(Player player, String sourcePrefix, boolean apply) {
            record Entry(Holder<Attribute> attr, String suffix, double pct) {}
            java.util.List<Entry> entries = java.util.List.of(
                    new Entry(Attributes.ATTACK_SPEED, "attack_speed", PERCENT_ATTACK_SPEED),
                    new Entry(Attributes.ARMOR, "armor", PERCENT_ARMOR),
                    new Entry(Attributes.ATTACK_DAMAGE, "attack_damage", PERCENT_ATTACK_DAMAGE),
                    new Entry(Attributes.ARMOR_TOUGHNESS, "armor_toughness", PERCENT_ARMOR_TOUGHNESS),
                    new Entry(Attributes.KNOCKBACK_RESISTANCE, "knockback_resistance", PERCENT_KNOCKBACK_RESISTANCE),
                    new Entry(Attributes.MOVEMENT_SPEED, "speed", PERCENT_SPEED));
            for (Entry e : entries) {
                AttributeInstance inst = player.getAttribute(e.attr);
                if (inst == null) continue;
                String id = sourcePrefix + "_" + e.suffix;
                ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, id);
                AttributeModifier modifier = new AttributeModifier(modId, e.pct, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                if (apply) {
                    if (inst.getModifier(modId) == null) {
                        inst.addTransientModifier(modifier);
                    }
                } else {
                    inst.removeModifier(modId);
                }
            }
        }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack inRelicSlot = player
        .getData(RelicSlotAttachment.RELIC_SLOT.get())
        .getStackInSlot(0);

        boolean hasInCustomSlot = !inRelicSlot.isEmpty() &&
                inRelicSlot.is(ModItems.REGENERATION_RELIC.get());
        if(hasInCustomSlot){
            event.setCanceled(true);
            player.setHealth(7.0F);
            player.removeAllEffects();
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));

            // player.level().broadcastEntityEvent(player, (byte)35);   
        }
    }

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();

            if (!player.level().isClientSide()) {
                ItemStack armorChestplate = player.getItemBySlot(EquipmentSlot.CHEST);
                ItemStack armorHelmet = player.getItemBySlot(EquipmentSlot.HEAD);

                // Relíquia: slot próprio do mod (tecla R) ou Curios, se instalado
                ItemStack inRelicSlot = player.getData(RelicSlotAttachment.RELIC_SLOT.get()).getStackInSlot(0);
                boolean hasRelic = CuriosHelper.findFirstCurio(player, ModItems.REGENERATION_RELIC.get())
                        || (!inRelicSlot.isEmpty() && inRelicSlot.is(ModItems.REGENERATION_RELIC.get()));
                boolean hasBismuthChestplate = armorChestplate.getItem() == ModItems.BISMUTH_CHESTPLATE.get();

                // Bônus % por fonte: relíquia e peitoral têm IDs diferentes, então os % stackam (ex.: +20% + 20% = 1,2 × 1,2)
                applyOrRemovePercentModifiers(player, "relic", hasRelic);
                applyOrRemovePercentModifiers(player, "chestplate", hasBismuthChestplate);

                // Resistência por peça da armadura de bismuto (cada peça dá 1 nível; 4 peças = Resistance IV)
                int bismuthPieces = 0;
                if (armorHelmet.getItem() == ModItems.BISMUTH_HELMET.get()) bismuthPieces = bismuthPieces + 4;
                if (armorChestplate.getItem() == ModItems.BISMUTH_CHESTPLATE.get()) bismuthPieces = bismuthPieces + 4;
                if (player.getItemBySlot(EquipmentSlot.LEGS).getItem() == ModItems.BISMUTH_LEGGINGS.get()) bismuthPieces = bismuthPieces + 4;
                if (player.getItemBySlot(EquipmentSlot.FEET).getItem() == ModItems.BISMUTH_BOOTS.get()) bismuthPieces = bismuthPieces + 4;
                if (bismuthPieces > 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, bismuthPieces - 1, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, bismuthPieces - 1, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 40, bismuthPieces - 1, false, false, true));
                }

                // Capacete bismuto: bônus de saturação (não é atributo de entidade, aplicado via FoodData)
                if (armorHelmet.getItem() == ModItems.BISMUTH_HELMET.get()) {
                    if (player.tickCount % SATURATION_TICK_INTERVAL == 0) {
                        var foodData = player.getFoodData();
                        float current = foodData.getSaturationLevel();
                        foodData.setSaturation(Math.min(MAX_SATURATION, current + SATURATION_BONUS_PER_TICK));
                    }
                }

                if (armorChestplate.getItem() == ModItems.JETPACK.get()) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                } else {
                    if (!player.isCreative() && !player.isSpectator()) {
                        player.getAbilities().mayfly = false;
                        player.getAbilities().flying = false;
                        player.onUpdateAbilities();
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onLivingFall(LivingFallEvent event) {
            if (event.getEntity() instanceof Player player
                    && player.getItemBySlot(EquipmentSlot.FEET).getItem() == ModItems.BISMUTH_BOOTS.get()) {
                event.setCanceled(true);
            }
        }
    }
}
