package com.hcs.status.manager;

public class WetnessManager {
    private double wetness = 1.0;
    public static final String WETNESS_NBT = "hcs_wetness";

    public double get() {
        if (wetness > 1.0) wetness = 1.0;
        else if (wetness < 0.0) wetness = 0.0;
        return wetness;
    }

    public void set(double val) {
        if (Double.isNaN(val)) {
            new NumberFormatException("Val is NaN").printStackTrace();
            return;
        }
        if (val > 1.0) val = 1.0;
        else if (val < 0.0) val = 0.0;
        wetness = val;
    }

    public void add(double val) {
        set(wetness + val);
    }

    public void reset() {
        add(1.0);
    }

}
