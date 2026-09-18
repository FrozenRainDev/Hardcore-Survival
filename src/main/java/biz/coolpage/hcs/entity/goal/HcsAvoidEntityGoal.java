package biz.coolpage.hcs.entity.goal;

import biz.coolpage.hcs.mixin.entity.goal.AvoidEntityGoalAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.phys.Vec3;

public class HcsAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {

    public HcsAvoidEntityGoal(PathfinderMob pMob, Class<T> pEntityClassToAvoid, float pMaxDistance, double pWalkSpeedModifier, double pSprintSpeedModifier) {
        super(pMob, pEntityClassToAvoid, pMaxDistance, pWalkSpeedModifier, pSprintSpeedModifier);
    }

    @Override
    public boolean canUse() {
        this.toAvoid = this.mob.level().getNearestEntity(this.mob.level().getEntitiesOfClass(this.avoidClass, this.mob.getBoundingBox().inflate(this.maxDist, this.maxDist / 2, this.maxDist), (p_148078_) -> true), ((AvoidEntityGoalAccessor) this).getAvoidEntityTargeting(), this.mob, this.mob.getX(), this.mob.getY(), this.mob.getZ());
        if (this.toAvoid != null) {
            Vec3 fromPlayer = this.mob.position().subtract(this.toAvoid.position()).normalize(); // vector from player to this.mob

            // Greedy enumeration: check multiple angles from 0 to 90 degrees left and right to find a safe path
            int[] angles = {0, 15, -15, 30, -30, 45, -45, 60, -60, 75, -75, 90, -90, 105, -105, 120, -120, 135, -135, 150, -150, 165, -165, 180, -180};
            net.minecraft.world.level.pathfinder.Path fallbackPath = null;

            for (int angle : angles) {
                float rad = (float) Math.toRadians(angle);

                // rotate vector horizontally
                double cos = Math.cos(rad);
                double sin = Math.sin(rad);
                double rx = fromPlayer.x * cos - fromPlayer.z * sin;
                double rz = fromPlayer.x * sin + fromPlayer.z * cos;

                Vec3 rotatedDir = new Vec3(rx, fromPlayer.y, rz).normalize();
                Vec3 fleeDirectionDelta = rotatedDir.scale(26.5); // flee distance

                // target position based on rotated vector
                double targetX = this.mob.getX() + fleeDirectionDelta.x;
                double targetZ = this.mob.getZ() + fleeDirectionDelta.z;

                // get actual terrain height to avoid pathing in the air or deep underground
                int targetY = this.mob.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) targetX, (int) targetZ);

                // quickly pre-check if the destination is water/lava to save pathfinding cost
                net.minecraft.core.BlockPos targetBlockPos = new net.minecraft.core.BlockPos((int) targetX, targetY - 1, (int) targetZ);
                net.minecraft.world.level.material.FluidState fluidState = this.mob.level().getFluidState(targetBlockPos);
                if (fluidState.is(net.minecraft.tags.FluidTags.WATER) || fluidState.is(net.minecraft.tags.FluidTags.LAVA)) {
                    continue; // skip fluid blocks
                }

                // generate potential path
                this.path = this.pathNav.createPath(targetX, targetY, targetZ, 0);

                if (this.path != null) {
                    net.minecraft.world.level.pathfinder.Node endNode = this.path.getEndNode();
                    if (endNode != null) {
                        net.minecraft.core.BlockPos endPos = endNode.asBlockPos();

                        // check if the path actually takes us away from the entity
                        if (this.toAvoid.distanceToSqr(endPos.getX(), endPos.getY(), endPos.getZ()) > this.toAvoid.distanceToSqr(this.mob)) {

                            // ensure the path doesn't end in water
                            if (!this.mob.level().getFluidState(endPos).is(net.minecraft.tags.FluidTags.WATER) &&
                                    !this.mob.level().getFluidState(endPos).is(net.minecraft.tags.FluidTags.LAVA)) {

                                // avoid cliffs/walls: if the path is too short, it means it's blocked by a drop or obstacle
                                double pathDistSqr = this.mob.distanceToSqr(endPos.getX(), endPos.getY(), endPos.getZ());
                                if (pathDistSqr > 25.0D) { // traveled at least 5 blocks
                                    return true;
                                } else if (fallbackPath == null) {
                                    // save the first valid short path as fallback (e.g. if trapped in a corner)
                                    fallbackPath = this.path;
                                }
                            }
                        }
                    }
                }
            }

            // use the fallback path if no long path was found (avoids freezing)
            if (fallbackPath != null) {
                this.path = fallbackPath;
                return true;
            }

        }
        return false;
    }

/*    @Override
    public boolean canUse() {
        // find the nearest entity to avoid
        this.toAvoid = this.mob.level().getNearestEntity(
                this.mob.level().getEntitiesOfClass(this.avoidClass, this.mob.getBoundingBox().inflate(this.maxDist, 3.0, this.maxDist), (p_148078_) -> true),
                this.avoidEntityTargeting, this.mob, this.mob.getX(), this.mob.getY(), this.mob.getZ()
        );
        return this.toAvoid != null;
    }

    @Override
    public void start() {
        if (this.toAvoid != null) {
            Vec3 fromPlayer = this.mob.position().subtract(this.toAvoid.position()).normalize(); // vector from player to this.mob
            Vec3 fleeDir = fromPlayer.scale(26.5); // flee distance
            Vec3 targetPos = this.mob.position().add(fleeDir); // target position

            // initiate movement towards the target position at sprint speed modifier
            this.mob.getNavigation().moveTo(
                    targetPos.x,
                    targetPos.y,
                    targetPos.z,
                    this.sprintSpeedModifier
            );
        }
    }*/

    @Override
    public void tick() {
        // call super to maintain vanilla tick logic (which updates standard walk/sprint modifiers based on distance)
        super.tick();

        // check if the animal is currently in water
        if (this.mob.isInWater()) {
            // significantly increase the navigation speed modifier to counteract fluid drag
            // you can tweak the 2.5D multiplier based on how fast you want them to swim
            this.mob.getNavigation().setSpeedModifier(((AvoidEntityGoalAccessor) this).getSprintSpeedModifier() * 5D);

            // OPTIONAL: if adjusting the speed modifier is still too slow due to entity's base attributes,
            // you can uncomment the code below to directly apply a physical push in the direction it's looking.
            /*
            Vec3 currentMovement = this.mob.getDeltaMovement();
            Vec3 lookAngle = this.mob.getLookAngle();
            // add a small forward velocity in X and Z axis
            this.mob.setDeltaMovement(currentMovement.add(lookAngle.x * 0.05, 0, lookAngle.z * 0.05));
            */
        }
    }
}