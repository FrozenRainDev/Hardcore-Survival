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
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class BreakBlockGoal extends Goal {
    protected final Mob mob;
    private LivingEntity hcsLastAttacker;
    protected BlockPos breakPos = BlockPos.ZERO;
    protected BlockState breakState = Blocks.AIR.defaultBlockState();
    protected boolean shouldStop;
    protected int breakProgress = -1, prevBreakStage = -1;

    // Variables for the two-phase pathfinding and digging logic
    protected boolean isWalkingToBreak = false;
    protected int walkStuckTimer = 0;
    protected Vec3 lastPos = Vec3.ZERO;

    public BreakBlockGoal(Mob mob) {
        this.mob = mob;
        if (!GoalUtils.hasGroundPathNavigation(mob)) {
            throw new IllegalArgumentException("Unsupported mob type for BreakBlockGoal");
        }
        // Take over both movement and view controls simultaneously
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        Level world = this.mob.level();
        if (target == null || !world.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return false;
        if (world instanceof ServerLevel serverWorld && !Configs.isEnabled(serverWorld, Configs.HOSTILE_ZOMBIE)) return false;

        // Core optimization: Trigger local radar scanning only when the zombie finishes pathfinding or hits a wall, avoiding heavy CPU usage per tick
        if (!this.mob.getNavigation().isDone() && !this.mob.horizontalCollision) return false;

        int radius = 3; // Expand search radius to find breakthroughs within a 7x7 area
        List<BlockPos> candidates = new ArrayList<>();
        int[] yOffsets = {1, 0, 2, -1};

        for (int yOffset : yOffsets) {
            // Should not dig downward when not above target
            if (yOffset == -1 && this.mob.getY() <= target.getY()) continue;
            // Should not dig upward when not under target
            if (yOffset == 2 && this.mob.getY() >= target.getY()) continue;

            for (int xOffset = -radius; xOffset <= radius; xOffset++) {
                for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                    // Ignore the coordinate space occupied by the zombie itself
                    if (xOffset == 0 && zOffset == 0 && (yOffset == 0 || yOffset == 1)) continue;

                    BlockPos pendingPos = BlockPos.containing(this.mob.getX() + xOffset, this.mob.getY() + yOffset, this.mob.getZ() + zOffset);

                    // 2D dot product: Ensure the block is generally in the direction of the target
                    double vecXToTarget = target.getX() - this.mob.getX();
                    double vecZToTarget = target.getZ() - this.mob.getZ();
                    double vecXToBlock = pendingPos.getX() + 0.5D - this.mob.getX();
                    double vecZToBlock = pendingPos.getZ() + 0.5D - this.mob.getZ();
                    if (vecXToTarget * vecXToBlock + vecZToTarget * vecZToBlock < 0) continue;

                    BlockState pendingState = world.getBlockState(pendingPos);
                    // Avoid redundant destroying if the block has no collision shape
                    if (pendingState.getCollisionShape(world, pendingPos).isEmpty()) continue;

                    // Core fix: Raycast (Line of Sight) check to prevent mining through walls
                    Vec3 eyePos = this.mob.getEyePosition();
                    Vec3 blockCenter = Vec3.atCenterOf(pendingPos);
                    BlockHitResult hitResult = world.clip(new ClipContext(
                            eyePos,
                            blockCenter,
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            this.mob
                    ));

                    // If the ray hits a block before reaching the pending block, it means another block is blocking the way
                    if (hitResult.getType() == HitResult.Type.BLOCK && !hitResult.getBlockPos().equals(pendingPos)) {
                        continue;
                    }

                    if (canBreakBlock(pendingState)) {
                        candidates.add(pendingPos);
                    }
                }
            }
        }

        if (candidates.isEmpty()) return false;

        // Weight sorting: Score = distance to zombie + distance to target * 1.5
        // Prioritize blocks that can significantly reduce the distance to the target
        candidates.sort(Comparator.comparingDouble(pos -> {
            double distToZombie = pos.distToCenterSqr(this.mob.position());
            double distToTarget = pos.distToCenterSqr(target.position());
            return distToZombie + distToTarget * 1.5;
        }));

        // Try to pick the best valid candidate block
        for (int i = 0; i < Math.min(candidates.size(), 5); i++) {
            BlockPos candidate = candidates.get(i);

            // Scenario A: Block is right in front, enter digging phase immediately
            if (candidate.closerToCenterThan(this.mob.position(), 2.5)) {
                this.breakPos = candidate;
                this.breakState = world.getBlockState(candidate);
                this.isWalkingToBreak = false;
                return true;
            }

            // Scenario B: Block is further away, check if there's a valid path to reach it
            Path path = this.mob.getNavigation().createPath(candidate, 1);
            if (path != null) {
                this.breakPos = candidate;
                this.breakState = world.getBlockState(candidate);
                this.isWalkingToBreak = true; // Mark as Phase 1: Approaching
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        this.shouldStop = false;
        this.breakProgress = 0;
        this.walkStuckTimer = 0;
        this.lastPos = this.mob.position();

        if (this.isWalkingToBreak) {
            // Phase 1: Not yet reached the block, issue move command, keep look control free
            this.mob.getNavigation().moveTo(this.breakPos.getX(), this.breakPos.getY(), this.breakPos.getZ(), 1.0D);
        } else {
            // Phase 2: Block is right in front, stop moving, lock look control
            this.mob.getNavigation().stop();
            if (this.mob.getLookControl() instanceof ILookControl ext) {
                ext.hcs$setLookLock(true);
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        // Send block destruction progress packet only if the digging actually started
        if (!this.isWalkingToBreak) {
            this.mob.level().destroyBlockProgress(this.mob.getId(), this.breakPos, -1);
        }
        // Unlock the LookControl
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
        if (this.mob.getLastHurtByMob() != null) this.hcsLastAttacker = this.mob.getLastHurtByMob();
        if (this.hcsLastAttacker != null && !this.hcsLastAttacker.isAlive()) this.hcsLastAttacker = null;
        if (this.mob.level() instanceof ServerLevel serverWorld && !Configs.isEnabled(serverWorld, Configs.HOSTILE_ZOMBIE)) return false;

        // Fetch the real-time block state to prevent digging air when doors are opened or blocks are destroyed
        BlockState currentState = this.mob.level().getBlockState(this.breakPos);
        boolean isOpened = currentState.hasProperty(BlockStateProperties.OPEN) && currentState.getValue(BlockStateProperties.OPEN);
        if (isOpened || currentState.getCollisionShape(this.mob.level(), this.breakPos).isEmpty()) return false;

        boolean canContinue = !this.shouldStop
                && this.breakProgress <= this.getMaxProgress()
                && canBreakBlock(currentState)
                && (this.hcsLastAttacker == null || (this.mob.tickCount - this.mob.getLastHurtByMobTimestamp()) > 20);

        // Relax distance constraint if in pathfinding phase (allow up to 8 blocks away), otherwise strictly within 2.5 blocks
        double allowedDistance = this.isWalkingToBreak ? 8.0 : 2.5;
        canContinue = canContinue && this.breakPos.closerToCenterThan(this.mob.position(), allowedDistance);

        if (canContinue) {
            this.breakState = currentState;
        }
        return canContinue;
    }

    public int getMaxProgress() {
        return (int) (HcsDifficulty.chooseVal(this.mob.level(), 4000.0F, 2000.0F, 1000.0F) * this.breakState.getDestroySpeed(this.mob.level(), this.breakPos) * ((this.mob.getMainHandItem().getItem() instanceof ShovelItem) ? 0.2F : 1.0F));
    }

    public boolean canBreakBlock(@NotNull BlockState state) {
        if (state.isAir()) return false;
        if (state.is(BlockTags.WOODEN_DOORS)) return true;
        boolean stoneConstraint = state.getDestroySpeed(this.mob.level(), this.breakPos) < Blocks.STONE.defaultDestroyTime() || this.mob.getMainHandItem().getItem() instanceof PickaxeItem;
        return DigRestrictHelper.canBreak(this.mob.getMainHandItem().getItem(), state) && stoneConstraint;
    }

    @Override
    public void tick() {
        //        if (this.offsetX * (float) ((double) this.breakPos.getX() + 0.5 - this.mob.getX()) + this.offsetZ * (float) ((double) this.breakPos.getZ() + 0.5 - this.mob.getZ()) < 0.0f)
//            this.shouldStop = true; // digging pos too distant for mob
        // ========== Phase 1: Walking towards the target block ==========
        if (this.isWalkingToBreak) {
            if (this.breakPos.closerToCenterThan(this.mob.position(), 2.0)) {
                // Successfully reached the block, seamlessly switch to Phase 2 (digging)
                this.isWalkingToBreak = false;
                this.mob.getNavigation().stop();
                if (this.mob.getLookControl() instanceof ILookControl ext) {
                    ext.hcs$setLookLock(true);
                }
            } else {
                // Anti-stuck check: If the zombie is stuck in place for 10 ticks, terminate the goal
                this.walkStuckTimer++;
                if (this.walkStuckTimer % 10 == 0) {
                    if (this.mob.position().distanceToSqr(this.lastPos) < 0.05) {
                        this.shouldStop = true;
                    }
                    this.lastPos = this.mob.position();
                }
                return; // Return early, skipping the digging code below
            }
        }

        // ========== Phase 2: Digging the block in place ==========
        ++this.breakProgress;
        if (this.breakProgress % 40 == 0 && !this.mob.swinging) this.mob.swing(this.mob.getUsedItemHand());

        int breakStage = (int) ((float) this.breakProgress / (float) this.getMaxProgress() * 10.0f);
        if (breakStage != this.prevBreakStage) {
            this.mob.level().destroyBlockProgress(this.mob.getId(), this.breakPos, breakStage);
            this.mob.level().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, this.breakPos, Block.getId(this.breakState));
            this.prevBreakStage = breakStage;
        }

        // Keep the zombie's sight locked on the center of the block being mined using forced look
        if (this.mob.getLookControl() instanceof ILookControl ctrl) {
            ctrl.hcs$forceLookAt(
                    this.breakPos.getX() + 0.5D,
                    this.breakPos.getY() + 0.5D,
                    this.breakPos.getZ() + 0.5D,
                    10.0F, // Maximum horizontal head rotation speed
                    (float) this.mob.getMaxHeadXRot() // Maximum vertical head rotation speed
            );
        }

        // Successfully broke the block
        if (this.breakProgress >= this.getMaxProgress()) {
            this.mob.level().destroyBlock(this.breakPos, true, this.mob);
            WorldHelper.checkBlockGravity(this.mob.level(), this.breakPos);
        }
    }
}