package com.hcs.main;

import com.hcs.main.event.ClientPlayConnectionEvent;
import com.hcs.misc.network.ClientS2C;
import com.hcs.misc.render.DryingRackBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

import static com.hcs.main.Reg.ROCK_PROJECTILE_ENTITY;

@Environment(EnvType.CLIENT)
public class Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientS2C.init();
        ClientPlayConnectionEvent.init();
        BlockRenderLayerMap.INSTANCE.putBlock(Reg.ICEBOX, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(Reg.DRYING_RACK, RenderLayer.getCutout());
        EntityRendererRegistry.register(ROCK_PROJECTILE_ENTITY, FlyingItemEntityRenderer::new);
        BlockEntityRendererFactories.register(Reg.DRYING_RACK_BLOCK_ENTITY, DryingRackBlockEntityRenderer::new);
    }
}
