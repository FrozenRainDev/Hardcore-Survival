package biz.coolpage.hcs.client.mixin.render;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(ItemInHandRenderer.class)
public class HeldItemRendererMixin {
    @Final
    @Shadow
    private Minecraft minecraft;
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float offHandHeight;
    @Shadow
    private ItemStack mainHandItem;
    @Shadow
    private ItemStack offHandItem;

    @Inject(at = @At("TAIL"), method = "tick")
    public void updateHeldItems(CallbackInfo ci) {
        // Debug for NBT change for hot water bottle
        if (this.minecraft.player == null) return;
        ItemStack mainStack = this.minecraft.player.getMainHandItem();
        ItemStack offStack = this.minecraft.player.getOffHandItem();
        if (mainStack.isEmpty() && this.minecraft.player != null && this.minecraft.player.hasEffect(HcsEffects.INSANITY) && ((StatAccessor) this.minecraft.player).getSanityManager().get() < 0.05)
            this.mainHandHeight = 0.0F;
        else if (mainStack.is(Hcs.HOT_WATER_BOTTLE) && this.mainHandItem.is(Hcs.HOT_WATER_BOTTLE)) {
            this.mainHandHeight = 1.0F;
            assert this.minecraft.player != null;
            this.mainHandItem = this.minecraft.player.getMainHandItem();
        }
        if (offStack.is(Hcs.HOT_WATER_BOTTLE) && this.offHandItem.is(Hcs.HOT_WATER_BOTTLE)) {
            this.offHandHeight = 1.0F;
            assert this.minecraft.player != null;
            this.offHandItem = this.minecraft.player.getOffHandItem();
        }
    }
}