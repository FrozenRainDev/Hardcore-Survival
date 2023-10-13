package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.SanityManager;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {
    @Shadow
    public abstract @Nullable LivingEntity getTarget();

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "squaredAttackRange", at = @At("RETURN"), cancellable = true)
    public void squaredAttackRange(LivingEntity target, @NotNull CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(cir.getReturnValue() + EntityHelper.getReachRangeAddition(this));
    }

    @Inject(method = "getXpToDrop", at = @At("RETURN"), cancellable = true)
    public void getXpToDrop(@NotNull CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Math.max(1, (int) ((double) cir.getReturnValue() / 3.0)));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        //noinspection RedundantCast
        if ((Object) this instanceof Monster && this.getTarget() instanceof PlayerEntity player && SanityManager.CAN_CLOSELY_SEE.test(player, this))
            ((StatAccessor) player).getSanityManager().addEnemy(this);
    }
}
