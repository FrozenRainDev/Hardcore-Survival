package biz.coolpage.hcs.entity.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import org.jetbrains.annotations.NotNull;

public class SpiderEscapeDangerGoal extends PanicGoal {

    public SpiderEscapeDangerGoal(@NotNull PathfinderMob mob) {
        super(mob, 1.15);
    }

    private int escapeCountdown = 150; //(0, 150]: can escape; [-150, 0]: revenge again temporary after escaping

    @Override
    public boolean canUse() {
        if (this.escapeCountdown <= 0) ++this.escapeCountdown;
        return super.canUse();
    }

    @Override
    protected boolean shouldPanic() {
        return super.shouldPanic() && (this.mob.getHealth() / this.mob.getMaxHealth()) < 0.6 && this.escapeCountdown > 0;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.getNavigation().isDone()) {
            if (this.findRandomPosition()) {
                this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
            }
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
        this.mob.setTarget(this.mob.getLastAttacker());
    }
}