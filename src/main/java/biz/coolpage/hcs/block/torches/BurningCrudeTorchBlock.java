package biz.coolpage.hcs.block.torches;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.entity.BurningCrudeTorchBlockEntity;
import biz.coolpage.hcs.item.BurningCrudeTorchItem;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
public class BurningCrudeTorchBlock extends BlockWithEntity {
    public BurningCrudeTorchBlock(Settings settings) {
        super(settings);
    }

    @Override
    public Item asItem() {
        return Reg.BURNING_CRUDE_TORCH_ITEM;
    }

    @Override
    protected Block asBlock() {
        return Reg.BURNING_CRUDE_TORCH_BLOCK;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL; // INVISIBLE WITHOUT THIS
    }

    @Override
    public void randomDisplayTick(BlockState state, @NotNull World world, BlockPos pos, Random random) {
        if (world.getTime() % 3L == 2L) return;
        Blocks.TORCH.randomDisplayTick(state, world, pos, random);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return Blocks.TORCH.getOutlineShape(state, world, pos, context);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return Blocks.TORCH.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, @NotNull BlockPos pos) {
        return TorchBlock.sideCoversSmallSquare(world, pos.down(), Direction.UP);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BurningCrudeTorchBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (world1, pos1, state1, blockEntity) -> {
            if (world1.getBlockEntity(pos1) instanceof BurningCrudeTorchBlockEntity torch) {
                if (torch.shouldExtinguish()) {
                    if (state1.isOf(Reg.WALL_BURNING_CRUDE_TORCH_BLOCK)) {
                        BlockState result = Reg.WALL_BURNT_TORCH_BLOCK.getDefaultState();
                        if (state1.contains(FACING)) result = result.with(FACING, state1.get(FACING));
                        world1.setBlockState(pos1, result);
                    } else
                        world1.setBlockState(pos1, Reg.BURNT_TORCH_BLOCK.getDefaultState());
                    world1.playSound(null, pos1, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS);
                    if (world1 instanceof ServerWorld serverWorld) serverWorld.getChunkManager().markForUpdate(pos1);
                } else if (world1.isRaining() && world1.isSkyVisible(pos1))
                    torch.extinguish();
            }
        };
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        if (stack.isOf(Reg.BURNING_CRUDE_TORCH_ITEM) && world.getBlockEntity(pos) instanceof BurningCrudeTorchBlockEntity torch) {
            NbtCompound nbt = stack.getOrCreateNbt();
            if (nbt.contains(BurningCrudeTorchItem.EXTINGUISH_NBT, NbtElement.LONG_TYPE))
                torch.setExtinguishTime(nbt.getLong(BurningCrudeTorchItem.EXTINGUISH_NBT));
            else torch.ignite();
        }
    }
}
