package biz.coolpage.hcs.client.mixin.gui;

import biz.coolpage.hcs.status.HcsEffects;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static biz.coolpage.hcs.status.HcsEffects.IS_EFFECT_NAME_VARIABLE;

@OnlyIn(Dist.CLIENT)
@Mixin(EffectRenderingInventoryScreen.class)
public abstract class AbstractInventoryScreenMixin {
    @Unique
    private static final float ZOOM_SCALE = 0.75F;

    // 1.20.1 中，文字绘制逻辑在 renderLabels 方法内
    @Redirect(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"))
    public int drawStatusEffectDescriptionsRedirected(GuiGraphics guiGraphics, Font font, @NotNull Component component, int x, int y, int color) {
        float descale = 1 / ZOOM_SCALE;
        // 如果不包含冒号（通常是效果名称），则进行缩放
        boolean shouldZoom = !component.getString().contains(":");

        if (shouldZoom) {
            guiGraphics.pose().pushPose(); // 使用 push/pop 确保坐标系安全
            guiGraphics.pose().scale(ZOOM_SCALE, ZOOM_SCALE, ZOOM_SCALE);
            x = (int) (x * descale);
            y = (int) (y * descale);
        }

        // 1.20.1 Mojang 映射中，drawString(font, text, x, y, color) 默认返回 int
        int result = guiGraphics.drawString(font, component, x, y, color);

        if (shouldZoom) {
            guiGraphics.pose().popPose();
        }

        return result;
    }

    @Inject(method = "getEffectName", at = @At("HEAD"), cancellable = true)
    private void getStatusEffectDescription(@NotNull MobEffectInstance mobEffectInstance, CallbackInfoReturnable<Component> cir) {
        if (IS_EFFECT_NAME_VARIABLE.test(mobEffectInstance.getEffect())) {
            cir.setReturnValue(Component.translatable(HcsEffects.getEffectVarName(mobEffectInstance.getDescriptionId(), mobEffectInstance.getAmplifier())));
        }
    }
}