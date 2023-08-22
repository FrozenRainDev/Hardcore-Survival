package com.hcs.util;

import com.hcs.Reg;
import net.minecraft.block.*;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldHelper {
    public static World theWorld = null;
    public static final BooleanProperty FERTILIZER_FREE = BooleanProperty.of("hcs_fertilizer_free");

    public static boolean isAffectedByGravityInHCS(BlockState state) {
        if (state == null) return false;
        return state.isOf(Blocks.DIRT) || state.isOf(Blocks.DIRT_PATH) || state.isOf(Blocks.CLAY) || state.isOf(Blocks.COARSE_DIRT);
    }

    public static void checkBlockGravity(World world, BlockPos pos) {
        if (!(world instanceof ServerWorld)) return;
        for (BlockPos bp : new BlockPos[]{pos, pos.up(), pos.down(), pos.east(), pos.west(), pos.south(), pos.north()}) {
            //Check the pos and its immediate pos
            BlockState state = world.getBlockState(bp);
            if (isAffectedByGravityInHCS(state)) {
                if (FallingBlock.canFallThrough(world.getBlockState(bp.down())) || bp.getY() < world.getBottomY()) {
                    if (isAffectedByGravityInHCS(state)) {
                        FallingBlockEntity.spawnFromBlock(world, bp, state);
                    }
                    //Recurse for further neighbor update
                    for (BlockPos bpn : new BlockPos[]{bp.up(), bp.down(), bp.east(), bp.west(), bp.south(), bp.north()}) {
                        checkBlockGravity(world, bpn);
                    }
                }
            }
        }
    }

    //Do not abuse
    @SuppressWarnings({"CommentedOutCode", "GrazieInspection"})
    public static @Nullable ServerWorld getServerWorld() {
        if (theWorld instanceof ServerWorld serverWorld) return serverWorld;
        return null;
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
        if (block instanceof CropBlock) {
            for (IntProperty property : new IntProperty[]{Properties.AGE_1, Properties.AGE_2, Properties.AGE_3, Properties.AGE_4, Properties.AGE_5, Properties.AGE_7, Properties.AGE_15, Properties.AGE_25}) {
                if (state.contains(property)) {
                    return state.get(property);
                }
            }
        }
        Reg.LOGGER.warn("WorldHelper/getCropAge/!state.contains(Properties.AGE_*);block=" + block);
        return 0;
    }
}
