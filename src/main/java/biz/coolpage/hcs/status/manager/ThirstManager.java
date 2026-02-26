package biz.coolpage.hcs.status.manager;

import biz.coolpage.hcs.Reg;

@SuppressWarnings("LoggingSimilarMessage")
public class ThirstManager {
    private double thirst = 1.0;
    private float saturation = 0.05F;
    private float thirstRateAffectedByTemp = 1.0F;

    public static final String THIRST_NBT = "hcs_thirst";
    public static final String THIRST_SATURATION_NBT = "hcs_thirst_saturation";

    public double get() {
        if (thirst > 1.0) thirst = 1.0;
        else if (thirst < 0.0) thirst = 0.0;
        return thirst;
    }

    public void set(double val) {
        if (Double.isNaN(val)) {
            Reg.LOGGER.error("{}: Val is NaN", this.getClass().getSimpleName());
            return;
        }
        if (val > 1.0F) val = 1.0F;
        else if (val < 0.0F) val = 0.0F;
        thirst = val;
    }

    public void add(double val) {
        if (Double.isNaN(val)) {
            Reg.LOGGER.error("{}: Val is NaN", this.getClass().getSimpleName());
            return;
        }
        if (val < 0.01F) {
            //Decrease; <0 and <0F and <0.0F are invalid
            if (saturation <= 0.0F) {
                saturation = 0.0F;
                //Slower thirst rate when thirsty
                float rate = 1.0F;
                if (thirst < 0.1) rate = 0.25F;
                else if (thirst < 0.2) rate = 0.4F;
                else if (thirst < 0.3) rate = 0.6F;
                addDirectly(val * rate * getThirstRateAffectedByTemp());
            } else saturation += (float) val;
        } else {
            saturation = Math.min(saturation + (float) val * 0.33F, 0.12F);
            addDirectly(val);
        }
    }

    public void addDirectly(double val) {
        set(thirst + val);
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float val) {
        if (Float.isNaN(val)) {
            Reg.LOGGER.error("{}/setSaturation(): Val is NaN", this.getClass().getSimpleName());
            return;
        }
        saturation = val;
    }


    public float getThirstRateAffectedByTemp() {
        return thirstRateAffectedByTemp;
    }

    public void setThirstRateAffectedByTemp(float val) {
        thirstRateAffectedByTemp = val;
    }

    public void updateThirstRateAffectedByTemp(float envTemp, float bodyTemp) {
        float rate = 1.0F;
        if (envTemp < 0.3) rate = Math.max(0.8F * envTemp + 0.76F, 0.6F);
        else if (bodyTemp > 0.6) rate = (bodyTemp + 0.4F) * (bodyTemp + 0.4F);
        setThirstRateAffectedByTemp(rate);
    }

    public void reset() {
        addDirectly(1.0);
        setSaturation(0.05F);
        setThirstRateAffectedByTemp(1.0F);
    }
}
