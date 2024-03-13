package biz.coolpage.hcs.item;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class BoosterShotItem extends BandageItem {
    public BoosterShotItem(int useTime) {
        super(0.0, useTime);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof ServerPlayerEntity player && player.isAlive()) {
            ((StatAccessor) player).getStatusManager().setSoulImpairedStat(0);
            world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, user.getBlockPos(), 0); // Play particles
        }
        return super.finishUsing(stack, world, user);
    }

}
