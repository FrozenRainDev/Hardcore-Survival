package com.hcs.mixin.entity;

import com.hcs.main.helper.EntityHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {
    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "squaredAttackRange", at = @At("RETURN"), cancellable = true)
    public void squaredAttackRange(LivingEntity target, @NotNull CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(cir.getReturnValue() + EntityHelper.getReachRangeAddition(this.getMainHandStack()));
    }

    @Inject(method = "getXpToDrop", at = @At("RETURN"), cancellable = true)
    public void getXpToDrop(@NotNull CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Math.max(1, (int) ((double) cir.getReturnValue() / 3.0)));
    }
}
