package biz.coolpage.hcs.block;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.entity.BurningCrudeTorchBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static biz.coolpage.hcs.block.CrudeTorchBlock.onLit;

@SuppressWarnings({"deprecation", "unused"})
public class BurningCrudeTorchBlock extends BlockWithEntity {

    public BurningCrudeTorchBlock(AbstractBlock.Settings settings) {
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
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
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
                    world1.setBlockState(pos1, Reg.CRUDE_TORCH_BLOCK.getDefaultState());
                    world1.playSound(null, pos1, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS);
                    if (world1 instanceof ServerWorld serverWorld) serverWorld.getChunkManager().markForUpdate(pos1);
                }
            }
        };
    }

    @Override
    public ActionResult onUse(BlockState state, @NotNull World world, BlockPos pos, @NotNull PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (onLit(world, pos, player, hand)) {
            world.setBlockState(pos, Reg.BURNING_CRUDE_TORCH_BLOCK.getDefaultState());
            if (world.getBlockEntity(pos) instanceof BurningCrudeTorchBlockEntity torch)
                torch.onLit(player);
            return ActionResult.success(world.isClient);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        if (stack.isOf(Reg.BURNING_CRUDE_TORCH_ITEM) && world.getBlockEntity(pos) instanceof BurningCrudeTorchBlockEntity torch)
            torch.ignite();
    }

    @Override
    public void precipitationTick(BlockState state, @NotNull World world, BlockPos pos, Biome.Precipitation precipitation) {
        if (world.getBlockEntity(pos) instanceof BurningCrudeTorchBlockEntity torch)
            torch.updateForExtinguish();
    }
}
