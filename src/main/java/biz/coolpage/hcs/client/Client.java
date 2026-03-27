package biz.coolpage.hcs.client;

import biz.coolpage.hcs.Hcs; // 按照要求 Reg 改为 Hcs
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
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 部分注册逻辑需要放在 enqueueWork 中确保线程安全
        event.enqueueWork(() -> {
            // 假设这两个类在 Forge 版中已转换
            ClientS2C.init();
            ClientPlayConnectionEvent.init();

            // todo 在 1.20.1 中虽然建议在 JSON 中配置 render_type，但保留原代码逻辑
            initBlockRenderLayerMap(
                    Hcs.ICEBOX,
                    Hcs.DRYING_RACK,
                    Hcs.CRUDE_TORCH_BLOCK,
                    Hcs.WALL_CRUDE_TORCH_BLOCK,
                    Hcs.BURNING_CRUDE_TORCH_BLOCK,
                    Hcs.WALL_BURNING_CRUDE_TORCH_BLOCK,
                    Hcs.UNLIT_TORCH_BLOCK,
                    Hcs.WALL_UNLIT_TORCH_BLOCK,
                    Hcs.BURNT_TORCH_BLOCK,
                    Hcs.WALL_BURNT_TORCH_BLOCK,
                    Hcs.GLOWSTONE_TORCH_BLOCK,
                    Hcs.WALL_GLOWSTONE_TORCH_BLOCK,
                    Hcs.SMOLDERING_CAMPFIRE_BLOCK
            );

            // ModelPredicateProviderRegistry -> ItemProperties
            ItemProperties.register(Hcs.IMPROVISED_SHIELD, HcsFactory.createPathResourceLocation("blocking"), // todo check prev:new ResourceLocation("blocking")
                    (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // EntityRendererRegistry -> event.registerEntityRenderer
        // FlyingItemEntityRenderer (Fabric) -> ThrownItemRenderer (Mojang/Forge)
        event.registerEntityRenderer(Hcs.ROCK_PROJECTILE_ENTITY, ThrownItemRenderer::new);
        event.registerEntityRenderer(Hcs.FLINT_PROJECTILE_ENTITY, ThrownItemRenderer::new);

        // BlockEntityRendererFactories -> event.registerBlockEntityRenderer
        event.registerBlockEntityRenderer(Hcs.DRYING_RACK_BLOCK_ENTITY, DryingRackBlockEntityRenderer::new);
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