package biz.coolpage.hcs.item;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class EffectiveFoodItem extends Item {

    final float health;

    final double sanity;

    public EffectiveFoodItem(Settings settings, float health, double sanity) {
        super(settings);
        this.health = health;
        this.sanity = sanity;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user != null) {
            user.heal(this.health);
            if (user instanceof ServerPlayerEntity player) ((StatAccessor) player).getSanityManager().add(this.sanity);
        }
        return super.finishUsing(stack, world, user);
    }

}
