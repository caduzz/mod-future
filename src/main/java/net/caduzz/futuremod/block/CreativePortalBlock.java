package net.caduzz.futuremod.block;

import net.caduzz.futuremod.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Bloco interno do portal; entidades que ficam dentro são teleportadas para a dimensão criativa ou de volta.
 */
public class CreativePortalBlock extends Block {

    private static final VoxelShape SHAPE = Shapes.empty();
    private static final int COOLDOWN_TICKS = 80;

    public CreativePortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (player.getPortalCooldown() > 0) return;

        ServerLevel targetLevel = ModDimensions.getOrCreateCreativeRealm(level.getServer());
        if (targetLevel == null) return;

        if (player.level().dimension() == ModDimensions.CREATIVE_REALM_LEVEL) {
            ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
            if (overworld != null) {
                ModDimensions.teleportToDimension(player, overworld);
            }
        } else {
            ModDimensions.teleportToDimension(player, targetLevel);
        }
        player.setPortalCooldown(COOLDOWN_TICKS);
    }
}
