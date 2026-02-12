package net.caduzz.futuremod;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.caduzz.futuremod.block.ModBlocks;
import net.caduzz.futuremod.client.ModKeyBindings;
import net.caduzz.futuremod.item.ModArmorMaterials;
import net.caduzz.futuremod.item.ModCreativeModeTabs;
import net.caduzz.futuremod.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
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
        ModArmorMaterials.ARMOR_MATERIALS.register(modEventBus);
        
        modEventBus.addListener(this::addCreative);
        
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

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

        private static final int ZOOM_FOV = 30;
        private static final float SMOOTH_SPEED = 0.15f;

        private static int originalFov = -1;
        private static float currentFov;

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options == null) return;

            if (originalFov < 0) {
                originalFov = mc.options.fov().get();
                currentFov = originalFov;
            }

            int targetFov = ModKeyBindings.ZOOM_KEY.isDown()
                    ? ZOOM_FOV
                    : originalFov;

            currentFov += (targetFov - currentFov) * SMOOTH_SPEED;

            mc.options.fov().set(Math.round(currentFov));
        }
}


    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = FutureMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    static class ClientModEvents {
        @SubscribeEvent
        static void onClientSetup(FMLClientSetupEvent event) {
            
        }
    }

    @EventBusSubscriber(modid = FutureMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public class ModArmorEvents {

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();

            if (!player.level().isClientSide()) {
                
                ItemStack armorHelmet = player.getItemBySlot(EquipmentSlot.HEAD);
                ItemStack armorBoot = player.getItemBySlot(EquipmentSlot.FEET);
                // ItemStack armorChesplate = player.getItemBySlot(EquipmentSlot.CHEST);

                if (armorBoot.getItem() == ModItems.BISMUTH_BOOTS.get()) {
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 20, 1, false, false, false));
                }

                if (armorHelmet.getItem() == ModItems.JUJU_GLASS.get() || armorHelmet.getItem() == ModItems.BISMUTH_HELMET.get()) {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 1, false, false, false));
                }

                // if (armorChesplate.getItem() == ModItems.BISMUTH_CHESTPLATE.get()) {
                //     player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20, 1, false, false, false));
                // }
            }
        }
    }
}
