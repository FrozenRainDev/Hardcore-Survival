package biz.coolpage.hcs.data;

import biz.coolpage.hcs.Hcs;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
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
        boolean includeClient = event.includeClient(), includeServer = event.includeServer(); // depends on?? build.gradle: args '--mod', mod_id, '--all', '--output'
        CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();
        ExistingFileHelper helper = event.getExistingFileHelper();

        // facades calling
        // Both sides needed
        HcsBlockTagGenerator blockTags = new HcsBlockTagGenerator(output, provider, helper);
        HcsItemTagGenerator itemTags = new HcsItemTagGenerator(output, provider, blockTags.contentsGetter()/*, helper*/);
        HcsRecipeProvider recipes = new HcsRecipeProvider(output);
        LootTableProvider lootTables = HcsLootTableProvider.create(output); // todo suspicious method called here
        // Server side only, logic
        generator.addProvider(includeServer, new HcsBlockStateProvider(output, helper));
        // Client side only, rendering
        generator.addProvider(includeClient, new HcsItemModelProvider(output, fileHelper));
    }
}