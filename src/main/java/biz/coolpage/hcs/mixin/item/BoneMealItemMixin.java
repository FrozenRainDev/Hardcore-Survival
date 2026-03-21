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
    private static void useOnFertilizable(ItemStack stack, @NotNull Level level, BlockPos pos, Player player, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof CropBlock && cir.getReturnValue()) {
            BlockPos posDown = pos.below();
            BlockState stateDown = level.getBlockState(posDown);
            if (stateDown.is(Blocks.FARMLAND) && stateDown.getValues().containsKey(FERTILIZER_FREE)) {
                if (stateDown.getValue(FERTILIZER_FREE))
                    level.setBlock(posDown, stateDown.setValue(FERTILIZER_FREE, false), 3);
                else {
                    /*
                    The case of abusing bone meals
                    Also see:
                        WorldHelper.FERTILIZER_FREE     - A block property indicates whether a crop was fertilized
                        FarmlandBlockMixin              - Farmland blocks stores the FERTILIZER_FREE property (Give up to onInteract CropBlock, as it has more frequent block tick and sophisticated links, and it also has many subclasses(include other mods) that override too many methods)
                        CropBlockMixin/applyGrowth()    - Prevent block tick when the crop needs to wither (If not, world.breakBlock becomes invalid as updating will regenerate that block)
                    */
                    level.destroyBlock(pos, false);
                    if (level instanceof ServerLevel serverLevel)
                        LootHelper.modifyDroppedStacksForCrops(state, serverLevel, pos, null);
                    level.levelEvent(LevelEvent.SOUND_EXTINGUISH_FIRE, pos, 0);
                }
            }
        }
    }
}