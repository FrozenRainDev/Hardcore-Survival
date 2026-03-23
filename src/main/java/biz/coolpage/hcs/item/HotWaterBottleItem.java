package biz.coolpage.hcs.item;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.entity.IceboxBlockEntity;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StatusManager;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static biz.coolpage.hcs.util.WorldHelper.cannotGetServerWorld;

public class HotWaterBottleItem extends Item {
    public HotWaterBottleItem() {
        super(new Properties().stacksTo(1));
    }

    public static final String HHE = "hcs_hwb_exp";
    public static final String HHES = "hcs_hwb_exp_slow";
    public static final String HHS = "hcs_hwb_stat";
    public static final String HHEP = "hcs_hwb_exp_percentage";
    public static final String HHCI = "hcs_hwb_cooldown_init_time";
    public static final String HHSM = "hcs_hwb_soul_campfire_marked";
    public static final long MAX_COOL_DOWN_LENGTH = 8000;
    public static final float ICEBOX_FREEZING_RATE = 4.0F;

    public static boolean isChangeable(@NotNull ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        return stack.is(Hcs.HOT_WATER_BOTTLE) && nbt.contains(HHS) && nbt.getInt(HHS) != 0;
    }

    public static @NotNull ItemStack setStatus(@NotNull ItemStack stack, int statId) {
        if (statId > 2 || statId < -2) Hcs.error("HotWaterBottleItem/setStatus;statId out of range");
        else {
            CompoundTag nbt = stack.getOrCreateTag();
            if (statId == 2) {
                statId = 1;
            } else if (statId == -2) {
                statId = -1;
            }
            nbt.putInt(HotWaterBottleItem.HHS, statId);
        }
        return stack;
    }

    public static long getExp(@NotNull ItemStack stack, boolean shouldSlowDown) {
        if (stack.getOrCreateTag().contains(shouldSlowDown ? HHES : HHE))
            return stack.getOrCreateTag().getLong(shouldSlowDown ? HHES : HHE);
        return 0;
    }

    public static float getExpPercent(Level world, ItemStack stack, boolean shouldSlowDown) {
        if (world == null) return 1.0F;
        CompoundTag nbt = stack.getOrCreateTag();
        if (nbt.contains(HHS) && nbt.getInt(HHS) == 0) return 0.0F;
        float percent = (float) (getExp(stack, shouldSlowDown) - world.getGameTime()) / (MAX_COOL_DOWN_LENGTH * (shouldSlowDown ? 3 : 1));
        if (percent > 1.0F) percent = 1.0F;
        else if (percent < 0.0F) percent = 0.0F;
        return percent;
    }

