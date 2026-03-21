package biz.coolpage.hcs.client.mixin.gui;

import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
@Mixin(SubtitleOverlay.class)
public class SubtitlesHudMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private static final Predicate<Player> SHOULD_OBFUSCATE_TEXT = player -> {
        if (player == null) return false;
        return ((player.hasEffect(HcsEffects.INSANITY) && ((StatAccessor) player).getSanityManager().get() < 0.25) || player.hasEffect(HcsEffects.DARKNESS_ENVELOPED));
    };

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"), index = 1)
    public Component renderModifiedArg1(Component text) {
        if (text != null && SHOULD_OBFUSCATE_TEXT.test(this.minecraft.player))
            return MutableComponent.create(text.getContents()).withStyle(ChatFormatting.OBFUSCATED);
        return text;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target ="Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"), index = 1)
    public String renderModifiedArg2(String string) {
        if (string != null && SHOULD_OBFUSCATE_TEXT.test(this.minecraft.player))
            return "";
        return string;
    }
}