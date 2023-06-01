package com.hcs.main.manager;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

public class SanityManager {
    private BigDecimal sanity = new BigDecimal("1.0");
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
        sanity = new BigDecimal(String.format("%.6f", val));
    }

    public void add(float val) {
        if (Float.isNaN(val)) {
            new NumberFormatException("Val is NaN").printStackTrace();
            return;
        }
        sanity = sanity.add(new BigDecimal(String.format("%.6f", val)));
        if (sanity.compareTo(ONE) > 0) sanity = ONE;
        else if (sanity.compareTo(ZERO) < 0) sanity = ZERO;
    }

    public void reset() {
        add(1.0F);
    }

}
