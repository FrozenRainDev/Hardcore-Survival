package biz.coolpage.hcs.client;

import biz.coolpage.hcs.Hcs; // 按照要求 Reg 改为 Hcs
import biz.coolpage.hcs.client.model.SpearModel;
import biz.coolpage.hcs.client.renderer.SpearEntityRenderer;
import biz.coolpage.hcs.util.HcsFactory;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;

// 使用 Forge 的 EventBusSubscriber 自动注册
@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class Client {

    @SubscribeEvent
    public static void onClientSetup(@NotNull FMLClientSetupEvent event) {
        // Certain registration logic needs to be placed within enqueueWork to ensure thread safety.
        event.enqueueWork(() -> {
            // Assuming these two classes have been converted in the Forge version
            ClientS2C.init();
            ClientPlayConnectionEvent.init();

            // todo 在 1.20.1 中虽然建议在 JSON 中配置 render_type，但保留原代码逻辑
            initBlockRenderLayerMap(
                    Hcs.ICEBOX.get(),
                    Hcs.DRYING_RACK.get(),
                    Hcs.CRUDE_TORCH_BLOCK.get(),
                    Hcs.WALL_CRUDE_TORCH_BLOCK.get(),
                    Hcs.BURNING_CRUDE_TORCH_BLOCK.get(),
                    Hcs.WALL_BURNING_CRUDE_TORCH_BLOCK.get(),
                    Hcs.UNLIT_TORCH_BLOCK.get(),
                    Hcs.WALL_UNLIT_TORCH_BLOCK.get(),
                    Hcs.BURNT_TORCH_BLOCK.get(),
                    Hcs.WALL_BURNT_TORCH_BLOCK.get(),
                    Hcs.GLOWSTONE_TORCH_BLOCK.get(),
                    Hcs.WALL_GLOWSTONE_TORCH_BLOCK.get(),
                    Hcs.SMOLDERING_CAMPFIRE_BLOCK.get()
            );

            // ModelPredicateProviderRegistry -> ItemProperties
            ItemProperties.register(Hcs.IMPROVISED_SHIELD.get(), HcsFactory.createResourceLocationWithPrefix("blocking"), // todo check prev:new ResourceLocation("blocking")
                    (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);

            ItemProperties.register(Hcs.STONE_SPEAR.get(), HcsFactory.createResourceLocationWithPrefix("throwing"), (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);

            ItemProperties.register(Hcs.FLINT_SPEAR.get(), HcsFactory.createResourceLocationWithPrefix("throwing"), (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);

        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.@NotNull RegisterRenderers event) {
        // EntityRendererRegistry -> event.registerEntityRenderer
        // FlyingItemEntityRenderer (Fabric) -> ThrownItemRenderer (Mojang/Forge)
        event.registerEntityRenderer(Hcs.ROCK_PROJECTILE_ENTITY.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(Hcs.FLINT_PROJECTILE_ENTITY.get(), ThrownItemRenderer::new);

        // BlockEntityRendererFactories -> event.registerBlockEntityRenderer
        event.registerBlockEntityRenderer(Hcs.DRYING_RACK_BLOCK_ENTITY.get(), DryingRackBlockEntityRenderer::new);

        // Register standard 2D item renderer for the spear entity
        event.registerEntityRenderer(Hcs.THROWN_SPEAR.get(), SpearEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.@NotNull RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SpearModel.LAYER_LOCATION, SpearModel::createBodyLayer);
    }

    // That's so sad :( , ALWAYS needs to call me, LOL
    @SuppressWarnings("removal")
    private static void initBlockRenderLayerMap(Block @NotNull ... blocks) {
        for (Block block : blocks) {
            // BlockRenderLayerMap -> ItemBlockRenderTypes
            ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout());
        }
    }
}