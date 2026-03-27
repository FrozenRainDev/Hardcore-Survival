package biz.coolpage.hcs.item;

import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class HCSCampfireItem extends Item {
    private final BlockState defaultBlockState;

    public HCSCampfireItem(BlockState defaultBlockState) {
        super(new Item.Properties());
        this.defaultBlockState = defaultBlockState;
    }

    @Override
    @NotNull
    public InteractionResult useOn(@NotNull UseOnContext context) {
        Level world = context.getLevel();
        if (world != null) {
            BlockPos pos = context.getClickedPos(), placePos = WorldHelper.getPosByDirection(pos, context.getClickedFace());
            if (canPlaceCampfire(world, pos)) return placeCampfire(world, pos, context);
            if (canPlaceCampfire(world, placePos)) return placeCampfire(world, placePos, context);
        }
        return InteractionResult.FAIL;
    }

    private boolean canPlaceCampfire(@NotNull Level world, BlockPos placePos) {
        return isReplaceableBlock(world.getBlockState(placePos))
                && !isReplaceableBlock(world.getBlockState(placePos.below()));
    }

    private boolean isReplaceableBlock(@NotNull BlockState state) {
        return state.is(Blocks.AIR) || state.is(Blocks.CAVE_AIR) || state.canBeReplaced();
    }

    private InteractionResult placeCampfire(@NotNull Level world, BlockPos pos, @NotNull UseOnContext context) {
        world.setBlock(pos, this.defaultBlockState, 3);
        if (EntityHelper.IS_SURVIVAL_AND_SERVER.test(context.getPlayer()))
            context.getItemInHand().shrink(1);
        return InteractionResult.sidedSuccess(world.isClientSide());
    }
}