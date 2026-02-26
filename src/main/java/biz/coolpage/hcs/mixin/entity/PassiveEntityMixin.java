package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableMob.class)
@SuppressWarnings("ConstantValue")
public abstract class PassiveEntityMixin extends PathfinderMob {

    protected PassiveEntityMixin(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    protected void defineSynchedData(CallbackInfo ci) {
        if ((Object) this instanceof Cow) {
            //Set init val for milked time; Missing this leads to NullPointerException!!
            this.entityData.define(EntityHelper.MILKED_TIME, -24001L);
        }
    }
}
