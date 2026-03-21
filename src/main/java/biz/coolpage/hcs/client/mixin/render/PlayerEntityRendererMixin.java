package biz.coolpage.hcs.client.mixin.render;

import biz.coolpage.hcs.util.EntityHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(PlayerRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
    public void renderRedirected(AbstractClientPlayer player, float f, float g, PoseStack matrix, MultiBufferSource vertex, int i, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        // 建议：此处建议使用方法参数中的 player 而不是 client.player，以确保对所有渲染的玩家（包括联机他人）生效
        LivingEntity entityToRender = EntityHelper.getHallucinationEntityForPlayer(client.level, player);

        if (entityToRender instanceof Skeleton) {
            client.getEntityRenderDispatcher().getRenderer(entityToRender).render(entityToRender, f, g, matrix, vertex, i);
            ci.cancel();
        }
    }
}