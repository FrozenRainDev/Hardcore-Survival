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
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

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
        for (double[] findPos : EntityHelper.FIND_NEAREST_BLOCKS) {
            // Should not dig upward when not above target
            if (findPos[1] == -1 && this.mob.getY() <= target.getY()) continue;
            // Should not dig downward when not under target
            if (findPos[1] == 2 && this.mob.getY() >= target.getY()) continue;
            // Choose a pos to break
            BlockPos pendingBreakPos = BlockPos.containing(this.mob.getX() + findPos[0], this.mob.getY() + findPos[1], this.mob.getZ() + findPos[2]);
            // Should not break the block behind itself
            BlockPos backPos = EntityHelper.getPosFacing(this.mob, true);
            if (pendingBreakPos.getX() == backPos.getX() && pendingBreakPos.getZ() == backPos.getZ()) continue;
            BlockState pendingBreakState = this.mob.level().getBlockState(pendingBreakPos);
            // Avoid redundant destroying if material == Material.REPLACEABLE_PLANT || material == Material.PLANT
            if (pendingBreakState.canBeReplaced() /*|| pendingBreakState.getSoundGroup().equals(BlockSoundGroup.GRASS)*/)
                continue;
            // Determine whether to start
            if (canBreakBlock(pendingBreakState) && this.mob.getNavigation().isDone()) {
//                System.out.println("Breaking block " + pendingBreakPos);
                this.breakPos = pendingBreakPos;
                this.breakState = pendingBreakState;
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
//        System.out.println("Starting BreakBlockGoal");
        this.shouldStop = false;
//        this.offsetX = (float) ((double) this.breakPos.getX() + 0.5 - this.mob.getX());
//        this.offsetZ = (float) ((double) this.breakPos.getZ() + 0.5 - this.mob.getZ());
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
                && this.breakPos.closerToCenterThan(this.mob.position(), 4)
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
        if (this.breakProgress >= this.getMaxProgress()) {
            this.mob.level().destroyBlock(this.breakPos, true, this.mob);
            WorldHelper.checkBlockGravity(this.mob.level(), this.breakPos);
        }
    }
}