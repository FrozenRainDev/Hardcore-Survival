package com.hcs.status.manager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class StaminaManager {
    private double stamina = 1.0;
    private int restoringCoolDown = 0;
    private Vec3d lastVecPos = Vec3d.ZERO;
    public static final String STAMINA_NBT = "hcs_stamina";

    public double get() {
        if (stamina > 1.0) stamina = 1.0;
        else if (stamina < 0.0) stamina = 0.0;
        return stamina;
    }

    public void set(double val) {
        if (Double.isNaN(val)) {
            new NumberFormatException("Val is NaN").printStackTrace();
            return;
        }
        if (val > 1.0) val = 1.0;
        else if (val < 0.0) val = 0.0;
        stamina = val;
    }

    public void add(double val, Entity entity) {
        if (entity instanceof PlayerEntity player) add(val, player);
    }

    public void add(double val, PlayerEntity player) {
        if (player == null || player.getAbilities().invulnerable) return;
        if (val >= 0.0) {
            if (restoringCoolDown <= 0) addDirectly(val);
            else --restoringCoolDown;
        } else if (!player.hasStatusEffect(StatusEffects.STRENGTH)) addDirectly(val);
    }

    public void addDirectly(double val) {
        set(stamina + val);
    }

    public void pauseRestoring() {
        pauseRestoring(15);
    }

    public void pauseRestoring(int ticks) {
        restoringCoolDown = Math.max(0, ticks);
    }

    public void reset() {
        addDirectly(1.0);
        restoringCoolDown = 0;
        lastVecPos = Vec3d.ZERO;
    }

    public Vec3d getLastVecPos() {
        return lastVecPos;
    }

    public void setLastVecPos(Vec3d val) {
        lastVecPos = val;
    }

}
