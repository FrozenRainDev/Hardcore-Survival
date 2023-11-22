package biz.coolpage.hcs.entity.goal;

import net.minecraft.entity.ai.goal.AvoidSunlightGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.ZombieEntity;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

public class AdvancedAvoidSunlightGoal extends AvoidSunlightGoal {

    public AdvancedAvoidSunlightGoal(PathAwareEntity mob) {
        super(mob);
    }

    @Override
    public boolean canStart() {
        boolean isSunVulnerableZombie = mob instanceof ZombieEntity zombie && zombie.burnsInDaylight();
        boolean isSunVulnerableMob = applyNullable(mob, MobEntity::isAffectedByDaylight, false);
        return super.canStart() && (isSunVulnerableZombie || isSunVulnerableMob);
    }
}
