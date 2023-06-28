package com.hcs.mixin.client.gui;

import com.hcs.misc.HcsEffects;
import com.hcs.misc.accessor.StatAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Screen.class)
public class ScreenMixin {
    @Shadow
    protected MinecraftClient client;

    @Inject(method = "getTooltipFromItem", at = @At("RETURN"))
    public void getTooltipFromItem(ItemStack stack, CallbackInfoReturnable<List<Text>> cir) {
        if (this.client.player != null && this.client.player.hasStatusEffect(HcsEffects.INSANITY) && ((StatAccessor) this.client.player).getSanityManager().get() < 0.1F) {
            List<Text> list = cir.getReturnValue();
            for (Text text : list) {
                if (text instanceof MutableText mutableText) {
                    for (Text siblingText : mutableText.getSiblings()) {
                        if (siblingText instanceof MutableText mutableSibling) mutableSibling.setStyle(Style.EMPTY);
                    }
                    mutableText.setStyle(Style.EMPTY).formatted(Formatting.GRAY).formatted(Formatting.OBFUSCATED);
                }
            }
        }
    }
}
