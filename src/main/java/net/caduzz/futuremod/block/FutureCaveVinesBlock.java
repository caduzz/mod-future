package net.caduzz.futuremod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Vinha simples pendurada: precisa de suporte acima (bloco solido ou outra
 * vinha).
 */
public class FutureCaveVinesBlock extends Block {
  public FutureCaveVinesBlock(BlockBehaviour.Properties properties) {
    super(properties);
  }

  @Override
  protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
    BlockState aboveState = level.getBlockState(pos.above());
    return isFutureCaveVine(aboveState) || aboveState.isFaceSturdy(level, pos.above(), Direction.DOWN);
  }

  @Override
  protected BlockState updateShape(
      BlockState state,
      Direction direction,
      BlockState neighborState,
      LevelAccessor level,
      BlockPos pos,
      BlockPos neighborPos) {
    if (direction == Direction.UP && !state.canSurvive(level, pos)) {
      return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
    }
    return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
  }

  private static boolean isFutureCaveVine(BlockState state) {
    return state.is(ModBlocks.FUTURE_CAVE_VINES.get()) || state.is(ModBlocks.FUTURE_CAVE_VINES_LIT.get());
  }
}
