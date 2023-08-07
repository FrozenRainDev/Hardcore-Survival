package com.hcs.mixin.client;

import com.hcs.util.EntityHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow
    private GameMode gameMode;

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "getReachDistance", at = @At("RETURN"), cancellable = true)
    public void getReachDistance(CallbackInfoReturnable<Float> cir) {
        if (client.player == null || !this.gameMode.isSurvivalLike()) return;
        float rangeAddition = EntityHelper.getReachRangeAddition(client.player.getMainHandStack());
        cir.setReturnValue(2.0F + ((client.player.isSneaking() && rangeAddition > 0.0F) ? 0.5F : 0.0F) + rangeAddition);
    }

}
