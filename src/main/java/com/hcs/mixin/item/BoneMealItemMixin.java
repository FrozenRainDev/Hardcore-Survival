package com.hcs.mixin.item;

import com.hcs.util.WorldHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin {
    //Also see CropBlockMixin/appendProperties()
    @Inject(method = "useOnFertilizable", at = @At("RETURN"))
    private static void useOnFertilizable(ItemStack stack, @NotNull World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof CropBlock && cir.getReturnValueZ()) {
            //Cant change it to FarmlandBlock as it will refresh after block update
            //See CropBlockMixin/withAge()
            if (state.getEntries().containsKey(WorldHelper.FERTILIZER_FREE)) {
                if (state.get(WorldHelper.FERTILIZER_FREE))
                    world.setBlockState(pos, state.with(WorldHelper.FERTILIZER_FREE, false));
                else {
                    //Abuse bone meal
                    world.syncWorldEvent(WorldEvents.LAVA_EXTINGUISHED, pos, 0);
                    world.breakBlock(pos, false);
                    //Also see break block at CropBlock/withAge()
                }
            }
        }
    }
}
