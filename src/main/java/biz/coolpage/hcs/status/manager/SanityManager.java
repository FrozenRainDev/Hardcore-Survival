package biz.coolpage.hcs.status.manager;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Objects;
import java.util.function.BiPredicate;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

public class SanityManager {
    public static final String SANITY_NBT = "hcs_sanity";
    public static BiPredicate<LivingEntity, LivingEntity> CAN_CLOSELY_SEE = (a, b) -> EntityHelper.isExistent(a, b) && !applyNullable(a.level(), level -> level.isClientSide, true) && a.distanceTo(b) < 16 && a.hasLineOfSight(b);
    public static BiPredicate<Mob, LivingEntity> IS_TARGET = (targeting, targeted) -> targeting != null && targeted != null && Objects.equals(targeted.getStringUUID(), applyNullable(targeting.getTarget(), Entity::getStringUUID, "~NonexistentEntity"));

    private double sanity = 1.0;
    private double lastSanity = 1.0;
    //Don't calculate difference between sanity and lastSanity when in InGameHud as it refreshes much faster than ticks() in PlayerEntity and cause twinkle of arrow which indicates trend of rising and falling
    private double sanDifference = 0.0;
    private final HashSet<Mob> enemies = new HashSet<>();

    public double get() {
        if (sanity > 1.0) sanity = 1.0;
        else if (sanity < 0.0) sanity = 0.0;
        return sanity;
    }

    public void set(double val) {
        if (Double.isNaN(val)) {
            Hcs.error("{}: Val is NaN", this.getClass().getSimpleName());
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
        if (entity instanceof Mob mob) enemies.add(mob);
        else
            Hcs.warn(this.getClass().getSimpleName() + ": " + entity + " is not MobEntity");
    }

    public void tickEnemies(Player player) {
        enemies.removeIf(enemy -> !IS_TARGET.and(CAN_CLOSELY_SEE).test(enemy, player));
    }

    public int countEnemies() {
        return enemies.size();
    }
}