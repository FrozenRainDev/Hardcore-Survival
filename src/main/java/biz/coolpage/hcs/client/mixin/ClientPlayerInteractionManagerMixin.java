package biz.coolpage.hcs.client.mixin;

import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow
    private GameType localPlayerMode;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "getPickRange", at = @At("RETURN"), cancellable = true)
    public void getReachDistance(CallbackInfoReturnable<Float> cir) {
        if (minecraft.player == null || !this.localPlayerMode.isSurvival()) return;
        float rangeAddition = EntityHelper.getReachRangeAddition(minecraft.player);
        cir.setReturnValue(HcsDifficulty.chooseVal(minecraft.player, 3.0F, 2.25F, 2.0F) + ((minecraft.player.isCrouching() && rangeAddition > 0.0F) ? 0.5F : 0.0F) + rangeAddition);
    }
}