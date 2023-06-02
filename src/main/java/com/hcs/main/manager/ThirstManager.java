package com.hcs.main.manager;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

public class ThirstManager {
    /*
    variable thirst↓ means thirsty↑
    BigDecimal is used to avoid inaccurate calculations
    */

    private BigDecimal thirst = new BigDecimal("1.0");
    private float saturation = 0.05F;
    private float thirstRateAffectedByTemp = 1.0F;

    public static final String THIRST_NBT = "hcs_thirst";
    public static final String THIRST_SATURATION_NBT = "hcs_thirst_saturation";

    public float get() {
        if (thirst.compareTo(ONE) > 0) thirst = ONE;//>1
        else if (thirst.compareTo(ZERO) < 0) thirst = ZERO;//<0
        return thirst.floatValue();
    }

    public void set(float val) {
        if (Float.isNaN(val)) {
            new NumberFormatException("Val is NaN").printStackTrace();
            return;
        }
        if (val > 1.0F) val = 1.0F;
        else if (val < 0.0F) val = 0.0F;
        thirst = new BigDecimal(String.format("%.7f", val));
    }

    public void add(float val) {
        if (Float.isNaN(val)) {
            new NumberFormatException("Val is NaN").printStackTrace();
            return;
        }
        if (val < 0.01F) {
            //Decrease; <0 and <0F and <0.0F are invalid
            if (saturation <= 0.0F) {
                saturation = 0.0F;
                //Slower thirst rate when thirsty
                float rate = 1.0F;
                if (thirst.compareTo(new BigDecimal("0.1")) < 0) rate = 0.25F;
                else if (thirst.compareTo(new BigDecimal("0.2")) < 0) rate = 0.6F;
                else if (thirst.compareTo(new BigDecimal("0.3")) < 0) rate = 0.8F;
                addDirectly(val * rate * getThirstRateAffectedByTemp());
            } else saturation += val;
        } else {
            saturation = Math.min(saturation + val * 0.33F, 0.12F);
            addDirectly(val);
        }
    }

    public void addDirectly(float val) {
        set(thirst.floatValue() + val);
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float val) {
        if (Float.isNaN(val)) {
            new NumberFormatException("Val is NaN").printStackTrace();
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
        addDirectly(1.0F);
        setSaturation(0.05F);
        setThirstRateAffectedByTemp(1.0F);
    }
}
