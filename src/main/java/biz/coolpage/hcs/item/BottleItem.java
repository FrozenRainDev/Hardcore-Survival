package biz.coolpage.hcs.item;

import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.TemperatureManager;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_LIKE;

public class BottleItem extends PotionItem {
    private @Nullable MobEffectInstance effectInstance = null;

    public BottleItem(Properties settings, @Nullable MobEffectInstance statusEffectInstance) {
        super(settings);
        this.effectInstance = statusEffectInstance;
    }

    public BottleItem(Properties settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity livingEntity) {
        if (livingEntity instanceof Player player) {
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
                RotHelper.addDebuff(world, serverPlayer, stack);
                TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
                EntityHelper.checkOvereaten(serverPlayer, true);
                if (temperatureManager.get() > 0.8) temperatureManager.add(-0.08);
                if (IS_SURVIVAL_LIKE.test(player)) {
                    ((StatAccessor) serverPlayer).getThirstManager().add(0.3);
                    if (this.effectInstance != null) {
                        if (this.effectInstance.getEffect() == HcsEffects.THIRST) {
                            livingEntity.addEffect(livingEntity.hasEffect(HcsEffects.THIRST) ? new MobEffectInstance(HcsEffects.THIRST, Math.min(Objects.requireNonNull(livingEntity.getEffect(HcsEffects.THIRST)).getDuration() + 1200, 9600), 0, false, false, true) : new MobEffectInstance(this.effectInstance));
                        } else serverPlayer.addEffect(new MobEffectInstance(this.effectInstance));
                    }
                    //Must new() to duplicate an effect to clone in case of invalid repeat adding
                    stack.shrink(1);
                    EntityHelper.dropItem(serverPlayer, Items.GLASS_BOTTLE);
                }
            }
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        if (livingEntity != null) livingEntity.gameEvent(GameEvent.DRINK);
        return stack;
    }
}