package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.client.gui.HcsGuiOverlays;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class ClientModEvents {

    // 1. 监听 MOD 总线 (主要用于注册表、UI 初始化等生命周期事件)
    @Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            HcsGuiOverlays.registerGuiOverlays(event);
        }
    }

    // 2. 监听 FORGE 总线 (主要用于游戏运行时的 Tick、渲染、玩家交互等事件)
    @Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeBusEvents {
        @SubscribeEvent
        public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
            HcsGuiOverlays.INSTANCE.onRenderGuiOverlayPre(event);
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            HcsGuiOverlays.INSTANCE.onClientTick(event);
        }
    }
}