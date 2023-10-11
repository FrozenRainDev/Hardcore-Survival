package com.hcs.item;

import com.hcs.status.accessor.StatAccessor;
import com.hcs.status.manager.InjuryManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.hcs.util.CommUtil.applyNullable;

public class BandageItem extends Item {
    private final double bleedingReduction;
    private final int useTime;

    public BandageItem(double bleedingReduction, int useTime) {
        super(new Item.Settings());
        this.bleedingReduction = bleedingReduction;
        this.useTime = useTime;
    }

    public BandageItem(double bleedingReduction) {
        this(bleedingReduction, 60);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    } //FIXME UseAction.CROSSBOW and TOOT_HORN have bug in 1.19.4 which effect is as same as trident(UseAction.SPEAR)

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return this.useTime;
    }

    @Override
    public TypedActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand); //Indispensable! It will update this.activeItemStack in Entity which will check before calling usageTick()
        return super.use(world, user, hand);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (remainingUseTicks <= 0 || !(user instanceof PlayerEntity)) applyNullable(user, LivingEntity::stopUsingItem);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof ServerPlayerEntity player) {
            InjuryManager injuryManager = ((StatAccessor) player).getInjuryManager();
            injuryManager.addBleeding(-bleedingReduction);
            stack.decrement(1);
            player.playSound(SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.PLAYERS, 1.0F, 0.5F);
        }
        return super.finishUsing(stack, world, user);
    }
}
