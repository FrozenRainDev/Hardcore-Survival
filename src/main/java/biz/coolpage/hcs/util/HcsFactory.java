package biz.coolpage.hcs.util;

import biz.coolpage.hcs.Hcs;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class HcsFactory {
    @Contract("_ -> new")
    public static @NotNull ResourceLocation createResourceLocation(String name) {
        return ResourceLocation.fromNamespaceAndPath(Hcs.MOD_ID, name);
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation createDefaultResourceLocation(@NotNull String path) {
        if (path.contains(":")) {
            // when the path has prefix "minecraft:", remove it
            path = path.split(":")[1]; // todo shit code here to optimize
        }
//        Hcs.info("withDefaultNamespace" + ResourceLocation.withDefaultNamespace(path));
        return ResourceLocation.withDefaultNamespace(path);
    }
}