    public static float getUnsignedPercentByInitTime(Level world, long initTime) {
        if (world == null) {
            Hcs.error("HotWaterBottleItem/getUnsignedPercentByInitTime;world==null");
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, (world.getGameTime() - initTime) / (MAX_COOL_DOWN_LENGTH / ICEBOX_FREEZING_RATE)));
    }

    public static float getUnsignedPercentByInitTimeAdvanced(Level world, @NotNull ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (cannotGetServerWorld()) {
            Hcs.error("getUnsignedPercentByInitTimeAdvanced client side called");
            return 1.0F;
        }
        return nbt.contains(HHS) && nbt.getInt(HHS) > 0 ? 1.0F - getUnsignedPercentByInitTime(world, nbt.getLong(HHCI)) : getUnsignedPercentByInitTime(WorldHelper.getServerWorld(), nbt.getLong(HHCI));
    }

    public static void createExp(Level world, ItemStack stack, boolean shouldSlowDown) {
        createExp(world, stack, 1.0F, shouldSlowDown);
    }

    public static void createExp(Level world, ItemStack stack, float percent, boolean shouldSlowDown) {
        if (world == null) {
            Hcs.error("HotWaterBottleItem/createExp;world==null");
            setExp(stack, 0, shouldSlowDown);
            return;
        }
        setExp(stack, world.getGameTime() + (long) (MAX_COOL_DOWN_LENGTH * percent * (shouldSlowDown ? 3 : 1)), shouldSlowDown);
    }

    public static void createInit(Level world, ItemStack stack) {
        if (world == null || stack == null) {
            Hcs.error("HotWaterBottleItem/createInit;world==null||stack==null");
            return;
        }
        CompoundTag nbt = stack.getOrCreateTag();
        boolean isHot = nbt.contains(HHS) && nbt.getInt(HHS) > 0;
        setStatus(stack, isHot ? 1 : -1);
        float percent = getExpPercent(world, stack, nbt.contains(HHES));
        if (isHot) percent = 1.0F - percent;
        nbt.putLong(HHCI, Math.max(0L, world.getGameTime() - (long) (percent * MAX_COOL_DOWN_LENGTH / ICEBOX_FREEZING_RATE)));
    }

    public static void setExp(@NotNull ItemStack stack, long expirationTime, boolean shouldSlowDown) {
        stack.getOrCreateTag().putLong(shouldSlowDown ? HHES : HHE, expirationTime);
    }

    public static void update(Level world, Container inv) {
        update(world, inv, 0);
    }

    public static void update(Level world, Container inv, int trendType) {
        if (world == null || inv == null) {
            Hcs.error("HotWaterBottleItem/tick();world==null||inv==null");
            return;
        }
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (!stack.is(Hcs.HOT_WATER_BOTTLE)) continue;
            CompoundTag nbt = stack.getOrCreateTag();
            if (nbt.contains(HHCI)) {
                if (inv instanceof IceboxBlockEntity) {
                    if (nbt.contains(HHS) && nbt.getInt(HHS) > 0 && getUnsignedPercentByInitTime(world, nbt.getLong(HHCI)) >= 1.0F) {
                        createInit(world, stack);
                        setStatus(stack, -1);
                    }
                } else {
                    createExp(world, stack, getUnsignedPercentByInitTimeAdvanced(world, stack), nbt.contains(HHES));
                    nbt.remove(HHCI);
                }
            } else if (inv instanceof IceboxBlockEntity) createInit(world, stack);

            if (nbt.contains(HHEP)) {
                float percent = nbt.getFloat(HHEP);
                createExp(world, stack, Math.abs(percent), false);
                setStatus(stack, percent >= 0 ? 1 : -1);
                nbt.remove(HHEP);
            }

            if (nbt.contains(HHS)) {
                int statType = nbt.getInt(HHS);
                if (statType == 0) {
                    if (nbt.contains(HHE)) nbt.remove(HHE);
                    if (nbt.contains(HHES)) nbt.remove(HHES);
                } else {
                    boolean shouldSlowDown = trendType == 0 || trendType == nbt.getInt(HHS);
                    if (nbt.contains(shouldSlowDown ? HHES : HHE)) {
                        if (getExp(stack, shouldSlowDown) <= world.getGameTime())
                            inv.setItem(i, stack.getItem().getDefaultInstance());
                    } else createExp(world, stack, shouldSlowDown);
                    if (nbt.contains(shouldSlowDown ? HHE : HHES)) {
                        createExp(world, stack, getExpPercent(world, stack, !shouldSlowDown), shouldSlowDown);
                        nbt.remove(shouldSlowDown ? HHE : HHES);
                    }
                }
            } else nbt.putInt(HHS, 0);
        }
    }

    public static void onLeaveGame(Level world, Container inv) {
        if (inv == null) {
            Hcs.error("HotWaterBottleItem/onLeaveGame;inv==null");
            return;
        }
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            CompoundTag nbt = stack.getOrCreateTag();
            int stat = nbt.contains(HHS) ? nbt.getInt(HHS) : 0;
            if (!isChangeable(stack) || nbt.contains(HHES)) continue;
            if (nbt.contains(HHES)) update(world, inv, 0);
            if (nbt.contains(HHE) && stat != 0) {
                nbt.putFloat(HHEP, getExpPercent(world, stack, false) * stat);
                nbt.remove(HHE);
            }
        }
    }

    @Override
    public ItemStack getDefaultInstance() {
        return setStatus(super.getDefaultInstance(), 0);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        CompoundTag nbt = stack.getOrCreateTag();
        int tempId = 0;
        if ((nbt.contains(HHS) && nbt.getInt(HHS) != 0) || nbt.contains(HHEP)) {
            float percent;
            if (nbt.contains(HHCI)) return;
            else
                percent = nbt.contains(HHEP) ? nbt.getFloat(HHEP) : getExpPercent(world, stack, nbt.contains(HHES)) * nbt.getInt(HHS);
            if (percent < -0.8F) tempId = -5;
            else if (percent < -0.6F) tempId = -4;
            else if (percent < -0.4F) tempId = -3;
            else if (percent < -0.2F) tempId = -2;
            else if (percent < -0.0F) tempId = -1;
            else if (percent < 0.2F) tempId = 1;
            else if (percent < 0.4F) tempId = 2;
            else if (percent < 0.6F) tempId = 3;
            else if (percent < 0.8F) tempId = 4;
            else tempId = 5;
        }
        tooltip.add(Component.translatable("item.hcs.hot_water_bottle.description.temp").withStyle(ChatFormatting.GRAY)
                .append(Component.translatable("item.hcs.hot_water_bottle.description.temp." + tempId).withStyle(switch (tempId) {
                    case -5 -> ChatFormatting.DARK_BLUE;
                    case -4 -> ChatFormatting.BLUE;
                    case -3 -> ChatFormatting.DARK_AQUA;
                    case -2, -1 -> ChatFormatting.AQUA;
                    case 1, 2 -> ChatFormatting.YELLOW;
                    case 3 -> ChatFormatting.GOLD;
                    case 4 -> ChatFormatting.RED;
                    case 5 -> ChatFormatting.DARK_RED;
                    default -> ChatFormatting.WHITE;
                })));
        tooltip.add(Component.translatable("item.hcs.hot_water_bottle.description1").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.hcs.hot_water_bottle.description2").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (world instanceof ServerLevel && entity instanceof ServerPlayer player) {
            StatusManager statusManager = ((StatAccessor) player).getStatusManager();
            CompoundTag nbt = stack.getOrCreateTag();
            if (nbt.contains(HHS)) {
                if (nbt.getInt(HHS) < 0) statusManager.setRecentHasColdWaterBagTicks(20);
                if (nbt.getInt(HHS) > 0) statusManager.setRecentHasHotWaterBagTicks(20);
            }
            if (nbt.contains(HHSM)) nbt.remove(HHSM);
        }
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (stack == null || cannotGetServerWorld()) return super.getBarWidth(stack);
        CompoundTag nbt = stack.getOrCreateTag();
        return Math.round(13.0F * (nbt.contains(HHCI) ? getUnsignedPercentByInitTimeAdvanced(WorldHelper.getServerWorld(), stack) : getExpPercent(WorldHelper.getServerWorld(), stack, stack.getOrCreateTag().contains(HHES))));
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (nbt.contains(HHS)) return nbt.getInt(HHS) > 0 ? 0xff6000 : 0x0084ff;
        return super.getBarColor(stack);
    }
}