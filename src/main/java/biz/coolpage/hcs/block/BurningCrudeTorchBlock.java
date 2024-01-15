package biz.coolpage.hcs.block;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.entity.CrudeTorchBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.NotNull;

import static biz.coolpage.hcs.block.CrudeTorchBlock.onLit;

@SuppressWarnings("deprecation")
public class BurningCrudeTorchBlock extends BlockWithEntity {
    //TODO register lang & img
    protected static final VoxelShape BOUNDING_SHAPE = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 10.0, 10.0);

    public BurningCrudeTorchBlock() {
        super(AbstractBlock.Settings.of(Material.DECORATION).noCollision().breakInstantly().sounds(BlockSoundGroup.WOOD));
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
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return BOUNDING_SHAPE;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !this.canPlaceAt(state, world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, @NotNull BlockPos pos) {
        return TorchBlock.sideCoversSmallSquare(world, pos.down(), Direction.UP);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CrudeTorchBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (world1, pos1, state1, blockEntity) -> {
            if (world1.getBlockEntity(pos1) instanceof CrudeTorchBlockEntity torch) {
                if (torch.shouldExtinguish()) {
                    world1.setBlockState(pos1, Reg.CRUDE_TORCH_BLOCK.getDefaultState());
                    world1.playSound(null, pos1, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS);
                    for (int i = 0; i < 4; ++i)
                        world1.addParticle(ParticleTypes.SMOKE, pos1.getX(), pos1.getY(), pos1.getZ(), 0.0, 5.0E-4, 0.0);
                    if (world1 instanceof ServerWorld serverWorld) serverWorld.getChunkManager().markForUpdate(pos1);
                }
            }
        };
    }

    @Override
    public ActionResult onUse(BlockState state, @NotNull World world, BlockPos pos, @NotNull PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (onLit(world, pos, player, hand)) {
            world.setBlockState(pos, Reg.BURNING_CRUDE_TORCH_BLOCK.getDefaultState());
            return ActionResult.success(world.isClient);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }
}
