package biz.coolpage.hcs.mixin.client.gui;

import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Screen.class)
public class ScreenMixin {
    @Shadow
    protected Minecraft minecraft;

    @Inject(method = "getTooltipFromItem", at = @At("RETURN"))
    private static void getTooltipFromItem(@NotNull Minecraft client, ItemStack stack, CallbackInfoReturnable<List<Component>> cir) {
        if (client.player != null && client.player.hasEffect(HcsEffects.INSANITY.get()) && ((StatAccessor) client.player).getSanityManager().get() < 0.1) {
            List<Component> list = cir.getReturnValue();
            for (Component text : list) {
                if (text instanceof MutableComponent mutableText) {
                    for (Component siblingText : mutableText.getSiblings()) {
                        if (siblingText instanceof MutableComponent mutableSibling) mutableSibling.setStyle(Style.EMPTY);
                    }
                    mutableText.setStyle(Style.EMPTY).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.OBFUSCATED);
                }
            }
        }
    }
}