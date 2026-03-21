package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Animal.class)
@SuppressWarnings("ConstantValue")
public abstract class AnimalMixin extends AgeableMob { // AnimalEntityMixin
    //See damage mixin in LivingEntityMixin
    @Unique
    private static String MILKED_NBT = "hcs_milked";

    protected AnimalMixin(EntityType<? extends AgeableMob> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "getExperienceReward", at = @At("HEAD"), cancellable = true)
    public void getExperienceReward(@NotNull CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
        if ((Object) this instanceof Cow) {
            if (nbt.contains(MILKED_NBT, Tag.TAG_LONG))
                this.entityData.set(EntityHelper.MILKED_TIME, nbt.getLong(MILKED_NBT));
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addAdditionalSaveData(@NotNull CompoundTag nbt, CallbackInfo ci) {
        if ((Object) this instanceof Cow) {
            nbt.putLong(MILKED_NBT, this.entityData.get(EntityHelper.MILKED_TIME));
        }
    }
}
