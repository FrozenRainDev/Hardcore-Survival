package biz.coolpage.hcs.item;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StaminaManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class FireBowItem extends Item {
    private final int[] stagesByTick = {225, 180, 150, 120, 90, 50};

    public FireBowItem(Properties properties, float efficiencyMultiplier) {
        super(properties);
        if (efficiencyMultiplier != 1) {
            for (int i = 0; i < this.stagesByTick.length; ++i)
                this.stagesByTick[i] = (int) (this.stagesByTick[i] * efficiencyMultiplier);
        }
    }

    @Override
    @NotNull
    public UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return this.stagesByTick[0];
    }

    @Override
    @NotNull
    public InteractionResult useOn(@NotNull UseOnContext context) {
        if (CampfireBlock.canLight(context.getLevel().getBlockState(context.getClickedPos()))) {
            Player player = context.getPlayer();
            if (player != null)
                player.startUsingItem(context.getHand());
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity user, @NotNull ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof Player player)) {
            user.stopUsingItem();
            return;
        }

        // Item.raycast 在 Mojang 映射中通常对应 getPlayerPOVHitResult
        BlockHitResult blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);
        StaminaManager staminaManager = ((StatAccessor) player).getStaminaManager();

        if (CampfireBlock.canLight(state) && staminaManager.get() > 0.01) {
            staminaManager.pauseRestoring();
            staminaManager.add(-0.0007, player);
            player.getFoodData().addExhaustion(0.015F);
            ((StatAccessor) player).getThirstManager().add(-0.0001);

            int i = this.getUseDuration(stack) - remainingUseTicks + 1;
            if (i % 5 == 0) {
                level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (i % 10 == 0) {
                    // addBlockBreakParticles 对应 level.addDestroyBlockEffect
                    level.addDestroyBlockEffect(pos, state);
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                }

                int mod = i % this.stagesByTick[1];
                ParticleOptions effect = null;
                if (mod > this.stagesByTick[2]) effect = ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
                else if (mod > this.stagesByTick[3]) effect = ParticleTypes.CAMPFIRE_COSY_SMOKE;
                else if (mod > this.stagesByTick[4]) effect = ParticleTypes.LARGE_SMOKE;
                else if (mod > this.stagesByTick[5]) effect = ParticleTypes.SMOKE;

                if (effect != null)
                    level.addParticle(effect, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0, 0.1, 0.0);

                if (mod == 0) {
                    level.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), 3);
                    level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
        } else {
            user.stopUsingItem();
        }
    }
}