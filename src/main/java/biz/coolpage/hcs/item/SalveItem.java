package biz.coolpage.hcs.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class SalveItem extends BandageItem {
    final int regenSec;

    public SalveItem(int regenSec, double bleedingReduction) {
        this(regenSec, bleedingReduction, 40);
    }

    public SalveItem(int regenSec, double bleedingReduction, int useTime) {
        super(bleedingReduction, useTime);
        this.regenSec = regenSec;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof ServerPlayerEntity player && regenSec > 0)
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, regenSec * 20, 0));
        return super.finishUsing(stack, world, user);
    }

}
