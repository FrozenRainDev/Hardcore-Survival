package com.hcs.mixin.entity;

import com.hcs.main.Reg;
import com.hcs.main.helper.EntityHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.*;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin extends PassiveEntity {

    protected AnimalEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "damage", at = @At("HEAD"))
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Integer> cir) {
        Object obj = this;
        if (obj instanceof ChickenEntity && source.toString().contains("EntityDamageSource"))
            EntityHelper.dropItem(this, Items.FEATHER, 1);
        //On death
        if ((this.getHealth() - amount) <= 0.0F && this.getHealth() > 0.0F) {
            if (obj instanceof ChickenEntity || obj instanceof CowEntity || obj instanceof PigEntity || obj instanceof SheepEntity) {
                if (!(obj instanceof ChickenEntity)) {
                    //EntityHelper.dropItem(this, Items.BONE, 2);
                    EntityHelper.dropItem(this, Reg.ANIMAL_VISCERA, 1);
                    if (obj instanceof SheepEntity && Math.random() < 0.6)
                        EntityHelper.dropItem(this, Items.LEATHER, 1);
                }
                if (this.isBaby())
                    EntityHelper.dropItem(this, this.getFireTicks() > 0 ? Reg.COOKED_MEAT : Reg.RAW_MEAT, 1);
            } else if (obj instanceof AxolotlEntity || obj instanceof CatEntity || obj instanceof FrogEntity || obj instanceof ParrotEntity || obj instanceof RabbitEntity)
                EntityHelper.dropItem(this, this.getFireTicks() > 0 ? Reg.COOKED_MEAT : Reg.RAW_MEAT, 1);
            else if (!(obj instanceof BeeEntity || obj instanceof TadpoleEntity)) {
                EntityHelper.dropItem(this, this.getFireTicks() > 0 ? Reg.COOKED_MEAT : Reg.RAW_MEAT, (int) (Math.random() * 3) + 1);
                //EntityHelper.dropItem(this, Items.BONE, 2);
                EntityHelper.dropItem(this, Reg.ANIMAL_VISCERA, 1);
            }
            //Also see at BatEntityMixin
        }
    }

    @Inject(method = "getXpToDrop", at = @At("HEAD"), cancellable = true)
    public void getXpToDrop(@NotNull CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }
}
