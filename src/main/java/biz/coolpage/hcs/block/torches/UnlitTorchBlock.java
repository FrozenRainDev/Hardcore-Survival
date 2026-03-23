package biz.coolpage.hcs.block.torches;

import biz.coolpage.hcs.Hcs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class UnlitTorchBlock extends CrudeTorchBlock {
    public UnlitTorchBlock(Properties settings) {
        super(settings);
    }

    protected BlockState getLitBlockStateForUpdate(@NotNull BlockState prevStat) {
        return Blocks.TORCH.defaultBlockState();
    }

    @Override
    public Item asItem() {
        return Hcs.UNLIT_TORCH_ITEM;
    }

    @Override
    protected Block asBlock() {
        return Hcs.UNLIT_TORCH_BLOCK;
    }
}