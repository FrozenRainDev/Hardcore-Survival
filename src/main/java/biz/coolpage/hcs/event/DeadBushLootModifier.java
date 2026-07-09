package biz.coolpage.hcs.event;

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

public class DeadBushLootModifier extends LootModifier {

    // Codec for registering the modifier in Forge
    public static final Codec<DeadBushLootModifier> CODEC = RecordCodecBuilder.create(inst ->
            codecStart(inst).apply(inst, DeadBushLootModifier::new)
    );

    public DeadBushLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context) {
        // Clear all original drops (vanilla behavior is dropping 0-2 sticks)
        generatedLoot.clear();

        // 30% chance to drop exactly 1 stick, otherwise drop nothing
        if (context.getRandom().nextFloat() <= 0.30f) {
            generatedLoot.add(new ItemStack(Items.STICK, 1));
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}