package biz.coolpage.hcs.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DropExperienceBlock.class)
public class ExperienceDroppingBlockMixin {
    @Inject(method = "spawnAfterBreak", at = @At("HEAD"), cancellable = true)
    public void onSpawnAfterBreak(BlockState state, ServerLevel world, BlockPos pos, ItemStack tool, boolean dropExperience, CallbackInfo ci) {
        if (state != null && (state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE))) {
            // By canceling method execution, prevent the call to super.spawnAfterBreak(), thereby intercepting experience drop
            ci.cancel();
        }
    }
}