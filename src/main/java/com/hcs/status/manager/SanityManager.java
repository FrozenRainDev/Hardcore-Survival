package com.hcs.status.manager;

public class SanityManager {
    private double sanity = 1.0;
    private double lastSanity = 1.0;
    //Don't calculate difference between sanity and lastSanity when in InGameHud as it refreshes much faster than ticks() in PlayerEntity and cause twinkle of arrow which indicates trend of rising and falling
    private double sanDifference = 0.0;
    private int panicTicks = 0;
    public static final String SANITY_NBT = "hcs_sanity";

    public double get() {
        if (sanity > 1.0) sanity = 1.0;
        else if (sanity < 0.0) sanity = 0.0;
        return sanity;
    }

    public void set(double val) {
        if (Double.isNaN(val)) {
            new NumberFormatException("Val is NaN").printStackTrace();
            return;
        }
        if (val > 1.0) val = 1.0;
        else if (val < 0.0) val = 0.0;
        sanity = val;
    }

    public void add(double val) {
        set(sanity + val);
    }

    public void reset() {
        add(1.0);
        lastSanity = 1.0;
        updateDifference();
    }

    public double getDifference() {
        return sanDifference;
    }

    public void setDifference(double val) {
        sanDifference = val;
    }

    public void updateDifference() {
        sanDifference = sanity - lastSanity;
        lastSanity = sanity;
    }

    public int getPanicTicks() {
        return panicTicks;
    }

    public void setPanicTicks(int val) {
        panicTicks = val;
    }
}
