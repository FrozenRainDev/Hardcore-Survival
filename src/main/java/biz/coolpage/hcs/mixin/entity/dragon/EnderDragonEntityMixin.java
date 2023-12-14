package biz.coolpage.hcs.mixin.entity.dragon;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.PhaseManager;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
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

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin extends MobEntity {
    protected EnderDragonEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    @Nullable
    public EndCrystalEntity connectedCrystal;

    @Unique
    private static boolean isDragonInSecondStage(LivingEntity entity) {
        if (entity instanceof EnderDragonEntity dragon) return dragon.getHealth() / dragon.getMaxHealth() < 0.25F;
        return false;
    }

    @Shadow
    public abstract boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source);

    @Shadow
    @Final
    private PhaseManager phaseManager;

    @Shadow
    @Final
    private static TargetPredicate CLOSE_PLAYER_PREDICATE;
    @Unique
    private PhaseType<?> lastType = null;

    @Unique
    public void addBuffWithoutChecking(StatusEffectInstance effect) {
        this.getActiveStatusEffects().put(effect.getEffectType(), effect);
    }

    @Unique
    private static void disableShield(List<Entity> entities) {
        applyNullable(entities, es -> es.forEach(e -> {
            if (e instanceof ServerPlayerEntity player && player.getActiveItem().isOf(Items.SHIELD))
                player.getItemCooldownManager().set(Items.SHIELD, 200);
        }));
    }

    @Inject(method = "crystalDestroyed", at = @At("HEAD"))
    public void crystalDestroyed(EndCrystalEntity endCrystal, BlockPos pos, DamageSource source, CallbackInfo ci) {
        EntityHelper.letEnderDragonChargeAtTheClosestPlayer(this);
    }

    @Inject(method = "damageLivingEntities", at = @At("HEAD"))
    private void damageLivingEntities(List<Entity> entities, CallbackInfo ci) {
        disableShield(entities);
    }

    @ModifyArg(method = "damageLivingEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), index = 1)
    private float damageLivingEntities(float amount) {
        return amount * 1.7F;
    }

    @ModifyArg(method = "tickWithEndCrystals", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Box;expand(D)Lnet/minecraft/util/math/Box;"), index = 0)
    private double tickWithEndCrystals(double value) {
        return 256.0;
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void tickMovement(CallbackInfo ci) {
        var type = this.phaseManager.getCurrent().getType();
        if (type != this.lastType) {
            this.lastType = type;
            System.out.println("dragon tasks phase updated: " + this.phaseManager.getCurrent().getType());
        }
        if (this.connectedCrystal == null) {
            boolean isInSecondStage = isDragonInSecondStage(this);
            int ampl = isInSecondStage ? 2 : 1;
            this.addBuffWithoutChecking(new StatusEffectInstance(StatusEffects.SPEED, 5, ampl, false, false, false));
            this.addBuffWithoutChecking(new StatusEffectInstance(StatusEffects.RESISTANCE, 5, ampl, false, false, false));
            if (isInSecondStage) {
                this.world.syncWorldEvent(WorldEvents.ELECTRICITY_SPARKS, this.getBlockPos(), 0);
                this.world.syncWorldEvent(WorldEvents.ELECTRICITY_SPARKS, this.getBlockPos().up(), 0);
                applyNullable(EntityHelper.getOthersEntitiesInRange(this, EndermanEntity.class),
                        entities -> {
                            var targetPlayer = this.world.getClosestPlayer(CLOSE_PLAYER_PREDICATE, this.getX(), this.getY(), this.getZ());
                            if (targetPlayer != null) {
                                entities.forEach(entity -> {
                                    if (Math.random() < 0.2) {
                                        this.world.syncWorldEvent(WorldEvents.ELECTRICITY_SPARKS, entity.getBlockPos(), 0);
                                        this.world.syncWorldEvent(WorldEvents.ELECTRICITY_SPARKS, entity.getBlockPos().up(), 0);
                                        entity.setTarget(targetPlayer);
                                    }
                                });
                            }
                        });
            }
        }
    }

    @Inject(method = "launchLivingEntities", at = @At("HEAD"))
    private void launchLivingEntities(List<Entity> entities, CallbackInfo ci) {
        disableShield(entities);
    }

    @ModifyArg(method = "launchLivingEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), index = 1)
    private float launchLivingEntities(float amount) {
        return amount * 2.5F;
    }
}
