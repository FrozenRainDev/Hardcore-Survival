package biz.coolpage.hcs.client.mixin;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.client.ClientC2S;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.WorldHelper;
import com.mojang.blaze3d.platform.WindowEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.ClipContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin extends ReentrantBlockableEventLoop<Runnable> implements WindowEventHandler {
    @Shadow
    @Nullable
    public HitResult hitResult;
    @Shadow
    @Nullable
    public LocalPlayer player;
    @Shadow
    @Nullable
    public MultiPlayerGameMode gameMode;

    @Shadow
    @Nullable
    public ClientLevel level;
    @Shadow
    @Nullable
    public Screen screen;
    @Shadow
    @Final
    public MouseHandler mouseHandler;

    public MinecraftClientMixin(String string) {
        super(string);
    }

    @ModifyArg(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;continueAttack(Z)V"), index = 0)
    private boolean handleInputEvents(boolean breaking) {
        // 保持原有逻辑，通过 StatAccessor 检查是否锁定挖掘
        return breaking || (this.player != null && ((StatAccessor) this.player).getStatusManager().lockDestroying() && this.screen == null && this.mouseHandler.isMouseGrabbed());
    }

    @SuppressWarnings("CancellableInjectionUsage")
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void doAttack(CallbackInfoReturnable<Boolean> cir) {
        if (hitResult != null && player != null && gameMode != null) {
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                // 修改为 Mojang 映射中的 getLocation() 和 getPickRange()
                if (hitResult.getLocation().distanceTo(player.getEyePosition(1.0F)) + (EntityHelper.IS_HOLDING_BLOCK.test(player.getMainHandItem(), player.getOffhandItem()) ? EntityHelper.HOLDING_BLOCK_REACHING_RANGE_ADDITION : 0.0F) + 0.5F > gameMode.getPickRange()) {
                    player.swing(InteractionHand.MAIN_HAND);
                    cir.setReturnValue(false);
                    cir.cancel();
                }
            }
        }
    }

    // 在 Mojang 映射中，Minecraft.class 对应的方法是 startUseItem 而不是 useItem
    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void doItemUse(CallbackInfo ci) {
        if (level == null || player == null) return;
        // 使用 Mojang 映射的 BlockHitResult 和 ClipContext
        BlockHitResult customHitResult = EntityHelper.rayCast(level, player, ClipContext.Fluid.SOURCE_ONLY, 2.5);
        BlockPos pos = customHitResult.getBlockPos();
        FluidState state = level.getFluidState(pos);
        if (state.is(FluidTags.WATER)) {
            ClientC2S.writeC2SPacketOnDrinkWater(player, pos.getX(), pos.getY(), pos.getZ());
        } else if (state.is(FluidTags.LAVA)) {
            ItemStack s1 = player.getMainHandItem();
            ItemStack s2 = player.getOffhandItem();
            // 冗余逻辑保留：如果主手不为空，则“逻辑上”检查副手（原代码逻辑）
            if (!s1.isEmpty()) s2 = Items.AIR.getDefaultInstance();
            boolean b1 = s1.is(Hcs.UNLIT_TORCH_ITEM) || s1.is(Hcs.CRUDE_TORCH_ITEM);
            boolean b2 = s2.is(Hcs.UNLIT_TORCH_ITEM) || s2.is(Hcs.CRUDE_TORCH_ITEM);
            if (b1 || b2) {
                ClientC2S.writeC2SPacketOnLitHoldingTorchInLava(player, b1 ? 1 : 2);
                ci.cancel();
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        WorldHelper.updateClientWorldAndMainPlayer(this.level, this.player);
    }
}