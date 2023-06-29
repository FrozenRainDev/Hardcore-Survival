package com.hcs.mixin.client.render;

import com.hcs.Reg;
import com.hcs.status.HcsEffects;
import com.hcs.status.accessor.StatAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value = EnvType.CLIENT)
@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Final
    @Shadow
    private MinecraftClient client;
    @Shadow
    private float equipProgressMainHand;
    @Shadow
    private float equipProgressOffHand;
    @Shadow
    private ItemStack mainHand;
    @Shadow
    private ItemStack offHand;


    @Inject(at = @At("TAIL"), method = "updateHeldItems")
    public void updateHeldItems(CallbackInfo ci) {
        //Debug for NBT change for hot water bottle
        if (this.client.player == null) return;
        ItemStack mainStack = this.client.player.getMainHandStack();
        ItemStack offStack = this.client.player.getOffHandStack();
        if (mainStack.isEmpty() && this.client.player != null && this.client.player.hasStatusEffect(HcsEffects.INSANITY) && ((StatAccessor) this.client.player).getSanityManager().get() < 0.05)
            this.equipProgressMainHand = 0.0F;
        else if (mainStack.isOf(Reg.HOT_WATER_BOTTLE) && mainStack.isItemEqual(this.mainHand)) {
            this.equipProgressMainHand = 1.0F;
            this.mainHand = this.client.player.getMainHandStack();
        }
        if (offStack.isOf(Reg.HOT_WATER_BOTTLE) && offStack.isItemEqual(this.offHand)) {
            this.equipProgressOffHand = 1.0F;
            this.offHand = this.client.player.getOffHandStack();
        }
    }
}
