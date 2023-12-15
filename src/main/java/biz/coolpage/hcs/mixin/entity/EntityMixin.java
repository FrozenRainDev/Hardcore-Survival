package biz.coolpage.hcs.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
@SuppressWarnings("ConstantValue")
public class EntityMixin {
    @Inject(method = "isInvulnerableTo", at = @At("RETURN"), cancellable = true)
    public void isInvulnerableTo(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        Object ent = this;
        if (ent instanceof WitherSkeletonEntity) // Wither skeletons are immune to wither boss
            cir.setReturnValue(cir.getReturnValueZ() || damageSource.getAttacker() instanceof WitherEntity);
        else if (ent instanceof EndermanEntity) // Endermen are immune to ender dragon boss
            cir.setReturnValue(cir.getReturnValueZ() || damageSource.getAttacker() instanceof EnderDragonEntity);
        else if (ent instanceof EnderDragonEntity || ent instanceof WitherEntity) // Bosses are immune to explosion damage and the damage caused by any boss
            cir.setReturnValue(cir.getReturnValueZ() || damageSource.getAttacker() instanceof EnderDragonEntity || damageSource.getAttacker() instanceof WitherEntity || damageSource.isIn(DamageTypeTags.IS_EXPLOSION));
    }
}
