package biz.coolpage.hcs.entity.goal;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static biz.coolpage.hcs.util.EntityHelper.toPlayer;

public class WolfStareGoal extends NearestAttackableTargetGoal<Player> {
    private final Wolf wolf;
    @Nullable
    private Player targetPlayer;
    private int provokeWarmup = 0;
    private int lookAtPlayerWarmup;
    private final TargetingConditions staringPlayerPredicate;
    private final TargetingConditions validTargetPredicate = TargetingConditions.forCombat().ignoreLineOfSight();
    private final Predicate<LivingEntity> angerPredicate;

    public WolfStareGoal(Wolf wolf) {
        super(wolf, Player.class, 10, true, false, null);
        this.wolf = wolf;

        this.angerPredicate = entity -> {
            Player player = toPlayer(entity);
            if (player == null) return false;

            // Do not process if the wolf is tamed
            if (this.wolf.isTame()) return false;

            // Check if player is staring using Enderman logic or if wolf is already angry
            boolean isStaring = EntityHelper.isPlayerStaring(this.wolf, player);
            boolean isAngry = this.wolf.isAngryAt(player);

            if (!isStaring && !isAngry) return false;
            if (this.wolf.hasIndirectPassenger(player)) return false;

            // If already angry, bypass the item/pup checks
            if (isAngry) return true;

            // Otherwise, evaluate specific wolf conditions (pups, alone, meat/bone)
            return shouldAnger(player);
        };

        this.staringPlayerPredicate = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(this.angerPredicate);
    }

    private boolean shouldAnger(Player player) {
        boolean hasBone = player.getMainHandItem().is(Items.BONE)
                || player.getOffhandItem().is(Items.BONE);

        // Ensure item is edible and food properties are not null before calling isMeat()
        boolean hasMeat = (player.getMainHandItem().getItem().isEdible() && player.getMainHandItem().getItem().getFoodProperties() != null && player.getMainHandItem().getItem().getFoodProperties().isMeat())
                || (player.getOffhandItem().getItem().isEdible() && player.getOffhandItem().getItem().getFoodProperties() != null && player.getOffhandItem().getItem().getFoodProperties().isMeat());

        boolean hasPups = false;
        boolean isAlone = true;

        // Check surrounding area for other wolves (16 blocks radius based on 32.0D inflate)
        AABB boundingBox = this.wolf.getBoundingBox().inflate(32.0D, 16.0D, 32.0D);
        List<Wolf> nearbyWolves = this.wolf.level().getEntitiesOfClass(Wolf.class, boundingBox);

        for (Wolf nearbyWolf : nearbyWolves) {
            if (nearbyWolf != this.wolf) {
                isAlone = false;
                if (nearbyWolf.isBaby()) {
                    hasPups = true;
                    break;
                }
            }
        }

        // Condition to anger:
        // 1. Has pups around (inevitably angered)
        // 2. Not alone AND player is not holding a bone AND player is not holding meat
        return hasPups || (!isAlone && !hasBone && !hasMeat);
    }

    @Override
    public boolean canUse() {
        this.targetPlayer = this.wolf.level().getNearestPlayer(this.staringPlayerPredicate, this.wolf);
        if (this.targetPlayer != null) {
            ++this.provokeWarmup;
            // 3 ticks provocation warmup like Enderman
            if (this.provokeWarmup > 3) {
                this.provokeWarmup = 0;
                return true;
            }
        } else if (this.provokeWarmup > 0) {
            --this.provokeWarmup;
        }
        return false;
    }

    @Override
    public void start() {
        // 15 ticks staring warmup before attacking
        this.lookAtPlayerWarmup = this.adjustedTickDelay(15);
        this.wolf.setTarget(this.targetPlayer);
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
            // Wolf stares back at the player
            this.wolf.getLookControl().setLookAt(this.targetPlayer, 10.0f, 10.0f);
            return true;
        }
        if (this.target != null) {
            if (this.wolf.hasIndirectPassenger(this.target)) return false;
            if (this.validTargetPredicate.test(this.wolf, this.target)) return true;
        }
        return super.canContinueToUse();
    }

    @Override
    public void tick() {
        if (this.wolf.getTarget() == null) {
            super.setTarget(null);
        }
        if (this.targetPlayer != null) {
            if (this.targetPlayer.isAlive()) {
                if (--this.lookAtPlayerWarmup <= 0) {
                    this.target = this.targetPlayer;
                    this.targetPlayer = null;

                    // Start persistent anger timer just like vanilla aggressive mobs
                    this.wolf.setPersistentAngerTarget(this.target.getUUID());
                    this.wolf.startPersistentAngerTimer();

                    super.start();
                }
            } else {
                this.stop();
            }
        } else {
            super.tick();
        }
    }
}