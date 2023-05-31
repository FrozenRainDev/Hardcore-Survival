package com.hcs.main.manager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

public class StaminaManager {
    private BigDecimal stamina = new BigDecimal("1.0");
    private int restoringCoolDown = 0;
    private Vec3d lastVecPos = Vec3d.ZERO;
    public static final String STAMINA_NBT = "hcs_stamina";

    public float get() {
        if (stamina.compareTo(ONE) > 0) stamina = ONE;
        else if (stamina.compareTo(ZERO) < 0) stamina = ZERO;
        return stamina.floatValue();
    }

    public void set(float val) {
        if (Float.isNaN(val)) {
            new NumberFormatException("Val is NaN").printStackTrace();
            return;
        }
        if (val > 1.0F) val = 1.0F;
        else if (val < 0.0F) val = 0.0F;
        stamina = new BigDecimal(String.format("%.5f", val));
    }

    public void add(float val, Entity entity) {
        if (entity instanceof PlayerEntity player) add(val, player);
    }

    public void add(float val, PlayerEntity player) {
        if (player == null || player.getAbilities().invulnerable) return;
        if (val >= 0.0F) {
            if (restoringCoolDown <= 0) addDirectly(val);
            else --restoringCoolDown;
        } else if (!player.hasStatusEffect(StatusEffects.STRENGTH)) addDirectly(val);
    }

    public void addDirectly(float val) {
        if (Float.isNaN(val)) {
            new NumberFormatException("Val is NaN").printStackTrace();
            return;
        }
        stamina = stamina.add(new BigDecimal(String.format("%.5f", val)));
        if (stamina.compareTo(ONE) > 0) stamina = ONE;
        else if (stamina.compareTo(ZERO) < 0) stamina = ZERO;
    }

    public void pauseRestoring() {
        pauseRestoring(15);
    }

    public void pauseRestoring(int ticks) {
        restoringCoolDown = Math.max(0, ticks);
    }

    public void reset() {
        addDirectly(1.0F);
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
