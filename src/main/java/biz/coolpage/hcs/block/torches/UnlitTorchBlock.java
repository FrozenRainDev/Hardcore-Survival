package biz.coolpage.hcs.block.torches;

import biz.coolpage.hcs.Reg;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class UnlitTorchBlock extends CrudeTorchBlock {
    public UnlitTorchBlock(Settings settings) {
        super(settings);
    }

    protected BlockState getLitBlockStateForUpdate() {
        return Blocks.TORCH.getDefaultState();
    }

    @Override
    public Item asItem() {
        return Reg.UNLIT_TORCH_ITEM;
    }

    @Override
    protected Block asBlock() {
        return Reg.UNLIT_TORCH_BLOCK;
    }

}
