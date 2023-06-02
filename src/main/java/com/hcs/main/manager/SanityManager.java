package com.hcs.main.manager;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

public class SanityManager {
    private BigDecimal sanity = new BigDecimal("1.0");
    private float lastSanity = 1.0F;
    //Don't calculate difference between sanity and lastSanity when in InGameHud as it refreshes much faster than ticks() in PlayerEntity and cause twinkle of arrow which indicates trend of rising and falling
    private float sanDifference = 0.0F;
    public static final String SANITY_NBT = "hcs_sanity";

    public float get() {
        if (sanity.compareTo(ONE) > 0) sanity = ONE;
        else if (sanity.compareTo(ZERO) < 0) sanity = ZERO;
        return sanity.floatValue();
    }

    public void set(float val) {
        if (Float.isNaN(val)) {
            new NumberFormatException("Val is NaN").printStackTrace();
            return;
        }
        if (val > 1.0F) val = 1.0F;
        else if (val < 0.0F) val = 0.0F;
        sanity = new BigDecimal(String.format("%.7f", val));
    }

    public void add(float val) {
        set(sanity.floatValue() + val);
    }

    public void reset() {
        add(1.0F);
        lastSanity = 1.0F;
        updateDifference();
    }

    public float getDifference() {
        return sanDifference;
    }

    public void setDifference(float val) {
        sanDifference = val;
    }

    public void updateDifference() {
        sanDifference = sanity.floatValue() - lastSanity;
        lastSanity = sanity.floatValue();
    }


}
