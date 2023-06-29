package com.hcs.mixin.entity;

import com.hcs.status.accessor.StatAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    private int lastAttackedTime;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "baseTick", at = @At("HEAD"))
    public void baseTick(CallbackInfo cir) {
        //Enable permanent panic caused by attack
        ++lastAttackedTime;
    }

    @SuppressWarnings("all")
    @Inject(method = "onAttacking", at = @At("HEAD"))
    public void onAttacking(Entity target, CallbackInfo ci) {
        if ((Object) this instanceof HostileEntity && target instanceof PlayerEntity player)
            ((StatAccessor) player).getSanityManager().add((Object) this instanceof EndermanEntity ? -0.08 : -0.005);
    }

    @SuppressWarnings("all")
    @Inject(method = "getNextAirOnLand", at = @At("RETURN"), cancellable = true)
    protected void getNextAirOnLand(int air, @NotNull CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof PlayerEntity player) {
            int lvl = ((StatAccessor) player).getStatusManager().getFinalOxygenLackLevel();
            if (lvl > 0 && !player.hasStatusEffect(StatusEffects.WATER_BREATHING)) {
                cir.setReturnValue(air + ((air + 1 >= this.getMaxAir()) ? 0 : switch (lvl) {
                    case 1 -> 1;
                    default -> 0;
                }));
            }
        }
    }
}
