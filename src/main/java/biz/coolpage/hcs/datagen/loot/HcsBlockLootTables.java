package biz.coolpage.hcs.datagen.loot;

import biz.coolpage.hcs.item.HcsBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class HcsBlockLootTables extends BlockLootSubProvider {
    public HcsBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    protected HcsBlockLootTables(Set<Item> pExplosionResistant, FeatureFlagSet pEnabledFeatures) {
        super(pExplosionResistant, pEnabledFeatures);
    }

    protected HcsBlockLootTables(Set<Item> pExplosionResistant, FeatureFlagSet pEnabledFeatures, Map<ResourceLocation, LootTable.Builder> pMap) {
        super(pExplosionResistant, pEnabledFeatures, pMap);
    }

    @Override
    protected void generate() {
        // todo for loop, auto add all blocks
        // use noLootTable() to exclude: https://www.bilibili.com/video/BV1Vw411s7h4?t=640.2&p=12
        this.dropSelf(HcsBlocks.ICEBOX.get());

        // generate custom loot table JSON????
    }

    // add specific loot table for silk touch enchantment: https://www.bilibili.com/video/BV1Vw411s7h4?t=756.7&p=12c

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return HcsBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get) // return iterable here(Collection), collection.iterator
                ::iterator;   // () -> stream.iterator(); stream operation like numpy
        /*
        * 这是一个非常深入且关键的问题。答案是：**`stream.iterator()` 返回的是一个“迭代器”（Iterator），而不是“集合”（Collection）。**

        但这里有一个 Java 语法上的“骚操作”，解释如下：

        ### 1. 核心矛盾
        *   **方法的返回值要求**：通常这个方法（比如 `getKnownBlocks`）要求的返回类型是 **`Iterable<Block>`**（注意：是 `Iterable`，不是 `Iterator`）。
        *   **`stream.iterator()` 的返回类型**：它是 **`Iterator<Block>`**。

        **`Iterable` 和 `Iterator` 是不一样的：**
        *   `Iterable`：是一个**容器接口**（比如 List, Set），它有一个 `iterator()` 方法。
        *   `Iterator`：是一个**扫描器**，它有 `next()` 和 `hasNext()` 方法。

        ### 2. 为什么代码能跑通？（函数式接口的魔法）
        代码中写的是 `map(...)::iterator`，这其实是一个**方法引用**，它被赋值给了一个 `Iterable` 类型的变量。

        在 Java 8+ 中，`Iterable` 是一个**函数式接口**，因为它只有一个抽象方法：`Iterator<T> iterator()`。

        当你写出如下代码时：
        ```java
        Iterable<Block> myBlocks = stream::iterator;
        ```
        编译器会进行如下转换：
        1.  编译器看到 `Iterable` 需要一个实现 `iterator()` 方法的对象。
        2.  你提供的 `stream::iterator` 正好是一个“能够提供 Iterator 的方法”。
        3.  于是编译器自动生成了一个匿名内部类，逻辑如下：
            ```java
            Iterable<Block> myBlocks = new Iterable<Block>() {
                @Override
                public Iterator<Block> iterator() {
                    return stream.iterator(); // 调用了 stream 的 iterator 方法
                }
            };
            ```

        ### 3. 总结
        *   **`stream.iterator()` 本身返回的是**：`Iterator`（迭代器）。
        *   **`map(...)::iterator` 整体代表的是**：一个 **`Iterable`（可迭代对象）**。

        **通俗的比喻：**
        *   `Iterator`（迭代器）像是一个**正在自动播放的磁带**，播完就没了，不能重头再来。
        *   `Iterable`（可迭代对象）像是一个**磁带盒**，你每次打开盒子（调用 `iterator()`），它都会给你提供一盘磁带供你播放。

        **注意风险：**
        由于 `Stream` 只能被消费一次，所以这个返回的 `Iterable` 实际上**只能被遍历一次**。如果你对这个返回值进行第二次 `for-each` 循环，程序会抛出异常（IllegalStateException: stream has already been operated upon or closed）。但在 Minecraft 的掉落表生成逻辑中，这个列表通常只会被读取一次，所以这样写是安全的且高效的。
                * */
    }

}
