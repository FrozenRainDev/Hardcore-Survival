package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.Hcs;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Shadow
    private int maxDamage;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Item.Properties properties, CallbackInfo ci) {
        Hcs.print("onInit called called");
        // 检查当前实例是否是铁制工具
        Object self = this;

        if (self instanceof TieredItem tieredItem) {
            // 判断其材质是否为铁 (Tiers.IRON)
            if (tieredItem.getTier() == Tiers.IRON) {
                // 强制修改耐久度为 128
                this.maxDamage = 128; // todo 谁让你这样写，篡改@Shadow
            }
        }

        // 特殊处理：铁剑不属于 TieredItem (在某些版本中)，或者你想单独处理剑
        if (self instanceof SwordItem swordItem) {
            if (swordItem.getTier() == Tiers.IRON) {
                this.maxDamage = 128;
            }
        }
    }
}