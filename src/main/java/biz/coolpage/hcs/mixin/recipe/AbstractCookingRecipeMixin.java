package biz.coolpage.hcs.mixin.recipe;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.item.HotWaterBottleItem;
import biz.coolpage.hcs.util.RotHelper;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractCookingRecipe.class)
public class AbstractCookingRecipeMixin {
    @Final
    @Mutable
    @Shadow
    protected ItemStack output;

    @SuppressWarnings("SameReturnValue")
    @Shadow
    public DefaultedList<Ingredient> getIngredients() {
        return null;
    }

    @Unique
    private static World theWorld = null;
    @Unique
    ItemStack stackIn = null;

    public AbstractCookingRecipeMixin(ItemStack output) {
        this.output = output;
    }

    @Inject(method = "matches", at = @At("HEAD"))
    private void matches(@NotNull Inventory inventory, World world, CallbackInfoReturnable<Boolean> cir) {
        theWorld = world;
        stackIn = inventory.getStack(0);
    }

    @Inject(method = "getOutput", at = @At("HEAD"), cancellable = true)
    private void getOutPut(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stackOut = this.output.copy();
        if (stackOut.isOf(Items.DRIED_KELP)) stackOut = Reg.COOKED_KELP.getDefaultStack();
        if (RotHelper.canRot(stackOut.getItem()) && theWorld != null && stackIn != null) {
            RotHelper.setFresh(theWorld, stackOut, RotHelper.getFreshCooked(RotHelper.getFresh(theWorld, stackIn)));
            cir.setReturnValue(stackOut);
        }
        if (stackOut.isOf(Reg.HOT_WATER_BOTTLE)) {
            int stat = 1;
            NbtCompound nbt = stackOut.getOrCreateNbt();
            HotWaterBottleItem.createExp(WorldHelper.currWorld, stackOut, true);
            if (nbt.contains(HotWaterBottleItem.HHSM) && nbt.getBoolean(HotWaterBottleItem.HHSM))
                stat = -1;
            HotWaterBottleItem.setStatus(stackOut, stat);
            cir.setReturnValue(stackOut);
        }
    }

    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    private void craft(CallbackInfoReturnable<ItemStack> cir) {
        getOutPut(cir);
    }

    @Inject(method = "getExperience", at = @At("RETURN"), cancellable = true)
    public void getExperience(@NotNull CallbackInfoReturnable<Float> cir) {
        if (cir.getReturnValue() >= 0.6F) cir.setReturnValue(cir.getReturnValue() * 2.0F);//Ores
        else cir.setReturnValue(Math.max(0.0F, cir.getReturnValue() - 0.15F));
    }

}
