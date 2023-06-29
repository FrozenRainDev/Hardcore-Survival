package com.hcs.mixin.entity;

import com.hcs.Reg;
import com.hcs.util.EntityHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpiderEntity.class)
public abstract class SpiderEntityMixin extends HostileEntity {
    protected SpiderEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "getDeathSound", at = @At("HEAD"))
    protected void getDeathSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (Math.random() < 0.4) EntityHelper.dropItem(this, Reg.SPIDER_GLAND.getDefaultStack());
    }
}
