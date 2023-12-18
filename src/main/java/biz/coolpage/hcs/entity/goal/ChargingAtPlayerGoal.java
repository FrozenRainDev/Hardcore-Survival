package biz.coolpage.hcs.entity.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.function.Predicate;

public class ChargingAtPlayerGoal<T extends MobEntity> extends Goal {
    protected final T mob;
    protected final Predicate<T> shouldRun;
    private int chargingCooldown = 0;

    public ChargingAtPlayerGoal(@NotNull T mob, @NotNull Predicate<T> prerequisite) {
        this.mob = mob;
        this.shouldRun = prerequisite.and(mb -> this.chargingCooldown <= 0);
        this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (this.chargingCooldown > 0) --this.chargingCooldown;
        return shouldRun.test(this.mob);
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        var target = this.mob.getTarget();
        if (target != null) {
            if (this.mob.distanceTo(this.mob.getTarget()) < 3) {
                CowKickRevengeGoal.flyOut(this.mob, target, 9.0F);
            } else this.mob.setVelocity(target.getPos().subtract(this.mob.getPos()).normalize().multiply(0.5));
        }
    }

    @Override
    public void stop() {
        this.chargingCooldown = (int) (200 * this.mob.getHealth() / this.mob.getMaxHealth());
        super.stop();
    }

    @Override
    public boolean shouldContinue() {
        return this.shouldRun.test(this.mob);
    }

}
