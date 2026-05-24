package biz.coolpage.hcs.entity.goal;

import biz.coolpage.hcs.util.DigRestrictHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class ZombieBreakBlockGoal extends BreakDoorGoal {

    protected BlockPos breakPos = BlockPos.ZERO;
    private final Predicate<Difficulty> validDifficulties;

    public ZombieBreakBlockGoal(Mob mob) {
        // Default to hard difficulty, matching vanilla zombie behavior
        super(mob, (difficulty) -> difficulty == Difficulty.HARD);
        this.validDifficulties = (difficulty) -> difficulty == Difficulty.HARD;
    }

    public ZombieBreakBlockGoal(Mob mob, Predicate<Difficulty> validDifficulties) {
        super(mob, validDifficulties);
        this.validDifficulties = validDifficulties;
    }

    public boolean canBreakBlock(@NotNull BlockState state) {
        if (state.isAir()) return false;
        if (state.is(BlockTags.WOODEN_DOORS)) return true;
        boolean stoneConstraint = state.getDestroySpeed(this.mob.level(), this.breakPos) < Blocks.STONE.defaultDestroyTime() || this.mob.getMainHandItem().getItem() instanceof PickaxeItem;
        return DigRestrictHelper.canBreak(this.mob.getMainHandItem().getItem(), state) && stoneConstraint;
    }

    @Override
    public boolean canUse() {
//        System.out.println("Zombie break block go was called can use");
        // Only attempt to break blocks when the zombie is physically blocked
        if (!this.mob.horizontalCollision) { // :(
            System.out.println("1Zombie break block go was called cant use");
            return false;
        }

        if (!this.isValidDifficulty(this.mob.level().getDifficulty())) {
            System.out.println("2Zombie break block go was called cant use");
            return false;
        }

        BlockPos pos = this.findBreakPos();
        if (pos == null) {
            System.out.println("3Zombie break block go was called cant use");
            return false;
        }

        // sha bi
        if (DigRestrictHelper.canBreak(this.mob.getMainHandItem().getItem(), this.mob.level().getBlockState(pos))) {
            System.out.println("4Zombie break block go was called cant use");
            return false;
        }

        this.breakPos = pos;
        this.doorPos = pos; // Sync with parent class field for progress and removal logic
        return true;
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
    public void tick() {
        System.out.println("ZombieBreakBlockGoal tick");
        // super.tick() handles breakTime increment, animation, and block removal using this.doorPos
        super.tick();
    }

    protected BlockPos findBreakPos() {
        BlockPos basePos = this.mob.blockPosition();

        // Check blocks intersecting with the mob's current position
        if (this.canBreakBlock(this.mob.level().getBlockState(basePos))) return basePos;
        if (this.canBreakBlock(this.mob.level().getBlockState(basePos.above()))) return basePos.above();

        // Check blocks in the direction the zombie is facing
        BlockPos frontPos = basePos.relative(this.mob.getDirection());
        if (this.canBreakBlock(this.mob.level().getBlockState(frontPos))) return frontPos;
        if (this.canBreakBlock(this.mob.level().getBlockState(frontPos.above()))) return frontPos.above();

        return null;
    }

    private boolean isValidDifficulty(Difficulty difficulty) {
        return this.validDifficulties.test(difficulty);
    }
}