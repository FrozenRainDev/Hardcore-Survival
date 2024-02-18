package biz.coolpage.hcs.client;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.event.ClientPlayConnectionEvent;
import biz.coolpage.hcs.network.ClientS2C;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientS2C.init();
        ClientPlayConnectionEvent.init();
        initBlockRenderLayerMap();
        EntityRendererRegistry.register(Reg.ROCK_PROJECTILE_ENTITY, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(Reg.FLINT_PROJECTILE_ENTITY, FlyingItemEntityRenderer::new);
        BlockEntityRendererFactories.register(Reg.DRYING_RACK_BLOCK_ENTITY, DryingRackBlockEntityRenderer::new);
        ModelPredicateProviderRegistry.register(Reg.IMPROVISED_SHIELD, new Identifier("blocking"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F);
    }

    // That's so sad :( , u always need to call me, lol
    private static void initBlockRenderLayerMap() {
        BlockRenderLayerMap.INSTANCE.putBlock(Reg.ICEBOX, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(Reg.DRYING_RACK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(Reg.CRUDE_TORCH_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(Reg.WALL_CRUDE_TORCH_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(Reg.BURNING_CRUDE_TORCH_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(Reg.WALL_BURNING_CRUDE_TORCH_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(Reg.UNLIT_TORCH_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(Reg.WALL_UNLIT_TORCH_BLOCK, RenderLayer.getCutout());
    }
}
