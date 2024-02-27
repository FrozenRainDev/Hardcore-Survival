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

import static biz.coolpage.hcs.entity.BurningCrudeTorchBlockEntity.MAX_BURNING_LENGTH;

public class BurningCrudeTorchItem extends VerticallyAttachableBlockItem {
    public BurningCrudeTorchItem() {
        super(Reg.BURNING_CRUDE_TORCH_BLOCK, Reg.WALL_BURNING_CRUDE_TORCH_BLOCK, new Item.Settings().maxCount(1), Direction.DOWN);
    }

    public static final String LIT_NBT = "hcs_torch_item_last_lit";

    private static boolean isInvalidStack(ItemStack stack) {
        if (stack == null || WorldHelper.currWorld == null) return true;
        NbtCompound nbt = stack.getOrCreateNbt();
        return !nbt.contains(LIT_NBT);
    }

    public static boolean shouldExtinguish(ItemStack stack) {
        if (isInvalidStack(stack)) return true;
        return WorldHelper.currWorld.getTime() - stack.getOrCreateNbt().getLong(LIT_NBT) > MAX_BURNING_LENGTH;
    }

    private static float getDurPercent(ItemStack stack) {
        if (isInvalidStack(stack)) return 0.0F;
        long numerator = WorldHelper.currWorld.getTime() - stack.getOrCreateNbt().getLong(LIT_NBT);
        numerator = MAX_BURNING_LENGTH - numerator;
        return MathHelper.clamp((float) numerator / (float) MAX_BURNING_LENGTH, 0.0F, 1.0F);
    }

    public static void initDurData(World world, ItemStack stack) {
        if (world == null || stack == null) return;
        stack.getOrCreateNbt().putLong(LIT_NBT, world.getTime());
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
