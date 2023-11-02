package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    private int lastAttackedTime;

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
    }

    @Unique
    @Deprecated
//    @ModifyArg(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(DDD)V"), index = 1)
    public double jump(double velY) {
        //noinspection ConstantValue
        if (((Object) this) instanceof ServerPlayerEntity player) {//Does not work for players to limit jump height, which is at least 1.2 blocks
            if (player.hasStatusEffect(HcsEffects.EXHAUSTED)) {
                return (velY * (player.getStatusEffect(HcsEffects.EXHAUSTED).getAmplifier() > 0 ? 0.05 : 0.3));
            }
        }
        return velY;
    }

    @Inject(method = "getStatusEffects", at = @At("RETURN"), cancellable = true)
    public void getStatusEffects(@NotNull CallbackInfoReturnable<Collection<StatusEffectInstance>> cir) {
        Collection<StatusEffectInstance> effects = cir.getReturnValue();
        if (effects != null) {
            ArrayList<StatusEffectInstance> effectList = new ArrayList<>(effects);
            effectList.sort(Comparator.comparing(StatusEffectInstance::getTranslationKey));
            cir.setReturnValue(effectList);
        }
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "onDeath", at = @At("TAIL"))
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        Object ent = this;
        Item meat = this.getFireTicks() > 0 ? Reg.COOKED_MEAT : Reg.RAW_MEAT;
        if (ent instanceof ChickenEntity || ent instanceof CowEntity || ent instanceof PigEntity || ent instanceof SheepEntity) {
            if (!(ent instanceof ChickenEntity)) {
                //EntityHelper.dropItem(this, Items.BONE, 2);
                EntityHelper.dropItem(this, Reg.ANIMAL_VISCERA);
                if (ent instanceof SheepEntity && Math.random() < 0.6)
                    EntityHelper.dropItem(this, Items.LEATHER);
            }
            if (this.isBaby())
                EntityHelper.dropItem(this, meat);
        } else if (ent instanceof AxolotlEntity || ent instanceof CatEntity || ent instanceof FrogEntity || ent instanceof ParrotEntity || ent instanceof SquidEntity)
            EntityHelper.dropItem(this, meat);
        else if (ent instanceof AnimalEntity && !(ent instanceof BeeEntity || ent instanceof TadpoleEntity || ent instanceof RabbitEntity)) {
            EntityHelper.dropItem(this, meat, (int) (Math.random() * 3) + 1);
            //EntityHelper.dropItem(this, Items.BONE, 2);
            EntityHelper.dropItem(this, Reg.ANIMAL_VISCERA);
        }
        //Also see at BatEntityMixin
    }

}
