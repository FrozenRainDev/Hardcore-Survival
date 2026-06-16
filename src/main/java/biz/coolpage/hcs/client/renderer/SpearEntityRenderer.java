package biz.coolpage.hcs.client.renderer;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.client.model.SpearModel;
import biz.coolpage.hcs.entity.ThrownSpearEntity;
import biz.coolpage.hcs.util.HcsFactory;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SpearEntityRenderer extends EntityRenderer<ThrownSpearEntity> {
    // Define the default texture location using the Mod ID
    public static final ResourceLocation STONE_TEXTURE = HcsFactory.createResourceLocation("textures/entity/projectiles/stone_spear.png"), FLINT_TEXTURE = HcsFactory.createResourceLocation("textures/entity/projectiles/flint_spear.png");

    // Map to allow different textures for different spear items in the future
    public static final Map<Item, ResourceLocation> SPEAR_TEXTURES = Util.make(new HashMap<>(), map -> {
        map.put(Hcs.STONE_SPEAR.get(), STONE_TEXTURE);
        map.put(Hcs.FLINT_SPEAR.get(), FLINT_TEXTURE);
        // You can easily register more spear types here, e.g., map.put(Hcs.IRON_SPEAR.get(), ...);
    });

    private final SpearModel model;

    public SpearEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        // Bake the model layer instead of using item renderer
        this.model = new SpearModel(context.bakeLayer(SpearModel.LAYER_LOCATION));
    }

    @Override
    public void render(@NotNull ThrownSpearEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // Calculate pitch and yaw
        float degrees = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(degrees));
        float degrees1 = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 90.0F;
        poseStack.mulPose(Axis.ZP.rotationDegrees(degrees1));

        // Check if the spear item has enchantment glint
        boolean hasFoil = entity.getPickupItem().hasFoil();

        // Use FoilBufferDirect to render the model, handling enchantment glints properly
        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(
                buffer,
                this.model.renderType(this.getTextureLocation(entity)),
                false,
                hasFoil
        );

        // Render the model to the buffer
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ThrownSpearEntity entity) {
        return SPEAR_TEXTURES.getOrDefault(entity.getPickupItem().getItem(), STONE_TEXTURE);
    }
}