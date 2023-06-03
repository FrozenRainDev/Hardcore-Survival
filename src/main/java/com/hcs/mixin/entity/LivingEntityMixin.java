package com.hcs.mixin.entity;

import com.hcs.misc.accessor.StatAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Shadow
    private int lastAttackedTime;

    @Inject(method = "baseTick", at = @At("HEAD"))
    public void baseTick(CallbackInfo cir) {
        //Enable permanent panic caused by attack
        ++lastAttackedTime;
    }

    @SuppressWarnings("all")
    @Inject(method = "onAttacking", at = @At("HEAD"))
    public void onAttacking(Entity target, CallbackInfo ci) {
        if ((Object) this instanceof HostileEntity && target instanceof PlayerEntity player)
            ((StatAccessor) player).getSanityManager().add((Object) this instanceof EndermanEntity ? -0.08F : -0.005F);
    }
}
