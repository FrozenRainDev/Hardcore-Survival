package com.hcs.mixin.entity;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Shadow private int lastAttackedTime;

    @Inject(method = "baseTick", at = @At("HEAD"))
    public void baseTick(CallbackInfo cir) {
        //Enable permanent panic caused by attack
        ++lastAttackedTime;
    }
}
