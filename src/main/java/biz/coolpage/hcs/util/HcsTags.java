package biz.coolpage.hcs.util;

import biz.coolpage.hcs.Hcs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class HcsTags {
    // https://www.bilibili.com/video/BV1Vw411s7h4?t=204.3&p=11
    public static class Blocks {
        private static @NotNull TagKey<Block> tag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(Hcs.MOD_ID, name));
        }
    }
}
