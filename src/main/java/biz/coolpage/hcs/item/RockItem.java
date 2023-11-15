package biz.coolpage.hcs.item;

import biz.coolpage.hcs.entity.RockProjectileEntity;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StaminaManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_AND_SERVER;

public class RockItem extends Item {
    public RockItem(Item.Settings settings) {
        super(settings);
    }

    public static @NotNull TypedActionResult<ItemStack> throwOut(@NotNull World world, @NotNull PlayerEntity user, Hand hand, Supplier<? extends ThrownItemEntity> entityType) {
        ItemStack stack = user.getStackInHand(hand);
        if (stack != null) {
            Item item = stack.getItem();
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 1F); // plays a globalSoundEvent
            user.getItemCooldownManager().set(item, 45);
            if (!world.isClient) {
                var projectileEntity = entityType.get();
                projectileEntity.setItem(stack);
                projectileEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.1F, 0F); //default speed is 1.5f
                world.spawnEntity(projectileEntity);
                user.incrementStat(Stats.USED.getOrCreateStat(item));
                if (IS_SURVIVAL_AND_SERVER.test(user)) {
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
        return throwOut(world, user, hand, () -> new RockProjectileEntity(user, world));
    }

}
