package biz.coolpage.hcs.data;

import biz.coolpage.hcs.Hcs;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class HcsBiomeTagsProvider extends BiomeTagsProvider {

    public HcsBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        // Use Hcs.MOD_ID as strictly required
        super(output, lookupProvider, Hcs.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Define a custom tag key under the mod namespace "hcsurvival"
        TagKey<Biome> customTag = TagKey.create(
                net.minecraft.core.registries.Registries.BIOME,
                new ResourceLocation(Hcs.MOD_ID, "no_desert_biomes")
        );

        // Add broad biomes (e.g., Overworld) and strictly remove the desert tag
        // This avoids complex and redundant biome enumerations
        this.tag(customTag)
                .addTag(BiomeTags.IS_OVERWORLD)
                .remove(Tags.Biomes.IS_DESERT);
    }
}