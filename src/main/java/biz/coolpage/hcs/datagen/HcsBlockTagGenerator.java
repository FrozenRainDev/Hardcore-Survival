package biz.coolpage.hcs.datagen;

import biz.coolpage.hcs.Hcs;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class HcsBlockTagGenerator extends BlockTagsProvider {

    public HcsBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Hcs.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        // https://www.bilibili.com/video/BV1Vw411s7h4?t=271.3&p=12
//        this.tag(); // custom tag but I do not have one currently
    }
}
