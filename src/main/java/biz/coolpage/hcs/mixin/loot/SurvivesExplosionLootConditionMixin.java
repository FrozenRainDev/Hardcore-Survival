package biz.coolpage.hcs.mixin.loot;

import biz.coolpage.hcs.util.LootHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExplosionCondition.class)
public class SurvivesExplosionLootConditionMixin {
    @Inject(at = @At("HEAD"), method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z", cancellable = true)
    public void test(LootContext lootContext, CallbackInfoReturnable<Boolean> cir) {
        LootHelper.delSpecificLoot(lootContext, cir, false);
    }
}