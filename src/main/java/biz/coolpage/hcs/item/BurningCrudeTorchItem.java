package biz.coolpage.hcs.item;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.item.Item;
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
        super(Reg.BURNING_CRUDE_TORCH_BLOCK, Reg.WALL_BURNING_CRUDE_TORCH_BLOCK, new Item.Settings().maxCount(1), Direction.DOWN);
    }

    public static final String EXTINGUISH_NBT = "hcs_torch_extinguish";

    // Shared methods for burning crude torch & campfire item, which are used for burning length sync in inventories
    private static boolean isInvalidStack(ItemStack stack) {
        if (stack == null || WorldHelper.currWorld == null) return true;
        NbtCompound nbt = stack.getOrCreateNbt();
        return !nbt.contains(EXTINGUISH_NBT);
    }

    public static boolean shouldExtinguish(ItemStack stack) {
        if (isInvalidStack(stack)) return true;
        return stack.getOrCreateNbt().getLong(EXTINGUISH_NBT) < WorldHelper.currWorld.getTime();
    }

    private static float getDurPercent(ItemStack stack) {
        if (isInvalidStack(stack)) return 0.0F;
        long diff = stack.getOrCreateNbt().getLong(EXTINGUISH_NBT) - WorldHelper.currWorld.getTime();
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
        return Math.round(getDurPercent(stack) * 13);
    }

    @Override
    public int getItemBarColor(@NotNull ItemStack stack) {
        float f = Math.max(0.0f, getDurPercent(stack));
        return MathHelper.hsvToRgb(f / 3.0f, 1.0f, 1.0f);
    }
}
