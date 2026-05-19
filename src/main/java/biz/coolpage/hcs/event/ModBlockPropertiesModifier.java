package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.function.ToIntFunction;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlockPropertiesModifier {

    @SubscribeEvent
    public static void onCommonSetup(@NotNull FMLCommonSetupEvent event) {
        // Use enqueueWork to ensure thread safety during parallel mod loading
        event.enqueueWork(() -> {
            try {
                // Find the protected 'lightEmission' field in BlockBehaviour using its SRG name (f_60446_) for 1.20.1
                Field lightEmissionField = ObfuscationReflectionHelper.findField(BlockBehaviour.class, "f_60446_");
                lightEmissionField.setAccessible(true);

                // Define the new emission logic: return 4 if the block is lit, otherwise 0
                ToIntFunction<BlockState> newEmission = state ->
                        state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT) ? 4 : 0;

                // Accurately apply the modification only to Redstone Torch and Redstone Wall Torch
                lightEmissionField.set(Blocks.REDSTONE_TORCH, newEmission);
                lightEmissionField.set(Blocks.REDSTONE_WALL_TORCH, newEmission);

                // You can add more specific block modifications here in the future

            } catch (Exception e) {
                // Log the exception using the mod's logger if reflection fails
                Hcs.error("Failed to modify redstone torch light emission", e);
            }
        });
    }
}