package biz.coolpage.hcs.item;

import biz.coolpage.hcs.entity.RockProjectileEntity;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StaminaManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class RockItem extends Item {
    public RockItem(Item.Settings settings) {
        super(settings);
    }

    public static @NotNull TypedActionResult<ItemStack> throwOut(@NotNull World world, @NotNull PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (stack != null) {
            Item item = stack.getItem();
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 1F); // plays a globalSoundEvent
            user.getItemCooldownManager().set(item, 45);
            user.setCurrentHand(hand);
            if (!world.isClient) {
                RockProjectileEntity projectileEntity = new RockProjectileEntity(user, world, item);
                projectileEntity.setItem(stack);
                projectileEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.0F, 0F);//default speed is 1.5f
                world.spawnEntity(projectileEntity);
                user.incrementStat(Stats.USED.getOrCreateStat(item));
                if (!user.getAbilities().invulnerable) {
                    stack.decrement(1);
                    StaminaManager staminaManager = ((StatAccessor) user).getStaminaManager();
                    staminaManager.add(-0.01, user);
                    staminaManager.pauseRestoring();
                }
            }
        }
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public TypedActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity user, Hand hand) {
        return throwOut(world, user, hand);
    }

}
