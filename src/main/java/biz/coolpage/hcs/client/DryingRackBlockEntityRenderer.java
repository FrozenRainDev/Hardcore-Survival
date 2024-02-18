package biz.coolpage.hcs.client;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.entity.DryingRackBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;

@Environment(value = EnvType.CLIENT)
public class DryingRackBlockEntityRenderer implements BlockEntityRenderer<DryingRackBlockEntity> {
    private static final float SCALE = 0.5F;
    private final ItemRenderer itemRenderer;

    public DryingRackBlockEntityRenderer(BlockEntityRendererFactory.@NotNull Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }


    @Override
    public void render(@NotNull DryingRackBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!entity.getCachedState().contains(Properties.HORIZONTAL_FACING)) return;
        Direction direction = entity.getCachedState().get(Properties.HORIZONTAL_FACING);
        ItemStack stack = entity.getInventoryStack();
        if (stack == null || stack.isEmpty()) return;
        String name = stack.getItem().getTranslationKey();
        boolean isRabbit = name.contains("rabbit");
        boolean isFishOrKelp = (stack.isIn(ItemTags.FISHES) || stack.isOf(Items.KELP));
        boolean isMorsel = (stack.isOf(Reg.RAW_MEAT) || stack.isOf(Reg.COOKED_MEAT));
        boolean isOfAxisX = (direction.getAxis() == Direction.Axis.X);
        boolean isOnPositiveAxis = (direction == Direction.EAST || direction == Direction.NORTH);
        float horizontalTranslation = (0.5F + (isRabbit ? 0.02F : (isFishOrKelp ? 0.005F : (isMorsel ? -0.04F : 0.03F))) * (isOnPositiveAxis ? 1.0F : -1.0F));
        matrices.push();
        matrices.translate(isOfAxisX ? 0.5F : horizontalTranslation, name.contains("jerky") ? 0.97F : 0.91F, isOfAxisX ? horizontalTranslation : 0.5F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-(Direction.fromHorizontal((direction.getHorizontal()) % 4)).asRotation()));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(isRabbit ? 225.0F : ((isFishOrKelp || isMorsel) ? 315.0F : 135.0F)));
        matrices.scale(SCALE, SCALE, SCALE);
        this.itemRenderer.renderItem(stack, ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, entity.getWorld(), (int) entity.getPos().asLong());
        matrices.pop();
    }

}
