package com.hcs.mixin.entity;

import com.hcs.Reg;
import com.hcs.status.HcsEffects;
import com.hcs.status.accessor.StatAccessor;
import com.hcs.util.EntityHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    private int lastAttackedTime;

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract boolean isBaby();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "baseTick", at = @At("HEAD"))
    public void baseTick(CallbackInfo cir) {
        //Enable permanent panic caused by attack
        ++lastAttackedTime;
    }


    @Inject(method = "onAttacking", at = @At("HEAD"))
    public void onAttacking(Entity target, CallbackInfo ci) {
        //noinspection ConstantValue
        if ((Object) this instanceof HostileEntity && target instanceof PlayerEntity player)
            ((StatAccessor) player).getSanityManager().add((Object) this instanceof EndermanEntity ? -0.08 : -0.005);
    }


    @Inject(method = "getNextAirOnLand", at = @At("RETURN"), cancellable = true)
    protected void getNextAirOnLand(int air, @NotNull CallbackInfoReturnable<Integer> cir) {
        //noinspection ConstantValue
        if ((Object) this instanceof PlayerEntity player) {
            int lvl = ((StatAccessor) player).getStatusManager().getFinalOxygenLackLevel();
            if (lvl > 0 && !player.hasStatusEffect(StatusEffects.WATER_BREATHING)) {
                //noinspection ConditionalExpression
                cir.setReturnValue(air + ((air + 1 >= this.getMaxAir()) ? 0 : switch (lvl) {
                    case 1 -> 1;
                    default -> 0;
                }));
            }
        }
    }


    @SuppressWarnings("ConstantValue")
    @Inject(method = "damage", at = @At("HEAD"))
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Integer> cir) {
        Object ent = this;
        if (ent instanceof ChickenEntity && source.toString().contains("EntityDamageSource"))
            EntityHelper.dropItem(this, Items.FEATHER, 1);
        //On death
        if ((this.getHealth() - amount) <= 0.0F && this.getHealth() > 0.0F) {
            if (ent instanceof ChickenEntity || ent instanceof CowEntity || ent instanceof PigEntity || ent instanceof SheepEntity) {
                if (!(ent instanceof ChickenEntity)) {
                    //EntityHelper.dropItem(this, Items.BONE, 2);
                    EntityHelper.dropItem(this, Reg.ANIMAL_VISCERA, 1);
                    if (ent instanceof SheepEntity && Math.random() < 0.6)
                        EntityHelper.dropItem(this, Items.LEATHER, 1);
                }
                if (this.isBaby())
                    EntityHelper.dropItem(this, this.getFireTicks() > 0 ? Reg.COOKED_MEAT : Reg.RAW_MEAT, 1);
            } else if (ent instanceof AxolotlEntity || ent instanceof CatEntity || ent instanceof FrogEntity || ent instanceof ParrotEntity || ent instanceof SquidEntity)
                EntityHelper.dropItem(this, this.getFireTicks() > 0 ? Reg.COOKED_MEAT : Reg.RAW_MEAT, 1);
            else if (!(ent instanceof BeeEntity || ent instanceof TadpoleEntity) && ent instanceof AnimalEntity) {
                EntityHelper.dropItem(this, this.getFireTicks() > 0 ? Reg.COOKED_MEAT : Reg.RAW_MEAT, (int) (Math.random() * 3) + 1);
                //EntityHelper.dropItem(this, Items.BONE, 2);
                EntityHelper.dropItem(this, Reg.ANIMAL_VISCERA, 1);
            }
            //Also see at BatEntityMixin
        }
    }

    @ModifyArg(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(DDD)V"), index = 1)
    public double jump(double velY) {
        //noinspection ConstantValue
        if (((Object) this) instanceof ServerPlayerEntity player) {
            if (player.hasStatusEffect(HcsEffects.EXHAUSTED)) {
                System.out.println(velY);
//                return (velY * (player.getStatusEffect(HcsEffects.EXHAUSTED).getAmplifier() > 0 ? 0.05 : 0.3));
                return 11;
            }
        }
        return velY;
    }


}
