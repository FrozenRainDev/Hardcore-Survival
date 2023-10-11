package com.hcs.item;

import com.hcs.Reg;
import com.hcs.status.accessor.StatAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class SpiderGlandItem extends BandageItem {
    final int regenSec;

    public SpiderGlandItem(int regenSec, double bleedingReduction) {
        super(bleedingReduction, 40);
        this.regenSec = regenSec;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof ServerPlayerEntity player) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, regenSec * 20, 0));
            if (stack.isOf(Reg.SELAGINELLA)) {
                ((StatAccessor) player).getStatusManager().setSoulImpairedStat(0);
                ((StatAccessor) player).getInjuryManager().applyPainkiller();
            }
        }
        return super.finishUsing(stack, world, user);
    }

}
