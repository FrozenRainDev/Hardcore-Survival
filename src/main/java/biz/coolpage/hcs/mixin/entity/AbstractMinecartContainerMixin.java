package biz.coolpage.hcs.mixin.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecartContainer.class)
public abstract class AbstractMinecartContainerMixin {

    // Intercept when a player opens the chest minecart
    @Inject(
            method = "createMenu(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/inventory/AbstractContainerMenu;",
            at = @At("HEAD")
    )
    private void hcsurvival$preCreateMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer, CallbackInfoReturnable<AbstractContainerMenu> cir) {
        this.hcsurvival$unpackAndFilterLoot(pPlayer);
    }

    // Intercept when the chest minecart is destroyed (e.g., hit by player)
    @Inject(
            method = "destroy(Lnet/minecraft/world/damagesource/DamageSource;)V",
            at = @At("HEAD")
    )
    private void hcsurvival$preDestroy(DamageSource pSource, CallbackInfo ci) {
        this.hcsurvival$unpackAndFilterLoot(null);
    }

    // Intercept when the chest minecart is removed (e.g., broken by environments)
    @Inject(
            method = "remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V",
            at = @At("HEAD")
    )
    private void hcsurvival$preRemove(Entity.RemovalReason pReason, CallbackInfo ci) {
        this.hcsurvival$unpackAndFilterLoot(null);
    }

    @Unique
    private void hcsurvival$unpackAndFilterLoot(Player player) {
        AbstractMinecartContainer minecart = (AbstractMinecartContainer) (Object) this;

        // Manually trigger the default interface method to unpack loot safely before original logic runs.
        // This is standard Java code inside the method body, so Mixin AP will not throw compilation errors.
        ((ContainerEntity) minecart).unpackChestVehicleLootTable(player);

        // Iterate through all slots and remove the specific banned items requested
        for (int i = 0; i < minecart.getContainerSize(); i++) {
            ItemStack stack = minecart.getItem(i);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();

                if (item == Items.IRON_PICKAXE ||
                        item == Items.IRON_INGOT ||
                        item == Items.GOLD_INGOT ||
                        item == Items.REDSTONE ||
                        item == Items.LAPIS_LAZULI ||
                        item == Items.DIAMOND ||
                        item == Items.COAL) {

                    // Clear the matching item stack from the slot
                    minecart.setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }
}