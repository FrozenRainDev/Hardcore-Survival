package biz.coolpage.hcs.block.torches;

import biz.coolpage.hcs.Reg;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.block.HorizontalFacingBlock.FACING;

public class WallBurntTorchBlock extends WallCrudeTorchBlock {
    public WallBurntTorchBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected BlockState getLitBlockStateForUpdate(@NotNull BlockState prevStat) {
        return Reg.WALL_BURNT_TORCH_BLOCK.getDefaultState().with(FACING, prevStat.get(FACING));
    }

    @Override
    public Item asItem() {
        return Items.AIR;
    }

    @Override
    protected Block asBlock() {
        return Reg.WALL_BURNT_TORCH_BLOCK;
    }
}
