package biz.coolpage.hcs.client.mixin.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(VideoSettingsScreen.class)
public abstract class VideoOptionsScreenMixin extends OptionsSubScreen {
    @Shadow
    private OptionsList list;

    @Inject(method = "init", at = @At("TAIL"))
    protected void init(CallbackInfo ci) {
        var widget = this.list.findOption(this.options.gamma());
        if (widget != null) {
            widget.active = false;
        }
    }

    public VideoOptionsScreenMixin(Screen parent, Options gameOptions, Component title) {
        super(parent, gameOptions, title);
    }
}