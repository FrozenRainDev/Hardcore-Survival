package biz.coolpage.hcs.data.loot;

import biz.coolpage.hcs.util.HcsFactory;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class GravelDropModifier extends LootModifier {

    // Codec for Forge registry serialization
    public static final Codec<GravelDropModifier> CODEC = RecordCodecBuilder.create(inst ->
            codecStart(inst).apply(inst, GravelDropModifier::new)
    );

    public GravelDropModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context) {
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        int fortuneLevel = 0;
        boolean hasSilkTouch = false;

        // Check enchantments on the tool
        if (tool != null) {
            fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
            hasSilkTouch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;
        }

        // If Silk Touch is applied, Vanilla handles it perfectly, we do not modify the drops
        if (hasSilkTouch) {
            return generatedLoot;
        }

        // Remove Vanilla gravel and flint drops to inject our own custom rates,
        // while preserving drops from other mods (maximize compatibility)
        boolean hasVanillaBaseDrops = generatedLoot.removeIf(stack ->
                stack.is(Items.GRAVEL) || stack.is(Items.FLINT)
        );

        // If no gravel or flint was about to drop (e.g. block not fully broken), skip modification
        if (!hasVanillaBaseDrops) {
            return generatedLoot;
        }

        double roll = context.getRandom().nextDouble();

        // 1. Roll for Copper Powder (MITE: 1/18 approx 0.0556)
        // Fortune increases the chance of finding rare minerals
        double copperChance = (1.0 / 18.0) * (1 + fortuneLevel * 0.5);

        if (roll < copperChance) {
            // Use Hcs.MOD_ID for the custom item
            Item copperPowder = ForgeRegistries.ITEMS.getValue(HcsFactory.createResourceLocation("raw_copper_powder"));
            if (copperPowder != null && copperPowder != Items.AIR) {
                generatedLoot.add(new ItemStack(copperPowder));
                return generatedLoot;
            }
        }

        // 2. Roll for Flint (MITE: 5/32 approx 0.156)
        // Fortune significantly increases flint drop rate like vanilla
        double flintChance = (5.0 / 32.0);
        if (fortuneLevel > 0) {
            flintChance = switch (fortuneLevel) {
                case 1 -> 0.25;
                case 2 -> 0.50;
                default -> 1.00;
            };
        }

        if (roll < (copperChance + flintChance)) {
            generatedLoot.add(new ItemStack(Items.FLINT));
            return generatedLoot;
        }

        // 3. Fallback: Drop default Gravel
        generatedLoot.add(new ItemStack(Items.GRAVEL));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}