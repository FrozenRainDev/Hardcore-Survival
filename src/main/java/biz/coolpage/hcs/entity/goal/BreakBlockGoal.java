package biz.coolpage.hcs.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BreakBlockGoal extends Goal {
    protected final Mob living;
    private LivingEntity target;
    private BlockPos markedLoc;
    private BlockPos entityPos;
    private int digTimer;

    // Replaced Config.CommonConfig cooldowns with standard constants
    private int cooldown = 60;
    private final int breakerCooldown = 20;

    private final List<BlockPos> breakAOE = new ArrayList<>();
    private int breakIndex;

    private final int digHeight;

    public BreakBlockGoal(@NotNull Mob living) {
        this.living = living;
        int digWidth = living.getBbWidth() < 1 ? 0 : Mth.ceil(living.getBbWidth());
        this.digHeight = (int) living.getBbHeight() + 1;
        for (int i = this.digHeight; i >= 0; i--)
            this.breakAOE.add(new BlockPos(0, i, 0));
        //north = neg z
        for (int z = digWidth + 1; z >= -digWidth; z--)
            for (int y = this.digHeight; y >= 0; y--) {
                for (int x = 0; x <= digWidth; x++) {
                    if (z != 0) {
                        this.breakAOE.add(new BlockPos(x, y, z));
                        if (x != 0)
                            this.breakAOE.add(new BlockPos(-x, y, z));
                    }
                }
            }
    }

    @Override
    public boolean canUse() {
        this.target = this.living.getTarget();
        if (this.entityPos == null) {
            this.entityPos = this.living.blockPosition();
            this.cooldown = this.breakerCooldown;
        }
        if (--this.cooldown <= 0) {
            if (!this.entityPos.equals(this.living.blockPosition())) {
                this.entityPos = null;
                this.cooldown = this.breakerCooldown;
                return false;
            } else if (this.target != null && this.living.distanceTo(this.target) > 1D) {
                BlockPos blockPos = this.getDiggingLocation();
                if (blockPos == null)
                    return false;
                this.cooldown = this.breakerCooldown;
                this.markedLoc = blockPos;
                this.entityPos = this.living.blockPosition();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null && this.target.isAlive() && this.living.isAlive() && this.markedLoc != null && this.nearSameSpace(this.entityPos, this.living.blockPosition()) && this.living.distanceTo(this.target) > 1D;
    }

    private boolean nearSameSpace(BlockPos pos1, BlockPos pos2) {
        return pos1 != null && pos2 != null && pos1.getX() == pos2.getX() && pos1.getZ() == pos2.getZ() && Math.abs(pos1.getY() - pos2.getY()) <= 1;
    }

    @Override
    public void stop() {
        this.breakIndex = 0;
        if (this.markedLoc != null)
            this.living.level().destroyBlockProgress(this.living.getId(), this.markedLoc, -1);
        this.markedLoc = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.markedLoc == null || this.living.level().getBlockState(this.markedLoc).getCollisionShape(this.living.level(), this.markedLoc).isEmpty()) {
            this.digTimer = 0;
            return;
        }

        BlockState state = this.living.level().getBlockState(this.markedLoc);

        // Replaced Utils.getBlockStrength with vanilla block destroy speed logic
        float hardness = state.getDestroySpeed(this.living.level(), this.markedLoc);
        float str = hardness <= 0.0F ? (hardness == -1.0F ? 0.0F : 1.0F) : (1.0F / hardness) / 100.0F * (this.digTimer * this.breakSpeedMod() + 1);

        if (str >= 1F) {
            this.digTimer = 0;
            this.cooldown *= 0.5;
            ItemStack item = this.living.getMainHandItem();
            ItemStack itemOff = this.living.getOffhandItem();

            // Removed BlockRestorationData, simplified harvest check
            boolean canHarvest = this.canHarvestItem(state, item) || this.canHarvestItem(state, itemOff);

            this.living.level().destroyBlock(this.markedLoc, canHarvest);
            this.living.level().destroyBlockProgress(this.living.getId(), this.markedLoc, -1);
            this.markedLoc = null;

            if (!this.aboveTarget()) {
                this.living.setSpeed(0);
                this.living.getNavigation().stop();
                this.living.getNavigation().moveTo(this.living.getNavigation().createPath(this.target, 0), 1D);
            } else {
                this.living.getNavigation().stop();
            }
        } else {
            this.digTimer++;
            if (this.digTimer % 5 == 0) {
                // Replaced CrossPlatformStuff with Forge/Vanilla sound fetcher
                SoundType sound = state.getSoundType(this.living.level(), this.markedLoc, this.living);
                this.living.level().playSound(null, this.markedLoc.getX() + 0.5, this.markedLoc.getY() + 0.5, this.markedLoc.getZ() + 0.5, sound.getBreakSound(), SoundSource.BLOCKS, 2F, 0.5F);

                this.living.swing(InteractionHand.MAIN_HAND);
                this.living.getLookControl().setLookAt(this.markedLoc.getX(), this.markedLoc.getY(), this.markedLoc.getZ(), 0.0F, 0.0F);
                this.living.level().destroyBlockProgress(this.living.getId(), this.markedLoc, (int) (str * 10) - 1);
            }
        }
    }

    private float breakSpeedMod() {
        // Simplified: removed Config and DifficultyFetcher variables. Defaulting to 1.0F.
        return 1.0F;
    }

    public BlockPos getDiggingLocation() {
        ItemStack item = this.living.getMainHandItem();
        ItemStack itemOff = this.living.getOffhandItem();
        BlockPos pos = this.living.blockPosition();
        BlockState state;

        if (this.living.getTarget() != null) {
            Vec3 target = this.living.getTarget().position();
            if (this.aboveTarget() && Math.abs(target.x - pos.getX()) <= 1 && Math.abs(target.z - pos.getZ()) <= 1) {
                pos = this.living.blockPosition().below();
                state = this.living.level().getBlockState(pos);
                if (this.canBreak(this.living, state, pos, item, itemOff)) {
                    this.breakIndex = 0;
                    return pos;
                }
            }
        }

        Rotation rot = getDigDirection(this.living);
        BlockPos offset = this.breakAOE.get(this.breakIndex);
        offset = new BlockPos(offset.getX(), this.aboveTarget() ? (-(this.digHeight - offset.getY())) : offset.getY(), offset.getZ());
        pos = pos.offset(offset.rotate(rot));
        state = this.living.level().getBlockState(pos);

        if (this.canBreak(this.living, state, pos, item, itemOff)) {
            this.breakIndex = 0;
            return pos;
        }

        this.breakIndex++;
        if (this.breakIndex == this.breakAOE.size())
            this.breakIndex = 0;
        return null;
    }

    private boolean canBreak(@NotNull LivingEntity entity, @NotNull BlockState state, BlockPos pos, ItemStack item, ItemStack itemOff) {
        // Replaced Config.CommonConfig.breakableBlocks check with Vanilla unbreakability check
        if (state.getDestroySpeed(entity.level(), pos) == -1.0F) {
            return false;
        }
        return this.canHarvestItem(state, item) || this.canHarvestItem(state, itemOff);
    }

    // Helper method to replace Utils.canHarvest
    private boolean canHarvestItem(@NotNull BlockState state, ItemStack item) {
        return !state.requiresCorrectToolForDrops() || item.isCorrectToolForDrops(state);
    }

    private boolean aboveTarget() {
        return this.target.getY() < this.living.getY() + 1.1;
    }

    public static Rotation getDigDirection(@NotNull Mob mob) {
        Path path = mob.getNavigation().getPath();
        if (path != null) {
            Node point = path.getNextNodeIndex() < path.getNodeCount() ? path.getNextNode() : null;
            if (point != null) {
                Vec3 dir = new Vec3(point.x + 0.5, mob.position().y, point.z + 0.5).subtract(mob.position());
                if (Math.abs(dir.x) < Math.abs(dir.z)) {
                    if (dir.z >= 0)
                        return Rotation.NONE;
                    return Rotation.CLOCKWISE_180;
                } else {
                    if (dir.x > 0)
                        return Rotation.COUNTERCLOCKWISE_90;
                    return Rotation.CLOCKWISE_90;
                }
            }
        }
        return switch (mob.getDirection()) {
            case SOUTH -> Rotation.CLOCKWISE_180;
            case EAST -> Rotation.CLOCKWISE_90;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }
}