package biz.coolpage.hcs.mixin.recipe;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.item.HotWaterBottleItem;
import biz.coolpage.hcs.util.RotHelper;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractCookingRecipe.class)
public abstract class AbstractCookingRecipeMixin {
    @Final
    @Mutable
    @Shadow
    protected ItemStack result;

    @Shadow
    public abstract NonNullList<Ingredient> getIngredients();

    @Unique
    private static Level theLevel = null;
    @Unique
    ItemStack stackIn = null;

    @Inject(method = "matches", at = @At("HEAD"))
    private void matches(@NotNull Container container, Level level, CallbackInfoReturnable<Boolean> cir) {
        theLevel = level;
        stackIn = container.getItem(0);
    }

    // 1.20.1 中 getResultItem 需要 RegistryAccess
    @Inject(method = "getResultItem", at = @At("HEAD"), cancellable = true)
    private void getResultItemInject(RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stackOut = this.result.copy();
        modifyResult(stackOut, cir);
    }

    @Inject(method = "assemble", at = @At("HEAD"), cancellable = true)
    private void assembleInject(Container container, RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stackOut = this.result.copy();
        modifyResult(stackOut, cir);
    }

    @Unique
    private void modifyResult(ItemStack stackOut, CallbackInfoReturnable<ItemStack> cir) {
        if (stackOut.is(Items.DRIED_KELP)) stackOut = Hcs.COOKED_KELP.getDefaultInstance();
        if (RotHelper.canRot(stackOut.getItem()) && theLevel != null && stackIn != null) {
            RotHelper.setFresh(theLevel, stackOut, RotHelper.getFreshCooked(RotHelper.getFresh(theLevel, stackIn)));
            cir.setReturnValue(stackOut);
        }
        if (stackOut.is(Hcs.HOT_WATER_BOTTLE)) {
            int stat = 1;
            CompoundTag nbt = stackOut.getOrCreateTag();
            if (!WorldHelper.cannotGetServerWorld())
                HotWaterBottleItem.createExp(WorldHelper.getServerWorld(), stackOut, true);
            if (nbt.contains(HotWaterBottleItem.HHSM) && nbt.getBoolean(HotWaterBottleItem.HHSM))
                stat = -1;
            HotWaterBottleItem.setStatus(stackOut, stat);
            cir.setReturnValue(stackOut);
        }
    }

    @Inject(method = "getExperience", at = @At("RETURN"), cancellable = true)
    public void getExperience(@NotNull CallbackInfoReturnable<Float> cir) {
        if (cir.getReturnValue() >= 0.6F) cir.setReturnValue(cir.getReturnValue() * 2.0F);
        else cir.setReturnValue(Math.max(0.0F, cir.getReturnValue() - 0.15F));
    }
}