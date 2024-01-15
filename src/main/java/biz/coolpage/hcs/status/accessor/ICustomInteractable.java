package biz.coolpage.hcs.status.accessor;

import net.minecraft.entity.player.PlayerEntity;

public interface ICustomInteractable {
     // Implements hcs block entities that need "onUse" logic operation
     boolean onInteract(PlayerEntity player);
}
