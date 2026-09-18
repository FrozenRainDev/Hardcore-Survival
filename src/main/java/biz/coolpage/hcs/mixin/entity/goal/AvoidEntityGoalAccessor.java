package biz.coolpage.hcs.mixin.entity.goal;

import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor mixin replacing the former {@code AvoidEntityGoal f_25024_} and {@code AvoidEntityGoal f_25025_}
 * access transformer entries.
 */
@Mixin(AvoidEntityGoal.class)
public interface AvoidEntityGoalAccessor {
    /**
     * Getter for the private final {@code TargetingConditions} field {@code avoidEntityTargeting}.
     *
     * @return the targeting conditions used to look for entities to avoid
     */
    @Accessor("avoidEntityTargeting")
    TargetingConditions getAvoidEntityTargeting();

    /**
     * Getter for the private final double field {@code sprintSpeedModifier}.
     *
     * @return the navigation speed modifier applied while fleeing at a distance
     */
    @Accessor("sprintSpeedModifier")
    double getSprintSpeedModifier();
}
