package com.hcs.status.manager;

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
    private int oxygenLackLevel = 0;
    private int oxygenGenLevelAccumulation = 0;
    private int oxygenGenLevel = 0;
    private boolean shouldLockDestroying = false; //Client only
    private int soulImpairedStat = 0; //Server only
    public static String IS_SOUL_IMPAIRED_NBT = "hcs_is_soul_impaired";

    private int recentSleepTicks = 0;

    public void reset(int lvlReached, int soulImpaired) {
        exhaustion = 0.0F;
        recentAttackTicks = 0;
        recentMiningTicks = 0;
        recentHasColdWaterBagTicks = 0;
        recentHasHotWaterBagTicks = 0;
        maxExpLevelReached = lvlReached;
        setSoulImpairedStat(soulImpaired);
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

    public int getOxygenLackLevel() {
        return oxygenLackLevel;
    }

    public void setOxygenLackLevel(int val) {
        oxygenLackLevel = val;
    }

    public void addOxygenGen() {
        oxygenGenLevelAccumulation += 1;
    }

    public int getOxygenGenLevel() {
        return oxygenGenLevel;
    }

    public void setOxygenGenLevel(int val) {
        oxygenGenLevel = val;
    }


    public void updateOxygenGen() {
        oxygenGenLevel = Math.min(2, oxygenGenLevelAccumulation);
        oxygenGenLevelAccumulation = 0;
    }

    public int getFinalOxygenLackLevel() {
        return oxygenLackLevel - oxygenGenLevel;
    }

    public boolean lockDestroying() {
        return shouldLockDestroying;
    }

    public void setLockDestroying(boolean val) {
        shouldLockDestroying = val;
    }

    public int getSoulImpairedStat() {
        if (soulImpairedStat > 4) soulImpairedStat = 4;
        else if (soulImpairedStat < 0) soulImpairedStat = 0;
        return soulImpairedStat;
    }

    public void setSoulImpairedStat(int val) {
        if (val > 4) val = 4;
        else if (val < 0) val = 0;
        soulImpairedStat = val;
    }

    public int getRecentSleepTicks() {
        return recentSleepTicks;
    }

    public void setRecentSleepTicks(int val) {
        recentSleepTicks = val;
    }
}
