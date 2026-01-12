package biz.coolpage.hcs.item;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.VerticallyAttachableBlockItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static biz.coolpage.hcs.util.CombustionHelper.*;

public class BurningCrudeTorchItem extends VerticallyAttachableBlockItem {
    public BurningCrudeTorchItem() {
        super(Reg.BURNING_CRUDE_TORCH_BLOCK, Reg.WALL_BURNING_CRUDE_TORCH_BLOCK, new Settings().maxCount(1), Direction.DOWN);
    }

    public static final String EXTINGUISH_NBT = "hcs_torch_extinguish";

    // Shared methods for burning crude torch & campfire item, which are used for burning length sync in inventories
    private static boolean isInvalidStack(ItemStack stack) {
        if (stack == null || WorldHelper.cannotGetServerWorld()) return true;
        if (!Configs.isEnabled(Configs.BURN)) return true;
        NbtCompound nbt = stack.getOrCreateNbt();
        return !nbt.contains(EXTINGUISH_NBT);
    }

    public static boolean shouldExtinguish(ItemStack stack) {
        if (isInvalidStack(stack)) return false;
        World world = WorldHelper.getServerWorld();
        if (world == null) return false;
        return stack.getOrCreateNbt().getLong(EXTINGUISH_NBT) < world.getTime();
    }

    private static float getDurationPercent(ItemStack stack) {
        if (isInvalidStack(stack)) return 0.0F;
        if (WorldHelper.cannotGetServerWorld()) {
            Reg.LOGGER.error("getDurPercent world==null since called on client side");
            return 0;
        }
        World world = WorldHelper.getServerWorld();
        assert world != null;
        long diff = stack.getOrCreateNbt().getLong(EXTINGUISH_NBT) - world.getTime();
        return MathHelper.clamp((float) diff / (float) getMaxBurningLength(stack), 0.0F, 1.0F);
    }

    public static void initDurData(World world, ItemStack stack) {
        if (world == null || stack == null) return;
        stack.getOrCreateNbt().putLong(EXTINGUISH_NBT, world.getTime() + getMaxBurningLength(stack));
    }

    public static long getMaxBurningLength(@NotNull ItemStack stack) {
        return isFuelableCampfire(stack.getItem()) ? MAX_CAMPFIRE_BURNING_LENGTH : MAX_TORCH_BURNING_LENGTH;
    }

    @Override
    public boolean isItemBarVisible(@NotNull ItemStack stack) {
        return !isInvalidStack(stack);
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(getDurationPercent(stack) * 13);
    }

    @Override
    public int getItemBarColor(@NotNull ItemStack stack) {
        float f = Math.max(0.0f, getDurationPercent(stack));
        return MathHelper.hsvToRgb(f / 3.0f, 1.0f, 1.0f);
    }
}
