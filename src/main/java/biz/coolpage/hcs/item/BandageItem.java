package biz.coolpage.hcs.item;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.InjuryManager;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;
import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_LIKE;

public class BandageItem extends Item {
    private final double bleedingReduction;
    private final int useTime;
    private final int bandageWorkTicks;

    public BandageItem(double bleedingReduction, int useTime) {
        this(bleedingReduction, useTime, 0);
    }

    public BandageItem(double bleedingReduction, int useTime, int bandageWorkTicks) {
        super(new Properties());
        this.bleedingReduction = bleedingReduction;
        this.useTime = useTime;
        this.bandageWorkTicks = bandageWorkTicks;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    } // FIXME UseAction.CROSSBOW and TOOT_HORN have bug in 1.19.4 which effect is as same as trident(UseAction.SPEAR)

    @Override
    public int getUseDuration(ItemStack stack) {
        return this.useTime;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (world != null && user != null) {
            user.startUsingItem(hand); //Indispensable! It will update this.activeItemStack in Entity which will check before calling usageTick()
            user.playSound(SoundEvents.SAND_PLACE, 0.5F, 0.5F);
        }
        return super.use(world, user, hand);
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (remainingUseTicks <= 0 || !(user instanceof Player)) applyNullable(user, LivingEntity::stopUsingItem);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (user instanceof ServerPlayer player && player.isAlive()) {
            InjuryManager injuryManager = ((StatAccessor) player).getInjuryManager();
            injuryManager.addBleeding(-bleedingReduction);
            if (IS_SURVIVAL_LIKE.test(player)) stack.shrink(1);
            ((StatAccessor) player).getStatusManager().addBandageWorkTicks(bandageWorkTicks);
            player.playSound(SoundEvents.WOOL_PLACE, 1.0F, 0.5F);
        }
        return super.finishUsingItem(stack, world, user);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        tooltip.add(Component.translatable("hcs.tip.bandage_heal").withStyle(ChatFormatting.GRAY));
    }
}