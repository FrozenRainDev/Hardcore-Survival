package com.hcs.item;

import com.hcs.util.EntityHelper;
import com.hcs.util.RotHelper;
import com.hcs.status.manager.TemperatureManager;
import com.hcs.status.HcsEffects;
import com.hcs.status.accessor.StatAccessor;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class BottleItem extends PotionItem {
    private @Nullable StatusEffectInstance effectInstance = null;

    public BottleItem(Settings settings, @Nullable StatusEffectInstance statusEffectInstance) {
        super(settings);
        this.effectInstance = statusEffectInstance;
    }

    public BottleItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity livingEntity) {
        if (livingEntity instanceof PlayerEntity player) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);
                RotHelper.addDebuff(world, serverPlayer, stack);
                TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
                EntityHelper.checkOvereaten(serverPlayer, true);
                if (temperatureManager.get() > 0.8) temperatureManager.add(-0.08);
                if (!serverPlayer.isCreative()) {
                    ((StatAccessor) serverPlayer).getThirstManager().add(0.3);
                    if (this.effectInstance != null) {
                        if (this.effectInstance.getEffectType() == HcsEffects.THIRST) {
                            livingEntity.addStatusEffect(livingEntity.hasStatusEffect(HcsEffects.THIRST) ? new StatusEffectInstance(HcsEffects.THIRST, Math.min(Objects.requireNonNull(livingEntity.getStatusEffect(HcsEffects.THIRST)).getDuration() + 1200, 9600), 0, false, false, true) : new StatusEffectInstance(this.effectInstance));
                        } else serverPlayer.addStatusEffect(new StatusEffectInstance(this.effectInstance));
                    }
                    //Must new() an effect in case of invalid repeat adding
                    stack.decrement(1);
                    if (!serverPlayer.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE))) {
                        serverPlayer.dropItem(stack, false);
                    }
                }
            }
            player.incrementStat(Stats.USED.getOrCreateStat(this));
        }
        if (livingEntity != null) livingEntity.emitGameEvent(GameEvent.DRINK);
        return stack;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        if (stack != null && RotHelper.canRot(stack.getItem())) RotHelper.appendInfo(world, stack, tooltip);
    }
}
