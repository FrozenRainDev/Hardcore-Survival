package biz.coolpage.hcs.item;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class EffectiveFoodItem extends Item {

    final float health;

    final double sanity;

    public EffectiveFoodItem(Properties settings, float health, double sanity) {
        super(settings);
        this.health = health;
        this.sanity = sanity;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (user != null) {
            user.heal(this.health);
            if (user instanceof ServerPlayer player) ((StatAccessor) player).getSanityManager().add(this.sanity);
        }
        return super.finishUsingItem(stack, world, user);
    }

}