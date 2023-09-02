package com.hcs.item;

import com.hcs.entity.RockProjectileEntity;
import com.hcs.status.accessor.StatAccessor;
import com.hcs.status.manager.StaminaManager;
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

    @Override
    public TypedActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 1F); // plays a globalSoundEvent
        user.getItemCooldownManager().set(this, 45);
        if (!world.isClient) {
            RockProjectileEntity snowballEntity = new RockProjectileEntity(user, world);
            snowballEntity.setItem(itemStack);
            snowballEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.1F, 0F);//default speed is 1.5f
            world.spawnEntity(snowballEntity);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!user.getAbilities().invulnerable) {
                itemStack.decrement(1);
                StaminaManager staminaManager = ((StatAccessor) user).getStaminaManager();
                staminaManager.add(-0.01, user);
                staminaManager.pauseRestoring();
            }
        }
        return TypedActionResult.success(itemStack, world.isClient());
    }

}
