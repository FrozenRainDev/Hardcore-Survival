package biz.coolpage.hcs.status.manager.disabled;

import biz.coolpage.hcs.status.manager.SanityManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

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
    public void tickEnemies(Player player) {
    }

    @Override
    public int countEnemies() {
        return 0;
    }
}