package biz.coolpage.hcs.status.manager;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.status.HcsEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_LIKE;

public class StaminaManager {
    private double stamina = 1.0;
    private int restoringCoolDown = 0;
    private Vec3 lastVecPos = Vec3.ZERO;
    public static final String STAMINA_NBT = "hcs_stamina";

    public double get() {
        if (stamina > 1.0) stamina = 1.0;
        else if (stamina < 0.0) stamina = 0.0;
        return stamina;
    }

    public void set(double val) {
        if (Double.isNaN(val)) {
            Hcs.error(this.getClass().getSimpleName() + ": Val is NaN");
            return;
        }
        if (val > 1.0) val = 1.0;
        else if (val < 0.0) val = 0.0;
        stamina = val;
    }

    public void add(double val, Entity entity) {
        if (entity instanceof Player player) add(val, player);
    }

    public void add(double val, Player player) {
        if (!IS_SURVIVAL_LIKE.test(player)) return;
        if (val >= 0.0) {
            if (restoringCoolDown <= 0) addDirectly(player.hasEffect(HcsEffects.COLD.get()) ? val * 0.65 : val);
            else --restoringCoolDown;
        } else if (!player.hasEffect(MobEffects.DAMAGE_BOOST))
            addDirectly(player.hasEffect(HcsEffects.HEAVY_LOAD.get()) ? val * 2.0 : val);
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
        lastVecPos = Vec3.ZERO;
    }

    public Vec3 getLastVecPos() {
        return lastVecPos;
    }

    public void setLastVecPos(Vec3 val) {
        lastVecPos = val;
    }

}