package biz.coolpage.hcs.block.torches;

import biz.coolpage.hcs.Hcs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class WallBurntTorchBlock extends WallCrudeTorchBlock {
    public WallBurntTorchBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any());
    }

    @Override
    protected BlockState getLitBlockStateForUpdate(@NotNull BlockState prevStat) {
        return Hcs.WALL_BURNT_TORCH_BLOCK.defaultBlockState().setValue(FACING, prevStat.getValue(FACING));
    }

    @Override
    public Item asItem() {
        return Items.AIR;
    }

    @Override
    protected Block asBlock() {
        return Hcs.WALL_BURNT_TORCH_BLOCK;
    }
}