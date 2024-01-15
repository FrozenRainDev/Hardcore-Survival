package biz.coolpage.hcs.status.manager;

import biz.coolpage.hcs.Reg;

public class TemperatureManager {
    public static final String TEMPERATURE_NBT = "hcs_temperature";
    public static final String TEMPERATURE_SATURATION_NBT = "hcs_temperature_saturation";
    public static final float CHANGE_SPAN = 0.0005F;
    public static final float MAX_SATURATION = 0.4F; //0.5 originally
    private double temperature = 0.5;
    private float envTempCache = 0.5F;
    private float feelTempCache = 0.5F;
    private float ambientBlocksTemp = 0.0F;
    //Used for debug when have both hot sources and cold sources
    private float ambientBlocksTempDecrement = 0.0F;
    private float cachedAmbientBlocksTemp = 0.0F;
    private boolean hasHeatSource = false;
    //The excess part of temperature out of [0,1], calculated by ticks
    private float saturation = 0.0F;
    private int trendType = 0; //An indicator only works when determine onInteract nbt either HHE or HHES for HotWaterBottleItem
    // -1: player's body temp will decrease, 0: stabilize, 1: increase

    public double get() {
        if (temperature > 1.0) temperature = 1.0;
        else if (temperature < 0.0) temperature = 0.0;
        return temperature;
    }

    public void set(double val) {
        if (Double.isNaN(val)) {
            Reg.LOGGER.error(this.getClass().getSimpleName() + "/setRealPain(): Val is NaN");
            return;
        }
        if (val > 1.0F) val = 1.0F;
        else if (val < 0.0F) val = 0.0F;
        temperature = val;
    }

    public void add(double val) {
        if (Double.isNaN(val)) {
            Reg.LOGGER.error(this.getClass().getSimpleName() + "/addRawPain(): Val is NaN");
            return;
        }
        double expected = temperature + val;
        if (saturation > 0.0F && expected < 1.0 && expected > 0.0)
            addSaturation(-Math.abs(val) * 2);
        else if (saturation < 0.0F) saturation = 0;
        else {
            temperature = expected;
            if (temperature > 1.0) {
                temperature = 1.0;
                addSaturation(Math.abs(val));
            } else if (temperature < 0.0) {
                temperature = 0.0;
                addSaturation(Math.abs(val));
            }
        }
    }

    public void reset() {
        temperature = 0.5;
        envTempCache = 0.5F;
        ambientBlocksTemp = 0.0F;
        ambientBlocksTempDecrement = 0.0F;
        cachedAmbientBlocksTemp = 0.0F;
        hasHeatSource = false;
        saturation = 0.0F;
    }

    public void addAmbient(float val) {
        if (Float.isNaN(val)) {
            Reg.LOGGER.error(this.getClass().getSimpleName() + "/addAmbient(): Val is NaN");
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
            Reg.LOGGER.error(this.getClass().getSimpleName() + "/setEnvTempCache(): Val is NaN");
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

    public void addSaturation(double val) {
        addSaturation((float) val);
    }

    public void addSaturation(float val) {
        if (Float.isNaN(val)) {
            Reg.LOGGER.error(this.getClass().getSimpleName() + "/addSaturation(): Val is NaN");
            return;
        }
        saturation += val;
        if (saturation > MAX_SATURATION) saturation = MAX_SATURATION;
        else if (saturation < 0.0F) saturation = 0.0F;
    }

    public void setSaturation(float val) {
        if (Float.isNaN(val)) {
            Reg.LOGGER.error(this.getClass().getSimpleName() + "/setSaturation(): Val is NaN");
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
