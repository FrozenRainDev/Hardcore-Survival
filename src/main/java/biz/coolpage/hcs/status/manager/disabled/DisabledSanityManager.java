package biz.coolpage.hcs.status.manager.disabled;

import biz.coolpage.hcs.status.manager.SanityManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

public class DisabledSanityManager extends SanityManager {
    @Override
    public double get() {
        return 1D;
    }

    @Override
    public void set(double val) {
    }

    @Override
    public void add(double val) {
    }

    @Override
    public double getDifference() {
        return 0D;
    }

    @Override
    public void setDifference(double val) {
    }

    @Override
    public void updateDifference() {
    }

    @Override
    public void addEnemy(LivingEntity entity) {
    }

    @Override
    public void tickEnemies(PlayerEntity player) {
    }

    @Override
    public int countEnemies() {
        return 0;
    }
}
