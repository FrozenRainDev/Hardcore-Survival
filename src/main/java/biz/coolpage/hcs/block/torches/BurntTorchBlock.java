package biz.coolpage.hcs.block.torches;

import biz.coolpage.hcs.Reg;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TorchBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class BurntTorchBlock extends CrudeTorchBlock {
    public BurntTorchBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, @NotNull World world, BlockPos pos, @NotNull PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ActionResult.FAIL;
    }

    @Override
    public Item asItem() {
        return Items.AIR;
    }

    @Override
    protected Block asBlock() {
        return Reg.BURNT_TORCH_BLOCK;
    }

}
