package com.hcs.mixin.item;

import com.hcs.Reg;
import com.hcs.util.EntityHelper;
import com.hcs.util.WorldHelper;
import net.minecraft.block.BlockState;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AxeItem.class)
public abstract class AxeItemMixin {
    @Shadow
    protected abstract Optional<BlockState> getStrippedState(BlockState state);

    @Inject(method = "useOnBlock", at = @At("HEAD"))
    public void useOnBlock(@NotNull ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        if (getStrippedState(world.getBlockState(pos)).isPresent() && context.getPlayer() != null && !context.getPlayer().isCreative() && !context.getPlayer().isSpectator()) {
            if (WorldHelper.enhancedIsWaterNearby(world, pos.down()) && Math.random() < 0.5)
                EntityHelper.dropItem(world, pos, new ItemStack(Reg.WILLOW_BARK));
            else EntityHelper.dropItem(world, pos, new ItemStack(Reg.BARK));
        }
    }

}
