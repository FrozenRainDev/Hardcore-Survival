package com.hcs.main.manager;

public class StatusManager {
    private float exhaustion = 0.0F; //A field in HungerManager that needs to used in InGameHudMixin
    private int recentAttackTicks = 0; //Increase when player attacks an entity; Decrease over time
    private int recentMiningTicks = 0;
    private int recentHasColdWaterBagTicks = 0;
    private int recentHasHotWaterBagTicks = 0;
    private int maxExpLevelReached = 0;
    private int recentLittleOvereatenTicks = 0;
    private boolean hasDecimalFoodLevel = false;
    public static final String MAX_LVL_NBT = "hcs_max_lvl_reached";

    public void reset(int lvlReached) {
        exhaustion = 0.0F;
        recentAttackTicks = 0;
        recentMiningTicks = 0;
        recentHasColdWaterBagTicks = 0;
        recentHasHotWaterBagTicks = 0;
        maxExpLevelReached = lvlReached;
    }

    public float getExhaustion() {
        return exhaustion;
    }

    public void setExhaustion(float val) {
        exhaustion = val;
    }

    public int getRecentAttackTicks() {
        return recentAttackTicks;
    }

    public void setRecentAttackTicks(int val) {
        recentAttackTicks = val;
    }

    public int getRecentMiningTicks() {
        return recentMiningTicks;
    }

    public void setRecentMiningTicks(int val) {
        recentMiningTicks = val;
    }

    public int getRecentHasColdWaterBagTicks() {
        return recentHasColdWaterBagTicks;
    }

    public void setRecentHasColdWaterBagTicks(int val) {
        recentHasColdWaterBagTicks = val;
    }

    public int getRecentHasHotWaterBagTicks() {
        return recentHasHotWaterBagTicks;
    }

    public void setRecentHasHotWaterBagTicks(int val) {
        recentHasHotWaterBagTicks = val;
    }

    public int getMaxExpLevelReached() {
        return maxExpLevelReached;
    }

    public void setMaxExpLevelReached(int val) {
        maxExpLevelReached = val;
    }

    public int getRecentLittleOvereatenTicks() {
        return recentLittleOvereatenTicks;
    }

    public void setRecentLittleOvereatenTicks(int val) {
        recentLittleOvereatenTicks = val;
    }

    public boolean hasDecimalFoodLevel() {
        return hasDecimalFoodLevel;
    }

    public void setHasDecimalFoodLevel(boolean val) {
        hasDecimalFoodLevel = val;
    }


}
