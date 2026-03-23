package biz.coolpage.hcs.item;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class BoosterShotItem extends BandageItem {
    public BoosterShotItem() {
        super(0.0, 70);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (user instanceof ServerPlayer player && player.isAlive()) {
            ((StatAccessor) player).getStatusManager().setSoulImpairedStat(0);
            world.levelEvent(2005, user.blockPosition(), 0); // Play particles (WorldEvents.BONE_MEAL_USED)
        }
        return super.finishUsingItem(stack, world, user);
    }

}