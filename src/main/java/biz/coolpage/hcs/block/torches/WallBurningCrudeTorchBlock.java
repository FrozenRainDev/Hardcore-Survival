package biz.coolpage.hcs.block.torches;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

@SuppressWarnings("deprecation")
public class WallBurningCrudeTorchBlock extends BurningCrudeTorchBlock {
    public WallBurningCrudeTorchBlock(Properties settings) {
        super(settings);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Blocks.WALL_TORCH.getShape(state, world, pos, context);
    }

    @SuppressWarnings("unused")
    public static VoxelShape getBoundingShape(BlockState state) {
        return WallTorchBlock.getShape(state);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, @NotNull BlockPos pos) {
        return Blocks.WALL_TORCH.canSurvive(state, world, pos);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState torchState = Blocks.WALL_TORCH.getStateForPlacement(ctx);
        if (torchState != null) {
            BlockState state = this.defaultBlockState();
            Direction d = torchState.getValue(FACING);
            return state.setValue(FACING, d);
        }
        return null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        return Blocks.WALL_TORCH.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return Blocks.WALL_TORCH.rotate(state, rotation);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return Blocks.WALL_TORCH.mirror(state, mirror);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(FACING);
    }

    @Override
    public void animateTick(BlockState state, @NotNull Level world, BlockPos pos, RandomSource random) {
        if (world.getGameTime() % 3L == 2L) return;
        Blocks.WALL_TORCH.animateTick(state, world, pos, random);
    }
}