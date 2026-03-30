package biz.coolpage.hcs.item;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static biz.coolpage.hcs.util.CombustionHelper.*;

public class BurningCrudeTorchItem extends StandingAndWallBlockItem {
    public BurningCrudeTorchItem() {
        super(Hcs.BURNING_CRUDE_TORCH_BLOCK.get(), Hcs.WALL_BURNING_CRUDE_TORCH_BLOCK.get(), new Properties().stacksTo(1), Direction.DOWN);
    }

    public static final String EXTINGUISH_NBT = "hcs_torch_extinguish";

    // Shared methods for burning crude torch & campfire item, which are used for burning length sync in inventories
    private static boolean isInvalidStack(ItemStack stack) {
        if (stack == null || WorldHelper.cannotGetServerWorld()) return true;
        if (!Configs.isEnabled(Configs.BURN)) return true;
        CompoundTag nbt = stack.getOrCreateTag();
        return !nbt.contains(EXTINGUISH_NBT);
    }

    public static boolean shouldExtinguish(ItemStack stack) {
        if (isInvalidStack(stack)) return false;
        Level world = WorldHelper.getServerWorld();
        if (world == null) return false;
        return stack.getOrCreateTag().getLong(EXTINGUISH_NBT) < world.getGameTime();
    }

    private static float getDurationPercent(ItemStack stack) {
        if (isInvalidStack(stack)) return 0.0F;
        if (WorldHelper.cannotGetServerWorld()) {
            Hcs.error("getDurPercent world==null since called on client side");
            return 0;
        }
        Level world = WorldHelper.getServerWorld();
        assert world != null;
        long diff = stack.getOrCreateTag().getLong(EXTINGUISH_NBT) - world.getGameTime();
        return Mth.clamp((float) diff / (float) getMaxBurningLength(stack), 0.0F, 1.0F);
    }

    public static void initDurData(Level world, ItemStack stack) {
        if (world == null || stack == null) return;
        stack.getOrCreateTag().putLong(EXTINGUISH_NBT, world.getGameTime() + getMaxBurningLength(stack));
    }

    public static long getMaxBurningLength(@NotNull ItemStack stack) {
        return isFuelableCampfire(stack.getItem()) ? MAX_CAMPFIRE_BURNING_LENGTH : MAX_TORCH_BURNING_LENGTH;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return !isInvalidStack(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(getDurationPercent(stack) * 13);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        float f = Math.max(0.0f, getDurationPercent(stack));
        return Mth.hsvToRgb(f / 3.0f, 1.0f, 1.0f);
    }
}