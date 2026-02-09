package biz.coolpage.hcs.item;

import biz.coolpage.hcs.Hcs;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Supplier;

public final class HcsItems {
    //todo data gen -> custom block -> custom item -> custom tool
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Hcs.MOD_ID);

    public static final RegistryObject<Item> FLINT_HATCHET = register("flint_hatchet", () -> new Item(new Item.Properties()));

    public static void register(IEventBus iEventBus) {
        ITEMS.register(iEventBus);
    }

    // register() = call create() in factory by facade+responsibility chain design pattern
    private static RegistryObject<Item> register(String name) {
        return register(name, () -> new Item(new Item.Properties()));
    }

    private static <T extends Item> RegistryObject<T> register(@NotNull String name, Supplier<T> item) {
        return ITEMS.register(name.toLowerCase(Locale.ROOT/* free from languages distinction */), item);
    }


}
