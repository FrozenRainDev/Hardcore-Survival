package biz.coolpage.hcs.mixin.entity.dragon;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonFireball.class)
public abstract class DragonFireballEntityMixin extends AbstractHurtingProjectile {
    protected DragonFireballEntityMixin(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @SuppressWarnings("resource")
    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/DragonFireball;discard()V"))
    protected void onHit(HitResult hitResult, CallbackInfo ci) {
        if (this.getOwner() instanceof LivingEntity owner) {
            // 在 Mojang 映射中，getWorld() 变为 level()，getPos() 通常展开为坐标，createExplosion 变为 explode
            // ExplosionSourceType 变为 Level.ExplosionInteraction
            this.level().explode(this, this.level().damageSources().mobProjectile(this, owner), null, this.getX(), this.getY(), this.getZ(), 3.0F, false, Level.ExplosionInteraction.MOB);
        }
    }
}