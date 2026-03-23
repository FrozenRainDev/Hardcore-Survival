package biz.coolpage.hcs.entity.goal;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

import static biz.coolpage.hcs.util.EntityHelper.toPlayer;

public class StaringRevengeGoal extends NearestAttackableTargetGoal<Player> {
    // See at EndermanEntity$TeleportTowardsPlayerGoal
    private final Mob mob;
    @Nullable
    private Player targetPlayer;
    private int provokeWarmup = 0, lookAtPlayerWarmup;
    private final TargetingConditions staringPlayerPredicate;
    private final TargetingConditions validTargetPredicate = TargetingConditions.forCombat().ignoreLineOfSight();
    private final Predicate<LivingEntity> angerPredicate;

    public StaringRevengeGoal(Mob mob, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(mob, Player.class, 10, true, false, targetPredicate);
        if (!(mob instanceof NeutralMob))
            throw new RuntimeException(this.getClass().getSimpleName() + ": mob is not Angerable!");
        this.mob = mob;
        // 在 Mojang 映射中，isIndirectPassenger 对应的逻辑通常是检查是否为间接乘客，1.20.1 中 Mob/Entity 使用 hasIndirectPassenger
        this.angerPredicate = player -> (EntityHelper.isPlayerStaring(mob, toPlayer(player)) || ((NeutralMob) mob).isAngryAt(player)) && !mob.hasIndirectPassenger(player);
        this.staringPlayerPredicate = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(this.angerPredicate);
    }

    @Override
    public boolean canUse() {
        this.targetPlayer = this.mob.level().getNearestPlayer(this.staringPlayerPredicate, this.mob);
        if (this.targetPlayer != null) {
            ++this.provokeWarmup;
            if (this.provokeWarmup > 3) {
                this.provokeWarmup = 0;
                return true;
            }
        } else if (this.provokeWarmup > 0) --this.provokeWarmup;
        return false;
    }

    @Override
    public void start() {
        this.lookAtPlayerWarmup = this.adjustedTickDelay(15);
        this.mob.setTarget(this.targetPlayer);
    }

    @Override
    public void stop() {
        super.stop();
        this.provokeWarmup = 0;
        this.targetPlayer = null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetPlayer != null) {
            if (!this.angerPredicate.test(this.targetPlayer)) return false;
            this.mob.getLookControl().setLookAt(this.targetPlayer, 10.0f, 10.0f);
            return true;
        }
        if (this.target != null) {
            if (this.mob.hasIndirectPassenger(this.target)) return false;
            if (this.validTargetPredicate.test(this.mob, this.target)) return true;
        }
        return super.canContinueToUse();
    }

    @Override
    public void tick() {
        if (this.mob.getTarget() == null) super.setTarget(null);
        if (this.targetPlayer != null) {
            if (this.targetPlayer.isAlive()) {
                if (--this.lookAtPlayerWarmup <= 0) {
                    this.target = this.targetPlayer;
                    this.targetPlayer = null;
                    super.start();
                }
            } else this.stop();
        } else super.tick();
    }
}