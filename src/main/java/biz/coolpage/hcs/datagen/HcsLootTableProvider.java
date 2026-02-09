package biz.coolpage.hcs.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import biz.coolpage.hcs.datagen.loot.HcsBlockLootTables;

import java.util.List;
import java.util.Set;

public class HcsLootTableProvider {
    @Contract("_ -> new")
    public static @NotNull LootTableProvider create(PackOutput output){
        return new LootTableProvider(output, Set.of(), List.of(new LootTableProvider.SubProviderEntry(HcsBlockLootTables::new,
                LootContextParamSets.BLOCK)));
    }
}
