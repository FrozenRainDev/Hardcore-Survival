package com.hcs.status.manager;

import com.hcs.Reg;

import static com.hcs.util.EntityHelper.PLASMA_CONCENTRATION;

public class PainManager {
    public static final String PAIN_NBT = "hcs_pain";
    public static final String PAINKILLER_APPLIED_NBT = "hcs_painkiller";
    private double pain = 0.0, alleviationCache = 0.0; // [0, 4]
    private int painkillerApplied = 0, painkillerUpdateInterval = 0;

    public double getPainkillerAlle() {
        if (painkillerUpdateInterval > 0) {
            // Refresh alleviation every 60 ticks to avoid too much calculation
            --painkillerUpdateInterval;
            return alleviationCache;
        }
        alleviationCache = PLASMA_CONCENTRATION.apply(Math.max(0, 4200 - painkillerApplied)/*ticks since applied (painkillerApplied=effect countdown)*/);
        if (alleviationCache < 0.1) alleviationCache = 0;
        painkillerUpdateInterval = 60;
        return alleviationCache;
    }

    public double getRaw() {
        //"raw" means without painkiller effect
        if (pain > 4.0) pain = 4.0;
        else if (pain < 0.0) pain = 0.0;
        return pain;
    }

    public double getReal() {
        return Math.max(0, getRaw() - getPainkillerAlle());
    }

    public void setRaw(double val) {
        if (Double.isNaN(val)) {
            Reg.LOGGER.error(this.getClass().getSimpleName() + ": Val is NaN");
            return;
        }
        if (val > 4.0) val = 4.0;
        else if (val < 0.0) val = 0.0;
        pain = val;
    }

    @Deprecated
    public void setReal(double val) {
        val += alleviationCache;
        setRaw(val);
    }

    public void setAlleviationCache(double val) {
        if (Double.isNaN(val)) {
            Reg.LOGGER.error(this.getClass().getSimpleName() + ": Val is NaN");
            return;
        }
        if (val > 4.0) val = 4.0;
        else if (val < 0.0) val = 0.0;
        alleviationCache = val;
        painkillerUpdateInterval = 60;
    }

    public void addRaw(double val) {
        setRaw(pain + val);
    }

    public void applyPainkiller() {
        if (alleviationCache < 1.4) painkillerApplied = (painkillerApplied > 0 ? 4200 - 500 : 4200);
        else painkillerApplied = 4200 - 600;
    }

    public int getPainkillerApplied() {
        return painkillerApplied;
    }

    public void setPainkillerApplied(int val) {
        if (val > 4200) val = 4200;
        else if (val < 0) val = 0;
        painkillerApplied = val;
    }

    public void tick() {
        addRaw(pain < 1.0 ? -0.0003 : (pain > 3.0 ? -0.0012 : -0.0008));
        if (painkillerApplied > 0) --painkillerApplied;
    }

    public void reset() {
        pain = alleviationCache = 0.0;
        painkillerApplied = painkillerUpdateInterval = 0;
    }

}
