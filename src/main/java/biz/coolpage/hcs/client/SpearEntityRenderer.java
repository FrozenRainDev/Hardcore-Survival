package biz.coolpage.hcs.client;

import biz.coolpage.hcs.entity.ThrownSpearEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;

public class SpearEntityRenderer extends EntityRenderer<ThrownSpearEntity> {
    private final ItemRenderer itemRenderer;

    public SpearEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(@NotNull ThrownSpearEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // 核心运算：获取飞行实体的偏航角(Yaw)和俯仰角(Pitch)，并应用旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));

        // 这里的 135.0F 决定了长矛在空中的倾斜角度。
        // 如果长矛贴图看起来是用横向的或方向不对，你可以尝试将其改为 45.0F 或 225.0F
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 135.0F));

        // 居中偏移修正
        poseStack.translate(0.0D, -0.15D, 0.0D);

        // 使用 ItemDisplayContext.NONE 渲染模型，彻底摆脱 2D 朝向面板（Billboard）
        this.itemRenderer.renderStatic(entity.getPickupItem(), ItemDisplayContext.NONE, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ThrownSpearEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}