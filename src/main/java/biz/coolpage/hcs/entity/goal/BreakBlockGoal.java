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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
        if (world instanceof ServerLevel serverWorld && !Configs.isEnabled(serverWorld, Configs.HOSTILE_ZOMBIE))
            return false;

        // 只有当僵尸当前没有有效路径或撞墙时，才触发扫描
        if (!this.mob.getNavigation().isDone() && !this.mob.horizontalCollision) return false;

        int radius = 3;
        List<BlockPos> candidates = new ArrayList<>();
        List<BlockState> states = new ArrayList<>();
        int[] yOffsets = {1, 0, 2, -1};

        // 1. 收集范围内所有方向正确的候选方块（移除射线检测，允许收集拐角后的方块）
        for (int yOffset : yOffsets) {
            if (yOffset == -1 && this.mob.getY() <= target.getY()) continue;
            if (yOffset == 2 && this.mob.getY() >= target.getY()) continue;

            for (int xOffset = -radius; xOffset <= radius; xOffset++) {
                for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                    if (xOffset == 0 && zOffset == 0 && (yOffset == 0 || yOffset == 1)) continue;

                    BlockPos pendingPos = BlockPos.containing(this.mob.getX() + xOffset, this.mob.getY() + yOffset, this.mob.getZ() + zOffset);

                    double vecXToTarget = target.getX() - this.mob.getX();
                    double vecZToTarget = target.getZ() - this.mob.getZ();
                    double vecXToBlock = pendingPos.getX() + 0.5D - this.mob.getX();
                    double vecZToBlock = pendingPos.getZ() + 0.5D - this.mob.getZ();
                    if (vecXToTarget * vecXToBlock + vecZToTarget * vecZToBlock < 0) continue;

                    BlockState pendingState = world.getBlockState(pendingPos);
                    if (pendingState.getCollisionShape(world, pendingPos).isEmpty()) continue;

                    if (canBreakBlock(pendingState)) {
                        candidates.add(pendingPos);
                        states.add(pendingState);
                    }
                }
            }
        }

        System.out.println("candidates are: " + Arrays.toString(candidates.toArray()));
        System.out.println("states are: " + Arrays.toString(states.toArray()));
        if (candidates.isEmpty()) return false;


        // 2. 权重排序（离僵尸越近、离玩家越近的方块越优先）
        candidates.sort(Comparator.comparingDouble(pos -> pos.distToCenterSqr(target.position())));

        // 3. 核心修复：基于路径终点的“连通性（无阻挡）”检测
        for (int i = 0; i < Math.min(candidates.size(), 5); i++) {
            BlockPos candidate = candidates.get(i);

            // 尝试生成到该方块的路径
            net.minecraft.world.level.pathfinder.Path path = this.mob.getNavigation().createPath(candidate, 1);
            boolean isAccessible = false;

            if (path != null && path.getNodeCount() > 0) {
                // 获取路径能到达的最后一个物理节点
                BlockPos endPos = path.getNode(path.getNodeCount() - 1).asBlockPos();

                // 计算终点与候选方块的三轴距离
                int dx = Math.abs(endPos.getX() - candidate.getX());
                int dy = Math.abs(endPos.getY() - candidate.getY());
                int dz = Math.abs(endPos.getZ() - candidate.getZ());

                // 如果终点紧挨着方块（水平距离<=1），说明对于僵尸的路径来说周围没有阻挡！
                if (dx <= 1 && dy <= 2 && dz <= 1) {
                    isAccessible = true;
                }
            }

            // 兜底逻辑：如果僵尸由于距离太近导致寻路引擎返回 null，直接判断它自身的物理位置是否挨着方块
            int mdx = Math.abs(this.mob.blockPosition().getX() - candidate.getX());
            int mdy = Math.abs(this.mob.blockPosition().getY() - candidate.getY());
            int mdz = Math.abs(this.mob.blockPosition().getZ() - candidate.getZ());
            if (mdx <= 1 && mdy <= 2 && mdz <= 1) {
                isAccessible = true;
            }

            // 只要方块是可及的，就选定它
            if (isAccessible) {
                this.breakPos = candidate;
                this.breakState = world.getBlockState(candidate);

                // 如果已经在此方块的攻击范围内，直接进入挖掘阶段；否则进入寻路接近阶段
                if (candidate.closerToCenterThan(this.mob.position(), 2.5)) {
                    this.isWalkingToBreak = false;
                } else {
                    this.isWalkingToBreak = true;
                }
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
        if (this.mob.level() instanceof ServerLevel serverWorld && !Configs.isEnabled(serverWorld, Configs.HOSTILE_ZOMBIE))
            return false;

        // Fetch the real-time block state to prevent digging air when doors are opened or blocks are destroyed
        BlockState currentState = this.mob.level().getBlockState(this.breakPos);
        boolean isOpened = currentState.hasProperty(BlockStateProperties.OPEN) && currentState.getValue(BlockStateProperties.OPEN);
        if (isOpened || currentState.getCollisionShape(this.mob.level(), this.breakPos).isEmpty()) return false;

        boolean canContinue = !this.shouldStop
                && this.breakProgress <= this.getMaxProgress()
                && canBreakBlock(currentState)
                && (this.hcsLastAttacker == null || (this.mob.tickCount - this.mob.getLastHurtByMobTimestamp()) > 20);

        // Fix 1: Relax distance constraint in Phase 2 to 3.5 blocks.
        // This prevents the goal from aborting if the collision engine pushes the zombie slightly away from the block.
        double allowedDistance = this.isWalkingToBreak ? 8.0 : 3.5;
        canContinue = canContinue && this.breakPos.closerToCenterThan(this.mob.position(), allowedDistance);

        if (canContinue) {
            this.breakState = currentState;
        }
        return canContinue;
    }

    public int getMaxProgress() {
        int progress = (int) (HcsDifficulty.chooseVal(this.mob.level(), 4000.0F, 2000.0F, 1000.0F) * this.breakState.getDestroySpeed(this.mob.level(), this.breakPos) * ((this.mob.getMainHandItem().getItem() instanceof ShovelItem) ? 0.2F : 1.0F));
        // Fix 2: Ensure it takes at least 20 ticks (1 second) to mine any block.
        // Prevents an infinite loop of instant-breaking failing due to event cancellations.
        return Math.max(progress, 20);
    }

    public boolean canBreakBlock(@NotNull BlockState state) {
        if (state.isAir()) return false;
        if (state.is(BlockTags.WOODEN_DOORS)) return true;
        boolean stoneConstraint = state.getDestroySpeed(this.mob.level(), this.breakPos) < Blocks.STONE.defaultDestroyTime() || this.mob.getMainHandItem().getItem() instanceof PickaxeItem;
        return DigRestrictHelper.canBreak(this.mob.getMainHandItem().getItem(), state) && stoneConstraint;
    }

    @Override
    public void tick() {
        // ========== Phase 1: Walking towards the target block ==========
        if (this.isWalkingToBreak) {
            System.out.println(this.breakPos + " " + this.breakPos.closerToCenterThan(this.mob.position(), 2.5) + " timer " + this.walkStuckTimer);
            // Fix 3: Change the distance threshold to 2.5 to match canUse() and eliminate the dead zone
            if (this.breakPos.closerToCenterThan(this.mob.position(), 2.5)) {
                // Successfully reached the block, seamlessly switch to Phase 2 (digging)
                this.isWalkingToBreak = false;
                this.mob.getNavigation().stop();
                if (this.mob.getLookControl() instanceof ILookControl ext) {
                    ext.hcs$setLookLock(true);
                }
            } else if (this.mob.getNavigation().isDone()) {
                // The navigation has stopped (path finished), but the zombie is STILL outside the 2.5 reach.
                this.shouldStop = true;
                return;
            } else {
                // Anti-stuck check: every 40 ticks
                this.walkStuckTimer++;
                if (this.walkStuckTimer % 40 == 0) {
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
                    10.0F,
                    (float) this.mob.getMaxHeadXRot()
            );
        }

        // Successfully broke the block
        if (this.breakProgress >= this.getMaxProgress()) {
            this.mob.level().destroyBlock(this.breakPos, true, this.mob);
            WorldHelper.checkBlockGravity(this.mob.level(), this.breakPos);
        }
    }
}