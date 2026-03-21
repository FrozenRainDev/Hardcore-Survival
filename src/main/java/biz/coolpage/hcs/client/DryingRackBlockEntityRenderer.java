package biz.coolpage.hcs.client;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.entity.DryingRackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class DryingRackBlockEntityRenderer implements BlockEntityRenderer<DryingRackBlockEntity> {
    private static final float SCALE = 0.5F;
    private final ItemRenderer itemRenderer;

    public DryingRackBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(@NotNull DryingRackBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        if (!entity.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return;
        Direction direction = entity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        ItemStack stack = entity.getInventoryStack();
        if (stack == null || stack.isEmpty()) return;
        String name = stack.getItem().getDescriptionId();
        boolean isRabbit = name.contains("rabbit");
        boolean isFishOrKelp = (stack.is(ItemTags.FISHES) || stack.is(Items.KELP));
        boolean isMorsel = (stack.is(Hcs.RAW_MEAT) || stack.is(Hcs.COOKED_MEAT));
        boolean isOfAxisX = (direction.getAxis() == Direction.Axis.X);
        boolean isOnPositiveAxis = (direction == Direction.EAST || direction == Direction.NORTH);
        float horizontalTranslation = (0.5F + (isRabbit ? 0.02F : (isFishOrKelp ? 0.005F : (isMorsel ? -0.04F : 0.03F))) * (isOnPositiveAxis ? 1.0F : -1.0F));

        matrices.pushPose();
        matrices.translate(isOfAxisX ? 0.5F : horizontalTranslation, name.contains("jerky") ? 0.97F : 0.91F, isOfAxisX ? horizontalTranslation : 0.5F);

        // 保持原逻辑：Direction.from2DDataValue 对应 Direction.fromHorizontal
        matrices.mulPose(Axis.YP.rotationDegrees(-(Direction.from2DDataValue(direction.get2DDataValue() % 4)).toYRot()));
        matrices.mulPose(Axis.ZP.rotationDegrees(isRabbit ? 225.0F : ((isFishOrKelp || isMorsel) ? 315.0F : 135.0F)));

        matrices.scale(SCALE, SCALE, SCALE);
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, matrices, vertexConsumers, entity.getLevel(), (int) entity.getBlockPos().asLong());
        matrices.popPose();
    }
}