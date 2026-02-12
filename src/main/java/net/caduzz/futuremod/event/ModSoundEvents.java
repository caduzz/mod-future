package net.caduzz.futuremod.event;

import net.caduzz.futuremod.FutureMod;
import net.caduzz.futuremod.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

@EventBusSubscriber(modid = FutureMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ModSoundEvents {

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getItemBySlot(EquipmentSlot.FEET).getItem() == ModItems.BISMUTH_BOOTS.get()) {
                Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.SLIME_JUMP, 0.8f, 0.5f)
                );
            }
        }
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player == null || event.getOriginalSound() == null) return;

        String path = event.getOriginalSound().getLocation().getPath();

        boolean isEquip = path.contains("armor.equip");

        if (isEquip) {
            boolean wearingBismuth = false;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.isArmor() && player.getItemBySlot(slot).getItem() instanceof ArmorItem armor) {
                    if (player.getItemBySlot(slot).is(ModItems.INGOT_BISMUTH.get()) || 
                        player.getItemBySlot(slot).getItem().toString().contains("bismuth")) {
                        wearingBismuth = true;
                        break;
                    }
                }
            }

            if (wearingBismuth) {
                event.setSound(new SimpleSoundInstance(
                    SoundEvents.AMETHYST_CLUSTER_STEP.getLocation(),
                    SoundSource.PLAYERS,
                    1.0f, 1.1f,
                    player.getRandom(), false, 0,
                    net.minecraft.client.resources.sounds.SoundInstance.Attenuation.LINEAR,
                    player.getX(), player.getY(), player.getZ(), false
                ));
            }
        }
    }
}