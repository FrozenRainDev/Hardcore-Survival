package biz.coolpage.hcs.data.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class MineshaftChestModifier extends LootModifier {

    // Codec registration required by Forge for data-driven loading
    public static final Supplier<Codec<MineshaftChestModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, MineshaftChestModifier::new))
    );

    /**
     * Constructs a LootModifier.
     *
     * @param conditionsIn the ILootConditions that need to be matched before the loot is modified.
     */
    public MineshaftChestModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // Mimic the first JSON behavior: setting weights of iron_ingot, gold_ingot, and diamond to 0
        // This ensures they will absolutely never appear in the chest loot pool
        generatedLoot.removeIf(stack ->
                stack.is(Items.IRON_INGOT) ||
                        stack.is(Items.GOLD_INGOT) ||
                        stack.is(Items.DIAMOND)
        );
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}