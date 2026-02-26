package biz.coolpage.hcs.mixin.entity.dragon;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.DragonFireball;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonFireball.class)
public abstract class DragonFireballEntityMixin extends AbstractHurtingProjectile {
    protected DragonFireballEntityMixin(EntityType<? extends AbstractHurtingProjectile> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/DragonFireball;discard()V"))
    protected void onHit(HitResult hitResult, CallbackInfo ci) {
        if (this.getOwner() instanceof LivingEntity owner)
            this.level().explode(this, this.level().damageSources().mobProjectile(this, owner), null, this.position(), 3.0F, false, Level.ExplosionInteraction.MOB);
    }
}
