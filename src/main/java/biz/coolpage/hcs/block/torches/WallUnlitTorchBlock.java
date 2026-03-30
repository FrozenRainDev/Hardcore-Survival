package biz.coolpage.hcs.block.torches;

import biz.coolpage.hcs.Hcs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class WallUnlitTorchBlock extends WallCrudeTorchBlock {
    public WallUnlitTorchBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected BlockState getLitBlockStateForUpdate(@NotNull BlockState prevStat) {
        return Blocks.WALL_TORCH.defaultBlockState().setValue(FACING, prevStat.getValue(FACING));
    }

    @Override
    public Item asItem() {
        return Hcs.UNLIT_TORCH_ITEM.get();
    }

    @Override
    protected Block asBlock() {
        return Hcs.UNLIT_TORCH_BLOCK.get();
    }
}