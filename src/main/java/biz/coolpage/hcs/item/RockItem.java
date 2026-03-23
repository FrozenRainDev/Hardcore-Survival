package biz.coolpage.hcs.item;

import biz.coolpage.hcs.entity.RockProjectileEntity;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StaminaManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_AND_SERVER;

public class RockItem extends Item {
    public RockItem(Properties settings) {
        super(settings);
    }

    public static @NotNull InteractionResultHolder<ItemStack> throwOut(@NotNull Level world, @NotNull Player user, InteractionHand hand, ThrowableItemProjectile projectileEntity) {
        ItemStack stack = user.getItemInHand(hand);
        if (stack != null) {
            Item item = stack.getItem();
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 1F);
            user.getCooldowns().addCooldown(item, 45);
            if (!world.isClientSide) {
                projectileEntity.setItem(stack);
                projectileEntity.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 1.1F, 0F);
                world.addFreshEntity(projectileEntity);
                user.awardStat(Stats.ITEM_USED.get(item));
                if (IS_SURVIVAL_AND_SERVER.test(user)) {
                    stack.shrink(1);
                    StaminaManager staminaManager = ((StatAccessor) user).getStaminaManager();
                    staminaManager.add(-0.01, user);
                    staminaManager.pauseRestoring();
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player user, InteractionHand hand) {
        return throwOut(world, user, hand, new RockProjectileEntity(user, world));
    }
}