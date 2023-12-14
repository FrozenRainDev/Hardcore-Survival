package biz.coolpage.hcs.mixin.entity.dragon;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonFireballEntity.class)
public abstract class DragonFireballEntityMixin extends ExplosiveProjectileEntity {
    protected DragonFireballEntityMixin(EntityType<? extends ExplosiveProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/DragonFireballEntity;discard()V"))
    protected void onCollision(HitResult hitResult, CallbackInfo ci) {
        if (this.getOwner() instanceof LivingEntity owner)
            this.world.createExplosion(this, this.world.getDamageSources().mobProjectile(this, owner), null, this.getPos(), 2.0F, false, World.ExplosionSourceType.MOB);
    }
}
