package biz.coolpage.hcs.block;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.entity.SmolderingOrBurntCampfireBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BurntCampfireBlock extends CampfireBlock {
    public BurntCampfireBlock() {
        super(false, 0, BlockBehaviour.Properties.copy(Blocks.CAMPFIRE).lightLevel(Blocks.litBlockEmission(0)));
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(SIGNAL_FIRE, false).setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected Block asBlock() {
        return Hcs.BURNT_CAMPFIRE_BLOCK;
    }

    @Override
    public Item asItem() {
        return Hcs.BURNT_CAMPFIRE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SmolderingOrBurntCampfireBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.FAIL;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        // Do nothing
    }

}