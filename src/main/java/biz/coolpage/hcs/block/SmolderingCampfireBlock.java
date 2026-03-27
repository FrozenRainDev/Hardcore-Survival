package biz.coolpage.hcs.block;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.entity.SmolderingOrBurntCampfireBlockEntity;
import biz.coolpage.hcs.util.CombustionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.particles.ParticleTypes;
import org.jetbrains.annotations.NotNull;

public class SmolderingCampfireBlock extends CampfireBlock {
    public SmolderingCampfireBlock() {
        super(true, 1, BlockBehaviour.Properties.copy(Blocks.CAMPFIRE).lightLevel(Blocks.litBlockEmission(5)));
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.WATERLOGGED, false).setValue(BlockStateProperties.LIT, true));
    }

    @Override
    protected Block asBlock() {
        return Hcs.SMOLDERING_CAMPFIRE_BLOCK;
    }

    @Override
    public Item asItem() {
        return Hcs.SMOLDERING_CAMPFIRE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SmolderingOrBurntCampfireBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return (world1, pos1, state1, blockEntity) -> {
            if (blockEntity instanceof SmolderingOrBurntCampfireBlockEntity campfire) { // TODO test carefully here; compare with other similar code
                if (state.hasProperty(BlockStateProperties.LIT) && !state.getValue(BlockStateProperties.LIT))
                    world1.setBlock(pos1, Hcs.BURNT_CAMPFIRE_BLOCK.defaultBlockState(), 3); // Parameter 3 is the most commonly used flag, equivalent to Block.UPDATE_ALL (update neighboring blocks and send to the client).
                else
                    SmolderingOrBurntCampfireBlockEntity.litServerTick(world1, pos1, state1, campfire);
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, @NotNull Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (CombustionHelper.checkAddFuel(world, pos, state, stack))
            return InteractionResult.sidedSuccess(world.isClientSide());
        return InteractionResult.FAIL;
    }

    @Override
    public void entityInside(@NotNull BlockState state, @NotNull Level world, BlockPos pos, Entity entity) {
        // Reduced fire damage
        if (state.getValue(LIT) && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity) entity)) {
            entity.hurt(world.damageSources().inFire(), 0.5F);
        }
        // Do NOT call super.onEntityCollision(state, world, pos, entity);
        if (entity instanceof ItemEntity itemEntity)
            CombustionHelper.checkAddFuel(world, entity.blockPosition(), state, itemEntity.getItem());
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, @NotNull RandomSource random) {
        if (random.nextInt(20) == 0) {
            world.playLocalSound((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5f + random.nextFloat(), random.nextFloat() * 0.7f + 0.6f, false);
        }
        if (random.nextInt(5) == 0) {
            for (int i = 0; i < random.nextInt(1) + 1; ++i) {
                world.addParticle(ParticleTypes.SMOKE, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, 0.0, 5.0E-5, 0.0);
            }
        }
    }
}