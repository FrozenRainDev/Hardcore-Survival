package biz.coolpage.hcs.mixin.item;

import net.minecraft.world.item.Tiers;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Tiers.class)
// Fabric: ToolMaterialsMixin; getDurability()
public class TiersMixin {
    @Inject(method = "getUses", at = @At("RETURN"), cancellable = true)
    public void getUses(@NotNull CallbackInfoReturnable<Integer> cir) {
        if (((Object) this) instanceof Tiers tier) {
            if (tier == Tiers.IRON) cir.setReturnValue(128);
            if (tier == Tiers.WOOD) cir.setReturnValue(18);
        }
    }
}
