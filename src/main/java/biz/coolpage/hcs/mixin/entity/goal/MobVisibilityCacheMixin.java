package biz.coolpage.hcs.mixin.entity.goal;

import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Note: MobVisibilityCache does not exist in Mojang/Forge mapping.
// The closest equivalent is handled differently. This mixin targets the NearestVisibleLivingEntitiesSensor
// or the mob's canSee/hasLineOfSight logic. For now we keep the structure as a placeholder.
// The actual Forge equivalent would need to target a different class.
// TODO: Verify correct target class in Forge 1.20.1
@Mixin(Mob.class)
public class MobVisibilityCacheMixin {
    @Inject(at = @At("HEAD"), method = "hasLineOfSight", cancellable = true)
    public void hasLineOfSight(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        Object owner = this;
        //Zombie-like mobs can gain an x-ray sight
        if (owner instanceof Zombie zombie && zombie.distanceTo(entity) <= (EntityHelper.ZOMBIE_SENSING_RANGE / HcsDifficulty.chooseVal(zombie.level(), 4.0F, 2.0F, 1.0F)))
            cir.setReturnValue(true);
    }
}
