package com.hcs.mixin.client.render;

import com.hcs.main.helper.EntityHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value = EnvType.CLIENT)
@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    public void renderRedirected(AbstractClientPlayerEntity player, float f, float g, MatrixStack matrix, VertexConsumerProvider vertex, int i, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        LivingEntity entityToRender = EntityHelper.getHallucinationEntityForPlayer(client.world, client.player);
        if (entityToRender instanceof SkeletonEntity) {
            client.getEntityRenderDispatcher().getRenderer(entityToRender).render(entityToRender, f, g, matrix, vertex, i);
            ci.cancel();
        }
    }
}
