package biz.coolpage.hcs.datagen;

import biz.coolpage.hcs.Hcs;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HcsDataGenerators {
    @SubscribeEvent
    public static void gatherData(@NotNull GatherDataEvent event) {
        Hcs.print("Gathering data is called");
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        boolean includeClient = event.includeClient(); // depends on?? build.gradle: args '--mod', mod_id, '--all', '--output'
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // facades
        // Server side, logic
//        generator.addProvider(event.includeServer(), new HcsItemTagGenerator(output));

        // Client side, rendering
        generator.addProvider(includeClient, new HcsItemModelProvider(output, fileHelper));
//        generator.addProvider(event.includeClient(), new MyBlockStateProvider(output, fileHelper));
//        generator.addProvider(event.includeClient(), new MyItemModelProvider(output, fileHelper));
    }
}