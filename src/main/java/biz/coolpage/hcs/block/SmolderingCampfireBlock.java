package biz.coolpage.hcs.block;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.entity.SmolderingOrBurntCampfireBlockEntity;
import biz.coolpage.hcs.util.CombustionHelper;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

// TODO submerged in water
public class SmolderingCampfireBlock extends CampfireBlock {
    public SmolderingCampfireBlock() {
        super(true, 1, AbstractBlock.Settings.of(Material.WOOD, MapColor.SPRUCE_BROWN).strength(2.0f).sounds(BlockSoundGroup.WOOD).luminance(Blocks.createLightLevelFromLitBlockState(5)).nonOpaque());
    }

    @Override
    protected Block asBlock() {
        return Reg.SMOLDERING_CAMPFIRE_BLOCK;
    }

    @Override
    public Item asItem() {
        return Reg.SMOLDERING_CAMPFIRE;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SmolderingOrBurntCampfireBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (world1, pos1, state1, blockEntity) -> {
            if (blockEntity instanceof SmolderingOrBurntCampfireBlockEntity campfire) { // TODO test carefully here; compare with other similar code
                if (state.contains(Properties.LIT) && !state.get(Properties.LIT))
                    world1.setBlockState(pos1, Reg.BURNT_CAMPFIRE_BLOCK.getDefaultState());
                else
                    SmolderingOrBurntCampfireBlockEntity.litServerTick(world1, pos1, state1, campfire);
            }
        };
    }

    // TODO handle on use
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, @NotNull PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        if (CombustionHelper.checkAddFuel(world, pos, state, stack))
            return ActionResult.success(world.isClient());
        return ActionResult.FAIL;
    }

    @Override
    public void onEntityCollision(@NotNull BlockState state, @NotNull World world, BlockPos pos, Entity entity) {
        // Reduced fire damage
        if (state.get(LIT) && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity) entity)) {
            entity.damage(world.getDamageSources().inFire(), 0.5F);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, @NotNull Random random) {
        if (random.nextInt(20) == 0) {
            world.playSound((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 0.5f + random.nextFloat(), random.nextFloat() * 0.7f + 0.6f, false);
        }
        if (random.nextInt(5) == 0) {
            for (int i = 0; i < random.nextInt(1) + 1; ++i) {
                world.addParticle(ParticleTypes.SMOKE, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, 0.0F, 5.0E-5, 0.0F);
            }
        }
    }
}
