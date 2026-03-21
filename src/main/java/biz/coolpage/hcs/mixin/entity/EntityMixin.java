package biz.coolpage.hcs.mixin.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.tags.DamageTypeTags;
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
        if (ent instanceof WitherSkeleton) // Wither skeletons are immune to wither boss
            cir.setReturnValue(cir.getReturnValueZ() || damageSource.getEntity() instanceof WitherBoss);
        else if (ent instanceof EnderMan) // Endermen are immune to ender dragon boss
            cir.setReturnValue(cir.getReturnValueZ() || damageSource.getEntity() instanceof EnderDragon);
        else if (ent instanceof EnderDragon || ent instanceof WitherBoss) // Bosses are immune to explosion damage and the damage caused by any boss
            cir.setReturnValue(cir.getReturnValueZ() || damageSource.getEntity() instanceof EnderDragon || damageSource.getEntity() instanceof WitherBoss || damageSource.is(DamageTypeTags.IS_EXPLOSION));
    }
}
