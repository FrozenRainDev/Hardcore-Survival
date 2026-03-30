package biz.coolpage.hcs.mixin.block;

import biz.coolpage.hcs.Hcs; // 改为Hcs
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin {
    // Yarn decaying() 在 Mojang 中通常对应 isRandomlyTicking 或手动检查距离，此处保留逻辑结构
    @Shadow
    public abstract boolean isRandomlyTicking(BlockState state);

    @Inject(method = "randomTick", at = @At("TAIL"))
    void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (state == null || world == null) return;
        int dropItem = 0;
        float temp = world.getBiome(pos).value().getBaseTemperature();
        if (this.isRandomlyTicking(state)) dropItem = 1;
        else if (!state.getValue(BlockStateProperties.PERSISTENT) && world.getBlockState(pos.below()).isAir() && Math.random() < (0.001 * Math.pow(Math.abs(temp) + 0.05, 2)))
            dropItem = 2;
        if (dropItem > 0) {
            BlockPos pos1 = dropItem == 2 ? pos.below() : pos;
            if (Math.random() < 0.008 && temp >= 0.8)
                EntityHelper.dropItem(world, pos1, Hcs.ORANGE.get()); // Reg -> Hcs
            else if (Math.random() < 0.003) EntityHelper.dropItem(world, pos1, Items.APPLE);
            else if (Math.random() < 0.005) EntityHelper.dropItem(world, pos1, Items.STICK);
        }
    }
}