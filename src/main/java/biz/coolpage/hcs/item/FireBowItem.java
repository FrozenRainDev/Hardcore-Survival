package biz.coolpage.hcs.item;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StaminaManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FireBowItem extends Item {
    private final int[] stagesByTick = {225, 180, 150, 120, 90, 50};

    public FireBowItem(Settings settings, float efficiencyMultiplier) {
        super(settings);
        if (efficiencyMultiplier != 1) {
            for (int i = 0; i < this.stagesByTick.length; ++i)
                this.stagesByTick[i] = (int) (this.stagesByTick[i] * efficiencyMultiplier);
        }
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return this.stagesByTick[0];
    }

    @Override
    public ActionResult useOnBlock(@NotNull ItemUsageContext context) {
        if (CampfireBlock.canBeLit(context.getWorld().getBlockState(context.getBlockPos()))) {
            PlayerEntity playerEntity = context.getPlayer();
            if (playerEntity != null)
                playerEntity.setCurrentHand(context.getHand());
            return ActionResult.CONSUME;
        }
        return ActionResult.FAIL;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) {
            user.stopUsingItem();
            return;
        }
        BlockHitResult blockHitResult = Item.raycast(world, player, RaycastContext.FluidHandling.NONE);
        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);
        StaminaManager staminaManager = ((StatAccessor) player).getStaminaManager();
        if (CampfireBlock.canBeLit(state) && staminaManager.get() > 0.01) {
            staminaManager.pauseRestoring();
            staminaManager.add(-0.0007, player);
            player.getHungerManager().addExhaustion(0.015F);
            ((StatAccessor) player).getThirstManager().add(-0.0001);
            int i = this.getMaxUseTime(stack) - remainingUseTicks + 1;
            if (i % 5 == 0) {
                world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS);
                if (i % 10 == 0) {
                    world.addBlockBreakParticles(pos, state);
                    stack.damage(1, player, p -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
                }
                int mod = i % this.stagesByTick[1];
                ParticleEffect effect = null;
                if (mod > this.stagesByTick[2]) effect = ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
                else if (mod > this.stagesByTick[3]) effect = ParticleTypes.CAMPFIRE_COSY_SMOKE;
                else if (mod > this.stagesByTick[4]) effect = ParticleTypes.LARGE_SMOKE;
                else if (mod > this.stagesByTick[5]) effect = ParticleTypes.SMOKE;
                if (effect != null)
                    world.addParticle(effect, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0, 0.1, 0.0);
                if (mod == 0) {
                    world.setBlockState(pos, state.with(Properties.LIT, true));
                    world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS);
                }
            }
        } else user.stopUsingItem();
    }

}
