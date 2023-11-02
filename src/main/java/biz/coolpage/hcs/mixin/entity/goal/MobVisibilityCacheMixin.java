package biz.coolpage.hcs.mixin.entity.goal;

import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.MobVisibilityCache;
import net.minecraft.entity.mob.ZombieEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobVisibilityCache.class)
public class MobVisibilityCacheMixin {
    @Final
    @Mutable
    @Shadow
    private final MobEntity owner;

    public MobVisibilityCacheMixin(MobEntity owner) {
        this.owner = owner;
    }

    @Inject(at = @At("HEAD"), method = "canSee", cancellable = true)
    public void canSee(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        //Zombie-like mobs can gain an x-ray sight
        if (this.owner instanceof ZombieEntity && this.owner.distanceTo(entity) <= (EntityHelper.ZOMBIE_SENSING_RANGE / HcsDifficulty.chooseVal(this.owner.world, 4.0F, 2.0F, 1.0F)))
            cir.setReturnValue(true);
    }
}
