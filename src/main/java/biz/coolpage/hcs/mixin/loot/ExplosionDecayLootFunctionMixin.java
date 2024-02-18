package biz.coolpage.hcs.mixin.loot;

import biz.coolpage.hcs.util.LootHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ExplosionDecayLootFunction;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExplosionDecayLootFunction.class)
public class ExplosionDecayLootFunctionMixin {
    //@See SurvivesExplosionLootCondition
    @Inject(at = @At("HEAD"), method = "process", cancellable = true)
    public void process(ItemStack stack, LootContext context, @NotNull CallbackInfoReturnable<ItemStack> cir) {
        LootHelper.delSpecificLoot(context, cir, ItemStack.EMPTY);
    }
}
