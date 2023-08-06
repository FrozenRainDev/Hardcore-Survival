package com.hcs.mixin.block;

import com.hcs.Reg;
import com.hcs.util.EntityHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin {
    @Shadow
    protected abstract boolean shouldDecay(BlockState state);

    @Inject(method = "randomTick", at = @At("TAIL"))
    void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (state == null || world == null) return;
        int dropItem = 0;
        float temp = world.getBiome(pos).value().getTemperature();
        if (this.shouldDecay(state)) dropItem = 1;
        else if (!state.get(Properties.PERSISTENT) && world.getBlockState(pos.down()).isAir() && Math.random() < (0.001 * Math.pow(Math.abs(temp) + 0.05, 2)))
            dropItem = 2;
        if (dropItem > 0) {
            BlockPos pos1 = dropItem == 2 ? pos.down() : pos;
            if (Math.random() < 0.008 && temp >= 0.8)
                EntityHelper.dropItem(world, pos1, new ItemStack(Reg.ORANGE));
            else if (Math.random() < 0.003) EntityHelper.dropItem(world, pos1, new ItemStack(Items.APPLE));
            else if (Math.random() < 0.005) EntityHelper.dropItem(world, pos1, new ItemStack(Items.STICK));
        }
    }

}
