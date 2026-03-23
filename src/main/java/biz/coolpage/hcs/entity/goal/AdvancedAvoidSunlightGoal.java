package biz.coolpage.hcs.entity.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.monster.Zombie;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

public class AdvancedAvoidSunlightGoal extends FleeSunGoal {

    // 修改构造函数，传入 1.0D 作为默认速度修饰符
    public AdvancedAvoidSunlightGoal(PathfinderMob mob) {
        super(mob, 1.0D);
    }

    @Override
    public boolean canUse() {
        // 在 1.20.1 Mojang 映射中逻辑保持不变
        boolean isSunVulnerableZombie = mob instanceof Zombie zombie && zombie.isSunSensitive();
        boolean isSunVulnerableMob = applyNullable(mob, Mob::isSunBurnTick, false);

        // 调用 super.canUse() 会检查是否是白天、是否能看到天空、是否着火等
        return super.canUse() && (isSunVulnerableZombie || isSunVulnerableMob);
    }
}