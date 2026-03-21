package biz.coolpage.hcs.status.manager.disabled;

import biz.coolpage.hcs.status.manager.StaminaManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

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
    public void add(double val, Player player) {
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