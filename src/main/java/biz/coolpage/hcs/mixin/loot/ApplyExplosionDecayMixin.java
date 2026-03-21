package biz.coolpage.hcs.mixin.loot;

import biz.coolpage.hcs.util.LootHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ApplyExplosionDecay.class)
public class ApplyExplosionDecayMixin { // ExplosionDecayLootFunctionMixin
    @Inject(at = @At("HEAD"), method = "run", cancellable = true)
    public void process(ItemStack stack, LootContext context, @NotNull CallbackInfoReturnable<ItemStack> cir) {
        LootHelper.delSpecificLoot(context, cir, ItemStack.EMPTY);
    }
}