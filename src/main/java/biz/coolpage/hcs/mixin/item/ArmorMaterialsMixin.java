package biz.coolpage.hcs.mixin.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorMaterials.class)
public class ArmorMaterialsMixin {
    @Inject(method = "getDefenseForType", at = @At("HEAD"), cancellable = true)
    public void getDefenseForType(ArmorItem.Type type, CallbackInfoReturnable<Integer> cir) {
        // Check by converting the Mixin object back to an enum constant
        ArmorMaterials material = (ArmorMaterials) (Object) this;

        if (material == ArmorMaterials.LEATHER) {
            switch (type) {
                case HELMET -> cir.setReturnValue(0);
                case CHESTPLATE -> cir.setReturnValue(1);
                case LEGGINGS -> cir.setReturnValue(0);
                case BOOTS -> cir.setReturnValue(0);
            }
        } else if (material == ArmorMaterials.IRON || material == ArmorMaterials.CHAIN) {
            switch (type) {
                case HELMET -> cir.setReturnValue(3);
                case CHESTPLATE -> cir.setReturnValue(4);
                case LEGGINGS -> cir.setReturnValue(2);
                case BOOTS -> cir.setReturnValue(1);
            }
        } else if (material == ArmorMaterials.GOLD) {
            switch (type) {
                case HELMET -> cir.setReturnValue(2);
                case CHESTPLATE -> cir.setReturnValue(2);
                case LEGGINGS -> cir.setReturnValue(1);
                case BOOTS -> cir.setReturnValue(1);
            }
        } else if (material == ArmorMaterials.DIAMOND) {
            switch (type) {
                case HELMET -> cir.setReturnValue(4);
                case CHESTPLATE -> cir.setReturnValue(5);
                case LEGGINGS -> cir.setReturnValue(3);
                case BOOTS -> cir.setReturnValue(2);
            }
        } else if (material == ArmorMaterials.NETHERITE) {
            switch (type) {
                case HELMET -> cir.setReturnValue(6);
                case CHESTPLATE -> cir.setReturnValue(8);
                case LEGGINGS -> cir.setReturnValue(4);
                case BOOTS -> cir.setReturnValue(2);
            }
        }
    }
}