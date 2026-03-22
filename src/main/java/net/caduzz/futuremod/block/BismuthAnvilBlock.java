package net.caduzz.futuremod.block;

import net.caduzz.futuremod.menu.BismuthAnvilMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.minecraft.world.InteractionResult;

public class BismuthAnvilBlock extends AnvilBlock {

    private static final Component MENU_TITLE = Component.translatable("container.futuremod.bismuth_anvil");

    public BismuthAnvilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, playerInventory, player) -> new BismuthAnvilMenu(containerId, playerInventory,
                        ContainerLevelAccess.create(level, pos)),
                MENU_TITLE);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer && !(player instanceof FakePlayer)) {
            serverPlayer.openMenu(
                    state.getMenuProvider(level, pos),
                    buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.CONSUME;
    }
}
