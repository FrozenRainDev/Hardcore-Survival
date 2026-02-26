package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.SanityManager;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
@SuppressWarnings("ConstantValue")
public abstract class MobEntityMixin extends LivingEntity {
    @Shadow
    public abstract @Nullable LivingEntity getTarget();

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "getMeleeAttackRangeSqr", at = @At("RETURN"), cancellable = true)
    public void getMeleeAttackRangeSqr(LivingEntity target, @NotNull CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(cir.getReturnValue() + EntityHelper.getReachRangeAddition(this));
    }

    @Inject(method = "getExperienceReward", at = @At("RETURN"), cancellable = true)
    public void getExperienceReward(@NotNull CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Math.max(1, (int) ((double) cir.getReturnValue() / 3.0)));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        Object ent = this;
        if (ent instanceof Enemy && this.getTarget() instanceof Player player && SanityManager.CAN_CLOSELY_SEE.test(player, this)) {
            if (!(ent instanceof Slime slime && slime.isTiny()))
                ((StatAccessor) player).getSanityManager().addEnemy(this);
        }
    }

    @ModifyArg(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), index = 1)
    public float doHurtTarget(float amount) {
        //noinspection ConstantValue
        if ((Object) this instanceof Zombie zombie && zombie.isBaby()) return amount / 2.0F;
        return amount;
    }
}
