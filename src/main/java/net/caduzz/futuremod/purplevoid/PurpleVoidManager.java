package net.caduzz.futuremod.purplevoid;

import net.caduzz.futuremod.Config;
import net.caduzz.futuremod.entity.ModEntities;
import net.caduzz.futuremod.entity.PurpleVoidOrbEntity;
import net.caduzz.futuremod.item.ModItems;
import net.caduzz.futuremod.network.SyncPurpleVoidHudPayload;
import net.caduzz.futuremod.relic.PurpleSlotAttachment;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PurpleVoidManager {

    public enum ActivateResult {
        ACTIVATED,
        ON_COOLDOWN,
        REQUIRES_PURPLE_RELIC
    }

    private PurpleVoidManager() {
    }

    public static PurpleVoidData data(ServerPlayer player) {
        return player.getData(PurpleVoidAttachment.PURPLE_VOID.get());
    }

    public static boolean hasPurpleRelicEquipped(Player player) {
        ItemStack stack = player.getData(PurpleSlotAttachment.PURPLE_SLOT.get()).getStackInSlot(0);
        return !stack.isEmpty() && stack.is(ModItems.PURPLE_VOID_RELIC.get());
    }

    public static void syncHud(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SyncPurpleVoidHudPayload(data(player).getCooldownTicks()));
    }

    public static ActivateResult tryActivate(ServerPlayer player) {
        PurpleVoidData voidData = data(player);
        if (!voidData.canActivate()) {
            return ActivateResult.ON_COOLDOWN;
        }
        if (!hasPurpleRelicEquipped(player)) {
            return ActivateResult.REQUIRES_PURPLE_RELIC;
        }
        ServerLevel level = player.serverLevel();
        PurpleVoidOrbEntity orb = new PurpleVoidOrbEntity(ModEntities.PURPLE_VOID_ORB.get(), level, player);
        level.addFreshEntity(orb);

        voidData.setCooldownTicks(Config.PURPLE_VOID_COOLDOWN_SECONDS.get() * 20);
        return ActivateResult.ACTIVATED;
    }

    public static void tickPlayer(ServerPlayer player) {
        PurpleVoidData voidData = data(player);
        int before = voidData.getCooldownTicks();
        voidData.tick();
        if (before > 0 && voidData.getCooldownTicks() == 0) {
            syncHud(player);
        }
    }
}
