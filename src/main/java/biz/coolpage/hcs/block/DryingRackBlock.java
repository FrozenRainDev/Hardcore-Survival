package biz.coolpage.hcs.block;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.entity.DryingRackBlockEntity;
import biz.coolpage.hcs.recipe.DryingRackRecipe;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class DryingRackBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    private static final VoxelShape SHAPE_1 = Block.box(1.0, 0.0, 7.0, 15.0, 24.0, 9.0);
    private static final VoxelShape SHAPE_2 = Block.box(7.0, 0.0, 1.0, 9.0, 24.0, 15.0);
    private static final VoxelShape SHAPE_1_16H = Block.box(1.0, 0.0, 7.0, 15.0, 16.0, 9.0);
    private static final VoxelShape SHAPE_2_16H = Block.box(7.0, 0.0, 1.0, 9.0, 16.0, 15.0);

    public DryingRackBlock(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH).setValue(BlockStateProperties.WATERLOGGED, false));
    }

    public VoxelShape getVoxShape(@NotNull BlockState state, boolean is16Height) {
        return switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case EAST, WEST -> is16Height ? SHAPE_2_16H : SHAPE_2;
            default -> is16Height ? SHAPE_1_16H : SHAPE_1;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.WATERLOGGED);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return getVoxShape(state, false);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return getVoxShape(state, true);
    }

    @Override
    public Item asItem() {
        return Hcs.DRYING_RACK_ITEM;
    }

    @Override
    protected Block asBlock() {
        return Hcs.DRYING_RACK;
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(BlockStateProperties.WATERLOGGED, ctx.getLevel().getFluidState(ctx.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    public boolean canSurvive(BlockState state, @NotNull LevelReader world, @NotNull BlockPos pos) {
        return world.getBlockState(pos.above()).isAir();
    }

    @Override
    public FluidState getFluidState(@NotNull BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(@NotNull BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED))
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return (world1, pos1, state1, blockEntity) -> {
            if (world1.getBlockEntity(pos1) instanceof DryingRackBlockEntity rack) {
                ItemStack stack = rack.getInventoryStack();
                long time = world1.getGameTime();
                if (rack.getDryingDeadline() >= 0) {
                    if (rack.getDryingDeadline() <= time) {
                        ItemStack stackOut = DryingRackRecipe.getOutput(stack.getItem()).getDefaultInstance();
                        RotHelper.createExp(world1, stackOut);
                        rack.setInventoryStack(stackOut);
                    } else if (world1.isRaining() && world1.canSeeSky(pos1)) {
                        this.handlePrecipitation(state1, world1, pos1, world1.getBiome(pos1).value().getPrecipitationAt(pos1));
                    }
                }
            }
            if (world1 instanceof ServerLevel serverWorld) serverWorld.getChunkSource().blockChanged(pos1);
        };
    }

    @Override
    public void handlePrecipitation(BlockState state, @NotNull Level world, BlockPos pos, @Nullable Biome.Precipitation precipitation) {
        if (world.getBlockEntity(pos) instanceof DryingRackBlockEntity rack) {
            rack.setDryingDeadline(world.getGameTime() + DryingRackBlockEntity.DRYING_LENGTH);
        }
    }

    @Override
    public InteractionResult use(BlockState state, @NotNull Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof DryingRackBlockEntity rack) {
            if (rack.onInteract(player)) return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (world.isClientSide() || state.is(newState.getBlock())) return;
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof DryingRackBlockEntity) {
            Containers.dropContents(world, pos, ((DryingRackBlockEntity) entity).getInventory());
            world.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
        return false;
    }
}