package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(AnimalEntity.class)
@SuppressWarnings("ConstantValue")
public abstract class AnimalEntityMixin extends PassiveEntity {
    //See damage mixin in LivingEntityMixin
    @Unique
    private static String MILKED_NBT = "hcs_milked";

    protected AnimalEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "getXpToDrop", at = @At("HEAD"), cancellable = true)
    public void getXpToDrop(@NotNull CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if ((Object) this instanceof CowEntity) {
            if (nbt.contains(MILKED_NBT, NbtElement.LONG_TYPE))
                this.dataTracker.set(EntityHelper.MILKED_TIME, nbt.getLong(MILKED_NBT));
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(@NotNull NbtCompound nbt, CallbackInfo ci) {
        if ((Object) this instanceof CowEntity) {
            nbt.putLong(MILKED_NBT, this.dataTracker.get(EntityHelper.MILKED_TIME));
        }
    }
}
