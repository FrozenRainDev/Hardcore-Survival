package biz.coolpage.hcs.util;

import biz.coolpage.hcs.Hcs;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class HcsFactory {
    @Contract("_ -> new")
    public static @NotNull ResourceLocation createResourceLocation(String name) {
        return createResourceLocation(Hcs.MOD_ID, name);
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation createResourceLocationWithPrefix(@NotNull String path) {
        String namespace = "minecraft";
        if (path.contains(":")) {
            String[] split = path.split(":");
            namespace = split[0];
            path = split[1];
        }
        return createResourceLocation(namespace, path);
    }

    public static @NotNull ResourceLocation createResourceLocation(String namespace, String path){
       return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
