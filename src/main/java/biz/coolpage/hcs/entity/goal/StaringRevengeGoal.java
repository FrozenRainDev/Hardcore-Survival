package biz.coolpage.hcs.entity.goal;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

import static biz.coolpage.hcs.util.EntityHelper.toPlayer;

public class StaringRevengeGoal extends ActiveTargetGoal<PlayerEntity> {
    // See at EndermanEntity$TeleportTowardsPlayerGoal
    private final MobEntity mob;
    @Nullable
    private PlayerEntity targetPlayer;
    private int provokeWarmup = 0, lookAtPlayerWarmup;
    private final TargetPredicate staringPlayerPredicate;
    private final TargetPredicate validTargetPredicate = TargetPredicate.createAttackable().ignoreVisibility();
    private final Predicate<LivingEntity> angerPredicate;

    public StaringRevengeGoal(MobEntity mob, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(mob, PlayerEntity.class, 10, true, false, targetPredicate);
        if (!(mob instanceof Angerable))
            throw new RuntimeException(this.getClass().getSimpleName() + ": mob is not Angerable!");
        this.mob = mob;
        this.angerPredicate = player -> (EntityHelper.isPlayerStaring(mob, toPlayer(player)) || ((Angerable) mob).shouldAngerAt(player)) && !mob.hasPassengerDeep(player);
        this.staringPlayerPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(this.getFollowRange()).setPredicate(this.angerPredicate);
    }

    @Override
    public boolean canStart() {
        this.targetPlayer = this.mob.world.getClosestPlayer(this.staringPlayerPredicate, this.mob);
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
        this.lookAtPlayerWarmup = this.getTickCount(15);
        this.mob.setTarget(this.targetPlayer);
    }

    @Override
    public void stop() {
        super.stop();
        this.provokeWarmup = 0;
        this.targetPlayer = null;
    }

    @Override
    public boolean shouldContinue() {
        if (this.targetPlayer != null) {
            if (!this.angerPredicate.test(this.targetPlayer)) return false;
            this.mob.lookAtEntity(this.targetPlayer, 10.0f, 10.0f);
            return true;
        }
        if (this.targetEntity != null) {
            if (this.mob.hasPassengerDeep(this.targetEntity)) return false;
            if (this.validTargetPredicate.test(this.mob, this.targetEntity)) return true;
        }
        return super.shouldContinue();
    }

    @Override
    public void tick() {
        if (this.mob.getTarget() == null) super.setTargetEntity(null);
        if (this.targetPlayer != null) {
            if (this.targetPlayer.isAlive()) {
                if (--this.lookAtPlayerWarmup <= 0) {
                    this.targetEntity = this.targetPlayer;
                    this.targetPlayer = null;
                    super.start();
                }
            } else this.stop();
        } else super.tick();
    }
}
