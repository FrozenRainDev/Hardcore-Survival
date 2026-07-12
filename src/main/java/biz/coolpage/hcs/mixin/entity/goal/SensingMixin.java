package biz.coolpage.hcs.mixin.entity.goal;

import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.util.EntityHelper;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Sensing.class)
public class SensingMixin {
    @Shadow @Final private Mob mob;

    @ModifyReturnValue(method = "hasLineOfSight", at = @At("RETURN"))
    private boolean hcsurvival$grantWallhackToZombies(boolean original, Entity entity) {
        // If the original raycast already returned true (can see), no need to calculate distance
        if (original) {
            return true;
        }
        if (this.mob instanceof Zombie) {
            double sensingRange = EntityHelper.ZOMBIE_AND_SKELETON_SENSING_RANGE * HcsDifficulty.chooseVal(this.mob.level(), 1.0F, 1.0F, 2.0F);            // Grant "smell" capability through walls if within the custom range
            //noinspection RedundantIfStatement
            if (this.mob.distanceTo(entity) <= sensingRange) {
                return true;
            }
        }
        return false;
    }
}