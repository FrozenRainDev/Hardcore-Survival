package com.hcs.mixin.block;

import com.hcs.Reg;
import com.hcs.util.RotHelper;
import com.hcs.util.WorldHelper;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Block.class)
public class BlockMixin {

    private static void checkFreshnessForReapingCrops(Block crop, Item seed, @NotNull BlockState state, ServerWorld world, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (state.isOf(crop)) {
            if (state.contains(Properties.AGE_1)) {
                if (state.get(Properties.AGE_1) == 0) {
                    halveFreshness(seed, world, cir);
                }
            } else if (state.contains(Properties.AGE_2)) {
                if (state.get(Properties.AGE_2) == 0) {
                    halveFreshness(seed, world, cir);
                }
            } else if (state.contains(Properties.AGE_3)) {
                if (state.get(Properties.AGE_3) == 0) {
                    halveFreshness(seed, world, cir);
                }
            } else if (state.contains(Properties.AGE_4)) {
                if (state.get(Properties.AGE_4) == 0) {
                    halveFreshness(seed, world, cir);
                }
            } else if (state.contains(Properties.AGE_5)) {
                if (state.get(Properties.AGE_5) == 0) {
                    halveFreshness(seed, world, cir);
                }
            } else if (state.contains(Properties.AGE_7)) {
                if (state.get(Properties.AGE_7) == 0) {
                    halveFreshness(seed, world, cir);
                }
            } else if (state.contains(Properties.AGE_15)) {
                if (state.get(Properties.AGE_15) == 0) {
                    halveFreshness(seed, world, cir);
                }
            } else if (state.contains(Properties.AGE_25)) {
                if (state.get(Properties.AGE_25) == 0) {
                    halveFreshness(seed, world, cir);
                }
            } else
                Reg.LOGGER.warn("BlockMixin/checkFreshnessForReapingCrops/!state.contains(Properties.AGE_*);crop=" + crop);
        }
    }

    private static void halveFreshness(Item item, ServerWorld world, CallbackInfoReturnable<List<ItemStack>> cir) {
        ItemStack stack = new ItemStack(item);
        RotHelper.setFresh(world, stack, 0.5F);
        ArrayList<ItemStack> dropList = new ArrayList<>();
        dropList.add(stack);
        cir.setReturnValue(dropList);
    }


    @Inject(at = @At("HEAD"), method = "onBroken")
    private void onBroken(WorldAccess world, BlockPos pos, BlockState state, CallbackInfo ci) {
        WorldHelper.checkBlockGravity((World) world, pos);
    }

    @Inject(at = @At("HEAD"), method = "onPlaced")
    private void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {
        WorldHelper.checkBlockGravity(world, pos);
    }

    @Inject(at = @At("HEAD"), method = "randomDisplayTick")
    public void randomDisplayTick(@NotNull BlockState state, World world, BlockPos pos, Random random, CallbackInfo ci) {
        if (state.isOf(Blocks.DIRT) || state.isOf(Blocks.DIRT_PATH) || state.isOf(Blocks.CLAY)) {
            if (random.nextInt(16) == 0 && FallingBlock.canFallThrough(world.getBlockState(pos.down()))) {
                double d = (double) pos.getX() + random.nextDouble();
                double e = (double) pos.getY() - 0.05;
                double f = (double) pos.getZ() + random.nextDouble();
                world.addParticle(new BlockStateParticleEffect(ParticleTypes.FALLING_DUST, state), d, e, f, 0.0, 0.0, 0.0);
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;", cancellable = true)
    private static void getDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack stack, CallbackInfoReturnable<List<ItemStack>> cir) {
        checkFreshnessForReapingCrops(Blocks.CARROTS, Items.CARROT, state, world, cir);
        checkFreshnessForReapingCrops(Blocks.POTATOES, Items.POTATO, state, world, cir);
        checkFreshnessForReapingCrops(Blocks.WHEAT, Items.WHEAT_SEEDS, state, world, cir);
        checkFreshnessForReapingCrops(Blocks.BEETROOTS, Items.BEETROOT_SEEDS, state, world, cir);
        checkFreshnessForReapingCrops(Blocks.MELON_STEM, Items.MELON_SEEDS, state, world, cir);
        checkFreshnessForReapingCrops(Blocks.PUMPKIN_STEM, Items.PUMPKIN_SEEDS, state, world, cir);
//        checkFreshnessForReapingCrops(Blocks.SUGAR_CANE,Items.SUGAR_CANE,state,world,cir); //age always 0
    }

    @Inject(at = @At("HEAD"), method = "onLandedUpon", cancellable = true)
    public void onLandedUpon(World world, @NotNull BlockState state, BlockPos pos, Entity entity, float fallDistance, CallbackInfo ci) {
        Block block = state.getBlock();
        float multiplier = 1.0F;
        if (block instanceof GrassBlock) multiplier = 0.8F;
        else if (block instanceof SandBlock) multiplier = 0.7F;
        else if (block == Blocks.PODZOL || block == Blocks.MYCELIUM) multiplier = 0.9F;
        if(multiplier!=1.0F){
            entity.handleFallDamage(fallDistance, multiplier, entity.getDamageSources().fall());
            ci.cancel();
        }
    }

}
