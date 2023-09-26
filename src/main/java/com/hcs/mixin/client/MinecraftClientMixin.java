package com.hcs.mixin.client;

import com.hcs.status.accessor.StatAccessor;
import com.hcs.status.network.ClientC2S;
import com.hcs.util.EntityHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(value = EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin extends ReentrantThreadExecutor<Runnable> implements WindowEventHandler {
    @Shadow
    public HitResult crosshairTarget;
    @Shadow
    public ClientPlayerEntity player;
    @Shadow
    public ClientPlayerInteractionManager interactionManager;

    @Shadow
    @Nullable
    public ClientWorld world;
    @Shadow
    public Screen currentScreen;
    @Shadow
    @Final
    public Mouse mouse;

    public MinecraftClientMixin(String string) {
        super(string);
    }

    @ModifyArg(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;handleBlockBreaking(Z)V"), index = 0)
    private boolean handleInputEvents(boolean breaking) {
        return breaking || (((StatAccessor) this.player).getStatusManager().lockDestroying() && this.currentScreen == null && this.mouse.isCursorLocked());
    }


    @SuppressWarnings("CancellableInjectionUsage")
    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void doAttack(CallbackInfoReturnable<Boolean> cir) {
        if (crosshairTarget != null && player != null) {
            if (crosshairTarget.getType() == HitResult.Type.ENTITY) {
                /*
                Default reach distance:
                block or interact with entity: 2
                attack entity: 2 - 0.5 = 1.5
                */
                if (crosshairTarget.getPos().distanceTo(player.getEyePos()) + (EntityHelper.HOLDING_BLOCK.test(new ItemStack[]{player.getMainHandStack(), player.getOffHandStack()}) ? EntityHelper.HOLDING_BLOCK_REACHING_RANGE_ADDITION : 0.0F) + 0.5F > interactionManager.getReachDistance()) {
                    player.swingHand(Hand.MAIN_HAND);
                    cir.cancel();
                }
            }
        }
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z", shift = At.Shift.AFTER))
//@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", shift = At.Shift.AFTER))
    private void doItemUse(CallbackInfo ci) {
        if (world == null) return;
        BlockHitResult customHitResult = EntityHelper.rayCast(world, player, RaycastContext.FluidHandling.SOURCE_ONLY, 2.5);
        BlockPos pos = customHitResult.getBlockPos();
        if (player != null && world.getFluidState(pos).isIn(FluidTags.WATER))
            ClientC2S.writeC2SPacketOnDrinkWater(player, pos.getX(), pos.getY(), pos.getZ());
    }
}
