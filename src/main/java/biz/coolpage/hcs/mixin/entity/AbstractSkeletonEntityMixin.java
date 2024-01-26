package biz.coolpage.hcs.mixin.entity;

import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSkeletonEntity.class)
public class AbstractSkeletonEntityMixin {
    @Inject(method = "createAbstractSkeletonAttributes", at = @At("RETURN"), cancellable = true)
    private static void createAbstractSkeletonAttributes(@NotNull CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.setReturnValue(cir.getReturnValue().add(EntityAttributes.GENERIC_MAX_HEALTH, 4.0));
        //Improving speed can cause bugs
    }

    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/AbstractSkeletonEntity;createArrowProjectile(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;"), index = 1)
    private float injected(float damageModifier) {
        return damageModifier * 0.8F;
    }
}
