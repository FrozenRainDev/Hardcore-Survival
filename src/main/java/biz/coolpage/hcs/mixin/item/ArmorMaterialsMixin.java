package biz.coolpage.hcs.mixin.item;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorMaterials.class)
public class ArmorMaterialsMixin {
    /*
     NOTES:
     Cannot edit finals
     getName() is not invoked spontaneously, so must onInteract getDurability()
     overwriting is both tricky and dangerous
     */
    @Unique
    private int accessibleArmorDurability;

    @Inject(method = "getProtection", at = @At("HEAD"), cancellable = true)
    public void getProtection(ArmorItem.Type type, CallbackInfoReturnable<Integer> cir) {
        switch (accessibleArmorDurability) {
            case 55 -> cir.setReturnValue(0);//leather helmet
            case 80 -> cir.setReturnValue(1);//leather chestplate
            case 75 -> cir.setReturnValue(0);//leather leggings
            case 65 -> cir.setReturnValue(0);//leather boots
            case 165 -> cir.setReturnValue(3);//iron & chain helmet
            case 240 -> cir.setReturnValue(4);//iron & chain chestplate
            case 225 -> cir.setReturnValue(2);//iron & chain leggings
            case 195 -> cir.setReturnValue(1);//iron & chain boots
            case 77 -> cir.setReturnValue(2);//golden helmet
            case 112 -> cir.setReturnValue(2);//golden chestplate
            case 105 -> cir.setReturnValue(1);//golden leggings
            case 91 -> cir.setReturnValue(1);//golden boots
            case 363 -> cir.setReturnValue(4);//diamond helmet
            case 528 -> cir.setReturnValue(5);//diamond chestplate
            case 495 -> cir.setReturnValue(3);//diamond leggings
            case 429 -> cir.setReturnValue(2);//diamond boots
            case 407 -> cir.setReturnValue(6);//netherite helmet
            case 592 -> cir.setReturnValue(8);//netherite chestplate
            case 555 -> cir.setReturnValue(4);//netherite leggings
            case 481 -> cir.setReturnValue(2);//netherite boots
        }
    }

    @Inject(method = "getDurability", at = @At("RETURN"))
    public void getDurability(ArmorItem.Type type, @NotNull CallbackInfoReturnable<Integer> cir) {
        this.accessibleArmorDurability = cir.getReturnValue();
    }
}
