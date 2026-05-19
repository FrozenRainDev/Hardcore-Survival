package biz.coolpage.hcs.entity.goal;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.function.Predicate;

public class ChargingAtPlayerGoal<T extends Mob> extends Goal {
    protected final T mob;
    protected final Predicate<T> shouldRun;
    private int chargingCooldown = 0;

    public ChargingAtPlayerGoal(@NotNull T mob, @NotNull Predicate<T> prerequisite) {
        this.mob = mob;
        this.shouldRun = prerequisite.and(mb -> this.chargingCooldown <= 0);
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.chargingCooldown > 0) --this.chargingCooldown;
        return shouldRun.test(this.mob);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        var target = this.mob.getTarget();
        if (target != null) {
            if (this.mob.distanceTo(this.mob.getTarget()) < 3) {
                EntityHelper.kickedAndFly(this.mob, target, 9.0F);
            } else this.mob.setDeltaMovement(target.position().subtract(this.mob.position()).normalize().scale(0.5));
        }
    }

    @Override
    public void stop() {
        this.chargingCooldown = (int) (200 * this.mob.getHealth() / this.mob.getMaxHealth());
        super.stop();
    }

    @Override
    public boolean canContinueToUse() {
        return this.shouldRun.test(this.mob);
    }
}