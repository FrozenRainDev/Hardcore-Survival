package com.hcs.mixin.block;

import net.minecraft.block.AbstractBlock.Settings;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Settings.class)
public abstract class BlockSettingsMixin {
    //There is no better solution up to now
    @Shadow float hardness, resistance;
    @Unique
    private static int number = 0, number2 = 0;

    @Inject(method = "breakInstantly", at = @At("RETURN"), cancellable = true)
    private void breakInstantly(@NotNull CallbackInfoReturnable<Settings> cir) {
        Settings sets = cir.getReturnValue();
        sets.strength(0.06F);
        cir.setReturnValue(sets);
    }

    @Inject(method = "strength(F)Lnet/minecraft/block/AbstractBlock$Settings;", at = @At("RETURN"), cancellable = true)
    private void strength(float strength, @NotNull CallbackInfoReturnable<Settings> cir) {
        Settings sets = cir.getReturnValue();
        if (this.hardness == 2.5F) {
            if (number == 0) {
                /*
                Protect chest from explosion
                In the Blocks.class, the hardness = 2.5F is registered firstly with chest
                NOTE: If without "static",number++ only works partially, number=0 is still global
                 */
                ++number;
                sets.resistance(3600000.0F);
                cir.setReturnValue(sets);
            }
        }
    }

    //Another way is to Mixin Block.java onDestroyedByExplosion()
    //likewise, break block hook can be replaced by Block.java dropStacks()
    @Inject(method = "strength(FF)Lnet/minecraft/block/AbstractBlock$Settings;", at = @At("RETURN"), cancellable = true)
    private void strength(float hardness, float resistance, @NotNull CallbackInfoReturnable<Settings> cir) {
        Settings sets = cir.getReturnValue();
        if (this.hardness == 3.0F && this.resistance == 6.0F) {
            if (number2 == 0) {
                ++number2;
                //protect gold block from explosion
                sets.resistance(100.0F);
                cir.setReturnValue(sets);
            }
        }
    }

}
