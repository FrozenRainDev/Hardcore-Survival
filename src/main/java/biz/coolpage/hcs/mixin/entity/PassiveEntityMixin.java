package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PassiveEntity.class)
@SuppressWarnings("ConstantValue")
public abstract class PassiveEntityMixin extends PathAwareEntity {

    protected PassiveEntityMixin(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initDataTracker(CallbackInfo ci) {
        if ((Object) this instanceof CowEntity) {
            //Set init val for milked time; Missing this leads to NullPointerException!!
            this.dataTracker.startTracking(EntityHelper.MILKED_TIME, -24001L);
        }
    }
}
