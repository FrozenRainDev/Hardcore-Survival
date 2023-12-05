package biz.coolpage.hcs.entity.goal;

import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import org.jetbrains.annotations.NotNull;

public class SpiderEscapeDangerGoal extends EscapeDangerGoal {

    public SpiderEscapeDangerGoal(@NotNull PathAwareEntity mob) {
        super(mob, 1.15);
    }

    private int escapeCountdown = 150; //(0, 150]: can escape; [-150, 0]: revenge again temporary after escaping

    @Override
    public boolean canStart() {
        if (this.escapeCountdown <= 0) ++this.escapeCountdown;
        return super.canStart();
    }

    @Override
    protected boolean isInDanger() {
        return super.isInDanger() && (this.mob.getHealth() / this.mob.getMaxHealth()) < 0.6 && this.escapeCountdown > 0;
    }

    @Override
    public boolean shouldContinue() {
        if (this.mob.getNavigation().isIdle()) {
            this.findTarget();
            this.mob.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed);
        }
        return this.escapeCountdown > 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.escapeCountdown > 0) --this.escapeCountdown;
    }

    @Override
    public void stop() {
        super.stop();
        this.escapeCountdown = -150;
        this.mob.setTarget(this.mob.getAttacker());
    }
}