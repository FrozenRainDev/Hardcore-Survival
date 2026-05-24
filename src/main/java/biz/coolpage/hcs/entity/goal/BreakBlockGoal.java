package biz.coolpage.hcs.entity.goal;

import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.util.DigRestrictHelper;
import biz.coolpage.hcs.util.EntityHelper;
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
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet; // Added import for EnumSet

public class BreakBlockGoal extends Goal {
    protected final Mob mob;
    private LivingEntity hcsLastAttacker;
    protected BlockPos breakPos = BlockPos.ZERO;
    protected BlockState breakState = Blocks.AIR.defaultBlockState();
    protected boolean shouldStop;
    //    private float offsetX, offsetZ;
    protected int breakProgress = -1, prevBreakStage = -1;

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
        // Choose a block to break
        LivingEntity target = this.mob.getTarget();
        Level world = this.mob.level();
        if (target == null || !this.mob.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))
            return false;
        if (world instanceof ServerLevel serverWorld && !Configs.isEnabled(serverWorld, Configs.HOSTILE_ZOMBIE))
            return false;

        // 设置 Y 轴遍历优先级：1 (上半身/面对高度) -> 0 (脚底) -> 2 (头顶) -> -1 (脚下)
        int[] yOffsets = {1, 0, 2, -1};

        for (int yOffset : yOffsets) {
            // Should not dig downward when not above target
            if (yOffset == -1 && this.mob.getY() <= target.getY()) continue;
            // Should not dig upward when not under target
            if (yOffset == 2 && this.mob.getY() >= target.getY()) continue;

            for (int xOffset = -1; xOffset <= 1; xOffset++) {
                for (int zOffset = -1; zOffset <= 1; zOffset++) {
                    // 仅检查十字方向（前后左右），跳过对角线方块。这与你之前硬编码的数组行为保持一致
                    if (Math.abs(xOffset) + Math.abs(zOffset) > 1) continue;

                    // 忽略僵尸自身所在的坐标空间 (0, 0, 0) 和 (0, 1, 0)
                    if (xOffset == 0 && zOffset == 0 && (yOffset == 0 || yOffset == 1)) continue;

                    // Choose a pos to break
                    BlockPos pendingBreakPos = BlockPos.containing(this.mob.getX() + xOffset, this.mob.getY() + yOffset, this.mob.getZ() + zOffset);

                    // Cannot mine blocks that are in the direction opposite to the player's approach
                    double vecXToTarget = target.getX() - this.mob.getX();
                    double vecZToTarget = target.getZ() - this.mob.getZ();
                    double vecXToBlock = pendingBreakPos.getX() + 0.5D - this.mob.getX();
                    double vecZToBlock = pendingBreakPos.getZ() + 0.5D - this.mob.getZ();

                    // Using 2D dot product(cosine): if < 0, the block is in the opposite direction of the target
                    if (vecXToTarget * vecXToBlock + vecZToTarget * vecZToBlock < 0) {
                        continue;
                    }

                    BlockState pendingBreakState = this.mob.level().getBlockState(pendingBreakPos);
                    // Avoid redundant destroying if the block has no collision shape (can be walked through)
                    if (pendingBreakState.getCollisionShape(this.mob.level(), pendingBreakPos).isEmpty()) continue;

                    // Determine whether to start
                    // 注意：如果你结合了之前修复的寻路逻辑，可以将此处的 isDone() 替换为 isDone() || this.mob.horizontalCollision
                    if (canBreakBlock(pendingBreakState) && this.mob.getNavigation().isDone()) {
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
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.level().destroyBlockProgress(this.mob.getId(), this.breakPos, -1);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }


    @Override
    public boolean canContinueToUse() {
//         System.out.println("state=" + this.breakState + "\tshouldStop=" + this.shouldStop + "\t breakProgress=" + this.breakProgress + "\t max=" + this.getMaxProgress() + "\t canBreak=" + this.canBreakBlock(this.breakState) + "\t withinDistance=" + this.breakPos.closerToCenterThan(this.mob.position(), 5) + "\tTimeSinceLastAttack=" + (this.mob.tickCount - this.mob.getLastHurtByMobTimestamp()));
        if (this.mob.getLastHurtByMob() != null) this.hcsLastAttacker = this.mob.getLastHurtByMob();
        if (this.hcsLastAttacker != null && !this.hcsLastAttacker.isAlive()) this.hcsLastAttacker = null;
        if (this.mob.level() instanceof ServerLevel serverWorld && !Configs.isEnabled(serverWorld, Configs.HOSTILE_ZOMBIE))
            return false;
        return !this.shouldStop
                && this.breakProgress <= this.getMaxProgress()
                && canBreakBlock(this.breakState)
                && this.breakPos.closerToCenterThan(this.mob.position(), 2.5)
                && (this.hcsLastAttacker == null /*wasRecentlyAttacked*/
                || (this.mob.tickCount - this.mob.getLastHurtByMobTimestamp()) > 20); // .getTimeSinceLastAttack()
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
        // Stop the current navigation when digging persists
//        this.mob.getNavigation().stop();
        // Keep the zombie's sight always locked on the center of the block being mined
//        this.mob.getLookControl().setLookAt(
//                this.breakPos.getX() + 0.5D,
//                this.breakPos.getY() + 0.5D,
//                this.breakPos.getZ() + 0.5D,
//                10.0F, // Maximum horizontal head rotation speed
//                (float) this.mob.getMaxHeadXRot() // Maximum vertical head rotation speed
//        );

        // Reach the target, successfully break the block
        if (this.breakProgress >= this.getMaxProgress()) {
            this.mob.level().destroyBlock(this.breakPos, true, this.mob);
            WorldHelper.checkBlockGravity(this.mob.level(), this.breakPos);
        }
    }
}