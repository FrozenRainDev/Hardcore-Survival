package biz.coolpage.hcs.item;

import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class HCSCampfireItem extends Item {
    private final BlockState defaultBlockState;

    public HCSCampfireItem(BlockState defaultBlockState) {
        super(new Settings());
        this.defaultBlockState = defaultBlockState;
    }

    @Override
    public ActionResult useOnBlock(@NotNull ItemUsageContext context) {
        World world = context.getWorld();
        if (world != null) {
            BlockPos pos = context.getBlockPos(), placePos = WorldHelper.getPosByDirection(pos, context.getSide());
            if (canPlaceCampfire(world, pos)) return placeCampfire(world, pos, context);
            if (canPlaceCampfire(world, placePos)) return placeCampfire(world, placePos, context);
        }
        return ActionResult.FAIL;
    }

    private boolean canPlaceCampfire(@NotNull World world, BlockPos placePos) {
        return isReplaceableBlock(world.getBlockState(placePos))
                && !isReplaceableBlock(world.getBlockState(placePos.down()));
    }

    private boolean isReplaceableBlock(@NotNull BlockState state) {
        return state.isOf(Blocks.AIR) || state.isOf(Blocks.CAVE_AIR) || state.isReplaceable();
    }

    private ActionResult placeCampfire(@NotNull World world, BlockPos pos, @NotNull ItemUsageContext context) {
        world.setBlockState(pos, this.defaultBlockState);
        if (EntityHelper.IS_SURVIVAL_AND_SERVER.test(context.getPlayer()))
            context.getStack().decrement(1);
        return ActionResult.success(world.isClient());
    }

}
