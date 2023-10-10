package com.hcs.status.manager;

import com.hcs.Reg;

import static com.hcs.util.EntityHelper.PLASMA_CONCENTRATION;

public class MoodManager {
    public static final String PANIC_NBT = "hcs_mood_panic";
    public static final String PANIC_KILLER_APPLIED_NBT = "hcs_panic_killer";
    private double panic = 0.0, panicAlleCache = 0.0;
    private int panicKillerApplied = 0, panicKillerUpdateInterval = 0;

    public double getRawPanic() {
        if (panic < 0.0) panic = 0.0;
        else if (panic > 4.0) panic = 4.0;
        return panic;
    }

    public double getRealPanic() {
        return Math.max(0, getRawPanic() - getPanicAlleCache());
    }

    public double getPanicAlleCache() {
        if (panicKillerUpdateInterval > 0) {
            --panicKillerUpdateInterval;
            return panicAlleCache;
        }
        panicAlleCache = 2.5 * PLASMA_CONCENTRATION.apply(Math.max(0, 4200 - panicKillerApplied));  //4200 - panicKillerApplied: ticks since applied (panicKillerApplied=effect countdown)
        if (panicAlleCache < 0.25) panicAlleCache = 0;
        panicKillerUpdateInterval = 60;
        return panicAlleCache;
    }

    public int getPanicKillerApplied() {
        return panicKillerApplied;
    }

    public void setPanic(double val) {
        if (Double.isNaN(val)) {
            Reg.LOGGER.error(this.getClass().getSimpleName() + "/setPanic(): Val is NaN");
            return;
        }
        panic = val;
    }

    public void setPanicAlle(double val) {
        if (Double.isNaN(val)) {
            Reg.LOGGER.error(this.getClass().getSimpleName() + "/setPanicAlle(): Val is NaN");
            return;
        }
        if (val > 4.0) val = 4.0;
        else if (val < 0.0) val = 0.0;
        panicAlleCache = val;
        panicKillerUpdateInterval = 60;
    }

    public void setPanicKillerApplied(int val) {
        if (val > 4200) val = 4200;
        else if (val < 0) val = 0;
        panicKillerApplied = val;
    }

    public void addPanic(double val) {
        setPanic(val + getRawPanic());
    }

    public void applyPanicKiller() {
        if (panicAlleCache < 3.5) panicKillerApplied = (panicKillerApplied > 0 ? 4200 - 500 : 4200);
        else panicKillerApplied = 4200 - 600;
    }

    public void tickPanic(double currRawPanic, double expectedRawPanic, double panicDiff) {
        if (panicKillerApplied > 0) --panicKillerApplied;
        if (panicDiff < 0.01) setPanic(expectedRawPanic);
        else if (currRawPanic > expectedRawPanic) addPanic(panic < 1 ? -0.003 : -0.007);
        else addPanic(Math.max(0.1, expectedRawPanic / 60));
    }

    public void reset() {
        panicAlleCache = panic = 0.0;
    }

}
