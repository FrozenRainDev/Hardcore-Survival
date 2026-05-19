package biz.coolpage.hcs.data.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class RemoveItemsModifier extends LootModifier {
    // Allows configuring the "item" field to be removed in JSON
    public static final Supplier<Codec<RemoveItemsModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(inst -> codecStart(inst).and(
                    ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(m -> m.targetItem)
            ).apply(inst, RemoveItemsModifier::new))
    );

    private final Item targetItem;

    /**
     * Constructs a LootModifier.
     *
     * @param conditionsIn the ILootConditions that need to be matched before the loot is modified.
     * @param targetItem   the item that should be filtered out from the final loot list.
     */
    public RemoveItemsModifier(LootItemCondition[] conditionsIn, Item targetItem) {
        super(conditionsIn);
        this.targetItem = targetItem;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // Dynamically exclude specified ingots (copper or iron) after mob loot generation
        // This allows for an accurate recreation of the generation settlement state where copper/iron ingots are absent in the target file
        generatedLoot.removeIf(stack -> stack.is(this.targetItem));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}