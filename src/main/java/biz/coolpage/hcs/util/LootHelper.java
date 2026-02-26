package biz.coolpage.hcs.util;

import biz.coolpage.hcs.Reg;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.*;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
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

// Also see LootModifier
@SuppressWarnings("CommentedOutCode")
public class LootHelper {

    public static int getCropAge(@NotNull BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock)
            for (IntProperty property : new IntProperty[]{Properties.AGE_1, Properties.AGE_2, Properties.AGE_3, Properties.AGE_4, Properties.AGE_5, Properties.AGE_7, Properties.AGE_15, Properties.AGE_25})
                if (state.contains(property)) return state.get(property);
        Reg.LOGGER.warn("WorldHelper/getCropAge/!state.contains(Properties.AGE_*);block={}", block);
        return 0;
    }

    public static void modifyDroppedStacksForCrops(Block crop, Item seed, @NotNull BlockState state, ServerWorld world, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (state.isOf(crop)) {
            int age = getCropAge(state);
            if (age == 0) WorldHelper.loseFreshness(seed, world, cir);
        }
    }

    public static boolean modifyDroppedStacksForCrops(@NotNull BlockState state, ServerWorld world, BlockPos pos, CallbackInfoReturnable<List<ItemStack>> cir) {
        // modifyDroppedStacksForCrops for all CropBlocks and StemBlocks
        boolean hasModified = false;
        Item seedItem = Items.AIR;
        Block block = state.getBlock();
        Block crop = Blocks.AIR;
        if (block instanceof CropBlock cropBlock) {
            crop = cropBlock;
            seedItem = cropBlock.getSeedsItem().asItem();
            hasModified = true;
        } else if (block instanceof StemBlock stemBlock) {
            crop = stemBlock;
            seedItem = stemBlock.pickBlockItem.get();
            hasModified = true;
        }
        if (hasModified) {
            if (cir == null) WorldHelper.loseFreshness(seedItem, world, pos);
            else modifyDroppedStacksForCrops(crop, seedItem, state, world, cir);
        }
        return hasModified;
    }

    @Contract(pure = true)
    public static void decreaseOreHarvest(Block @NotNull [] ores, Item oreItem, @NotNull BlockState state, @Nullable Entity entity, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (entity instanceof LivingEntity breaker) {
            for (Block ore : ores) {
                AtomicBoolean hasFortuneEnchantment = new AtomicBoolean(false);
                breaker.getMainHandStack().getEnchantments().forEach(nbtElement -> {
                    if (nbtElement == null) return;
                    if (nbtElement.toString().contains("fortune")) hasFortuneEnchantment.set(true);
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
    public static <T> void delSpecificLoot(@Nullable LootContext context, @Nullable CallbackInfoReturnable<T> cir, @Nullable T cancelVal) {
        if (context == null || cir == null || cancelVal == null) return;
        BlockState state = context.get(LootContextParameters.BLOCK_STATE);
        Entity exploder = context.get(LootContextParameters.THIS_ENTITY);
        if (state == null || exploder == null) return;
        Block block = state.getBlock();
        final boolean isLog = block.getTranslationKey().contains("_log"); // && state.getSoundGroup().equals(BlockSoundGroup.WOOD);
        final boolean isOreOrMetal = state.getSoundGroup().equals(BlockSoundGroup.METAL) || (block instanceof ExperienceDroppingBlock && state.getSoundGroup().equals(BlockSoundGroup.STONE));
        final boolean isCreeper = exploder instanceof CreeperEntity;
        final boolean isTnt = exploder instanceof TntEntity;
        final boolean isTorch = block.getTranslationKey().contains("torch");
        final boolean isFireball = exploder instanceof FireballEntity || exploder instanceof DragonFireballEntity;
        if ((isTorch && (isTnt || isFireball || isCreeper))
                || ((isLog || block == Blocks.STONE || isOreOrMetal) && isCreeper)
                || (isOreOrMetal && isTnt)) {
            cir.setReturnValue(cancelVal);
        }
    }

    public static void mixinToolsPostMine(ItemStack stack, BlockState state, LivingEntity miner, CallbackInfoReturnable<Boolean> cir) {
        if (stack == null || state == null || miner == null || cir == null) return;
        Block block = state.getBlock();
        if (state.isIn(BlockTags.FLOWERS) || block instanceof TorchBlock || (stack.getItem() instanceof SwordItem swordItem && swordItem.getMaterial() == ToolMaterials.WOOD && (block instanceof FernBlock || block instanceof TallPlantBlock)))
            cir.setReturnValue(true);
    }

    /*
    private static final Identifier CAMPFIRE_LOOT_TABLE_ID = Blocks.CAMPFIRE.getLootTableId();

    public static void init() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
             // Removed, JSON instead
            if (source.isBuiltin()) {
                if (CAMPFIRE_LOOT_TABLE_ID.equals(id)) {
                    tableBuilder.pools.clear(); // delete default looting
                }
            }
        });
    }
     */
}
