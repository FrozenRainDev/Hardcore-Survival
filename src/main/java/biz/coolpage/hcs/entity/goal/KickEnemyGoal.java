package biz.coolpage.hcs.entity.goal;

import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static biz.coolpage.hcs.util.CommUtil.hasNull;

public class KickEnemyGoal extends PanicGoal {
    private int kickCoolDown = 20;
    // The NBT key for persistent attacker memory
    private static final String ATTACKER_UUID_KEY = "HcsAttacker";

    public KickEnemyGoal(PathfinderMob mob, double speedModifier) {
        super(mob, speedModifier);
    }

    private boolean canKick() {
        if (Configs.isEnabled(Configs.HOSTILE_COW)) return this.kickCoolDown <= 0;
        return false;
    }

    private void notifyKick() {
        this.kickCoolDown = 70;
    }

    private void updateCooldown() {
        if (this.kickCoolDown > 0) this.kickCoolDown--;
    }

    /**
     * Gets the attacker from Vanilla first, and syncs to Forge Persistent NBT.
     * If Vanilla forgets (timeout or reload), it reads from NBT and restores it.
     */
    @Nullable
    private LivingEntity getPersistentAttacker() {
        LivingEntity vanillaAttacker = this.mob.getLastHurtByMob();

        if (vanillaAttacker != null) {
            // Save to persistent data (NBT) to survive world reloads
            this.mob.getPersistentData().putUUID(ATTACKER_UUID_KEY, vanillaAttacker.getUUID());
            return vanillaAttacker;
        }

        // If vanilla attacker is null (due to game reload or vanilla timeout), check NBT
        if (this.mob.getPersistentData().hasUUID(ATTACKER_UUID_KEY)) {
            UUID uuid = this.mob.getPersistentData().getUUID(ATTACKER_UUID_KEY);
            if (this.mob.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(uuid);
                if (entity instanceof LivingEntity livingAttacker && livingAttacker.isAlive()) {
                    // Restore vanilla state for other AI compatibility
                    this.mob.setLastHurtByMob(livingAttacker);
                    return livingAttacker;
                }
            }
        }
        return null;
    }

    private boolean isAttackerAfar(LivingEntity attacker) {
        return attacker == null || this.mob.distanceTo(attacker) > 48;
    }

    @Override
    public boolean canUse() {
        // Priority check: Eating grass overrides panic (Horse, Donkey, Mule)
        if (this.mob instanceof AbstractHorse horse && horse.isEating()) {
            return false;
        }

        LivingEntity attacker = this.getPersistentAttacker();

        if (attacker != null && !this.isAttackerAfar(attacker)) {
            // BUG FIX: Must call findRandomPosition() to calculate the actual escape coordinates.
            // Returning true directly bypasses coordinate generation, causing the entity to freeze.
            return this.findRandomPosition();
        }
        return super.canUse();
    }

    @Override
    public void start() {
        double speed = this.speedModifier;
        // Improve escaping speed
        if (this.mob instanceof Cow) speed *= 1.1;
        else if (this.mob instanceof Chicken) speed *= 1.2;
        else speed *= 1.4;

        this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, speed);
        this.isRunning = true;
    }

    @Override
    protected boolean findRandomPosition() {
        // Change from vanilla's getLastHurtByMob to our persistent attacker
        LivingEntity attacker = this.getPersistentAttacker();
        if (attacker == null) return false;

        Vec3 attackerPos = attacker.position();
        Vec3 mobPos = this.mob.position();
        if (hasNull(attackerPos, mobPos)) return false;

        double escVectorX = mobPos.x - attackerPos.x;
        double escVectorZ = mobPos.z - attackerPos.z;
        Vec3 escPos = DefaultRandomPos.getPosTowards(this.mob, 5, 4, Vec3.atBottomCenterOf(BlockPos.containing(mobPos.x + escVectorX, mobPos.y, mobPos.z + escVectorZ)), 1.5707963705062866D);

        if (escPos == null) {
            Vec3 escVector = attackerPos.subtract(mobPos);
            escPos = DefaultRandomPos.getPosTowards(this.mob, 5, 4, Vec3.atBottomCenterOf(BlockPos.containing(escVector.x, escVector.y, escVector.z)), 1.5707963705062866D);
        }

        if (escPos == null) {
            return false;
        }

        this.posX = escPos.x;
        this.posY = escPos.y;
        this.posZ = escPos.z;
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity attacker = this.getPersistentAttacker();

        // BUG FIX: Refresh the path if the navigation is done but the attacker is still nearby.
        // This prevents the mob from standing still after completing its initial escape route.
        if (this.mob.getNavigation().isDone() && attacker != null && !this.isAttackerAfar(attacker)) {
            if (this.findRandomPosition()) {
                this.start(); // Restart movement to new pos
            }
        }

        /* `CowKickRevengeGoal` Core Code */
        // Moved to tick() for standard convention. Ensures kicking works even if cornered and unable to find a path.
        if ((this.mob instanceof Cow || this.mob instanceof AbstractHorse) && !this.mob.isBaby()) {
            this.updateCooldown();
            if (this.canKick() && attacker != null) {
                // Check if the mob is a tamed horse and the attacker is its owner
                boolean isAttackerOwner = false;
                if (this.mob instanceof AbstractHorse horse && horse.isTamed()) {
                    if (attacker.getUUID().equals(horse.getOwnerUUID())) {
                        isAttackerOwner = true;
                    }
                }

                // Only kick if attacker is NOT the owner, is not wearing full leather, and is close enough
                if (!isAttackerOwner && !EntityHelper.isInLeather(attacker) && this.mob.distanceTo(attacker) < 3.0D) {
                    EntityHelper.kickedAndFly(this.mob, attacker, 6.0F);
                    this.notifyKick();
                }
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        // Eating grass completely stops the panic
        if (this.mob instanceof AbstractHorse horse && horse.isEating()) {
            this.mob.getPersistentData().remove(ATTACKER_UUID_KEY); // Reset panic memory when calmed down by eating
            return false;
        }

        LivingEntity attacker = this.getPersistentAttacker();

        // Prolonged panic logic: Keep the goal active as long as the attacker is near
        if (attacker != null && !this.isAttackerAfar(attacker)) {
            return true;
        }

        // Attacker is gone or out of range, clear persistent memory
        this.mob.getPersistentData().remove(ATTACKER_UUID_KEY);
        return false;
    }

    public static double getDefaultSpeed(PathfinderMob mob) {
        if (mob instanceof Cow) return 2.0D;
        if (mob instanceof Chicken) return 1.4D;
        if (mob instanceof AbstractHorse) return 1.2D;
        return 1.25D;
    }
}