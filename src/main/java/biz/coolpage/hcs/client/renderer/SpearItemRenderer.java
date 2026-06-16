package biz.coolpage.hcs.client.renderer;

import biz.coolpage.hcs.client.model.SpearModel;
import biz.coolpage.hcs.client.renderer.SpearEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpearItemRenderer extends BlockEntityWithoutLevelRenderer {
    private SpearModel model;

    public SpearItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // 延迟初始化模型，防止在客户端加载初期触发空指针崩溃
        if (this.model == null) {
            this.model = new SpearModel(Minecraft.getInstance().getEntityModels().bakeLayer(SpearModel.LAYER_LOCATION));
        }

        poseStack.pushPose();
        // 缩放并翻转以适配手部的原版模型骨骼
        poseStack.scale(1.0F, -1.0F, -1.0F);

        // 获取材质（复用你实体渲染器里的映射）
        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(
                buffer,
                this.model.renderType(SpearEntityRenderer.SPEAR_TEXTURES.getOrDefault(stack.getItem(), SpearEntityRenderer.STONE_TEXTURE)),
                false,
                stack.hasFoil()
        );

        // 渲染 3D 模型
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}