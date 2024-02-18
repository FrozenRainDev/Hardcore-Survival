package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.util.LootHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static biz.coolpage.hcs.util.WorldHelper.FERTILIZER_FREE;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin {
    @Inject(method = "useOnFertilizable", at = @At("RETURN"))
    private static void useOnFertilizable(ItemStack stack, @NotNull World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof CropBlock && cir.getReturnValueZ()) {
            BlockPos posDown = pos.down();
            BlockState stateDown = world.getBlockState(posDown);
            if (stateDown.isOf(Blocks.FARMLAND) && stateDown.getEntries().containsKey(FERTILIZER_FREE)) {
                if (stateDown.get(FERTILIZER_FREE))
                    world.setBlockState(posDown, stateDown.with(FERTILIZER_FREE, false));
                else {
                    /*
                    The case of abusing bone meals
                    Also see:
                        WorldHelper.FERTILIZER_FREE     - A block property indicates whether a crop was fertilized
                        FarmlandBlockMixin              - Farmland blocks stores the FERTILIZER_FREE property (Give up to onInteract CropBlock, as it has more frequent block tick and sophisticated links, and it also has many subclasses(include other mods) that override too many methods)
                        CropBlockMixin/applyGrowth()    - Prevent block tick when the crop needs to wither (If not, world.breakBlock becomes invalid as updating will regenerate that block)
                    */
                    world.breakBlock(pos, false);
                    if (world instanceof ServerWorld serverWorld)
                        LootHelper.modifyDroppedStacksForCrops(state, serverWorld, pos, null);
                    world.syncWorldEvent(WorldEvents.LAVA_EXTINGUISHED, pos, 0);
                }
            }
        }
    }
}
