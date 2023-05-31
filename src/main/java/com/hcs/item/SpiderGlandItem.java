package com.hcs.item;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class SpiderGlandItem extends ItemWithTip {
    int durationSeconds;

    public SpiderGlandItem(Settings settings, String tip, int durationSeconds) {
        super(settings, tip);
        this.durationSeconds = durationSeconds;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) return super.use(world, user, hand);
        ItemStack itemStack = user.getStackInHand(hand);
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, durationSeconds * 20, 0));
        if (!user.getAbilities().creativeMode) itemStack.decrement(1);
        return TypedActionResult.success(itemStack, world.isClient());
    }

}
