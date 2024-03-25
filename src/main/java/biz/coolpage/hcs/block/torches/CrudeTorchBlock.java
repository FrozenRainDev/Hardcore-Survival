package biz.coolpage.hcs.block.torches;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.entity.BurningCrudeTorchBlockEntity;
import biz.coolpage.hcs.util.CombustionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TorchBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class CrudeTorchBlock extends TorchBlock {
    public CrudeTorchBlock(Settings settings) {
        super(settings, ParticleTypes.FLAME);
    }

    protected BlockState getLitBlockStateForUpdate(BlockState prevStat) {
        return Reg.BURNING_CRUDE_TORCH_BLOCK.getDefaultState();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, @NotNull World world, BlockPos pos, @NotNull PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (CombustionHelper.onLit(world, pos, player, hand)) {
            world.setBlockState(pos, this.getLitBlockStateForUpdate(state));
            if (world.getBlockEntity(pos) instanceof BurningCrudeTorchBlockEntity torch)
                torch.onLit(player);
            return ActionResult.success(world.isClient);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public Item asItem() {
        return Reg.CRUDE_TORCH_ITEM;
    }

    @Override
    protected Block asBlock() {
        return Reg.CRUDE_TORCH_BLOCK;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        // Just do nothing
    }
}
