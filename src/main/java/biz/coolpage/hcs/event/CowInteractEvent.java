package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import static biz.coolpage.hcs.config.Configs.HOSTILE_COW;
import static biz.coolpage.hcs.util.EntityHelper.isInLeather;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CowInteractEvent {

    @SubscribeEvent
    public static void onCowInteract(PlayerInteractEvent.@NotNull EntityInteract event) {
        if (event.getTarget() instanceof Cow cow) {
            Player player = event.getEntity();
            InteractionHand hand = event.getHand();

            // NOT SOLELY `ServerPlayerEntity` -- Both server and client side need the interaction
            boolean isMilking = player.getItemInHand(hand).is(Items.BUCKET) && !cow.isBaby();

            if (isMilking && hand == InteractionHand.MAIN_HAND) {

                // Server Side Handling
                if (EntityHelper.IS_SURVIVAL_AND_SERVER.test(player)) {
                    // Block vanilla logic to prevent desync
                    event.setCanceled(true);

                    if (isInLeather(player) || !Configs.isEnabled(player, HOSTILE_COW)) {
                        long time = cow.level().getGameTime();
                        CompoundTag persistentData = cow.getPersistentData();
                        long milkedTime = persistentData.getLong("MilkedTime");

                        if (persistentData.contains("MilkedTime") && Math.abs(milkedTime - time) < 24000L) {
                            // Prevent multiple milking in one day
                            // Use level.playSound with null player to broadcast sound to all nearby players including the actor
                            cow.level().playSound(null, cow.blockPosition(), SoundEvents.COW_AMBIENT, SoundSource.NEUTRAL, 1.0F, 1.0F);
                            event.setCancellationResult(InteractionResult.FAIL);
                        } else {
                            persistentData.putLong("MilkedTime", time);

                            // Manually execute vanilla milking logic on server side
                            // Use level.playSound instead of player.playSound to ensure packet is sent correctly
                            cow.level().playSound(null, cow.blockPosition(), SoundEvents.COW_MILK, SoundSource.PLAYERS, 1.0F, 1.0F);

                            ItemStack filledResult = net.minecraft.world.item.ItemUtils.createFilledResult(
                                    player.getItemInHand(hand), player, Items.MILK_BUCKET.getDefaultInstance());
                            player.setItemInHand(hand, filledResult);

                            event.setCancellationResult(InteractionResult.SUCCESS);
                        }
                    } else {
                        // Rebel and become panic when someone milking without any leather clothing
                        cow.setLastHurtByMob(player); // It will automatically set this.lastHurtByMobTimestamp
                        event.setCancellationResult(InteractionResult.FAIL);
                    }
                }
                // Client Side Handling
                else if (player.level().isClientSide) {
                    // Cancel vanilla event to prevent phantom items (desync) and stuck drinking animation
                    if (!player.isCreative() && !player.isSpectator()) {
                        event.setCanceled(true);
                        // Swing hand and let server handle the actual inventory changes
                        event.setCancellationResult(InteractionResult.SUCCESS);
                    }
                }
            }
        }
    }
}