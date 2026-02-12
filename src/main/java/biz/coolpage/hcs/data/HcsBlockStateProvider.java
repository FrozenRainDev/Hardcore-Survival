package biz.coolpage.hcs.data;

import biz.coolpage.hcs.Hcs;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

// Also used for generating block textures-related JSON files
public class HcsBlockStateProvider extends BlockStateProvider {

    public HcsBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Hcs.MOD_ID, exFileHelper); // https://www.bilibili.com/video/BV1Vw411s7h4?t=153.5&p=12
    }

    @Override
    protected void registerStatesAndModels() {
        // https://www.bilibili.com/video/BV1Vw411s7h4?t=192.9&p=12
    }

    private void blockWithItem(@NotNull RegistryObject<Block> blockRegistryobject){
        simpleBlockWithItem(blockRegistryobject.get(),cubeAll(blockRegistryobject.get()));
    }

}
