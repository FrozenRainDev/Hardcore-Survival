package biz.coolpage.hcs.mixin.client.render;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.util.HcsFactory;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer; // 修正包路径
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BannerRenderer; // Yarn: BannerBlockEntityRenderer -> Mojang: BannerRenderer
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Holder;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@OnlyIn(Dist.CLIENT)
@Mixin(BlockEntityWithoutLevelRenderer.class)
public class BuiltinModelItemRendererMixin {
    @Shadow
    private ShieldModel shieldModel;

    @Unique
    private static final Material IMPROVISED_SHIELD_BASE = new Material(Sheets.SHIELD_SHEET, HcsFactory.createResourceLocation("entity/improvised_shield_base"));
    @Unique
    private static final Material IMPROVISED_SHIELD_BASE_NO_PATTERN = new Material(Sheets.SHIELD_SHEET, HcsFactory.createResourceLocation("entity/improvised_shield_base_nopattern"));

    @Inject(at = @At("HEAD"), method = "renderByItem", cancellable = true)
    public void render(@NotNull ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, CallbackInfo ci) {
        // 如果 Hcs.IMPROVISED_SHIELD 是 RegistryObject，请务必使用 .get()
        if (stack.getItem() == Hcs.IMPROVISED_SHIELD.get()) {
            boolean hasData = BlockItem.getBlockEntityData(stack) != null;
            matrices.pushPose();
            matrices.scale(1.0f, -1.0f, -1.0f);

            Material material = hasData ? IMPROVISED_SHIELD_BASE : IMPROVISED_SHIELD_BASE_NO_PATTERN;
            VertexConsumer vertexConsumer = material.sprite().wrap(ItemRenderer.getFoilBufferDirect(vertexConsumers, this.shieldModel.renderType(material.atlasLocation()), true, stack.hasFoil()));

            this.shieldModel.handle().render(matrices, vertexConsumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);

            if (hasData) {
                // Yarn: getItemBannerTag -> Mojang: getItemPatterns
                ListTag listTag = BannerBlockEntity.getItemPatterns(stack);
                List<Pair<Holder<BannerPattern>, DyeColor>> list = BannerBlockEntity.createPatterns(ShieldItem.getColor(stack), listTag);
                // Yarn: BannerBlockEntityRenderer -> Mojang: BannerRenderer
                BannerRenderer.renderPatterns(matrices, vertexConsumers, light, overlay, this.shieldModel.plate(), material, false, list, stack.hasFoil());
            } else {
                this.shieldModel.plate().render(matrices, vertexConsumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);
            }

            matrices.popPose();
            ci.cancel();
        }
    }
}