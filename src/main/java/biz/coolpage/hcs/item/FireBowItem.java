package biz.coolpage.hcs.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.NotNull;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

public class FireBowItem extends Item {
    public FireBowItem(Settings settings) {
        super(settings);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public ActionResult useOnBlock(@NotNull ItemUsageContext context) {
        ItemStack stack = context.getStack();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state;
        if (world == null || !((state = world.getBlockState(pos)).isIn(BlockTags.CAMPFIRES) && state.contains(Properties.LIT) && !state.get(Properties.LIT)))
            return ActionResult.FAIL;
        long time = world.getTime();
        if (time % 2 == 0) {
            world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, 0); // Play particles & sounds
            if (time % 10 == 0)
                applyNullable(context.getPlayer(), p -> stack.damage(1, p, u -> u.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)));
            float rand = world.getRandom().nextFloat();
            if (rand < 0.005F) {
                world.setBlockState(pos, state.with(Properties.LIT, true));
                world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS);
            }
        }
        return ActionResult.success(world.isClient());
    }

    /*
    @Override
    public void usageTick(World world, LivingEntity user2, ItemStack stack, int remainingUseTicks) {
        if (remainingUseTicks < 0 || !(user2 instanceof PlayerEntity)) {
            user2.stopUsingItem();
            return;
        }
        PlayerEntity player = (PlayerEntity) user2;
        BlockHitResult blockHitResult = Item.raycast(world, player, RaycastContext.FluidHandling.NONE);
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            user2.stopUsingItem();
            return;
        }

    }
    */

}
