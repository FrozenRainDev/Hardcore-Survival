package biz.coolpage.hcs.entity.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class CowKickRevengeGoal extends Goal {
    //Reference: PounceAtTargetGoal
    private final MobEntity mob;
    private LivingEntity attacker;
    private int kickCooldown = 0;

    public CowKickRevengeGoal(MobEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (this.kickCooldown > 0) --this.kickCooldown;
        this.attacker = this.mob.getAttacker();
        return this.attacker != null && this.mob.distanceTo(this.attacker) < 3.0;
    }

    @Override
    public boolean shouldContinue() {
        return this.kickCooldown <= 0;
    }

    @Override
    public void start() {
        if (this.attacker == null) return;
        Vec3d vec1 = this.attacker.getVelocity();
        Vec3d vec2 = new Vec3d(this.attacker.getX() - this.mob.getX(), 0.0, this.attacker.getZ() - this.mob.getZ());
        if (vec2.lengthSquared() > 1.0E-7) vec2 = vec2.normalize().multiply(0.8).add(vec1.multiply(0.2));
        this.attacker.damage(this.attacker.world.getDamageSources().mobAttack(this.mob), 5.0F);
        this.attacker.setVelocity(vec2.x, 0.5, vec2.z);
        this.kickCooldown = 200;
    }
}