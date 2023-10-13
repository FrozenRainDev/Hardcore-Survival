package biz.coolpage.hcs.status.manager;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashSet;
import java.util.Objects;
import java.util.function.BiPredicate;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

public class SanityManager {
    public static final String SANITY_NBT = "hcs_sanity";
    public static BiPredicate<LivingEntity, LivingEntity> CAN_CLOSELY_SEE = (a, b) -> EntityHelper.isExistent(a, b) && !applyNullable(a.world, world -> world.isClient, true) && a.distanceTo(b) < 16 && a.canSee(b);
    public static BiPredicate<MobEntity, LivingEntity> IS_TARGET = (targeting, targeted) -> targeting != null && targeted != null && Objects.equals(targeted.getUuidAsString(), applyNullable(targeting.getTarget(), Entity::getUuidAsString, "~NonexistentEntity"));

    private double sanity = 1.0;
    private double lastSanity = 1.0;
    //Don't calculate difference between sanity and lastSanity when in InGameHud as it refreshes much faster than ticks() in PlayerEntity and cause twinkle of arrow which indicates trend of rising and falling
    private double sanDifference = 0.0;
    private final HashSet<MobEntity> enemies = new HashSet<>();

    public double get() {
        if (sanity > 1.0) sanity = 1.0;
        else if (sanity < 0.0) sanity = 0.0;
        return sanity;
    }

    public void set(double val) {
        if (Double.isNaN(val)) {
            Reg.LOGGER.error(this.getClass().getSimpleName() + ": Val is NaN");
            return;
        }
        if (val > 1.0) val = 1.0;
        else if (val < 0.0) val = 0.0;
        sanity = val;
    }

    public void add(double val) {
        set(sanity + val);
    }

    public void reset() {
        add(1.0);
        lastSanity = 1.0;
        updateDifference();
    }

    public double getDifference() {
        return sanDifference;
    }

    public void setDifference(double val) {
        sanDifference = val;
    }

    public void updateDifference() {
        sanDifference = sanity - lastSanity;
        lastSanity = sanity;
    }

    public void addEnemy(LivingEntity entity) {
        if (entity instanceof MobEntity mob) enemies.add(mob);
        else
            Reg.LOGGER.warn(this.getClass().getSimpleName() + ": " + entity + " is not MobEntity");
    }

    public void tickEnemies(PlayerEntity player) {
        enemies.removeIf(enemy -> !IS_TARGET.and(CAN_CLOSELY_SEE).test(enemy, player));
    }

    public int countEnemies() {
        return enemies.size();
    }
}
