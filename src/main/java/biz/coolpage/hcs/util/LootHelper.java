package biz.coolpage.hcs.util;

import biz.coolpage.hcs.Reg;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LootHelper {

    public static int getCropAge(@NotNull BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock)
            for (IntProperty property : new IntProperty[]{Properties.AGE_1, Properties.AGE_2, Properties.AGE_3, Properties.AGE_4, Properties.AGE_5, Properties.AGE_7, Properties.AGE_15, Properties.AGE_25})
                if (state.contains(property)) return state.get(property);
        Reg.LOGGER.warn("WorldHelper/getCropAge/!state.contains(Properties.AGE_*);block=" + block);
        return 0;
    }

    public static void modifyDroppedStacks(Block crop, Item seed, @NotNull BlockState state, ServerWorld world, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (state.isOf(crop)) {
            int age = getCropAge(state);
            if (age == 0) WorldHelper.loseFreshness(seed, world, cir);
        }
    }

    public static boolean modifyDroppedStacks(@NotNull BlockState state, ServerWorld world, BlockPos pos, CallbackInfoReturnable<List<ItemStack>> cir) {
        // modifyDroppedStacks for all CropBlocks and StemBlocks
        boolean isEligible = false;
        Item seedItem = Items.AIR;
        Block block = state.getBlock();
        Block crop = Blocks.AIR;
        if (block instanceof CropBlock cropBlock) {
            crop = cropBlock;
            seedItem = cropBlock.getSeedsItem().asItem();
            isEligible = true;
        } else if (block instanceof StemBlock stemBlock) {
            crop = stemBlock;
            seedItem = stemBlock.pickBlockItem.get();
            isEligible = true;
        }
        if (isEligible) {
            if (cir == null) WorldHelper.loseFreshness(seedItem, world, pos);
            else modifyDroppedStacks(crop, seedItem, state, world, cir);
        }
        return isEligible;
    }

    @Contract(pure = true)
    public static void decreaseOreHarvest(Block @NotNull [] ores, Item oreItem, @NotNull BlockState state, @Nullable Entity entity, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (entity instanceof LivingEntity breaker) {
            for (Block ore : ores) {
                AtomicBoolean hasFortuneEnchantment = new AtomicBoolean(false);
                breaker.getMainHandStack().getEnchantments().forEach(nbtElement -> {
                    if (nbtElement == null) return;
                    if (nbtElement.asString().contains("fortune")) hasFortuneEnchantment.set(true);
                });
                if (state.isOf(ore) && !hasFortuneEnchantment.get()) {
                    Item prevDrop = cir.getReturnValue().get(0).getItem();
                    if (prevDrop == oreItem) { //exclude silk touch
                        ArrayList<ItemStack> dropList = new ArrayList<>();
                        dropList.add(new ItemStack(oreItem));
                        cir.setReturnValue(dropList);
                    }
                }
            }
        }
    }

    @Contract(pure = true)
    public static <T> void delSpecificExplosionLoot(@Nullable LootContext context, @Nullable CallbackInfoReturnable<T> cir, @Nullable T cancelVal) {
        if (context == null || cir == null || cancelVal == null) return;
        BlockState state = context.get(LootContextParameters.BLOCK_STATE);
        Entity explodedEnt = context.get(LootContextParameters.THIS_ENTITY);
        if (state == null || explodedEnt == null) return;
        final boolean isLog = state.getBlock() instanceof PillarBlock && (state.getMaterial() == Material.WOOD || state.getMaterial() == Material.NETHER_WOOD);
        final boolean isOreOrMetal = state.getMaterial() == Material.METAL || (state.getBlock() instanceof ExperienceDroppingBlock && state.getMaterial() == Material.STONE);
        final boolean isCreeper = explodedEnt instanceof CreeperEntity;
        final boolean isTnt = explodedEnt instanceof TntEntity;
        if ((isCreeper && (isLog || state.getBlock() == Blocks.STONE || isOreOrMetal)) || (isTnt && isOreOrMetal))
            cir.setReturnValue(cancelVal);
    }

}
