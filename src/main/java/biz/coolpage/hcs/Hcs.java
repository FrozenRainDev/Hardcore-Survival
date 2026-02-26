package biz.coolpage.hcs;

import biz.coolpage.hcs.config.Config;
import biz.coolpage.hcs.item.HcsItems;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static biz.coolpage.hcs.item.HcsItems.*;

@Mod(Hcs.MOD_ID)
public final class Hcs {
    public static final String MOD_ID = "hcsurvival", MOD_NAME = "Hardcore Survival";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register(MOD_ID /* lower case only */, () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT) // placed after the combat tab
            .title(Component.translatable("itemGroup.hcsurvival.main"))
            .icon(() -> FLINT_HATCHET.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(FLINT_HATCHET.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build());

    public Hcs(@NotNull FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        // Add listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        // Call sub-registers(facade)
        HcsItems.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        // Config
        context.registerConfig(ModConfig.Type.COMMON, Config.CFG_SPEC);
        // todo test
        info(LivingEntity.SLOW_FALLING_ID);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    // Add items to creative tabs; mod items would be automatically added to mod creative tab, but require manual addition for vanilla adding
    private void addCreative(@NotNull BuildCreativeModeTabContentsEvent event) {
//        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS){event.accept(FLINT_HATCHET.get())} // for vanilla tabs, e.g.
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }

    private static @NotNull String getInfoString(Object... contents) {
        if (contents == null) {
            LOGGER.warn("Printing content is null");
            return "";
        }
        StringBuilder outputBuilder = new StringBuilder();
        for (Object content : contents) {
            outputBuilder.append(content);
            outputBuilder.append(" ");
        }
        return outputBuilder.toString();
    }

    public static void info(Object... contents) {
        LOGGER.info(getInfoString(contents));
    }

    public static void warn(Object... contents) {
        LOGGER.warn(getInfoString(contents));
    }

    public static void error(Object... contents) {
        LOGGER.error(getInfoString(contents));
    }
}
