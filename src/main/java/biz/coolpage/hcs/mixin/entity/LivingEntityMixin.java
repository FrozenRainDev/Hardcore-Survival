package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.status.accessor.ILivingEntity;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.util.ArmorHelper;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ILivingEntity {
    @Shadow
    private int lastHurtByMobTimestamp;

    @Shadow
    public abstract boolean isBaby();

    @Shadow
    @Nullable
    public abstract LivingEntity getLastHurtByMob();

    @Unique
    private LivingEntity hcsLastAttacker = null;

    @Unique
    @Override
    public LivingEntity getHcsLastAttacker() {
        return this.hcsLastAttacker;
    }

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "baseTick", at = @At("HEAD"))
    public void baseTick(CallbackInfo ci) {
        if (this.getLastHurtByMob() != null) this.hcsLastAttacker = this.getLastHurtByMob();
        if (this.hcsLastAttacker != null && !this.hcsLastAttacker.isAlive()) this.hcsLastAttacker = null;
        if ((Object) this instanceof Animal animal
                && this.hcsLastAttacker != null && animal.distanceTo(this.hcsLastAttacker) < 48)
            ++this.lastHurtByMobTimestamp;
    }

    @Inject(method = "increaseAirSupply", at = @At("RETURN"), cancellable = true)
    protected void increaseAirSupply(int air, @NotNull CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof Player player) {
            int lvl = ((StatAccessor) player).getOxygenManager().getFinalOxygenLackLevel();
            if (lvl > 0 && !player.hasEffect(MobEffects.WATER_BREATHING)) {
                cir.setReturnValue(air + ((air + 1 >= this.getMaxAirSupply()) ? 0 : (lvl == 1 ? 1 : 0)));
            }
        }
    }

    @Inject(method = "hurt", at = @At("TAIL"))
    public void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Object victim = this;
        if (source != null && source.getEntity() instanceof LivingEntity attacker) {
            if (victim instanceof Player player && attacker instanceof Enemy)
                ((StatAccessor) player).getSanityManager().add(attacker instanceof Enderman ? -0.08 : -0.005);
            if (victim instanceof Animal animal) {
                EntityHelper.getOthersEntitiesInRange(animal, Animal.class, 1.0).stream()
                        .filter(entity -> entity != null && entity != animal)
                        .forEach(entity -> {
                            entity.setLastHurtByMob(animal.getLastHurtByMob());
                            entity.lastHurtByMobTimestamp = entity.tickCount;
                        });
                if (victim instanceof Chicken) EntityHelper.dropItem(this, Items.FEATHER, 1);
            }
        }
    }

    @Inject(method = "die", at = @At("TAIL"))
    public void die(@NotNull DamageSource source, CallbackInfo ci) {
        if (EntityHelper.SHOULD_DROP_AFTER_DEATH.test(this, source)) return;
        Object ent = this;
        Item meat = this.getRemainingFireTicks() > 0 ? Reg.COOKED_MEAT : Reg.RAW_MEAT;
        if (ent instanceof Chicken || ent instanceof Cow || ent instanceof Pig || ent instanceof Sheep) {
            if (!(ent instanceof Chicken)) {
                EntityHelper.dropItem(this, Reg.ANIMAL_VISCERA);
                if (ent instanceof Sheep && Math.random() < 0.3) EntityHelper.dropItem(this, Items.LEATHER);
            }
            if (this.isBaby()) EntityHelper.dropItem(this, meat);
        } else if (ent instanceof Axolotl || ent instanceof Cat || ent instanceof Frog || ent instanceof Parrot || ent instanceof Squid)
            EntityHelper.dropItem(this, meat);
        else if (ent instanceof Animal && !(ent instanceof Bee || ent instanceof Tadpole || ent instanceof Rabbit || ent instanceof ZombieHorse || ent instanceof SkeletonHorse)) {
            if (ent instanceof AbstractHorse)
                EntityHelper.dropItem(this, this.getRemainingFireTicks() > 0 ? Items.COOKED_BEEF : Items.BEEF, (int) (Math.random() * 3) + 1);
            else EntityHelper.dropItem(this, meat, (int) (Math.random() * 3) + 1);
            EntityHelper.dropItem(this, Reg.ANIMAL_VISCERA);
        } else if (ent instanceof Spider && Math.random() < 0.33) EntityHelper.dropItem(this, Reg.SPIDER_GLAND);
        else if (ent instanceof Bat)
            EntityHelper.dropItem(this, this.getRemainingFireTicks() > 0 ? Reg.ROASTED_BAT_WINGS : Reg.BAT_WINGS);
        else if (ent instanceof WitherBoss) {
            EntityHelper.dropItem(this, Items.NETHERITE_INGOT, 2 + (int) (Math.random() * 2));
            EntityHelper.dropItem(this, Items.DIAMOND, 12 + (int) (Math.random() * 6));
        } else if (ent instanceof EnderDragon) {
            EntityHelper.dropItem(this, Items.NETHERITE_INGOT, 6 + (int) (Math.random() * 6));
            EntityHelper.dropItem(this, Items.DIAMOND, 32 + (int) (Math.random() * 32));
            EntityHelper.dropItem(this, Items.ENCHANTED_GOLDEN_APPLE, 20 + (int) (Math.random() * 20));
        }
    }

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
    protected void dropAllDeathLoot(DamageSource source, CallbackInfo ci) {
        if (EntityHelper.SHOULD_DROP_AFTER_DEATH.test(this, source)) ci.cancel();
    }

    @Inject(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/DamageUtil;getDamageLeft(FFF)F"), cancellable = true)
    protected void getDamageAfterArmorAbsorb(DamageSource source, float amount, @NotNull CallbackInfoReturnable<Float> cir) {
        if (((Object) this) instanceof Player player)
            cir.setReturnValue(ArmorHelper.getDamageLeft(player, amount));
    }
}