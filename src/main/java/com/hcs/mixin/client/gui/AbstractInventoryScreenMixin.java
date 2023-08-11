package com.hcs.mixin.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(value = EnvType.CLIENT)
@Mixin(AbstractInventoryScreen.class)
public abstract class AbstractInventoryScreenMixin {
    @Unique
    private static final float ZOOM_SCALE = 0.75F;

    @Redirect(method = "drawStatusEffectDescriptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I"))
    public int drawStatusEffectDescriptionsInjected(TextRenderer textRenderer, MatrixStack matrices, @NotNull Text text, float x, float y, int color) {
        float descale = 1 / ZOOM_SCALE;
        boolean shouldZoom = !text.getString().contains(":");
        if (shouldZoom) {
            matrices.scale(ZOOM_SCALE, ZOOM_SCALE, ZOOM_SCALE);
            x *= descale;
            y *= descale;
        }
        int result = textRenderer.drawWithShadow(matrices, text, x, y, color);
        if (shouldZoom) matrices.scale(descale, descale, descale);
        return result;
    }

}
