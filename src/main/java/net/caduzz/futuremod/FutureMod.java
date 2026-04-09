package net.caduzz.futuremod;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.caduzz.futuremod.block.ModBlocks;
import net.caduzz.futuremod.block.entity.ModBlockEntities;
import net.caduzz.futuremod.client.DomainFreezeClientState;
import net.caduzz.futuremod.client.InfiniteVoidClientState;
import net.caduzz.futuremod.client.ModKeyBindings;
import net.caduzz.futuremod.client.PurpleVoidClientState;
import net.caduzz.futuremod.client.CheckersBlockRenderer;
import net.caduzz.futuremod.client.ChessBlockRenderer;
import net.caduzz.futuremod.client.ChessBlockbenchModelLoader;
import net.caduzz.futuremod.client.FusionOrbRenderer;
import net.caduzz.futuremod.client.PurpleVoidRenderer;
import net.caduzz.futuremod.command.ModCommands;
import net.caduzz.futuremod.domain.InfiniteVoidDomainAttachment;
import net.caduzz.futuremod.menu.ModMenuTypes;
import net.caduzz.futuremod.network.ActivateInfiniteVoidDomainPayload;
import net.caduzz.futuremod.network.ActivatePurpleVoidPayload;
import net.caduzz.futuremod.network.OpenRelicMenuPayload;
import net.caduzz.futuremod.network.ModPayloadHandlers;
import net.caduzz.futuremod.purplevoid.PurpleVoidAttachment;
import net.caduzz.futuremod.relic.PurpleSlotAttachment;
import net.caduzz.futuremod.relic.RelicSlotAttachment;
import net.caduzz.futuremod.worldgen.ModStructurePieces;
import net.caduzz.futuremod.worldgen.ModStructures;
import net.caduzz.futuremod.entity.BismuthWarden;
import net.caduzz.futuremod.entity.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.entity.WardenRenderer;
import net.caduzz.futuremod.item.ModArmorMaterials;
import net.caduzz.futuremod.item.ModCreativeModeTabs;
import net.caduzz.futuremod.item.ModItems;
import net.caduzz.futuremod.client.RelicSlotScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
import net.neoforged.neoforge.event.RegisterCommandsEvent;
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
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModEntities.register(modEventBus);
        ModArmorMaterials.ARMOR_MATERIALS.register(modEventBus);
        RelicSlotAttachment.ATTACHMENT_TYPES.register(modEventBus);
        PurpleSlotAttachment.ATTACHMENT_TYPES.register(modEventBus);
        InfiniteVoidDomainAttachment.ATTACHMENT_TYPES.register(modEventBus);
        PurpleVoidAttachment.ATTACHMENT_TYPES.register(modEventBus);
        ModStructures.register(modEventBus);
        ModStructurePieces.register(modEventBus);
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

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
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
            InfiniteVoidClientState.tick();
            PurpleVoidClientState.tick();
            Minecraft mc = Minecraft.getInstance();
            if (DomainFreezeClientState.isFrozen() && mc.player != null && mc.player.input != null) {
                mc.player.input.forwardImpulse = 0.0f;
                mc.player.input.leftImpulse = 0.0f;
                mc.player.input.jumping = false;
                mc.player.input.shiftKeyDown = false;
                mc.player.setSprinting(false);
            }
            if (ModKeyBindings.RELIC_SLOT_KEY.consumeClick()) {
                PacketDistributor.sendToServer(new OpenRelicMenuPayload());
            }
            if (ModKeyBindings.INFINITE_VOID_DOMAIN_KEY.consumeClick()) {
                PacketDistributor.sendToServer(new ActivateInfiniteVoidDomainPayload());
            }
            if (ModKeyBindings.PURPLE_VOID_KEY.consumeClick()) {
                PacketDistributor.sendToServer(new ActivatePurpleVoidPayload());
            }
        }
}

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = FutureMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    static class ClientModEvents {
        @SubscribeEvent
        static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                // Blocos com textura alpha precisam de cutout para nao renderizar fundo preto.
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.FUTURE_CAVE_VINES.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.FUTURE_CAVE_VINES_LIT.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.FUTURE_GLOW_FLOWER.get(), RenderType.cutout());

                net.minecraft.client.renderer.item.ItemProperties.register(
                    ModItems.CIGARETTE.get(),
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(FutureMod.MOD_ID, "use_progress"),
                    (stack, level, entity, seed) -> {
                        if (entity == null || !entity.isUsingItem() || !entity.getUseItem().is(ModItems.CIGARETTE.get())) return 0f;
                        int remaining = entity.getUseItemRemainingTicks();
                        int duration = ModItems.CIGARETTE.get().getUseDuration(stack, entity);
                        return duration > 0 ? 1f - (float) remaining / duration : 0f;
                    }
                );
            });
        }

        @SubscribeEvent
        static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.BISMUTH_ANVIL.get(), AnvilScreen::new);
            event.register(ModMenuTypes.RELIC_SLOT.get(), RelicSlotScreen::new);
        }

        @SubscribeEvent
        static void registerChessModelReload(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(ChessBlockbenchModelLoader.reloader());
        }

        @SubscribeEvent
        static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.BISMUTH_WARDEN.get(), WardenRenderer::new);
            event.registerEntityRenderer(ModEntities.BLUE_VOID_ORB.get(), ctx -> new FusionOrbRenderer<>(ctx, 64, 158, 255));
            event.registerEntityRenderer(ModEntities.RED_VOID_ORB.get(), ctx -> new FusionOrbRenderer<>(ctx, 255, 72, 72));
            event.registerEntityRenderer(ModEntities.PURPLE_VOID.get(), PurpleVoidRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.CHECKERS.get(), CheckersBlockRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.CHESS.get(), ChessBlockRenderer::new);
        }

        /** Cor rosa-roxa para musgo / tapete do bioma Magenta. */
        private static final int FUTURE_MOSS_COLOR = 0xf003fc;
        /** Cor roxa para grama do bioma Magenta. */
        private static final int FUTURE_GRASS_COLOR = 0xf558fc;
        /** Ciano claro para musgo / tapete do bioma Azure. */
        private static final int AZURE_MOSS_COLOR = 0x4dd9d9;
        /** Ciano para grama do bioma Azure. */
        private static final int AZURE_GRASS_COLOR = 0x7aebf0;

        @SubscribeEvent
        static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
            event.register((state, level, pos, tintIndex) -> FUTURE_MOSS_COLOR,
                    ModBlocks.FUTURE_MOSS_BLOCK.get(),
                    ModBlocks.FUTURE_MOSS_CARPET.get()
                );
            event.register((state, level, pos, tintIndex) -> FUTURE_GRASS_COLOR,
                    ModBlocks.FUTURE_GRASS_BLOCK.get());
            event.register((state, level, pos, tintIndex) -> AZURE_MOSS_COLOR,
                    ModBlocks.AZURE_MOSS_BLOCK.get(),
                    ModBlocks.AZURE_MOSS_CARPET.get());
            event.register((state, level, pos, tintIndex) -> AZURE_GRASS_COLOR,
                    ModBlocks.AZURE_GRASS_BLOCK.get());
        }

        @SubscribeEvent
        static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
            event.register((stack, tintIndex) -> FUTURE_MOSS_COLOR,
                ModBlocks.FUTURE_MOSS_BLOCK.get(),
                ModBlocks.FUTURE_MOSS_CARPET.get()
            );
            event.register((stack, tintIndex) -> FUTURE_GRASS_COLOR, ModBlocks.FUTURE_GRASS_BLOCK.get().asItem());
            event.register((stack, tintIndex) -> AZURE_MOSS_COLOR,
                ModBlocks.AZURE_MOSS_BLOCK.get(),
                ModBlocks.AZURE_MOSS_CARPET.get());
            event.register((stack, tintIndex) -> AZURE_GRASS_COLOR, ModBlocks.AZURE_GRASS_BLOCK.get().asItem());
        }
    }

    @EventBusSubscriber(modid = FutureMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class KadouTooltipEvents {

        private static final Component KADOU_LORE = Component.literal("Um artefato que perteceu a カドゥ...").withStyle(ChatFormatting.RED);

        private static final Component RELIC_SLOT_LORE = Component.literal("Uma reliquia que perteceu a カドゥ").withStyle(ChatFormatting.RED);

        /** Linha "Slot: [nome]" (Slot: dourado, nome do slot em amarelo). */
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
            if (stack.is(ModItems.PURPLE_VOID_RELIC.get())) {
                addSlotTooltip(event.getToolTip(), "tooltip.futuremod.slot.purple_void");
                if (!event.getToolTip().stream().anyMatch(c -> c.getString().contains("Purple Void"))) {
                    event.getToolTip().add(
                            Component.translatable("tooltip.futuremod.purple_void_relic.desc")
                                    .withStyle(ChatFormatting.LIGHT_PURPLE));
                }
                if (event.getToolTip().stream().map(Component::getString).noneMatch(s -> s.contains("Vazio Roxo"))) {
                    event.getToolTip().add(
                            Component.translatable("tooltip.futuremod.purple_void_relic.unlock")
                                    .withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            }
            if (stack.is(ModItems.REGENERATION_RELIC.get())) {
                if (!event.getToolTip().contains(RELIC_SLOT_LORE)) {
                    event.getToolTip().add(1, RELIC_SLOT_LORE);
                }

                if (event.getToolTip().stream().map(Component::getString).noneMatch(s -> s.contains("Vazio Infinito"))) {
                    event.getToolTip().add(
                            Component.translatable("tooltip.futuremod.relic.unlocks_domain")
                                    .withStyle(ChatFormatting.LIGHT_PURPLE));
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

                ItemStack relicSlotStack = player
                        .getData(RelicSlotAttachment.RELIC_SLOT.get())
                        .getStackInSlot(0);

                boolean hasRelic =
                        !relicSlotStack.isEmpty() && relicSlotStack.is(ModItems.REGENERATION_RELIC.get());

                // Armadura
                boolean hasBismuthChestplate =
                        armorChestplate.is(ModItems.BISMUTH_CHESTPLATE.get());

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
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, bismuthPieces - 1, false, false, true));
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
