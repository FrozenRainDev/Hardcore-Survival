package com.hcs.main.helper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import org.jetbrains.annotations.Nullable;

public class WorldHelper {
    public static void checkBlockGravity(World world, BlockPos pos) {
        if (!(world instanceof ServerWorld)) return;
        for (BlockPos bp : new BlockPos[]{pos, pos.up(), pos.down(), pos.east(), pos.west(), pos.south(), pos.north()}) {
            //Check the pos and its immediate pos
            BlockState state = world.getBlockState(bp);
            if (state.isOf(Blocks.DIRT) || state.isOf(Blocks.DIRT_PATH)) {
                if (FallingBlock.canFallThrough(world.getBlockState(bp.down())) || bp.getY() < world.getBottomY()) {
                    if (state.isOf(Blocks.DIRT) || state.isOf(Blocks.DIRT_PATH)) {
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
    @SuppressWarnings("all")
    public static @Nullable ServerWorld getServerWorld() {
        if (RotHelper.theWorld instanceof ServerWorld serverWorld) return serverWorld;
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
        if (world == null || pos == null) return false;
        @Nullable ServerWorld serverWorld = getServerWorld();
        if (serverWorld == null) return false;
        if (!(serverWorld.getChunkManager().getChunkGenerator().getBiomeSource() instanceof MultiNoiseBiomeSource))
            return false;//Superflat is not included
        return world.getLightLevel(LightType.SKY, pos) <= 10 && world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos).getY() > pos.getY();
    }
}
