package biz.coolpage.hcs.block;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class StrawBed extends BedBlock {

    public StrawBed(DyeColor pColor, BlockBehaviour.Properties pProperties) {
        super(pColor, pProperties);
    }

    // Inherit all vanilla BedBlock logic to respect the original code structure.
    // Spawning point modification and single-use logic are handled via Forge Events
    // to avoid heavily overriding complex Player sleep routines in the use() method.
}