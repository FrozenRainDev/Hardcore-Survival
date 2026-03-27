package biz.coolpage.hcs.mixin.entity.goal;

import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Sensing.class)
public class MobVisibilityCacheMixin {
    @Shadow @Final private Mob mob;
    @Inject(at = @At("HEAD"), method = "hasLineOfSight", cancellable = true)
    public void hasLineOfSight(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        // 在 Sensing 类中，this.mob 指向拥有该感官缓存的生物
        if (this.mob instanceof Zombie zombie) {
            // 计算僵尸的 X-ray 感知范围
            double sensingRange = EntityHelper.ZOMBIE_SENSING_RANGE / HcsDifficulty.chooseVal(zombie.level(), 4.0F, 2.0F, 1.0F);

            if (zombie.distanceTo(entity) <= sensingRange) {
                cir.setReturnValue(true);
            }
        }
    }
}