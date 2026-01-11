package biz.coolpage.hcs.status.manager.disabled;

import biz.coolpage.hcs.status.manager.StaminaManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_LIKE;

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
