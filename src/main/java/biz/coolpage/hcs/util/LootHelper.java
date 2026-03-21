package biz.coolpage.hcs.util;

import biz.coolpage.hcs.Hcs; // Reg -> Hcs
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel; // ServerWorld -> ServerLevel
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt; // TntEntity -> PrimedTnt
import net.minecraft.world.entity.monster.Creeper; // CreeperEntity -> Creeper
import net.minecraft.world.entity.projectile.DragonFireball; // DragonFireballEntity -> DragonFireball
import net.minecraft.world.entity.projectile.LargeFireball; // FireballEntity -> LargeFireball
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties; // Properties -> BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty; // IntProperty -> IntegerProperty
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams; // LootContextParameters -> LootContextParams
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
            for (IntegerProperty property : new IntegerProperty[]{BlockStateProperties.AGE_1, BlockStateProperties.AGE_2, BlockStateProperties.AGE_3, BlockStateProperties.AGE_4, BlockStateProperties.AGE_5, BlockStateProperties.AGE_7, BlockStateProperties.AGE_15, BlockStateProperties.AGE_25})
                if (state.hasProperty(property))
                    return state.getValue(property); // contains -> hasProperty, get -> getValue
        Hcs.warn("WorldHelper/getCropAge/!state.contains(Properties.AGE_*);block={}", block); // Reg -> Hcs
        return 0;
    }

    public static void modifyDroppedStacksForCrops(Block crop, Item seed, @NotNull BlockState state, ServerLevel world, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (state.is(crop)) { // isOf -> is
            int age = getCropAge(state);
            if (age == 0) WorldHelper.loseFreshness(seed, world, cir);
        }
    }

    public static boolean modifyDroppedStacksForCrops(@NotNull BlockState state, ServerLevel world, BlockPos pos, CallbackInfoReturnable<List<ItemStack>> cir) {
        // modifyDroppedStacksForCrops for all CropBlocks and StemBlocks
        boolean hasModified = false;
        Item seedItem = Items.AIR;
        Block block = state.getBlock();
        Block crop = Blocks.AIR;
        if (block instanceof CropBlock cropBlock) {
            crop = cropBlock;
            seedItem = cropBlock.getCloneItemStack(world, pos, state).getItem(); // getSeedsItem (Yarn) -> getCloneItemStack/getBaseSeedId (Mojang)
            hasModified = true;
        } else if (block instanceof StemBlock stemBlock) {
            crop = stemBlock;
            seedItem = stemBlock.seedSupplier.get(); // pickBlockItem -> seedSupplier
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
                breaker.getMainHandItem().getEnchantmentTags().forEach(nbtElement -> { // getMainHandStack -> getMainHandItem, getEnchantments -> getEnchantmentTags
                    if (nbtElement == null) return;
                    if (nbtElement.toString().contains("fortune")) hasFortuneEnchantment.set(true);
                });
                if (state.is(ore) && !hasFortuneEnchantment.get()) { // isOf -> is
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
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE); // get -> getParamOrNull
        Entity exploder = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (state == null || exploder == null) return;
        Block block = state.getBlock();
        final boolean isLog = block.getDescriptionId().contains("_log"); // getTranslationKey -> getDescriptionId
        final boolean isOreOrMetal = state.getSoundType().equals(SoundType.METAL) || (block instanceof DropExperienceBlock && state.getSoundType().equals(SoundType.STONE)); // getSoundGroup -> getSoundType, ExperienceDroppingBlock -> DropExperienceBlock
        final boolean isCreeper = exploder instanceof Creeper;
        final boolean isTnt = exploder instanceof PrimedTnt;
        final boolean isTorch = block.getDescriptionId().contains("torch");
        final boolean isFireball = exploder instanceof LargeFireball || exploder instanceof DragonFireball;
        if ((isTorch && (isTnt || isFireball || isCreeper))
                || ((isLog || block == Blocks.STONE || isOreOrMetal) && isCreeper)
                || (isOreOrMetal && isTnt)) {
            cir.setReturnValue(cancelVal);
        }
    }

    public static void mixinToolsPostMine(ItemStack stack, BlockState state, LivingEntity miner, CallbackInfoReturnable<Boolean> cir) {
        if (stack == null || state == null || miner == null || cir == null) return;
        Block block = state.getBlock();
        // In Minecraft 1.20.1 Mojang mappings, FernBlock (fern) and GrassBlock (grass) in Yarn mappings are actually both instances of TallGrassBlock at the code level.
        // Meanwhile, TallPlantBlock (two-block-tall plants, such as large ferns and tall grass) in Yarn corresponds to DoublePlantBlock in Mojang mappings.
        if (state.is(BlockTags.FLOWERS) || block instanceof TorchBlock || (stack.getItem() instanceof SwordItem swordItem && swordItem.getTier() == Tiers.WOOD && (block instanceof TallGrassBlock || block instanceof DoublePlantBlock)))
            cir.setReturnValue(true);
    }

    /*
    private static final ResourceLocation CAMPFIRE_LOOT_TABLE_ID = Blocks.CAMPFIRE.getLootTableId(); // Identifier -> ResourceLocation

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