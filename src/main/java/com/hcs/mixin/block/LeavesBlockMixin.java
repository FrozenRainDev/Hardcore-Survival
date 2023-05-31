package com.hcs.mixin.block;

import com.hcs.main.Reg;
import com.hcs.main.helper.EntityHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin {
    @Shadow protected boolean shouldDecay(BlockState state) {
        return false;
    }

    @Inject(method = "randomTick", at = @At("TAIL"))
    void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (this.shouldDecay(state)) {
            if (Math.random() < 0.008) EntityHelper.dropItem(world, pos, new ItemStack(Reg.ORANGE));
            if (Math.random() < 0.003) EntityHelper.dropItem(world, pos, new ItemStack(Items.APPLE));
        }
    }

}
