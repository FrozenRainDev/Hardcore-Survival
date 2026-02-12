package biz.coolpage.hcs.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class HcsItemTagGenerator extends ItemTagsProvider {
    public HcsItemTagGenerator(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagLookup<Block>> pBlockTags) {
        super(pOutput, pLookupProvider, pBlockTags); // todo strange here
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        // customize tag here
        // https://github.com/TeamTwilight/twilightforest/blob/1.21.x/src/data/java/twilightforest/datagen/data/tags/ItemTagGenerator.java#L22
    }

    @Override
    public String getName() {
        return "Hardcore Survival Item Tags";
    }
}
