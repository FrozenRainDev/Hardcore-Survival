package biz.coolpage.hcs.entity.goal;

import biz.coolpage.hcs.util.DigRestrictHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

public class ZombieBreakBlockGoal extends BreakDoorGoal {
    protected BlockPos breakPos = BlockPos.ZERO;
    private final Predicate<Difficulty> validDifficulties;

    public ZombieBreakBlockGoal(Mob mob, Predicate<Difficulty> validDifficulties) {
        super(mob, validDifficulties);
        this.validDifficulties = validDifficulties;
    }

    public ZombieBreakBlockGoal(Mob mob, int breakTime, Predicate<Difficulty> validDifficulties) {
        super(mob, breakTime, validDifficulties);
        this.validDifficulties = validDifficulties;
    }

    public boolean canBreakBlock(@NotNull BlockState state) {
        if (state.isAir()) return false;
        if (state.is(BlockTags.WOODEN_DOORS)) return true;
        boolean stoneConstraint = state.getDestroySpeed(this.mob.level(), this.breakPos) < Blocks.STONE.defaultDestroyTime()
                || this.mob.getMainHandItem().getItem() instanceof PickaxeItem;
        return DigRestrictHelper.canBreak(this.mob.getMainHandItem().getItem(), state) && stoneConstraint;
    }

    @Override
    public boolean canUse() {
        if (!this.isValidDifficulty(this.mob.level().getDifficulty())) {
            return false;
        }

        BlockPos pos = this.findBreakPos();
        if (pos == null) {
            return false;
        }

        if (!ForgeHooks.canEntityDestroy(this.mob.level(), pos, this.mob)) {
            return false;
        }

        this.breakPos = pos;
        this.doorPos = pos;
        return true;
    }

    @Override
    public void start() {
        super.start();
        this.breakTime = 0;
        this.lastBreakProgress = -1;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.isValidDifficulty(this.mob.level().getDifficulty())) {
            return false;
        }

        if (this.breakTime > this.getDoorBreakTime()) {
            return false;
        }

        if (!this.breakPos.closerToCenterThan(this.mob.position(), 2.0D)) {
            return false;
        }

        BlockState state = this.mob.level().getBlockState(this.breakPos);
        return this.canBreakBlock(state) && ForgeHooks.canEntityDestroy(this.mob.level(), this.breakPos, this.mob);
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.level().destroyBlockProgress(this.mob.getId(), this.breakPos, -1);
    }

    @Override
    public void tick() {
        if (this.mob.getRandom().nextInt(20) == 0) {
            this.mob.level().levelEvent(1019, this.breakPos, 0);
            if (!this.mob.swinging) {
                this.mob.swing(this.mob.getUsedItemHand());
            }
        }

        ++this.breakTime;

        int progress = (int) ((float) this.breakTime / (float) this.getDoorBreakTime() * 10.0F);
        if (progress != this.lastBreakProgress) {
            this.mob.level().destroyBlockProgress(this.mob.getId(), this.breakPos, progress);
            this.lastBreakProgress = progress;
        }

        if (this.breakTime == this.getDoorBreakTime() && this.isValidDifficulty(this.mob.level().getDifficulty())) {
            BlockState state = this.mob.level().getBlockState(this.breakPos);
            if (this.canBreakBlock(state) && ForgeHooks.canEntityDestroy(this.mob.level(), this.breakPos, this.mob)) {
                this.mob.level().removeBlock(this.breakPos, false);
                this.mob.level().levelEvent(1021, this.breakPos, 0);
                this.mob.level().levelEvent(2001, this.breakPos, Block.getId(state));
            }
        }
    }

    protected BlockPos findBreakPos() {
        Set<BlockPos> candidates = new LinkedHashSet<>();

        BlockPos basePos = this.mob.blockPosition();
        Direction direction = this.mob.getDirection();

        BlockPos frontPos = basePos.relative(direction);

        // Prefer the blocking block in front of the mob.
        candidates.add(frontPos);
        candidates.add(frontPos.above());

        // Fallback for blocks intersecting with the mob position.
        candidates.add(basePos);
        candidates.add(basePos.above());

        for (BlockPos pos : candidates) {
            this.breakPos = pos;
            BlockState state = this.mob.level().getBlockState(pos);
            if (this.canBreakBlock(state)) {
                return pos;
            }
        }

        return null;
    }

    private boolean isValidDifficulty(Difficulty difficulty) {
        return this.validDifficulties.test(difficulty);
    }
}