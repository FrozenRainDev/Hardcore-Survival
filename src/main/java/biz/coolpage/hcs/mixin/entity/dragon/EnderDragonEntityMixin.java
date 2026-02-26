package biz.coolpage.hcs.mixin.entity.dragon;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

@Mixin(EnderDragon.class)
public abstract class EnderDragonEntityMixin extends Mob {
    protected EnderDragonEntityMixin(EntityType<? extends Mob> entityType, Level world) {
        super(entityType, world);
    }

    @Shadow
    @Nullable
    public EndCrystal nearestCrystal;

    @Unique
    private static boolean isDragonInSecondStage(LivingEntity entity) {
        if (entity instanceof EnderDragon dragon) return dragon.getHealth() / dragon.getMaxHealth() < 0.2F;
        return false;
    }

    @Shadow
    public abstract boolean addEffect(MobEffectInstance effect, @Nullable Entity source);

    @Shadow
    @Final
    private static TargetingConditions NEAR_PLAYER_TARGETING;

    @Unique
    public void addBuffWithoutChecking(MobEffectInstance effect) {
        this.getActiveEffectsMap().put(effect.getEffect(), effect);
    }

    @Unique
    private static void disableShield(List<Entity> entities) {
        applyNullable(entities, es -> es.forEach(e -> {
            if (e instanceof ServerPlayer player && player.getUseItem().is(Items.SHIELD))
                player.getCooldowns().addCooldown(Items.SHIELD, 200);
        }));
    }

    @Inject(method = "onCrystalDestroyed", at = @At("HEAD"))
    public void onCrystalDestroyed(EndCrystal endCrystal, BlockPos pos, DamageSource source, CallbackInfo ci) {
        EntityHelper.letEnderDragonChargeAtTheClosestPlayer(this);
    }

    @Inject(method = "knockBack", at = @At("HEAD"))
    private void knockBack(List<Entity> entities, CallbackInfo ci) {
        disableShield(entities);
    }

    @ModifyArg(method = "knockBack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), index = 1)
    private float knockBackDamage(float amount) {
        return amount * 1.7F;
    }

    @ModifyArg(method = "checkCrystals", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;"), index = 0)
    private double checkCrystals(double value) {
        return 256.0;
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    public void aiStep(CallbackInfo ci) {
        if (this.nearestCrystal == null) {
            boolean isInSecondStage = isDragonInSecondStage(this);
            this.addBuffWithoutChecking(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 5, isInSecondStage ? 2 : 1, false, false, false));
            this.addBuffWithoutChecking(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 5, 1, false, false, false));
            if (isInSecondStage) {
                for (int i = 0; i < 4; ++i)
                    this.level().levelEvent(LevelEvent.PARTICLES_ELECTRIC_SPARK, this.blockPosition().above(i), 0);
                applyNullable(EntityHelper.getOthersEntitiesInRange(this, Enderman.class, 8.0),
                        entities -> {
                            var targetPlayer = this.level().getNearestPlayer(NEAR_PLAYER_TARGETING, this.getX(), this.getY(), this.getZ());
                            if (targetPlayer != null) {
                                entities.forEach(entity -> {
                                    if (entity.distanceTo(this) < 10) entity.setTarget(targetPlayer);
                                    if (entity.getTarget() instanceof Player)
                                        this.level().levelEvent(LevelEvent.PARTICLES_ELECTRIC_SPARK, entity.blockPosition().above(1), 0);
                                });
                            }
                        });
            }
        }
    }

    @Inject(method = "bite", at = @At("HEAD"))
    private void bite(List<Entity> entities, CallbackInfo ci) {
        disableShield(entities);
    }

    @ModifyArg(method = "bite", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), index = 1)
    private float biteDamage(float amount) {
        return amount * 2.5F;
    }
}
