package biz.coolpage.hcs.entity.goal;

import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.mob.PathAwareEntity;

public class SpiderEscapeDangerGoal extends EscapeDangerGoal {

    public SpiderEscapeDangerGoal(PathAwareEntity mob) {
        super(mob, 1.0);
    }

    private int escapeCountdown = 200; //(0, 200]: can escape; [-200, 0]: revenge again temporary after escaping

    @Override
    public boolean canStart() {
//        System.out.println(escapeCountdown);
        if (this.escapeCountdown <= 0) ++this.escapeCountdown;
        return super.canStart();
    }

    @Override
    protected boolean isInDanger() {
        return super.isInDanger() && (this.mob.getHealth() / this.mob.getMaxHealth()) < 0.5 && this.escapeCountdown > 0;
    }

    @Override
    public boolean shouldContinue() {
        return super.shouldContinue() && this.escapeCountdown > 0;
    }

    @Override
    public void tick() {
//        System.out.println(escapeCountdown);
        super.tick();
        if (this.escapeCountdown > 0) --this.escapeCountdown;
    }

    @Override
    public void stop() {
        super.stop();
        this.escapeCountdown = -200;
    }
}