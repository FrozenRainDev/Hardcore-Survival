package biz.coolpage.hcs.block.torches;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.entity.BurningCrudeTorchBlockEntity;
import biz.coolpage.hcs.util.CombustionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class CrudeTorchBlock extends TorchBlock {
    public CrudeTorchBlock(Properties settings) {
        super(settings, ParticleTypes.FLAME);
    }

    protected BlockState getLitBlockStateForUpdate(BlockState prevStat) {
        return Hcs.BURNING_CRUDE_TORCH_BLOCK.get().defaultBlockState();
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, @NotNull Level world, BlockPos pos, @NotNull Player player, InteractionHand hand, BlockHitResult hit) {
        if (CombustionHelper.onLit(world, pos, player, hand)) {
            world.setBlock(pos, this.getLitBlockStateForUpdate(state), 3);
            if (world.getBlockEntity(pos) instanceof BurningCrudeTorchBlockEntity torch)
                torch.onLit(player);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public Item asItem() {
        return Hcs.CRUDE_TORCH_ITEM.get();
    }

    @Override
    protected Block asBlock() {
        return Hcs.CRUDE_TORCH_BLOCK.get();
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        // Just do nothing
    }
}