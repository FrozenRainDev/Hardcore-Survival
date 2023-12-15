package biz.coolpage.hcs.entity.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class CowKickRevengeGoal extends Goal {
    //Reference: PounceAtTargetGoal
    private final MobEntity mob;
    private LivingEntity attacker;
    private int kickCooldown = 0;

    public CowKickRevengeGoal(MobEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
    }

    public static boolean isInLeather(LivingEntity entity) {
        if (entity == null) return false;
        AtomicBoolean b = new AtomicBoolean(false);
        entity.getArmorItems().forEach(stack -> {
            if (stack.getItem() instanceof ArmorItem armor && armor.getMaterial() == ArmorMaterials.LEATHER)
                b.set(true);
        });
        return b.get();
    }

    public static void flyOut(@NotNull MobEntity attacker, @NotNull LivingEntity victim, float damage) {
        Vec3d vec1 = victim.getVelocity();
        Vec3d vec2 = new Vec3d(victim.getX() - attacker.getX(), 0.0, victim.getZ() - attacker.getZ());
        if (vec2.lengthSquared() > 1.0E-7) vec2 = vec2.normalize().add(vec1.multiply(0.2));
        victim.damage(victim.world.getDamageSources().mobAttack(attacker), damage);
        victim.setVelocity(vec2.x, 0.5, vec2.z);
    }

    @Override
    public boolean canStart() {
        if (this.kickCooldown > 0) --this.kickCooldown;
        this.attacker = this.mob.getAttacker();
        return this.attacker != null && !isInLeather(this.attacker) && this.mob.distanceTo(this.attacker) < 3.0 && !this.mob.isBaby();
    }

    @Override
    public boolean shouldContinue() {
        return this.kickCooldown <= 0;
    }

    @Override
    public void start() {
        if (this.attacker == null) return;
        if (this.kickCooldown > 0) {
            --this.kickCooldown;
            return;
        }
        flyOut(this.mob, this.attacker, 6.0F);
        this.kickCooldown = 100;
    }
}