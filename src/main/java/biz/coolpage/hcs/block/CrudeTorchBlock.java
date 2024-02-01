package biz.coolpage.hcs.block;

import biz.coolpage.hcs.entity.CrudeTorchBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.property.Properties;
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

import java.util.function.Consumer;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

@SuppressWarnings("deprecation")
public class CrudeTorchBlock extends AbstractCrudeTorchBlock {
    private static final Consumer<BlockEntity> IGNITE_OPERATION = blockEntity -> {
        if (blockEntity instanceof CrudeTorchBlockEntity torch/*Always true*/) torch.igniteSync();
        blockEntity.markDirty();
        applyNullable(blockEntity.getWorld(), world -> world.updateListeners(blockEntity.getPos(), blockEntity.getCachedState(), blockEntity.getCachedState(), Block.NOTIFY_LISTENERS));
    };

    public CrudeTorchBlock(AbstractBlock.Settings settings) {
        super(settings);
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
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return Blocks.TORCH.canPlaceAt(state, world, pos);
    }

    @Override
    public ActionResult onUse(BlockState state, @NotNull World world, BlockPos pos, @NotNull PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (AbstractCrudeTorchBlock.onIgnite(world, pos, player, hand, IGNITE_OPERATION)) {
            AbstractCrudeTorchBlock.ignite(world, state, pos);
            return ActionResult.success(world.isClient);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }
}
