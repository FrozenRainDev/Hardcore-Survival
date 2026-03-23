package biz.coolpage.hcs.data;

import biz.coolpage.hcs.Hcs;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static biz.coolpage.hcs.util.HcsPredicates.isTool;

// Generate texture-based models
public class HcsItemModelProvider extends ItemModelProvider {

    public HcsItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Hcs.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (var entry : Hcs.ITEMS.getEntries()) {
            if (isTool(entry.get())) registerItemModel("handheld", entry);
            else registerItemModel(entry);
        }
    }

    private ItemModelBuilder registerItemModel(@NotNull RegistryObject<Item> item) {
        return registerItemModel(null, item);
    }

    private ItemModelBuilder registerItemModel(@Nullable String prefix, @NotNull RegistryObject<Item> item) {
        if (prefix == null) prefix = "generated";
        return withExistingParent(item.getId().getPath(),
                mcLoc("item/" + prefix))
                .texture("layer0", modLoc("item/" + item.getId().getPath()));
    }
}

