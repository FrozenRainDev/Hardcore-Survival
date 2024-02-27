package biz.coolpage.hcs.client;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.event.ClientPlayConnectionEvent;
import biz.coolpage.hcs.network.ClientS2C;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientS2C.init();
        ClientPlayConnectionEvent.init();
        initBlockRenderLayerMap(
                Reg.ICEBOX,
                Reg.DRYING_RACK,
                Reg.CRUDE_TORCH_BLOCK,
                Reg.WALL_CRUDE_TORCH_BLOCK,
                Reg.BURNING_CRUDE_TORCH_BLOCK,
                Reg.WALL_BURNING_CRUDE_TORCH_BLOCK,
                Reg.UNLIT_TORCH_BLOCK,
                Reg.WALL_UNLIT_TORCH_BLOCK,
                Reg.BURNT_TORCH_BLOCK,
                Reg.WALL_BURNT_TORCH_BLOCK,
                Reg.GLOWSTONE_TORCH_BLOCK,
                Reg.WALL_GLOWSTONE_TORCH_BLOCK
        );
        EntityRendererRegistry.register(Reg.ROCK_PROJECTILE_ENTITY, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(Reg.FLINT_PROJECTILE_ENTITY, FlyingItemEntityRenderer::new);
        BlockEntityRendererFactories.register(Reg.DRYING_RACK_BLOCK_ENTITY, DryingRackBlockEntityRenderer::new);
        ModelPredicateProviderRegistry.register(Reg.IMPROVISED_SHIELD, new Identifier("blocking"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F);
    }

    // That's so sad :( , ALWAYS needs to call me, LOL
    private static void initBlockRenderLayerMap(Block @NotNull ... blocks) {
        for (Block block : blocks)
            BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout());
    }
}
