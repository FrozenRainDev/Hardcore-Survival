package biz.coolpage.hcs.util;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.status.HcsPersistentState;
import net.minecraft.world.level.block.*;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

public class WorldHelper {
    private static ServerLevel serverWorld = null;
    // Do not call ClientLevel class type directly! In dedicated server, there's no such class! This will disable running!
    private static Level clientWorld = null;
    private static Player clientPlayer = null;
    public static final BooleanProperty FERTILIZER_FREE = BooleanProperty.create("hcs_fertilizer_free");
    public static final Predicate<net.minecraft.world.level.block.state.BlockState> IS_GRAVITY_AFFECTED = state -> state != null && (state.getBlock() instanceof FallingBlock || state.is(Blocks.DIRT) || state.is(Blocks.DIRT_PATH) || state.is(Blocks.CLAY) || state.is(Blocks.COARSE_DIRT));
    public static final Predicate<Holder<Biome>> IS_SALTY_WATER_BIOME = entry -> entry.is(BiomeTags.IS_OCEAN) || entry.is(BiomeTags.IS_DEEP_OCEAN) || entry.is(BiomeTags.IS_BEACH) || TemperatureHelper.getBiomeName(entry).contains("stony_shore");

    public static void checkBlockGravity(Level world, BlockPos pos) {
        try {
            if (!(world instanceof ServerLevel)) return;
            for (BlockPos bp : new BlockPos[]{pos, pos.above(), pos.below(), pos.east(), pos.west(), pos.south(), pos.north()}) {
                // Check the pos and its immediate pos
                net.minecraft.world.level.block.state.BlockState state = world.getBlockState(bp);
                if (IS_GRAVITY_AFFECTED.test(state)) {
                    if (FallingBlock.isFree(world.getBlockState(bp.below())) || bp.getY() < world.getMinBuildHeight()) {
                        if (IS_GRAVITY_AFFECTED.test(state)) FallingBlockEntity.fall(world, bp, state);
                        // Recurse for further neighbor tick
                        for (BlockPos bpn : new BlockPos[]{bp.above(), bp.below(), bp.east(), bp.west(), bp.south(), bp.north()})
                            checkBlockGravity(world, bpn);
                    }
                }
            }
        } catch (StackOverflowError error) {
            Hcs.error("WorldHelper/checkBlockGravity(): StackOverflowError");
        }
    }

    // Do not abuse
    // NOTE: CLIENT SIDE ALWAYS == NULL!!!!!
    public static @Nullable ServerLevel getServerWorld() {
        if (serverWorld == null) {
            Hcs.error("WorldHelper::getServerWorld() You're likely to called this from client side. You should call cannotGetServerWorld first before getServerWorld()");
        }
        return serverWorld;
    }

    public static boolean cannotGetServerWorld() {
        return serverWorld == null;
    }

    @SuppressWarnings("unused")
    public static Level getClientWorld() {
        return clientWorld;
    }

    @Deprecated
    public static Player getClientPlayer() {
        return clientPlayer;
    }

    // CLIENT SIDE ONLY
    public static void updateClientWorldAndMainPlayer(@Nullable Level world, @Nullable Player player) {
        clientWorld = world;
        clientPlayer = player;
    }

    public static void trySetServerWorld(@Nullable Level world) {
        if (world instanceof ServerLevel wld) {
            serverWorld = wld;
        }
    }

    //Replaced by C2S packet
    @Deprecated
    public static @Nullable ServerPlayer getServerPlayerEntity(Player player) {
        if (player instanceof ServerPlayer serverPlayerEntity) return serverPlayerEntity;
        if (cannotGetServerWorld()) return null;
        ServerLevel serverWorld = getServerWorld();
        if (serverWorld != null && player != null) {
            Player closestPlayer = serverWorld.getNearestPlayer(player, 0.01D);
            if (closestPlayer instanceof ServerPlayer serverPlayerEntity) return serverPlayerEntity;
        }
        return null;
    }

    public static boolean isDeepInCave(Level world, BlockPos pos) {
        return isDeepInCave(world, pos, 10, Integer.MAX_VALUE);
    }

    public static boolean isDeepInCave(Level world, BlockPos pos, int maxSkyBrightness, int maxHeight) {
        if (world == null || pos == null || cannotGetServerWorld()) return false;
        @Nullable ServerLevel serverWorld = getServerWorld();
        if (serverWorld == null || !(serverWorld.dimension() == Level.OVERWORLD)) return false;
        if (!(serverWorld.getChunkSource().getGenerator().getBiomeSource() instanceof MultiNoiseBiomeSource))
            return false; // Super flat is not included
        return world.getBrightness(LightLayer.SKY, pos) <= maxSkyBrightness && world.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) > pos.getY() && pos.getY() <= maxHeight;
    }

    public static void loseFreshness(Item item, ServerLevel world, @NotNull CallbackInfoReturnable<List<ItemStack>> cir) {
        // Crops will lose freshness when they are harvested at the initial stage of growth or being applied to excessive bone meals
        ItemStack stack = new ItemStack(item);
        RotHelper.setFresh(world, stack, 0.2F);
        ArrayList<ItemStack> dropList = new ArrayList<>();
        dropList.add(stack);
        cir.setReturnValue(dropList);
    }

    public static void loseFreshness(Item item, ServerLevel world, BlockPos pos) {
        ItemStack stack = new ItemStack(item);
        RotHelper.setFresh(world, stack, 0.2F);
        EntityHelper.dropItem(world, pos, stack);
    }

    @Deprecated
    public static boolean enhancedIsWaterNearby(@Nullable Level world, BlockPos pos) {
        if (world != null && !world.isClientSide) {
            for (int i = 0; i < 5; ++i) {
                if (FarmBlock.isNearWater(world, pos)) return true;
                pos = pos.below();
            }
        }
        return false;
    }

    public static boolean shouldGenerateVillages() {
        if (serverWorld == null) return false;
        return applyNullable(HcsPersistentState.getServerState(serverWorld), HcsPersistentState::hasObtainedCopperPickaxe, false);
    }

    @Contract(value = "null -> new", pure = true)
    public static int @NotNull [] getTimeAsReal(Level world) {
        int[] time = {0, 0, 0};
        if (world == null) return time;
        long lunarTime = world.getDayTime();
        while (lunarTime > 24000L) lunarTime -= 24000L;
        time[0] = (int) (Math.floor(lunarTime / 1000.0) + 6);
        time[1] = (int) Math.floor((lunarTime - Math.floor(lunarTime / 1000.0) * 1000) * 0.06);
        time[2] = (int) Math.floor((lunarTime - Math.floor(lunarTime / 100.0) * 100) * 0.6);
        return time;
    }

    public static BlockPos getPosByDirection(BlockPos pos, @NotNull Direction direction) {
        return switch (direction) {
            case UP -> pos.above();
            case DOWN -> pos.below();
            case NORTH -> pos.north();
            case SOUTH -> pos.south();
            case WEST -> pos.west();
            case EAST -> pos.east();
        };
    }
}