package biz.coolpage.hcs.mixin.entity;

import net.minecraft.world.entity.animal.WaterAnimal;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterAnimal.class)
public class WaterAnimalMixin { // WaterCreatureEntityMixin
    @Inject(method = "getExperienceReward", at = @At("RETURN"), cancellable = true)
    public void getExperienceReward(@NotNull CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }
}
