package biz.coolpage.hcs.status.manager.disabled;

import biz.coolpage.hcs.status.manager.StaminaManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class DisabledStaminaManager extends StaminaManager {
    @Override
    public double get() {
        return 1D;
    }

    @Override
    public void set(double val) {
    }

    @Override
    public void add(double val, Entity entity) {
    }

    @Override
    public void add(double val, PlayerEntity player) {
    }

    @Override
    public void addDirectly(double val) {
    }

    @Override
    public void pauseRestoring() {
    }

    @Override
    public void pauseRestoring(int ticks) {
    }
}
