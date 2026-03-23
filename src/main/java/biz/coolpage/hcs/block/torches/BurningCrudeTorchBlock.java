package biz.coolpage.hcs.block.torches;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.entity.BurningCrudeTorchBlockEntity;
import biz.coolpage.hcs.item.BurningCrudeTorchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static biz.coolpage.hcs.config.Configs.BURN;
import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

@SuppressWarnings("deprecation")
public class BurningCrudeTorchBlock extends BaseEntityBlock {
    public BurningCrudeTorchBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any());
    }

    @Override
    public Item asItem() {
        return Hcs.BURNING_CRUDE_TORCH_ITEM;
    }

    @Override
    protected Block asBlock() {
        return Hcs.BURNING_CRUDE_TORCH_BLOCK;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // INVISIBLE WITHOUT THIS
    }

    @Override
    public void animateTick(BlockState state, @NotNull Level world, BlockPos pos, RandomSource random) {
        if (world.getGameTime() % 3L == 2L) return;
        Blocks.TORCH.animateTick(state, world, pos, random);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Blocks.TORCH.getShape(state, world, pos, context);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        return Blocks.TORCH.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, @NotNull BlockPos pos) {
        return TorchBlock.canSupportCenter(world, pos.below(), Direction.UP);
    }

    // 修复点：方法名从 createBlockEntity 修改为 newBlockEntity
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BurningCrudeTorchBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return (world1, pos1, state1, blockEntity) -> {
            if (Configs.isEnabled(BURN)
                    && world1.getBlockEntity(pos1) instanceof BurningCrudeTorchBlockEntity torch
                    && world1 instanceof ServerLevel serverWorld) {
                if (torch.shouldExtinguish()) {
                    world1.playSound(null, pos1, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                    if (state1.is(Hcs.WALL_BURNING_CRUDE_TORCH_BLOCK)) {
                        BlockState result = Hcs.WALL_BURNT_TORCH_BLOCK.defaultBlockState();
                        if (state1.hasProperty(FACING)) result = result.setValue(FACING, state1.getValue(FACING));
                        world1.setBlock(pos1, result, 3);
                    } else {
                        world1.setBlock(pos1, Hcs.BURNT_TORCH_BLOCK.defaultBlockState(), 3);
                    }
                    serverWorld.getChunkSource().blockChanged(pos1);
                } else if (world1.isRaining() && world1.canSeeSky(pos1))
                    torch.extinguish();
            }
        };
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (stack.is(Hcs.BURNING_CRUDE_TORCH_ITEM) && world.getBlockEntity(pos) instanceof BurningCrudeTorchBlockEntity torch) {
            CompoundTag nbt = stack.getOrCreateTag();
            if (nbt.contains(BurningCrudeTorchItem.EXTINGUISH_NBT, Tag.TAG_LONG))
                torch.setExtinguishTime(nbt.getLong(BurningCrudeTorchItem.EXTINGUISH_NBT));
            else torch.ignite();
        }
    }
}