package biz.coolpage.hcs.block.torches;

import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.block.HorizontalFacingBlock.FACING;

@SuppressWarnings("deprecation")
public class WallGlowstoneTorchBlock extends GlowstoneTorchBlock{
    public WallGlowstoneTorchBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return Blocks.WALL_TORCH.getOutlineShape(state, world, pos, context);
    }

    @SuppressWarnings("unused")
    public static VoxelShape getBoundingShape(BlockState state) {
        return WallTorchBlock.getBoundingShape(state);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, @NotNull BlockPos pos) {
        return Blocks.WALL_TORCH.canPlaceAt(state, world, pos);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState torchState = Blocks.WALL_TORCH.getPlacementState(ctx);
        if (torchState != null) {
            BlockState state = this.getDefaultState();
            Direction d = torchState.get(FACING);
            return state.with(FACING, d);
        }
        return null;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return Blocks.WALL_TORCH.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return Blocks.WALL_TORCH.rotate(state, rotation);
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return Blocks.WALL_TORCH.mirror(state, mirror);
    }

    @Override
    protected void appendProperties(StateManager.@NotNull Builder<Block, BlockState> stateManager) {
        stateManager.add(FACING);
    }

    @Override
    public void randomDisplayTick(@NotNull BlockState state, @NotNull World world, @NotNull BlockPos pos, @NotNull Random random) {
        Direction direction = state.get(FACING).getOpposite();
        double e = (double)pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.2 + 0.27 * (double)direction.getOffsetX();
        double f = (double)pos.getY() + 0.7 + (random.nextDouble() - 0.5) * 0.2 + 0.22;
        double g = (double)pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.2 + 0.27 * (double)direction.getOffsetZ();
        world.addParticle(this.particle, e, f, g, 0.0, 0.0, 0.0);
    }
}
