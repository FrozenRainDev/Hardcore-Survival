package biz.coolpage.hcs.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (user instanceof ServerPlayer player && regenSec > 0)
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, regenSec * 20, 0));
        return super.finishUsingItem(stack, world, user);
    }
}