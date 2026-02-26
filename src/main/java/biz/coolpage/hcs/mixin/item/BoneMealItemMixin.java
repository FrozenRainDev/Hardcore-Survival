package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.util.LootHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static biz.coolpage.hcs.util.WorldHelper.FERTILIZER_FREE;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin {

    @Inject(method = "applyBonemeal", at = @At("RETURN"))
    private static void applyBonemeal(ItemStack stack, Level level, BlockPos pos, Player player, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = level.getBlockState(pos);
        // 在 Mojang 映射中 cir.getReturnValue() 直接返回 Boolean
        if (state.getBlock() instanceof CropBlock && cir.getReturnValue()) {
            BlockPos posDown = pos.below();
            BlockState stateDown = level.getBlockState(posDown);

            if (stateDown.is(Blocks.FARMLAND) && stateDown.getValues().containsKey(FERTILIZER_FREE)) {
                if (stateDown.getValue(FERTILIZER_FREE)) {
                    level.setBlockState(posDown, stateDown.setValue(FERTILIZER_FREE, false), 3);
                } else {
                    /*
                    滥用骨粉的情况
                    详见:
                        WorldHelper.FERTILIZER_FREE     - 指示作物是否施肥过的方块属性
                        FarmlandBlockMixin              - 耕地方块存储 FERTILIZER_FREE 属性
                        CropBlockMixin/applyGrowth()    - 当作物需要枯萎时阻止 Block Tick
                    */
                    level.destroyBlock(pos, false);
                    if (level instanceof ServerLevel serverLevel) {
                        LootHelper.modifyDroppedStacksForCrops(state, serverLevel, pos, null);
                    }
                    level.levelEvent(LevelEvent.LAVA_FIZZ, pos, 0);
                }
            }
        }
    }
}