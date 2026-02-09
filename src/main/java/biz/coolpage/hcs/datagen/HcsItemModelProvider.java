package biz.coolpage.hcs.datagen;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.item.HcsItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

// Generate texture-based models
public class HcsItemModelProvider extends ItemModelProvider {

    public HcsItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Hcs.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // not called when client is launching, need manual operation???
//        for (var entry : HcsItems.ITEMS.getEntries()) {
//            simpleItem(entry);
//        }
        simpleItem(HcsItems.FLINT_HATCHET);
    }

    // todo 借鉴代码 https://github.com/TeamTwilight/twilightforest/blob/1.20.1/src/main/java/twilightforest/data/DataGenerators.java
    private ItemModelBuilder simpleItem(@NotNull RegistryObject<Item> item) {
        Hcs.print("Generating model for: " + item.getId().getPath());
        return withExistingParent(item.getId().getPath(),
                // Use base class built-in method mcLoc = new ResourceLocation("minecraft", "item/generated")
                mcLoc("item/generated")) // todo replace with HcsFactory
                .texture("layer0",
                        modLoc("item/" + item.getId().getPath()));
//                new ResourceLocation("item/generated")).texture("layer0",
//                new ResourceLocation(Hcs.MOD_ID, "item/" + item.getId().getPath()));
    }
}
