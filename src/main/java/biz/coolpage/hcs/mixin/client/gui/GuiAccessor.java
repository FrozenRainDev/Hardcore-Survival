package biz.coolpage.hcs.mixin.client.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor mixin replacing the former {@code Gui f_279580_} and {@code Gui f_168666_} access transformer entries.
 */
@Mixin(Gui.class)
public interface GuiAccessor {
    /**
     * Getter for the private static final {@code ResourceLocation} field {@code GUI_ICONS_LOCATION}.
     *
     * @return the vanilla HUD icons texture
     */
    @Accessor("GUI_ICONS_LOCATION")
    static ResourceLocation getGuiIconsLocation() {
        throw new AssertionError();
    }

    /**
     * Getter for the private static final {@code ResourceLocation} field {@code POWDER_SNOW_OUTLINE_LOCATION}.
     *
     * @return the freezing powder snow vignette texture
     */
    @Accessor("POWDER_SNOW_OUTLINE_LOCATION")
    static ResourceLocation getPowderSnowOutlineLocation() {
        throw new AssertionError();
    }
}
