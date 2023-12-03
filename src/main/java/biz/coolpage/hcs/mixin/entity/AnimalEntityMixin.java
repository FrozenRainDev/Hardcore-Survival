package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.status.accessor.IAnimalEntity;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;


@Mixin(AnimalEntity.class)
@SuppressWarnings({"AddedMixinMembersNamePattern", "ConstantValue"})
public abstract class AnimalEntityMixin extends PassiveEntity implements IAnimalEntity {
    protected AnimalEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    private int panicInfectedTicks = 0;

    @Unique
    @Override
    public boolean hasInfectedPanic() {
        return this.panicInfectedTicks > 0;
    }

    @Unique
    @Override
    public void doPanicInfection() {
        this.panicInfectedTicks = 1200;
    }

    //From UniversalAngerGoal/getOthersInRange()
    @Unique
    private List<? extends MobEntity> getOthersAnimalsInRange(@NotNull AnimalEntity animal) {
        Box box = Box.from(animal.getPos()).expand(EntityHelper.ZOMBIE_SENSING_RANGE, 10.0, EntityHelper.ZOMBIE_SENSING_RANGE);
        return animal.world.getEntitiesByClass(AnimalEntity.class, box, EntityPredicates.EXCEPT_SPECTATOR);
    }


    @Inject(method = "getXpToDrop", at = @At("HEAD"), cancellable = true)
    public void getXpToDrop(@NotNull CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }

    @Inject(method = "damage", at = @At("HEAD"))
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Object victim = this;
        if (source != null && source.getAttacker() instanceof LivingEntity) {
            if (victim instanceof AnimalEntity animal) {
                getOthersAnimalsInRange(animal).stream().filter(entity -> entity != animal).forEach(entity -> ((IAnimalEntity) entity).doPanicInfection());
                if (victim instanceof ChickenEntity) EntityHelper.dropItem(this, Items.FEATHER, 1);
            }
        }
    }

    @Inject(method = "mobTick", at = @At("HEAD"))
    protected void mobTick(CallbackInfo ci) {
        if (this.hasInfectedPanic()) --this.panicInfectedTicks;
    }
}
