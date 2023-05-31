package com.hcs.mixin.item;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.Settings.class)
public class ItemSettingsMixin {
    @Shadow
    int maxCount;
    private static int number = 0;

    @Deprecated
    @Inject(method = "maxCount", at = @At("RETURN"))
    public void maxCount(int maxCount, CallbackInfoReturnable<Item.Settings> cir) {
        /*
        if (this.maxCount == 1) {
            ++number;
            if (number == 68 && maxCount == 1) {
                //Potions
                Item.Settings sets = cir.getReturnValue();
                sets.maxCount(16);
                cir.setReturnValue(sets);
            }
        }
         */
    }

}
