package biz.coolpage.hcs.item;

import net.minecraft.block.BlockState;
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

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

public class FireBowItem extends Item {
    public FireBowItem(Settings settings) {
        super(settings);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 225;
    }

    @Override
    public ActionResult useOnBlock(@NotNull ItemUsageContext context) {
        PlayerEntity playerEntity = context.getPlayer();
        if (playerEntity != null) {
            playerEntity.setCurrentHand(context.getHand());
        }
        return ActionResult.CONSUME;
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
        if (state.isIn(BlockTags.CAMPFIRES) && state.contains(Properties.LIT) && !state.get(Properties.LIT)) {
            int i = this.getMaxUseTime(stack) - remainingUseTicks + 1;
            if (i % 5 == 0) {
                world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS);
                world.addBlockBreakParticles(pos, state);
                applyNullable(player, p -> stack.damage(1, p, u -> u.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)));
                int mod = i % 160;
                ParticleEffect effect = null;
                if (mod > 130) effect = ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
                else if (mod > 100) effect = ParticleTypes.CAMPFIRE_COSY_SMOKE;
                else if (mod > 80) effect = ParticleTypes.LARGE_SMOKE;
                else if (mod > 60) effect = ParticleTypes.SMOKE;
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
