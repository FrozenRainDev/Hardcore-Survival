package com.hcs.main.manager;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

public class TemperatureManager {
    public static final String TEMPERATURE_NBT = "hcs_temperature";
    public static final String TEMPERATURE_SATURATION_NBT = "hcs_temperature_saturation";
    public static final float CHANGE_SPAN = 0.0005F;
    public static final float MAX_SATURATION = 0.4F; //0.5 originally
    private BigDecimal temperature = new BigDecimal("0.5");
    private float envTempCache = 0.5F;
    private float feelTempCache = 0.5F;
    private float ambientBlocksTemp = 0.0F;
    //Used for debug when have both hot sources and cold sources
    private float ambientBlocksTempDecrement = 0.0F;
    private float cachedAmbientBlocksTemp = 0.0F;
    private boolean hasHeatSource = false;
    //The excess part of temperature out of [0,1], calculated by ticks
    private float saturation = 0.0F;
    private int trendType = 0;//An indicator only works when determine use nbt either HHE or HHES for HotWaterBottleItem
    // -1: player's body temp will decrease, 0: stabilize, 1: increase

    public float get() {
        if (temperature.compareTo(ONE) > 0) temperature = ONE;
        else if (temperature.compareTo(ZERO) < 0) temperature = ZERO;
        return temperature.floatValue();
    }

    public void set(float val) {
        if (Float.isNaN(val)) {
            new NumberFormatException("Val is NaN").printStackTrace();
            return;
        }
        if (val > 1.0F) val = 1.0F;
        else if (val < 0.0F) val = 0.0F;
        temperature = new BigDecimal(String.format("%.7f", val));
    }

    public void add(float val) {
        if (Float.isNaN(val)) {
            new NumberFormatException("Val is NaN").printStackTrace();
            return;
        }
        BigDecimal expected = temperature.add(new BigDecimal(String.format("%.7f", val)));
        if (saturation > 0.0F && expected.compareTo(ONE) < 0 && expected.compareTo(ZERO) > 0)
            addSaturation(-Math.abs(val) * 2);
        else if (saturation < 0.0F) saturation = 0;
        else {
            temperature = expected;
            if (temperature.compareTo(ONE) > 0) {
                temperature = ONE;
                addSaturation(Math.abs(val));
            } else if (temperature.compareTo(ZERO) < 0) {
                temperature = ZERO;
                addSaturation(Math.abs(val));
            }
        }
    }

    public void reset() {
        set(0.5F);
        envTempCache = 0.5F;
        ambientBlocksTemp = 0.0F;
        ambientBlocksTempDecrement = 0.0F;
        cachedAmbientBlocksTemp = 0.0F;
        hasHeatSource = false;
        saturation = 0.0F;
    }

    public void addAmbient(float val) {
        if (Float.isNaN(val)) {
            new NumberFormatException("Val is NaN").printStackTrace();
            return;
        }
        if (val > 0.0F) {
            hasHeatSource = true;
            ambientBlocksTemp += val;
        } else ambientBlocksTempDecrement += val;
    }

    public void updateAmbient() {
        if (!hasHeatSource) ambientBlocksTemp += Math.max(ambientBlocksTempDecrement, -0.6F);
        cachedAmbientBlocksTemp = ambientBlocksTemp;
        ambientBlocksTemp = ambientBlocksTempDecrement = 0.0F;
        hasHeatSource = false;
    }

    public float getAmbientCache() {
        return cachedAmbientBlocksTemp;
    }

    public float getEnvTempCache() {
        return envTempCache;
    }

    public void setEnvTempCache(float val) {
        if (Float.isNaN(val)) {
            new NumberFormatException("Val is NaN").printStackTrace();
            return;
        }
        envTempCache = val;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getSaturationPercentage() {
        float percent = saturation / MAX_SATURATION;
        if (percent > 1.0F) percent = 1.0F;
        else if (percent < 0.0F) percent = 0.0F;
        return percent;
    }

    public void addSaturation(float val) {
        if (Float.isNaN(val)) {
            new NumberFormatException("Val is NaN").printStackTrace();
            return;
        }
        saturation += val;
        if (saturation > MAX_SATURATION) saturation = MAX_SATURATION;
        else if (saturation < 0.0F) saturation = 0.0F;
    }

    public void setSaturation(float val) {
        if (Float.isNaN(val)) {
            new NumberFormatException("Val is NaN").printStackTrace();
            return;
        }
        saturation = val;
    }

    public float getFeelTempCache() {
        return feelTempCache;
    }

    public void setFeelTempCache(float val) {
        feelTempCache = val;
    }

    public int getTrendType() {
        return trendType;
    }

    public void setTrendType(int val) {
        if (val < -1 || val > 1) val = 0;
        trendType = val;
    }

}
