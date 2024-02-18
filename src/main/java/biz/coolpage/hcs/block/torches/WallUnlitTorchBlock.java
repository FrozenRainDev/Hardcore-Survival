package biz.coolpage.hcs.block.torches;

import biz.coolpage.hcs.Reg;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.block.HorizontalFacingBlock.FACING;

public class WallUnlitTorchBlock extends WallCrudeTorchBlock {
    public WallUnlitTorchBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected BlockState getLitBlockStateForUpdate(@NotNull BlockState prevStat) {
        return Blocks.WALL_TORCH.getDefaultState().with(FACING, prevStat.get(FACING));
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
