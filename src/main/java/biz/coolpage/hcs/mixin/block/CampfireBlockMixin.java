package biz.coolpage.hcs.mixin.block;

import biz.coolpage.hcs.util.CombustionHelper;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static biz.coolpage.hcs.util.CombustionHelper.*;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin extends BlockWithEntity {
    protected CampfireBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    public void onUse(@NotNull BlockState state, World world, BlockPos pos, @NotNull PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);
        if (state.contains(CampfireBlock.LIT) && !state.get(CampfireBlock.LIT)) {
            // Lit up with burning torch
            if (isTorchWithFlame(stack.getItem())) {
                world.setBlockState(pos, state.with(CampfireBlock.LIT, true));
                world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS);
                cir.setReturnValue(ActionResult.success(world.isClient()));
            }
        }
        // Add fuel
        if (CombustionHelper.checkAddFuel(world, pos, state, stack))
            cir.setReturnValue(ActionResult.success(world.isClient()));
        // Cook single meat at once
        if (world.getBlockEntity(pos) instanceof CampfireBlockEntity campfire) {
            Optional<CampfireCookingRecipe> result = campfire.getRecipeFor(stack);
            if (result.isPresent() && RotHelper.isMeat(result.get().output))
                for (ItemStack cooking : campfire.getItemsBeingCooked())
                    if (RotHelper.isMeat(cooking)) {
                        EntityHelper.msgById(player, "hcs.tip.cannot_cook_more_meat");
                        cir.setReturnValue(ActionResult.PASS);
                    }
        }
    }

    @Inject(method = "appendProperties", at = @At("HEAD"))
    protected void appendProperties(StateManager.@NotNull Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(CombustionHelper.COMBUST_LUMINANCE);
    }
}
