package biz.coolpage.hcs.item;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.status.HcsEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PotionItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Supplier;

public final class HcsItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Hcs.MOD_ID);
    public static final RegistryObject<Item>
            FLINT_HATCHET = register("flint_hatchet", new AxeItem(HcsTiers.FLINT_HATCHET, 6.0F, -3.1F, new Item.Properties())),
            IRONSKIN_POTION = register("hcs_ironskin", new PotionItem(new MobEffectInstance(HcsEffects.IRONSKIN, 3600, 0)));

    public static void register(IEventBus iEventBus) {
        ITEMS.register(iEventBus);
    }

    // register() = call create() in factory by facade+responsibility chain design pattern
    private static RegistryObject<Item> register(String name) {
        return register(name, new Item(new Item.Properties()));
    }

    private static RegistryObject<Item> register(@NotNull String name, Item item) {
        return register(name, () -> item);
    }

    private static <T extends Item> RegistryObject<T> register(@NotNull String name, Supplier<T> item) {
        return ITEMS.register(name.toLowerCase(Locale.ROOT/* free from languages distinction */), item);
    }
}
