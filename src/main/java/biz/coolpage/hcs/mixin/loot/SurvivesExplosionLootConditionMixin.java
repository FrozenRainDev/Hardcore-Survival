package biz.coolpage.hcs.mixin.loot;

import biz.coolpage.hcs.util.LootHelper;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.context.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SurvivesExplosionLootCondition.class)
public class SurvivesExplosionLootConditionMixin {
    // @See ExplosionDecayLootFunctionMixin NOTES: ExperienceDroppingBlock won't trigger
    @Inject(at = @At("HEAD"), method = "test(Lnet/minecraft/loot/context/LootContext;)Z", cancellable = true)
    public void test(LootContext lootContext, CallbackInfoReturnable<Boolean> cir) {
        LootHelper.delSpecificLoot(lootContext, cir, false);
    }
}
