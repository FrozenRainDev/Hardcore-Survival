package com.hcs.mixin.client.gui;

import com.hcs.main.helper.EntityHelper;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {

    public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(at = @At("HEAD"), method = "render")
    protected void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
//        EntityHelper.canShowMoreStat = true;
    }

    /*
    @Inject(at = @At("HEAD"), method = "removed", cancellable = true)
    protected void removed(CallbackInfo ci) {
        EntityHelper.canShowMoreStat=false;
    }
     */

}
