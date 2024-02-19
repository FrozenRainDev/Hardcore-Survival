package biz.coolpage.hcs.block;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.entity.DryingRackBlockEntity;
import biz.coolpage.hcs.util.RotHelper;
import biz.coolpage.hcs.recipe.CustomDryingRackRecipe;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class DryingRackBlock extends BlockWithEntity implements Waterloggable {
    private static final VoxelShape SHAPE_1 = Block.createCuboidShape(1.0F, 0.0F, 7.0F, 15.0F, 24.0F, 9.0F);
    private static final VoxelShape SHAPE_2 = Block.createCuboidShape(7.0F, 0.0F, 1.0F, 9.0F, 24.0F, 15.0F);
    private static final VoxelShape SHAPE_1_16H = Block.createCuboidShape(1.0F, 0.0F, 7.0F, 15.0F, 16.0F, 9.0F);
    private static final VoxelShape SHAPE_2_16H = Block.createCuboidShape(7.0F, 0.0F, 1.0F, 9.0F, 16.0F, 15.0F);

    public DryingRackBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH).with(Properties.WATERLOGGED, false));
    }

    public VoxelShape getVoxShape(@NotNull BlockState state, boolean is16Height) {
        return switch (state.get(Properties.HORIZONTAL_FACING)) {
            case EAST, WEST -> is16Height ? SHAPE_2_16H : SHAPE_2;
            default -> is16Height ? SHAPE_1_16H : SHAPE_1;
        };
    }

    @Override
    protected void appendProperties(StateManager.@NotNull Builder<Block, BlockState> builder) {
        builder.add(Properties.HORIZONTAL_FACING, Properties.WATERLOGGED);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getVoxShape(state, false);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getVoxShape(state, true);
    }

    @Override
    public Item asItem() {
        return Reg.DRYING_RACK_ITEM;
    }

    @Override
    protected Block asBlock() {
        return Reg.DRYING_RACK;
    }

    @Override
    public BlockState getPlacementState(@NotNull ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                .with(Properties.WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
    }


    @Override
    public boolean canPlaceAt(BlockState state, @NotNull WorldView world, @NotNull BlockPos pos) {
        return world.getBlockState(pos.up()).isAir();
    }


    @Override
    public FluidState getFluidState(@NotNull BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }


    @Override
    public BlockState getStateForNeighborUpdate(@NotNull BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(Properties.WATERLOGGED))
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (world1, pos1, state1, blockEntity) -> {
            if (world1.getBlockEntity(pos1) instanceof DryingRackBlockEntity rack) {
                ItemStack stack = rack.getInventoryStack();
                long time = world1.getTime();
                if (rack.getDryingDeadline() >= 0) {
                    if (rack.getDryingDeadline() <= time) {
                        ItemStack stackOut = CustomDryingRackRecipe.getOutput(stack.getItem()).getDefaultStack();
                        RotHelper.createExp(world1, stackOut);
                        rack.setInventoryStack(stackOut);
                    } else if (world1.isRaining() && world1.isSkyVisible(pos1)) {
                        this.precipitationTick(state1, world1, pos1, world1.getBiome(pos1).value().getPrecipitation(pos1));
                    }
                }
            }
            if (world1 instanceof ServerWorld serverWorld) serverWorld.getChunkManager().markForUpdate(pos1);
        };
    }

    @Override
    public void precipitationTick(BlockState state, @NotNull World world, BlockPos pos, @Nullable Biome.Precipitation precipitation) {
        if (world.getBlockEntity(pos) instanceof DryingRackBlockEntity rack) {
            rack.setDryingDeadline(world.getTime() + DryingRackBlockEntity.DRYING_LENGTH);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, @NotNull World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof DryingRackBlockEntity rack) {
            if (rack.onInteract(player)) return ActionResult.SUCCESS;
        }
        return ActionResult.CONSUME;
    }

    @Override
    public void onStateReplaced(BlockState state, @NotNull World world, BlockPos pos, BlockState newState, boolean moved) {
        if (world.isClient() || state.isOf(newState.getBlock())) return;
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof DryingRackBlockEntity) {
            ItemScatterer.spawn(world, pos, ((DryingRackBlockEntity) entity).getInventory());
            world.updateComparators(pos, this);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }


    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }


    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

}
