package biz.coolpage.hcs.item;

import biz.coolpage.hcs.Hcs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class HcsBlocks {
    // A register prepared for registering all mod blocks during game loading
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Hcs.MOD_ID);
    public static final RegistryObject<Block> ICEBOX = registerBlock("icebox", BlockBehaviour.Properties.copy(Blocks.COBBLESTONE).sound(SoundType.STONE)); // todo .mapColor(MapColor.WHITE_GRAY).strength(2.0F, 3.0F).requiresTool().nonOpaque()

    private static RegistryObject<Block> registerBlock(String name, BlockBehaviour.Properties properties) {
        return registerBlock(name, () -> new Block(properties));
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        // Register block firstly, then register corresponding block item, by calling the method below
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return Hcs.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    // todo add creative tab
    // facade todo
    public static void register(@NotNull IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
