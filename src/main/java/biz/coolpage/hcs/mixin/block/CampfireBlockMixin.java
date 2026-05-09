package biz.coolpage.hcs.mixin.block;

import biz.coolpage.hcs.util.CombustionHelper;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static biz.coolpage.hcs.util.CombustionHelper.isTorchWithFlame;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin extends BaseEntityBlock {
    @Shadow
    @Final
    public static BooleanProperty WATERLOGGED;

    protected CampfireBlockMixin(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(@NotNull BlockState state, Level world, BlockPos pos, @NotNull Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (state.hasProperty(CampfireBlock.LIT) && !state.getValue(CampfireBlock.LIT)) {
            // Lit up with burning torch
            if (isTorchWithFlame(stack.getItem()) && !state.getValue(WATERLOGGED)) {
                world.setBlock(pos, state.setValue(CampfireBlock.LIT, true), 3);
                world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS);
                cir.setReturnValue(InteractionResult.sidedSuccess(world.isClientSide()));
            }
        }
        // Add fuel
        if (CombustionHelper.checkAddFuel(world, pos, state, stack))
            cir.setReturnValue(InteractionResult.sidedSuccess(world.isClientSide()));
        // Cook single meat at once
        if (world.getBlockEntity(pos) instanceof CampfireBlockEntity campfire) {
            Optional<CampfireCookingRecipe> result = campfire.getCookableRecipe(stack);
            if (result.isPresent() && RotHelper.isMeat(result.get().getResultItem(world.registryAccess())))
                for (ItemStack cooking : campfire.getItems())
                    if (RotHelper.isMeat(cooking)) {
                        EntityHelper.msgById(player, "tip.hcsurvival.cannot_cook_more_meat");
                        cir.setReturnValue(InteractionResult.PASS);
                    }
        }
    }

    @Inject(method = "createBlockStateDefinition", at = @At("HEAD"))
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(CombustionHelper.COMBUST_LUMINANCE);
    }
}
