package biz.coolpage.hcs.block.torches;

import biz.coolpage.hcs.Hcs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class BurntTorchBlock extends CrudeTorchBlock {
    public BurntTorchBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, @NotNull Level world, BlockPos pos, @NotNull Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.FAIL;
    }

    @Override
    public Item asItem() {
        return Items.AIR;
    }

    @Override
    protected Block asBlock() {
        return Hcs.BURNT_TORCH_BLOCK.get();
    }

}