package biz.coolpage.hcs.mixin.block;

import biz.coolpage.hcs.item.BurningCrudeTorchItem;
import biz.coolpage.hcs.status.accessor.ICampfireBlockEntity;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.InjuryManager;
import biz.coolpage.hcs.util.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(at = @At("HEAD"), method = "onBroken")
    private void onBroken(@NotNull WorldAccess world, BlockPos pos, @NotNull BlockState state, CallbackInfo ci) {
        WorldHelper.checkBlockGravity((World) world, pos);
        BlockPos up = pos.up();
        if (world instanceof ServerWorld && world.getBlockState(up).isIn(BlockTags.CAMPFIRES))
            world.breakBlock(up, false, null);
    }

    @Inject(at = @At("HEAD"), method = "onPlaced")
    private void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo ci) {
        WorldHelper.checkBlockGravity(world, pos);
        if (CombustionHelper.isFuelableCampfire(world.getBlockState(pos).getBlock().asItem())) {
            if (world.getBlockEntity(pos) instanceof ICampfireBlockEntity campfire) {
                NbtCompound nbt = stack.getOrCreateNbt();
                if (nbt.contains(BurningCrudeTorchItem.EXTINGUISH_NBT))
                    campfire.setBurnOutTime(stack.getOrCreateNbt().getLong(BurningCrudeTorchItem.EXTINGUISH_NBT));
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "randomDisplayTick")
    public void randomDisplayTick(@NotNull BlockState state, World world, BlockPos pos, Random random, CallbackInfo ci) {
        if (WorldHelper.IS_GRAVITY_AFFECTED.test(state)) {
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
        //DO NOT addRawPain sugar cane as its age always 0 (game ver 1.19)
        if (LootHelper.modifyDroppedStacksForCrops(state, world, pos, cir)) return;
        LootHelper.modifyDroppedStacksForCrops(Blocks.MELON_STEM, Items.MELON_SEEDS, state, world, cir);
        LootHelper.modifyDroppedStacksForCrops(Blocks.PUMPKIN_STEM, Items.PUMPKIN_SEEDS, state, world, cir);
        LootHelper.decreaseOreHarvest(new Block[]{Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE}, Items.RAW_COPPER, state, entity, cir);
        LootHelper.decreaseOreHarvest(new Block[]{Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE}, Items.RAW_IRON, state, entity, cir);
    }

    @Inject(at = @At("HEAD"), method = "onLandedUpon", cancellable = true)
    public void onLandedUpon(World world, @NotNull BlockState state, BlockPos pos, Entity entity, float fallDistance, CallbackInfo ci) {
        Block block = state.getBlock();
        float multiplier = 1.0F;
        if (state.isIn(BlockTags.WOOL)) multiplier = 0.5F;
        else if (block instanceof GrassBlock) multiplier = 0.8F;
        else if (block instanceof SandBlock) multiplier = 0.7F;
        else if (block == Blocks.PODZOL || block == Blocks.MYCELIUM || block == Blocks.DIRT_PATH || block == Blocks.DIRT)
            multiplier = 0.9F;
        else if (block.getHardness() > 2.0F) multiplier = 1.2F;
        if (!(fallDistance <= 4.5F && multiplier < 1)) {
            if (entity instanceof PlayerEntity player && fallDistance >= 2.0F) { //Moved from PlayerEntity/handleFallDamage()
                player.increaseStat(Stats.FALL_ONE_CM, Math.round(fallDistance * 100.0F));
                if (player.isSneaking()) fallDistance -= 1.2F;
            }
            if (fallDistance > 3.0F) {
                float exaggeratedFallDistance = (float) Math.pow(fallDistance, 1.2); //Gain more falling damage than before
                entity.handleFallDamage(multiplier < 1 ? exaggeratedFallDistance - 2 : exaggeratedFallDistance, multiplier, entity.getDamageSources().fall());
                if (entity instanceof ServerPlayerEntity player && EntityHelper.IS_SURVIVAL_LIKE.test(player) && fallDistance > 9.0F && (player.computeFallDamage(exaggeratedFallDistance, multiplier) / player.getMaxHealth()) > 0.4F) {
                    AtomicBoolean hasFF = new AtomicBoolean(false);
                    player.getArmorItems().forEach(stack -> hasFF.set(hasFF.get() || EnchantmentHelper.getLevel(Enchantments.FEATHER_FALLING, CommUtil.optElse(stack, ItemStack.EMPTY)) > 0));
                    if (!hasFF.get()) { //If player does NOT wear armor with feather falling enchantment, then apply fracture effect
                        InjuryManager injuryManager = ((StatAccessor) player).getInjuryManager();
                        injuryManager.addFracture(1.0);
                        injuryManager.addBleeding(1.6);
                    }
                }
            }
        }
        ci.cancel();
    }

    // onDestroyedByExplosion only triggers when breaking air(1.19.4)

}
