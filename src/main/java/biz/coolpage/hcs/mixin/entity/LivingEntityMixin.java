package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.Hcs;
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
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
@SuppressWarnings({"ConstantValue", "AddedMixinMembersNamePattern"})
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract boolean isBaby();

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    // Yarn getNextAirOnLand = Mojang increaseAirSupply
    @Inject(method = "increaseAirSupply", at = @At("HEAD"), cancellable = true)
    protected void increaseAirSupply(int pCurrentAir, @NotNull CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof Player player) {
            int lvl = ((StatAccessor) player).getOxygenManager().getFinalOxygenLackLevel();
            if (lvl > 0 && !player.hasEffect(MobEffects.WATER_BREATHING)) {
                // Original logic: Increase based on level on top of the current air recovery
                int recovery = (lvl == 1) ? 1 : 0;
                cir.setReturnValue(Math.min(pCurrentAir + recovery, this.getMaxAirSupply()));
            }
        }
    }

    @Inject(method = "hurt", at = @At("TAIL"))
    public void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Object victim = this;
        if (source != null && source.getEntity() instanceof LivingEntity attacker) {
            if (victim instanceof Player player && attacker instanceof Monster)
                ((StatAccessor) player).getSanityManager().add(attacker instanceof EnderMan ? -0.08 : -0.005);
            if (victim instanceof Animal animal) {
                EntityHelper.getOthersEntitiesInRange(animal, Animal.class, 1.0).stream()
                        .filter(entity -> entity != null && entity != animal)
                        .forEach(entity -> entity.setLastHurtByMob(animal.getLastHurtByMob()));
                if (victim instanceof Animal) {
                    if (victim instanceof Chicken) EntityHelper.dropItem(this, Items.FEATHER, 1);
                }
            }
        }
    }

    @Inject(method = "die", at = @At("TAIL"))
    public void die(@NotNull DamageSource source, CallbackInfo ci) {
        if (EntityHelper.SHOULD_DROP_AFTER_DEATH.test(this, source)) return;
        Object ent = this;
        Item meat = this.getRemainingFireTicks() > 0 ? Hcs.COOKED_MEAT.get() : Hcs.RAW_MEAT.get();
        if (ent instanceof Chicken || ent instanceof Cow || ent instanceof Pig || ent instanceof Sheep) {
            if (!(ent instanceof Chicken)) {
                //EntityHelper.dropItem(this, Items.BONE, 2);
                EntityHelper.dropItem(this, Hcs.ANIMAL_VISCERA.get());
                if (ent instanceof Sheep && Math.random() < 0.3) EntityHelper.dropItem(this, Items.LEATHER);
            }
            if (this.isBaby()) EntityHelper.dropItem(this, meat);
        } else if (ent instanceof Axolotl || ent instanceof Cat || ent instanceof Frog || ent instanceof Parrot || ent instanceof Squid)
            EntityHelper.dropItem(this, meat);
        else if (ent instanceof Animal && !(ent instanceof Bee || ent instanceof Tadpole || ent instanceof Rabbit || ent instanceof ZombieHorse || ent instanceof SkeletonHorse)) {
            if (ent instanceof AbstractHorse)
                EntityHelper.dropItem(this, this.getRemainingFireTicks() > 0 ? Items.COOKED_BEEF : Items.BEEF, (int) (Math.random() * 3) + 1);
            else EntityHelper.dropItem(this, meat, (int) (Math.random() * 3) + 1);
            // EntityHelper.dropItem(this, Items.BONE, 2);
            EntityHelper.dropItem(this, Hcs.ANIMAL_VISCERA.get());
        } else if (ent instanceof Spider && Math.random() < 0.33) EntityHelper.dropItem(this, Hcs.SPIDER_GLAND.get());
        else if (ent instanceof Bat)
            EntityHelper.dropItem(this, this.getRemainingFireTicks() > 0 ? Hcs.ROASTED_BAT_WINGS.get() : Hcs.BAT_WINGS.get());
        else if (ent instanceof WitherBoss) {
            EntityHelper.dropItem(this, Items.NETHERITE_INGOT, 2 + (int) (Math.random() * 2));
            EntityHelper.dropItem(this, Items.DIAMOND, 12 + (int) (Math.random() * 6));
        } else if (ent instanceof EnderDragon) {
            EntityHelper.dropItem(this, Items.NETHERITE_INGOT, 6 + (int) (Math.random() * 6));
            EntityHelper.dropItem(this, Items.DIAMOND, 32 + (int) (Math.random() * 32));
            EntityHelper.dropItem(this, Items.ENCHANTED_GOLDEN_APPLE, 20 + (int) (Math.random() * 20));
        }
    }

    @Inject(method = "dropFromLootTable", at = @At("HEAD"), cancellable = true)
    protected void dropFromLootTable(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if (EntityHelper.SHOULD_DROP_AFTER_DEATH.test(this, source)) ci.cancel();
    }

    @Inject(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(FFF)F"), cancellable = true)
    protected void getDamageAfterArmorAbsorb(DamageSource source, float amount, @NotNull CallbackInfoReturnable<Float> cir) {
        if (((Object) this) instanceof Player player)
            cir.setReturnValue(ArmorHelper.getDamageLeft(player, amount));
    }

}