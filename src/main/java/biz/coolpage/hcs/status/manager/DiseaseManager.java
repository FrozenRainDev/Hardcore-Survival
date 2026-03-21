package biz.coolpage.hcs.status.manager;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.status.HcsEffectss;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;

import static biz.coolpage.hcs.recipe.DryingRackRecipe.IS_RAW_MEAT;

public class DiseaseManager {
    public static final String PARASITE_NBT = "hcs_parasite";
    public static final String COLD_NBT = "hcs_cold";
    private double parasite = 0.0; //[0,3] 0~1 early stage 1~2 medium term 2~3 later period
    private double cold = 0.0; //[0,2] 0~1 pre 1~2 symptom appearing

    // facade
    public static void updateRawFoodDetriment(Item item, Object entity) {
        if (item != null && entity instanceof ServerPlayer player
                && Configs.isEnabled(player, Configs.FOOD_POISON)) {
            double poss = getBasicPoisonPoss(item);
            if (Math.random() < (poss * 0.5))
                (((StatAccessor) player)).getDiseaseManager().addParasite(0.12);
            if (Math.random() < (poss * 4) || isFoodPoisonous(item.getFoodProperties()))
                player.addEffect(new MobEffectInstance(HcsEffectss.FOOD_POISONING, 1200, 0, false, false, true));
        }
    }

    private static double getBasicPoisonPoss(Item item) {
        if (item == Items.PORKCHOP || item == Hcs.ANIMAL_VISCERA) return 0.22;
        if (item == Items.ROTTEN_FLESH || item == Hcs.ROT || item == Hcs.BAT_WINGS) return 0.26;
        if (IS_RAW_MEAT.test(item)) return 0.17;
        return -1.0;
    }

    private static boolean isFoodPoisonous(FoodProperties component) {
        if (component != null) {
            var effects = component.getEffects();
            if (effects != null) {
                for (var effectFloatPair : effects) {
                    var first = effectFloatPair.getFirst();
                    if (first != null && first.getEffect() == MobEffects.POISON) return true;
                }
            }
        }
        return false;
    }

    public double getParasite() {
        if (parasite < 0.0) parasite = 0.0;
        else if (parasite > 3.0) parasite = 3.0;
        return parasite;
    }

    public void setParasite(double val) {
        if (Double.isNaN(val)) {
            Hcs.error("{}/setParasite(): Val is NaN", this.getClass().getSimpleName());
            return;
        }
        if (val > 3.0) val = 3.0;
        else if (val < 0.0) val = 0.0;
        parasite = val;
    }

    public void addParasite(double val) {
        setParasite(getParasite() + val);
    }

    public double getCold() {
        if (cold < 0.0) cold = 0.0;
        else if (cold > 2.0) cold = 2.0;
        return cold;
    }

    public void setCold(double val) {
        if (Double.isNaN(val)) {
            Hcs.error("{}/setCold(): Val is NaN", this.getClass().getSimpleName());
            return;
        }
        if (val > 2.0) val = 2.0;
        else if (val < 0.0) val = 0.0;
        cold = val;
    }

    public void addCold(double val) {
        setCold(getCold() + val);
    }

    public void tick(boolean shouldReduceCold) {
        if (this.parasite > 0.01) addParasite(0.000035);
        if (shouldReduceCold) this.addCold(-0.000015);
    }

    public void reset() {
        parasite = 0.0;
        cold = 0.0;
    }
}