package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PreventUnderwaterEatingHandler {

    /**
     * Reject the interaction before vanilla starts using the item.  Cancelling
     * LivingEntityUseItemEvent.Start is too late for the client: the use
     * animation has already been started by then.
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.@NotNull RightClickItem event) {
        Player player = event.getEntity();
        ItemStack itemStack = event.getItemStack();

        if (isUnderwaterEatingBlocked(player, itemStack)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            applyFailureEffects(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerEatStart(LivingEntityUseItemEvent.Start event) {
        checkAndCancelEating(event, true);
    }

    @SubscribeEvent
    public static void onPlayerEatTick(LivingEntityUseItemEvent.Tick event) {
        checkAndCancelEating(event, false);
    }

    /**
     * Common logic to check if a player is illegally eating underwater and cancel it.
     */
    private static void checkAndCancelEating(@NotNull LivingEntityUseItemEvent event, boolean isStart) {
        // Check if the entity is a player
        if (event.getEntity() instanceof Player player && EntityHelper.IS_SURVIVAL_LIKE.test(player)) {
            ItemStack itemStack = event.getItem();

            if (isUnderwaterEatingBlocked(player, itemStack)) {
                event.setCanceled(true);

                if (isStart) {
                    applyFailureEffects(player);
                }
            }
        }
    }

    private static boolean isUnderwaterEatingBlocked(Player player, ItemStack itemStack) {
        if (!EntityHelper.IS_SURVIVAL_LIKE.test(player)) {
            return false;
        }

        // Food and drink items are the only consumables restricted here.
        boolean isConsumable = itemStack.getItem().isEdible() || itemStack.getUseAnimation() == UseAnim.DRINK;
        if (!isConsumable || !player.isUnderWater()) {
            return false;
        }

        boolean hasWaterBreathing = player.hasEffect(MobEffects.WATER_BREATHING);
        boolean hasRespiration = EnchantmentHelper.getRespiration(player) > 0;
        BlockPos headPos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        boolean inBubbleColumn = player.level().getBlockState(headPos).is(Blocks.BUBBLE_COLUMN);

        return !hasWaterBreathing && !hasRespiration && !inBubbleColumn;
    }

    private static void applyFailureEffects(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerLevel level = serverPlayer.serverLevel();
            level.sendParticles(ParticleTypes.BUBBLE, player.getX(), player.getEyeY(), player.getZ(),
                    8, 0.2, 0.2, 0.2, 0.05);
            level.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getEyeY(), player.getZ(),
                    5, 0.1, 0.1, 0.1, 0.05);
            // EntityHelper.msgById(player, "tip.hcsurvival.cannot_eat_underwater");
        }
    }
}
