package biz.coolpage.hcs.entity.goal;

import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.status.accessor.ILookControl;
import biz.coolpage.hcs.util.DigRestrictHelper;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class ZombieBreakBlockGoal extends Goal {
    protected final Mob mob;
    private LivingEntity hcsLastAttacker;
    protected BlockPos breakPos = BlockPos.ZERO;
    protected BlockState breakState = Blocks.AIR.defaultBlockState();
    protected boolean shouldStop;
    //    private float offsetX, offsetZ;
    protected int breakProgress = -1, prevBreakStage = -1;

    public ZombieBreakBlockGoal(Mob mob) {
        this.mob = mob;
        if (!GoalUtils.hasGroundPathNavigation(mob)) {
            throw new IllegalArgumentException("Unsupported mob type for BreakBlockGoal");
        }
        // Take over both movement and view controls simultaneously
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Choose a block to break
        LivingEntity target = this.mob.getTarget();
        Level world = this.mob.level();
        if (target == null || !this.mob.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))
            return false;
        if (world instanceof ServerLevel serverWorld && !Configs.isEnabled(serverWorld, Configs.HOSTILE_ZOMBIE))
            return false;

        // Set Y-axis traversal priority: 1 (upper body/facing height) -> 0 (sole of foot) -> 2 (top of head) -> -1 (below feet)
        int[] yOffsets = {1, 0, 2, -1};

        for (int yOffset : yOffsets) {
            // Should not dig downward when not above target
            if (yOffset == -1 && this.mob.getY() <= target.getY()) continue;
            // Should not dig upward when not under target
            if (yOffset == 2 && this.mob.getY() >= target.getY()) continue;

            for (int xOffset = -1; xOffset <= 1; xOffset++) {
                for (int zOffset = -1; zOffset <= 1; zOffset++) {
                    // Check only the cardinal directions (forward, backward, left, right), skipping diagonal blocks.
                    if (Math.abs(xOffset) + Math.abs(zOffset) > 1) continue;

                    // Ignore the coordinate space occupied by the zombie itself (0, 0, 0) and (0, 1, 0)
                    if (xOffset == 0 && zOffset == 0 && (yOffset == 0 || yOffset == 1)) continue;

                    // Choose a pos to break
                    BlockPos pendingBreakPos = BlockPos.containing(this.mob.getX() + xOffset, this.mob.getY() + yOffset, this.mob.getZ() + zOffset);

                    // Using 2D dot product (cosine): if < -0.2, the block is largely in the opposite direction of the target
                    if (!this.canBreakByAngleJudge(target, pendingBreakPos, 0.2D)) {
                        continue;
                    }

                    BlockState pendingBreakState = this.mob.level().getBlockState(pendingBreakPos);
                    // Avoid redundant destroying if the block has no collision shape (can be walked through)
                    if (pendingBreakState.getCollisionShape(this.mob.level(), pendingBreakPos).isEmpty()) continue;

                    // Determine whether to start
                    // FIX: Added `this.mob.horizontalCollision` so the zombie starts mining immediately upon hitting a wall,
                    // instead of waiting to slide to the absolute closest point to the player.
                    if (canBreakBlock(pendingBreakState) && (this.mob.getNavigation().isDone() || this.mob.horizontalCollision)) {
//                        System.out.println("Breaking block " + pendingBreakPos);
                        this.breakPos = pendingBreakPos;
                        this.breakState = pendingBreakState;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void start() {
        this.shouldStop = false;
        this.breakProgress = 0;

        // Core Fix 1: Completely halt pathfinding all at once when mining starts, instead of repeatedly calling it during ticks, resolving intermittent walking/jerking issues
        this.mob.getNavigation().stop();

        // Lock the LookControl when digging starts
        if (this.mob.getLookControl() instanceof ILookControl ext) {
            ext.hcs$setLookLock(true);
        }
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.level().destroyBlockProgress(this.mob.getId(), this.breakPos, -1);
        // Unlock the LookControl when digging is interrupted or finished
        if (this.mob.getLookControl() instanceof ILookControl ext) {
            ext.hcs$setLookLock(false);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
//         System.out.println("..."); // 省略你的调试代码
        if (this.mob.getLastHurtByMob() != null) this.hcsLastAttacker = this.mob.getLastHurtByMob();
        if (this.hcsLastAttacker != null && !this.hcsLastAttacker.isAlive()) this.hcsLastAttacker = null;
        if (this.mob.level() instanceof ServerLevel serverWorld && !Configs.isEnabled(serverWorld, Configs.HOSTILE_ZOMBIE))
            return false;

        // Core Fix 2: Retrieve the real-time block state of the current world to prevent zombies from continuing to dig at empty air after a door is opened or a block is destroyed
        BlockState currentState = this.mob.level().getBlockState(this.breakPos);

        // Check whether the door/hatch has been opened by a player, or whether the block collision volume is empty
        boolean isOpened = currentState.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.OPEN)
                && currentState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.OPEN);
        if (isOpened || currentState.getCollisionShape(this.mob.level(), this.breakPos).isEmpty()) {
            return false;
        }

        // Disabled, otherwise mining may be interrupted when directly above or below.
        // Core Fix 3: Normalize vectors and relax the angle check.
        // If the player moves slightly laterally, the zombie will no longer abruptly abandon mining.
//        boolean isTargetStillValid = false;
//        LivingEntity target = this.mob.getTarget();
//        if (target != null && target.isAlive()) {
//            // As long as the angle is not excessively wide (e.g., >= -0.5 allows approx 120 degrees tolerance), keep mining
//            isTargetStillValid = this.canBreakByAngleJudge(target, this.breakPos, -0.5D);
//        }

        boolean canContinue = !this.shouldStop
                && this.breakProgress <= this.getMaxProgress()
                && canBreakBlock(currentState) // Pass in real-time status
//                && isTargetStillValid          // Is the player still behind the block / not fully around the back
                && this.breakPos.closerToCenterThan(this.mob.position(), 3) // Relax distance limit (2.5 is too easily interrupted)
                && (this.hcsLastAttacker == null || (this.mob.tickCount - this.mob.getLastHurtByMobTimestamp()) > 20);

        // Synchronize the latest state for use in tick() (e.g., update mining particles and time-consuming calculations)
        if (canContinue) {
            this.breakState = currentState;
        }

        return canContinue;
    }

    public int getMaxProgress() {
        float toolAcceleration = 1.0F;
        Item item = this.mob.getMainHandItem().getItem();
        if (item instanceof ShovelItem) toolAcceleration = 0.2F;
        else if (item instanceof TieredItem) toolAcceleration = 0.4F;
        return (int) (HcsDifficulty.chooseVal(this.mob.level(), 2400.0F, 1200.0F, 600.0F)
                * this.breakState.getDestroySpeed(this.mob.level(), this.breakPos)
                * toolAcceleration);
    }

    public boolean canBreakBlock(@NotNull BlockState state) {
        if (state.isAir()) return false;
        if (state.is(BlockTags.WOODEN_DOORS)) return true;
        boolean stoneConstraint = state.getDestroySpeed(this.mob.level(), this.breakPos) < Blocks.STONE.defaultDestroyTime() || this.mob.getMainHandItem().getItem() instanceof PickaxeItem;
        return DigRestrictHelper.canBreak(this.mob.getMainHandItem().getItem(), state) && stoneConstraint;
    }

    @Override
    public void tick() {
        ++this.breakProgress;
//        if (this.offsetX * (float) ((double) this.breakPos.getX() + 0.5 - this.mob.getX()) + this.offsetZ * (float) ((double) this.breakPos.getZ() + 0.5 - this.mob.getZ()) < 0.0f)
//            this.shouldStop = true; // digging pos too distant for mob
        if (this.breakProgress % 40 == 0 && !this.mob.swinging) this.mob.swing(this.mob.getUsedItemHand());
        int breakStage = (int) ((float) this.breakProgress / (float) this.getMaxProgress() * 10.0f);
        if (breakStage != this.prevBreakStage) {
            this.mob.level().destroyBlockProgress(this.mob.getId(), this.breakPos, breakStage);
            this.mob.level().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, this.breakPos, Block.getId(this.breakState));
            this.prevBreakStage = breakStage;
        }

        // （已删除）此处移除了原有在 tick 里对 this.mob.getNavigation().stop() 的调用

        // Use forced look to bypass our own mixin lock
        if (this.mob.getLookControl() instanceof ILookControl ctrl) {
            ctrl.hcs$forceLookAt(
                    this.breakPos.getX() + 0.5D,
                    this.breakPos.getY() + 0.5D,
                    this.breakPos.getZ() + 0.5D,
                    10.0F, // Maximum horizontal head rotation speed
                    (float) this.mob.getMaxHeadXRot() // Maximum vertical head rotation speed
            );
        }

        // Reach the target, successfully break the block
        if (this.breakProgress >= this.getMaxProgress()) {
            this.mob.level().destroyBlock(this.breakPos, true, this.mob);
            WorldHelper.checkBlockGravity(this.mob.level(), this.breakPos);
        }
    }

    /**
     * Determines whether the angle between the target and the block allows mining.
     * Extracts dot product logic for reusability.
     */
    private boolean canBreakByAngleJudge(@NotNull LivingEntity target, @NotNull BlockPos blockPos, double threshold) {
        double vecXToTarget = target.getX() - this.mob.getX();
        double vecZToTarget = target.getZ() - this.mob.getZ();
        double vecXToBlock = blockPos.getX() + 0.5D - this.mob.getX();
        double vecZToBlock = blockPos.getZ() + 0.5D - this.mob.getZ();

        // Normalize vectors to accurately calculate the cosine of the angle and relax the strict angle limit
        double targetDist = Math.sqrt(vecXToTarget * vecXToTarget + vecZToTarget * vecZToTarget);
        if (targetDist > 0.001) {
            vecXToTarget /= targetDist;
            vecZToTarget /= targetDist;
        }
        double blockDist = Math.sqrt(vecXToBlock * vecXToBlock + vecZToBlock * vecZToBlock);
        if (blockDist > 0.001) {
            vecXToBlock /= blockDist;
            vecZToBlock /= blockDist;
        }

        // Using 2D dot product (cosine)
        return (vecXToTarget * vecXToBlock + vecZToTarget * vecZToBlock) >= threshold;
    }
}