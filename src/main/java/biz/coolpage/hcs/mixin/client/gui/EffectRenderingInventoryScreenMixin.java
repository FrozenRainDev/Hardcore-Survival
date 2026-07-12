package biz.coolpage.hcs.mixin.client.gui;

import biz.coolpage.hcs.status.HcsEffects;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static biz.coolpage.hcs.status.HcsEffects.IS_EFFECT_NAME_VARIABLE;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class EffectRenderingInventoryScreenMixin {

    @Unique
    private static final float hcsurvival$ZOOM_SCALE = 0.75F;

    // In 1.20.1, text rendering logic is within renderLabels
    @WrapOperation(
            method = "renderLabels",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"
            )
    )
    private int hcsurvival$wrapDrawStatusEffectDescriptions(GuiGraphics instance, Font font, @NotNull Component component, int x, int y, int color, Operation<Integer> original) {
        float descale = 1.0F / hcsurvival$ZOOM_SCALE;
        // Zoom if the string does not contain a colon (usually implies it's the effect name)
        boolean shouldZoom = !component.getString().contains(":");

        if (shouldZoom) {
            instance.pose().pushPose(); // Use push/pop to ensure coordinate system safety
            instance.pose().scale(hcsurvival$ZOOM_SCALE, hcsurvival$ZOOM_SCALE, hcsurvival$ZOOM_SCALE);
            x = (int) (x * descale);
            y = (int) (y * descale);
        }

        // Call the original drawString method with potentially modified coordinates
        int result = original.call(instance, font, component, x, y, color);

        if (shouldZoom) {
            instance.pose().popPose();
        }

        return result;
    }

    @Inject(method = "getEffectName", at = @At("HEAD"), cancellable = true)
    private void hcsurvival$getStatusEffectDescription(@NotNull MobEffectInstance mobEffectInstance, CallbackInfoReturnable<Component> cir) {
        if (IS_EFFECT_NAME_VARIABLE.test(mobEffectInstance.getEffect())) {
            cir.setReturnValue(Component.translatable(HcsEffects.getEffectVarName(mobEffectInstance.getDescriptionId(), mobEffectInstance.getAmplifier())));
        }
    }
}