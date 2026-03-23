package biz.coolpage.hcs.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class BowlOfFoodItem extends Item {

    public BowlOfFoodItem(Properties settings) {
        super(settings.stacksTo(1));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        ItemStack itemStack = super.finishUsingItem(stack, world, user);
        if (user instanceof Player && ((Player) user).getAbilities().instabuild) {
            return itemStack;
        }
        return new ItemStack(Items.BOWL);
    }
}