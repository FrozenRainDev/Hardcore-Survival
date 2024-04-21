package biz.coolpage.hcs.block.torches;

import biz.coolpage.hcs.Reg;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class UnlitTorchBlock extends CrudeTorchBlock {
    public UnlitTorchBlock(Settings settings) {
        super(settings);
    }

    protected BlockState getLitBlockStateForUpdate(@NotNull BlockState prevStat) {
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
