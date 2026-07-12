package biz.coolpage.hcs.mixin.block;

import biz.coolpage.hcs.item.BurningCrudeTorchItem;
import biz.coolpage.hcs.status.accessor.ICampfireBlockEntity;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.InjuryManager;
import biz.coolpage.hcs.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
    @Inject(at = @At("HEAD"), method = "destroy")
    private void destroy(@NotNull LevelAccessor world, BlockPos pos, @NotNull BlockState state, CallbackInfo ci) {
        WorldHelper.checkBlockGravity((Level) world, pos);
        BlockPos up = pos.above();
        if (world instanceof ServerLevel && world.getBlockState(up).is(BlockTags.CAMPFIRES))
            world.destroyBlock(up, false, null);
    }

    @Inject(at = @At("HEAD"), method = "setPlacedBy")
    private void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo ci) {
        WorldHelper.checkBlockGravity(world, pos);
        if (CombustionHelper.isFuelableCampfire(world.getBlockState(pos).getBlock().asItem())) {
            if (world.getBlockEntity(pos) instanceof ICampfireBlockEntity campfire) {
                CompoundTag nbt = stack.getOrCreateTag();
                if (nbt.contains(BurningCrudeTorchItem.EXTINGUISH_NBT))
                    campfire.setBurnOutTime(stack.getOrCreateTag().getLong(BurningCrudeTorchItem.EXTINGUISH_NBT));
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "animateTick")
    public void animateTick(@NotNull BlockState state, Level world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (WorldHelper.IS_GRAVITY_AFFECTED.test(state)) {
            if (random.nextInt(16) == 0 && FallingBlock.isFree(world.getBlockState(pos.below()))) {
                double d = (double) pos.getX() + random.nextDouble();
                double e = (double) pos.getY() - 0.05;
                double f = (double) pos.getZ() + random.nextDouble();
                world.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, state), d, e, f, 0.0, 0.0, 0.0);
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;", cancellable = true)
    private static void getDrops(BlockState state, ServerLevel world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack stack, CallbackInfoReturnable<List<ItemStack>> cir) {
        //DO NOT addRawPain sugar cane as its age always 0 (game ver 1.19)
        if (LootHelper.modifyDroppedStacksForCrops(state, world, pos, cir)) return;
        LootHelper.modifyDroppedStacksForCrops(Blocks.MELON_STEM, Items.MELON_SEEDS, state, world, cir);
        LootHelper.modifyDroppedStacksForCrops(Blocks.PUMPKIN_STEM, Items.PUMPKIN_SEEDS, state, world, cir);
        LootHelper.decreaseOreHarvest(new Block[]{Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE}, Items.RAW_COPPER, state, entity, cir);
        LootHelper.decreaseOreHarvest(new Block[]{Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE}, Items.RAW_IRON, state, entity, cir);
    }

    @Inject(at = @At("HEAD"), method = "fallOn", cancellable = true)
    public void fallOn(Level world, @NotNull BlockState state, BlockPos pos, Entity entity, float fallDistance, CallbackInfo ci) {
        Block block = state.getBlock();
        float multiplier = 1.0F;
        if (state.is(BlockTags.WOOL) || state.getBlock() instanceof LeavesBlock) multiplier = 0.3F;
        else if (block instanceof GrassBlock) multiplier = 0.8F;
        else if (block instanceof SandBlock) multiplier = 0.7F;
        else if (block == Blocks.PODZOL || block == Blocks.MYCELIUM || block == Blocks.DIRT_PATH || block == Blocks.DIRT)
            multiplier = 0.9F;
        else if (state.getDestroySpeed(world, pos) > 2.0F) multiplier = 1.2F;
        if (!(fallDistance <= 4.5F && multiplier < 1)) {
            if (entity instanceof Player player && fallDistance >= 2.0F) { //Moved from Player/causeFallDamage()
                player.awardStat(Stats.FALL_ONE_CM, Math.round(fallDistance * 100.0F));
                if (player.isShiftKeyDown()) fallDistance -= 1.2F;
            }
            if (fallDistance > 3.0F) {
                float exaggeratedFallDistance = (float) Math.pow(fallDistance, 1.2); //Gain more falling damage than before
                entity.causeFallDamage(multiplier < 1 ? exaggeratedFallDistance - 2 : exaggeratedFallDistance, multiplier, entity.damageSources().fall());
                if (entity instanceof ServerPlayer player && EntityHelper.IS_SURVIVAL_LIKE.test(player) && fallDistance > 9.0F && (player.calculateFallDamage(exaggeratedFallDistance, multiplier) / player.getMaxHealth()) > 0.5F) {
                    AtomicBoolean hasFF = new AtomicBoolean(false);
                    player.getArmorSlots().forEach(armorStack -> hasFF.set(hasFF.get() || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FALL_PROTECTION, CommUtil.optElse(armorStack, ItemStack.EMPTY)) > 0));
                    if (!hasFF.get()) { // If player does NOT wear armor with feather falling enchantment, then apply fracture effect
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
