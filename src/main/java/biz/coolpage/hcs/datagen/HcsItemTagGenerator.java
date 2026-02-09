package biz.coolpage.hcs.datagen;

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

    }
}
