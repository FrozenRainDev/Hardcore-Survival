package biz.coolpage.hcs.status.manager;

import biz.coolpage.hcs.config.HcsDifficulty;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import static biz.coolpage.hcs.util.EntityHelper.toPlayer;

public class StatusManager {
    public static final String MAX_LVL_NBT = "hcs_max_lvl_reached";
    public static final String IS_SOUL_IMPAIRED_NBT = "hcs_is_soul_impaired";
    public static final String IN_DARKNESS_TICKS_NBT = "hcs_in_darkness";
    public static final String ENTER_TIMES_NBT = "hcs_enter_curr_wld";
    private float exhaustion = 0.0F; //A field in HungerManager that needs to used in InGameHudMixin
    private int recentAttackTicks = 0; //Increase when player attacks an entity; Decrease over time
    private int recentMiningTicks = 0;
    private int recentHasColdWaterBagTicks = 0;
    private int recentHasHotWaterBagTicks = 0;
    private int maxExpLevelReached = 0;
    private int recentLittleOvereatenTicks = 0;
    private boolean hasDecimalFoodLevel = false;
    private int oxygenLackLevel = 0;
    private int oxygenGenLevelAccumulation = 0;
    private int oxygenGenLevel = 0;
    private boolean shouldLockDestroying = false; //Client only
    private int soulImpairedStat = 0; //Server only
    private int recentSleepTicks = 0;
    private int recentWetTicks = 0;
    private int inDarknessTicks = 0, lastInDarknessTicks = 0;
    private int bareDiggingTicks = 0; //Ticks of digging blocks with bare hand; Server only
    private int enterCurrWldTimes = 0;
    private int stonesSmashed = 0;
    private boolean hasCheckInitTips = false; //Client side only
    Enum<HcsDifficulty.HcsDifficultyEnum> hcsDifficulty = HcsDifficulty.HcsDifficultyEnum.standard;
    private boolean hasDarknessEnvelopedDebuff = false; //Server side only -- add all debuffs in ServerPlayerEntityMixin/tick() so the order of hcs debuffs won't change randomly
    private boolean hasHeavyLoadDebuff = false; //Server side only
    private int bandageWorkTicks = 0; //Server side only

    public static int getMaxSoulImpaired(@Nullable LivingEntity entity) {
        return HcsDifficulty.chooseVal(toPlayer(entity), 0, 4, 7);
    }

    public void reset(int lvlReached, int soulImpaired, int smashed, Enum<HcsDifficulty.HcsDifficultyEnum> hcsDifficulty, boolean hasCheckInitTips, int enterCurrWldTimes) {
        setSoulImpairedStat(soulImpaired);
        exhaustion = 0.0F;
        recentAttackTicks = 0;
        recentMiningTicks = 0;
        recentHasColdWaterBagTicks = 0;
        recentHasHotWaterBagTicks = 0;
        maxExpLevelReached = lvlReached;
        recentLittleOvereatenTicks = 0;
        hasDecimalFoodLevel = false;
        oxygenLackLevel = 0;
        oxygenGenLevelAccumulation = 0;
        oxygenGenLevel = 0;
        shouldLockDestroying = false;
        recentSleepTicks = 0;
        recentWetTicks = 0;
        lastInDarknessTicks = inDarknessTicks = 0;
        bareDiggingTicks = 0;
        stonesSmashed = smashed;
        this.hcsDifficulty = hcsDifficulty;
        this.hasCheckInitTips = hasCheckInitTips;
        hasDarknessEnvelopedDebuff = false;
        hasHeavyLoadDebuff = false;
        bandageWorkTicks = 0;
        this.enterCurrWldTimes = enterCurrWldTimes;
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
        if (soulImpairedStat < 0) soulImpairedStat = 0;
        return soulImpairedStat;
    }

    public void setSoulImpairedStat(int val) {//See end of PlayerEntityMixin/ticks(); -- controls max value
        if (val < 0) val = 0;
        soulImpairedStat = val;
    }

    public int getRecentSleepTicks() {
        return recentSleepTicks;
    }

    public void setRecentSleepTicks(int val) {
        recentSleepTicks = val;
    }

    public int getRecentWetTicks() {
        return recentWetTicks;
    }

    public void setRecentWetTicks(int val) {
        recentWetTicks = val;
    }

    public int getInDarknessTicks() {
        if (inDarknessTicks < 0) inDarknessTicks = 0;
        return inDarknessTicks;
    }

    public int getLastInDarknessTicks() {
        return lastInDarknessTicks;
    }

    public void setInDarknessTicks(int val) {
        lastInDarknessTicks = inDarknessTicks;
        inDarknessTicks = Math.min(114514, val);
    }

    @Deprecated
    public int getBareDiggingTicks() {
        return bareDiggingTicks;
    }

    public void setBareDiggingTicks(int val) {
        bareDiggingTicks = val;
    }

    @Deprecated
    public void addBareDiggingTicks() {
        if (bareDiggingTicks < 10000) ++bareDiggingTicks;
    }

    public int getEnterCurrWldTimes() {
        return enterCurrWldTimes;
    }

    public void setEnterCurrWldTimes(int val) {
        enterCurrWldTimes = val;
    }

    public int getStonesSmashed() {
        if (stonesSmashed < 0) stonesSmashed = 0;
        return stonesSmashed;
    }

    public void setStonesSmashed(int val) {
        stonesSmashed = val;
        if (stonesSmashed < 0) stonesSmashed = 0;
        else if (stonesSmashed + val > 114514) stonesSmashed = 114514;
    }

    @Deprecated
    public void addStonesSmashed() {
        setStonesSmashed(getStonesSmashed() + 1);
    }

    public boolean hasShownInitTips() {
        return hasCheckInitTips;
    }

    public void setHasCheckInitTips(boolean hasCheckInitTips) {
        this.hasCheckInitTips = hasCheckInitTips;
    }

    public Enum<HcsDifficulty.HcsDifficultyEnum> getHcsDifficulty() {
        return hcsDifficulty;
    }

    public void setHcsDifficulty(Enum<HcsDifficulty.HcsDifficultyEnum> hcsDifficulty) {
        this.hcsDifficulty = hcsDifficulty;
    }

    public boolean hasDarknessEnvelopedDebuff() {
        return hasDarknessEnvelopedDebuff;
    }

    public void setHasDarknessEnvelopedDebuff(boolean hasDarknessEnvelopedDebuff) {
        this.hasDarknessEnvelopedDebuff = hasDarknessEnvelopedDebuff;
    }

    public boolean hasHeavyLoadDebuff() {
        return hasHeavyLoadDebuff;
    }

    public void setHasHeavyLoadDebuff(boolean hasHeavyLoadDebuff) {
        this.hasHeavyLoadDebuff = hasHeavyLoadDebuff;
    }

    public int getBandageWorkTicks() {
        return bandageWorkTicks;
    }

    public void setBandageWorkTicks(int bandageWorkTicks) {
        this.bandageWorkTicks = bandageWorkTicks;
    }

    public void addBandageWorkTicks(int increment) {
        int result = getBandageWorkTicks() + increment;
        if (result < 0) result = 0;
        setBandageWorkTicks(result);
    }

}
