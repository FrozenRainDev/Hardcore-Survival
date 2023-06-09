package com.hcs.mixin.entity;

import com.hcs.Reg;
import com.hcs.util.EntityHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BatEntity.class)
public abstract class BatEntityMixin extends AmbientEntity {
    protected BatEntityMixin(EntityType<? extends AmbientEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "getDeathSound")
    protected void getDeathSound(CallbackInfoReturnable<SoundEvent> cir) {
        //On death
        EntityHelper.dropItem(this, this.getFireTicks() > 0 ? Reg.COOKED_MEAT : Reg.RAW_MEAT, 1);
    }
}
