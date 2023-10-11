package com.hcs.status.manager;

import com.hcs.Reg;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import static com.hcs.recipe.CustomDryingRackRecipe.IS_RAW_MEAT;

public class DiseaseManager {
    public static final String PARASITE_NBT = "hcs_parasite";

    public static double getParasitePossibility(Item item) {
        if (IS_RAW_MEAT.test(item)) {
            if (item == Items.PORKCHOP || item == Reg.ANIMAL_VISCERA) return 0.08;
            return 0.04;
        }
        if (item == Items.ROTTEN_FLESH) return 0.25;
        else if (item == Reg.ROT) return 0.1;
        return -1.0;//impossible
    }

    private double parasite = 0.0; //[0,3] 0~1 early stage 1~2 medium term 2~3 later period

    public double getParasite() {
        if (parasite < 0.0) parasite = 0.0;
        else if (parasite > 3.0) parasite = 3.0;
        return parasite;
    }

    public void setParasite(double val) {
        if (Double.isNaN(val)) {
            Reg.LOGGER.error(this.getClass().getSimpleName() + "/setParasite(): Val is NaN");
            return;
        }
        if (val > 3.0) val = 3.0;
        else if (val < 0.0) val = 0.0;
        parasite = val;
    }

    public void addParasite(double val) {
        setParasite(getParasite() + val);
    }

    public void tick() {
        if (this.parasite > 0.01) addParasite(0.000035);
    }

    public void reset() {
        parasite = 0.0;
    }
}
