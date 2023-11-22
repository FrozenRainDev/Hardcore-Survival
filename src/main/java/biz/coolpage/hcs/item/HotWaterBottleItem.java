package biz.coolpage.hcs.item;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.entity.IceboxBlockEntity;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StatusManager;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HotWaterBottleItem extends Item {
    public HotWaterBottleItem() {
        super(new Item.Settings().maxCount(1));
    }

    public static final String HHE = "hcs_hwb_exp"; //The expiry time for effect of cooling down/heating process
    public static final String HHES = "hcs_hwb_exp_slow"; //Expiry time will be deferred when cold bottle in chilly env or warm one in hot env; On the contrary, it will be transformed into HHE and vice versa
    public static final String HHS = "hcs_hwb_stat"; //Three status: 0: normal, -1: cold, 1:hot
    public static final String HHEP = "hcs_hwb_exp_percentage"; //percentage=(expiry time-now)/length of cool down. Saved after quit and removed and transferred to HHE or HHS when entering
    public static final String HHCI = "hcs_hwb_cooldown_init_time";
    public static final String HHSM = "hcs_hwb_soul_campfire_marked";
    public static final long MAX_COOL_DOWN_LENGTH = 8000; //The length of time that a hot water bottle needs to cool down to normal
    public static final float ICEBOX_FREEZING_RATE = 4.0F;

    public static boolean isChangeable(@NotNull ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        return stack.isOf(Reg.HOT_WATER_BOTTLE) && nbt.contains(HHS) && nbt.getInt(HHS) != 0;
    }

    public static @NotNull ItemStack setStatus(@NotNull ItemStack stack, int statId) {
        if (statId > 2 || statId < -2) Reg.LOGGER.error("HotWaterBottleItem/setStatus;statId out of range");
        else {
            NbtCompound nbt = stack.getOrCreateNbt();
            if (statId == 2) {
//                nbt.putFloat(HHEP, 1.0F);//Just heated
                statId = 1;
            } else if (statId == -2) {
//                nbt.putFloat(HHEP, -1.0F);//Just chilled
                statId = -1;
            }
            nbt.putInt(HotWaterBottleItem.HHS, statId);
        }
        return stack;
    }

    public static long getExp(@NotNull ItemStack stack, boolean shouldSlowDown) {
        if (stack.getOrCreateNbt().contains(shouldSlowDown ? HHES : HHE))
            return stack.getOrCreateNbt().getLong(shouldSlowDown ? HHES : HHE);
        return 0;
    }

    public static float getExpPercent(World world, ItemStack stack, boolean shouldSlowDown) {
        if (world == null) return 1.0F;
        NbtCompound nbt = stack.getOrCreateNbt();
        if (nbt.contains(HHS) && nbt.getInt(HHS) == 0) return 0.0F;
        float percent = (float) (getExp(stack, shouldSlowDown) - world.getTime()) / (MAX_COOL_DOWN_LENGTH * (shouldSlowDown ? 3 : 1));
        if (percent > 1.0F) percent = 1.0F;
        else if (percent < 0.0F) percent = 0.0F;
        return percent;
    }

    public static float getUnsignedPercentByInitTime(World world, long initTime) {
        if (world == null) {
            Reg.LOGGER.error("HotWaterBottleItem/getUnsignedPercentByInitTime;world==null");
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, (world.getTime() - initTime) / (MAX_COOL_DOWN_LENGTH / ICEBOX_FREEZING_RATE)));
    }

    public static float getUnsignedPercentByInitTimeAdvanced(World world, @NotNull ItemStack stack) {
        //hot water bottle will getRealPain inverse percent for debug
        NbtCompound nbt = stack.getOrCreateNbt();
        return nbt.contains(HHS) && nbt.getInt(HHS) > 0 ? 1.0F - getUnsignedPercentByInitTime(world, nbt.getLong(HHCI)) : getUnsignedPercentByInitTime(WorldHelper.currWorld, nbt.getLong(HHCI));
    }

    public static void createExp(World world, ItemStack stack, boolean shouldSlowDown) {
        createExp(world, stack, 1.0F, shouldSlowDown);
    }

    public static void createExp(World world, ItemStack stack, float percent, boolean shouldSlowDown) {
        if (world == null) {
            Reg.LOGGER.error("HotWaterBottleItem/createExp;world==null");
            setExp(stack, 0, shouldSlowDown);
            return;
        }
        setExp(stack, world.getTime() + (long) (MAX_COOL_DOWN_LENGTH * percent * (shouldSlowDown ? 3 : 1)), shouldSlowDown);
    }

    public static void createInit(World world, ItemStack stack) {
        if (world == null || stack == null) {
            Reg.LOGGER.error("HotWaterBottleItem/createInit;world==null||stack==null");
            return;
        }
        NbtCompound nbt = stack.getOrCreateNbt();
        boolean isHot = nbt.contains(HHS) && nbt.getInt(HHS) > 0;
        setStatus(stack, isHot ? 1 : -1);
        float percent = getExpPercent(world, stack, nbt.contains(HHES));
        if (isHot) percent = 1.0F - percent;
        nbt.putLong(HHCI, Math.max(0L, world.getTime() - (long) (percent * MAX_COOL_DOWN_LENGTH / ICEBOX_FREEZING_RATE)));
    }

    public static void setExp(@NotNull ItemStack stack, long expirationTime, boolean shouldSlowDown) {
        stack.getOrCreateNbt().putLong(shouldSlowDown ? HHES : HHE, expirationTime);
    }

    public static void update(World world, Inventory inv) {
        update(world, inv, 0);
    }

    public static void update(World world, Inventory inv, int trendType) {
        if (world == null || inv == null) {
            Reg.LOGGER.error("HotWaterBottleItem/tick();world==null||inv==null");
            return;
        }
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isOf(Reg.HOT_WATER_BOTTLE)) continue;
            NbtCompound nbt = stack.getOrCreateNbt();
            //Update temp after retrieved from icebox
            if (nbt.contains(HHCI)) {
                if (inv instanceof IceboxBlockEntity) {
                    if (nbt.contains(HHS) && nbt.getInt(HHS) > 0 && getUnsignedPercentByInitTime(world, nbt.getLong(HHCI)) >= 1.0F) {
                        //Turn warm to cool(transfer stat from 1 to -1)
                        createInit(world, stack);
                        setStatus(stack, -1);
                    }
                } else {
                    createExp(world, stack, getUnsignedPercentByInitTimeAdvanced(world, stack), nbt.contains(HHES));
//                    setStatus(stack, -1);
                    nbt.remove(HHCI);
                }
            } else if (inv instanceof IceboxBlockEntity) createInit(world, stack);
            //Transfer percentage to expiry time
            if (nbt.contains(HHEP)) {
                float percent = nbt.getFloat(HHEP);
                createExp(world, stack, Math.abs(percent), false);
                setStatus(stack, percent >= 0 ? 1 : -1);
                nbt.remove(HHEP);
            }
            //Update status
            if (nbt.contains(HHS)) {
                int statType = nbt.getInt(HHS);
                if (statType == 0) {
                    if (nbt.contains(HHE)) nbt.remove(HHE);
                    if (nbt.contains(HHES)) nbt.remove(HHES);
                } else {
                    boolean shouldSlowDown = trendType == 0 || trendType == nbt.getInt(HHS);
                    if (nbt.contains(shouldSlowDown ? HHES : HHE)) {
                        if (getExp(stack, shouldSlowDown) <= world.getTime())//coolDown time is up
                            inv.setStack(i, stack.getItem().getDefaultStack());//remove all nbt and HHS=0
                    } else createExp(world, stack, shouldSlowDown);
                    if (nbt.contains(shouldSlowDown ? HHE : HHES)) {
                        createExp(world, stack, getExpPercent(world, stack, !shouldSlowDown), shouldSlowDown);
                        nbt.remove(shouldSlowDown ? HHE : HHES);
                    }
                }
            } else nbt.putInt(HHS, 0);
        }
    }

    public static void onLeaveGame(World world, Inventory inv) {
        if (inv == null) {
            Reg.LOGGER.error("HotWaterBottleItem/onLeaveGame;inv==null");
            return;
        }
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getStack(i);
            NbtCompound nbt = stack.getOrCreateNbt();
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
    public ItemStack getDefaultStack() {
        return setStatus(super.getDefaultStack(), 0);
    }

    @Override
    public void appendTooltip(@NotNull ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        NbtCompound nbt = stack.getOrCreateNbt();
//        tooltip.addRawPain(Text.of(nbt.toString()));
        int tempId = 0;
        if ((nbt.contains(HHS) && nbt.getInt(HHS) != 0) || nbt.contains(HHEP)) {
            float percent;
            if (nbt.contains(HHCI)) percent = getUnsignedPercentByInitTimeAdvanced(world, stack) * nbt.getInt(HHS);
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
        tooltip.add(Text.translatable("item.hcs.hot_water_bottle.description.temp").formatted(Formatting.GRAY)
                .append(Text.translatable("item.hcs.hot_water_bottle.description.temp." + tempId).formatted(switch (tempId) {
                    case -5 -> Formatting.DARK_BLUE;
                    case -4 -> Formatting.BLUE;
                    case -3 -> Formatting.DARK_AQUA;
                    case -2, -1 -> Formatting.AQUA;
                    case 1, 2 -> Formatting.YELLOW;
                    case 3 -> Formatting.GOLD;
                    case 4 -> Formatting.RED;
                    case 5 -> Formatting.DARK_RED;
                    default -> Formatting.WHITE;
                })));
        tooltip.add(Text.translatable("item.hcs.hot_water_bottle.description1").formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.translatable("item.hcs.hot_water_bottle.description2").formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.of(String.valueOf(stack.getNbt())));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world instanceof ServerWorld && entity instanceof ServerPlayerEntity player) {
            StatusManager statusManager = ((StatAccessor) player).getStatusManager();
            NbtCompound nbt = stack.getOrCreateNbt();
            if (nbt.contains(HHS)) {
                //View TemperatureHelper;getFeelingTemp
                if (nbt.getInt(HHS) < 0) statusManager.setRecentHasColdWaterBagTicks(20);
                if (nbt.getInt(HHS) > 0) statusManager.setRecentHasHotWaterBagTicks(20);
            }
            if (nbt.contains(HHSM)) nbt.remove(HHSM);
        }
    }

    @Override
    public boolean isItemBarVisible(@NotNull ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        return nbt.contains(HHS) && nbt.getInt(HHS) != 0;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        if (stack == null || WorldHelper.currWorld == null) return super.getItemBarStep(stack);
        NbtCompound nbt = stack.getOrCreateNbt();
        return Math.round(13.0F * (nbt.contains(HHCI) ? getUnsignedPercentByInitTimeAdvanced(WorldHelper.currWorld, stack) : getExpPercent(WorldHelper.currWorld, stack, stack.getOrCreateNbt().contains(HHES))));
    }

    @Override
    public int getItemBarColor(@NotNull ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (nbt.contains(HHS)) return nbt.getInt(HHS) > 0 ? 0xff6000 : 0x0084ff;
        return super.getItemBarColor(stack);
    }


}
