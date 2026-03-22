package net.caduzz.futuremod.item;

import net.caduzz.futuremod.dimension.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * Cigarro que, ao ser fumado, teleporta o jogador para a Creative Realm (ou de volta ao Overworld).
 */
public class CigaretteItem extends Item {

    private static final int USE_DURATION_TICKS = 40; // ~2 segundos

    public CigaretteItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) {
            return super.finishUsingItem(stack, level, entity);
        }

        ServerLevel targetLevel;
        if (player.level().dimension() == ModDimensions.CREATIVE_REALM_LEVEL) {
            targetLevel = level.getServer().getLevel(Level.OVERWORLD);
        } else {
            targetLevel = ModDimensions.getOrCreateCreativeRealm(level.getServer());
        }

        if (targetLevel != null) {
            ModDimensions.teleportToDimension(player, targetLevel);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return stack.isEmpty() ? ItemStack.EMPTY : stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT; // animação de "levar à boca"
    }
}
