package biz.coolpage.hcs.status.manager;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.util.EntityHelper;

public class InjuryManager {
    public static final String PAIN_NBT = "hcs_pain";
    public static final String PAINKILLER_APPLIED_NBT = "hcs_painkiller";
    public static final String BLEEDING_NBT = "hcs_bleeding";
    public static final String FRACTURE_NBT = "hcs_fracture";
    private double pain = 0.0, alleviationCache = 0.0; //range: [0, 4]
    private int painkillerApplied = 0, painkillerUpdateInterval = 0;
    private double bleeding = 0.0; //range: [0, 5]; note that [0, 1] won't result in bleeding debuff
    private double fracture = 0.0;

    public void tick() {
        addRawPain(pain < 1.0 ? -0.0004 : (pain > 3.0 ? -0.0012 : -0.0008)); //Panic self-recovery
        if (bleeding < 3.0) addBleeding(bleeding < 1.0 ? -0.005 : -0.001); //Bleeding self-stopping
        if (painkillerApplied > 0) --painkillerApplied;
        addFracture(-0.000007);
    }

    public void reset() {
        pain = alleviationCache = 0.0;
        painkillerApplied = painkillerUpdateInterval = 0;
        bleeding = 0.0;
        fracture = 0.0;
    }

    public double getPainkillerAlle() {
        if (painkillerUpdateInterval > 0) {
            // Refresh alleviation every 60 ticks to avoid too much calculation
            --painkillerUpdateInterval;
            return alleviationCache;
        }
        alleviationCache = EntityHelper.PLASMA_CONCENTRATION.apply(Math.max(0, 4200 - painkillerApplied)); //4200 - painkillerApplied: ticks since applied (painkillerApplied=effect countdown)
        if (alleviationCache < 0.1) alleviationCache = 0;
        painkillerUpdateInterval = 60;
        return alleviationCache;
    }

    public double getRawPain() {
        //"raw" means without painkiller effect
        if (pain > 4.0) pain = 4.0;
        else if (pain < 0.0) pain = 0.0;
        return pain;
    }

    public double getRealPain() {
        return Math.max(0, getRawPain() - getPainkillerAlle());
    }

    public void setRawPain(double val) {
        if (Double.isNaN(val)) {
            Reg.LOGGER.error(this.getClass().getSimpleName() + "/setParasite(): Val is NaN");
            return;
        }
        if (val > 4.0) val = 4.0;
        else if (val < 0.0) val = 0.0;
        pain = val;
    }

    @Deprecated
    public void setRealPain(double val) {
        val += alleviationCache;
        setRawPain(val);
    }

    public void setAlleviationCache(double val) {
        if (Double.isNaN(val)) {
            Reg.LOGGER.error(this.getClass().getSimpleName() + "/setAlleviationCache(): Val is NaN");
            return;
        }
        if (val > 4.0) val = 4.0;
        else if (val < 0.0) val = 0.0;
        alleviationCache = val;
        painkillerUpdateInterval = 60;
    }

    public void addRawPain(double val) {
        setRawPain(pain + val);
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

    public double getBleeding() {
        if (bleeding > 5.0) bleeding = 5.0;
        else if (bleeding < 0.0) bleeding = 0.0;
        return bleeding;
    }

    public void setBleeding(double val) {
        if (Double.isNaN(val)) {
            Reg.LOGGER.error(this.getClass().getSimpleName() + "/setBleeding(): Val is NaN");
            return;
        }
        if (val > 5.0) val = 5.0;
        else if (val < 0.0) val = 0.0;
        bleeding = val;
    }

    public void addBleeding(double val) {
        if (val >= 1.0 && val <= 2.0) val = Math.max(1.0, val * 0.75);
        setBleeding(getBleeding() + val);
    }

    public double getFracture() {
        if (fracture > 1.0) fracture = 1.0;
        else if (fracture < 0.0) fracture = 0.0;
        return fracture;
    }

    public void setFracture(double val) {
        if (Double.isNaN(val)) {
            Reg.LOGGER.error(this.getClass().getSimpleName() + "/setFracture(): Val is NaN");
            return;
        }
        if (val > 1.0) val = 1.0;
        else if (val < 0.0) val = 0.0;
        fracture = val;
    }

    public void addFracture(double val) {
        setFracture(getFracture() + val);
    }
}
