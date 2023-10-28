package biz.coolpage.hcs.util;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.status.HcsPersistentState;
import net.minecraft.block.*;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

public class WorldHelper {
    public static ServerWorld currWorld = null;
    public static final BooleanProperty FERTILIZER_FREE = BooleanProperty.of("hcs_fertilizer_free");
    public static final Predicate<BlockState> IS_GRAVITY_AFFECTED = state -> state != null && (state.isOf(Blocks.DIRT) || state.isOf(Blocks.DIRT_PATH) || state.isOf(Blocks.CLAY) || state.isOf(Blocks.COARSE_DIRT));
    public static final Predicate<RegistryEntry<Biome>> IS_SALTY_WATER_BIOME = entry -> entry.isIn(BiomeTags.IS_OCEAN) || entry.isIn(BiomeTags.IS_DEEP_OCEAN) || entry.isIn(BiomeTags.IS_BEACH) || TemperatureHelper.getBiomeName(entry).contains("stony_shore");

    public static void checkBlockGravity(World world, BlockPos pos) {
        try {
            if (!(world instanceof ServerWorld)) return;
            for (BlockPos bp : new BlockPos[]{pos, pos.up(), pos.down(), pos.east(), pos.west(), pos.south(), pos.north()}) {
                //Check the pos and its immediate pos
                BlockState state = world.getBlockState(bp);
                if (IS_GRAVITY_AFFECTED.test(state)) {
                    if (FallingBlock.canFallThrough(world.getBlockState(bp.down())) || bp.getY() < world.getBottomY()) {
                        if (IS_GRAVITY_AFFECTED.test(state)) FallingBlockEntity.spawnFromBlock(world, bp, state);
                        //Recurse for further neighbor tick
                        for (BlockPos bpn : new BlockPos[]{bp.up(), bp.down(), bp.east(), bp.west(), bp.south(), bp.north()})
                            checkBlockGravity(world, bpn);
                    }
                }
            }
        } catch (StackOverflowError error) {
            Reg.LOGGER.error("WorldHelper/checkBlockGravity(): StackOverflowError");
        }
    }

    //Do not abuse
    @SuppressWarnings({"CommentedOutCode", "GrazieInspection"})
    public static @Nullable ServerWorld getServerWorld() {
        return currWorld;
        //NOTE: Using MinecraftClient.class will crash in server env
        /*
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            Reg.LOGGER.error("WorldHelper/getServerWorld;Client instance is invalid");
            return null;
        }
        IntegratedServer integratedServer = client.getServer();
        if (integratedServer != null) {
            return integratedServer.getWorld(client.world.getRegistryKey());
        }
        return null;
         */
    }

    //Replaced by C2S packet
    @Deprecated
    public static @Nullable ServerPlayerEntity getServerPlayerEntity(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayerEntity) return serverPlayerEntity;
        ServerWorld serverWorld = getServerWorld();
        if (serverWorld != null && player != null) {
            PlayerEntity closestPlayer = serverWorld.getClosestPlayer(player, 0.01D);
            if (closestPlayer instanceof ServerPlayerEntity serverPlayerEntity) return serverPlayerEntity;
        }
        return null;
    }

    public static boolean isDeepInCave(World world, BlockPos pos) {
        return isDeepInCave(world, pos, 10, Integer.MAX_VALUE);
    }

    public static boolean isDeepInCave(World world, BlockPos pos, int maxSkyBrightness, int maxHeight) {
        if (world == null || pos == null) return false;
        @Nullable ServerWorld serverWorld = getServerWorld();
        if (serverWorld == null || !(serverWorld.getRegistryKey() == World.OVERWORLD)) return false;
        if (!(serverWorld.getChunkManager().getChunkGenerator().getBiomeSource() instanceof MultiNoiseBiomeSource))
            return false;//Superflat is not included
        return world.getLightLevel(LightType.SKY, pos) <= maxSkyBrightness && world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos).getY() > pos.getY() && pos.getY() <= maxHeight;
    }

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
            if (age == 0) loseFreshness(seed, world, cir);
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
            if (cir == null) loseFreshness(seedItem, world, pos);
            else modifyDroppedStacks(crop, seedItem, state, world, cir);
        }
        return isEligible;
    }

    @Contract(pure = true)
    public static void decreaseOreHarvest(Block @NotNull [] ores, Item oreItem, @NotNull BlockState state, @Nullable Entity entity, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (entity instanceof LivingEntity breaker) {
            for (Block ore : ores) {
                //noinspection SuspiciousMethodCalls
                if (state.isOf(ore) && !breaker.getMainHandStack().getEnchantments().contains(Enchantments.FORTUNE)) {
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

    public static void loseFreshness(Item item, ServerWorld world, @NotNull CallbackInfoReturnable<List<ItemStack>> cir) {
        // Crops will lose freshness when they are harvested at the initial stage of growth or being applied to excessive bone meals
        ItemStack stack = new ItemStack(item);
        RotHelper.setFresh(world, stack, 0.2F);
        ArrayList<ItemStack> dropList = new ArrayList<>();
        dropList.add(stack);
        cir.setReturnValue(dropList);
    }

    public static void loseFreshness(Item item, ServerWorld world, BlockPos pos) {
        ItemStack stack = new ItemStack(item);
        RotHelper.setFresh(world, stack, 0.2F);
        EntityHelper.dropItem(world, pos, stack);
    }

    public static boolean enhancedIsWaterNearby(World world, BlockPos pos) {
        for (int i = 0; i < 5; ++i) {
            if (FarmlandBlock.isWaterNearby(world, pos)) return true;
            pos = pos.down();
        }
        return false;
    }

    public static boolean shouldGenerateVillages() {
        if (currWorld == null) return false;
        return currWorld.getTime() > 768000L && applyNullable(HcsPersistentState.getServerState(currWorld), HcsPersistentState::hasObtainedCopperPickaxe, false);
    }
}
