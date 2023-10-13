package biz.coolpage.hcs.mixin.client.gui;

import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.SubtitlesHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Environment(value = EnvType.CLIENT)
@Mixin(SubtitlesHud.class)
public class SubtitlesHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Unique
    private static final Predicate<PlayerEntity> SHOULD_OBFUSCATE_TEXT = player -> {
        if (player == null) return false;
        return ((player.hasStatusEffect(HcsEffects.INSANITY) && ((StatAccessor) player).getSanityManager().get() < 0.25) || player.hasStatusEffect(HcsEffects.DARKNESS_ENVELOPED));
    };

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/SubtitlesHud;drawTextWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"), index = 2)
    public Text renderModifiedArg1(Text text) {
        if (text != null && SHOULD_OBFUSCATE_TEXT.test(this.client.player))
            return MutableText.of(text.getContent()).formatted(Formatting.OBFUSCATED);
        return text;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/SubtitlesHud;drawTextWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V"), index = 2)
    public String renderModifiedArg2(String string) {
        if (string != null && SHOULD_OBFUSCATE_TEXT.test(this.client.player))
            return "";
        return string;
    }
}
