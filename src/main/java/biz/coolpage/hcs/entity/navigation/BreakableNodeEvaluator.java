package biz.coolpage.hcs.entity.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import biz.coolpage.hcs.util.DigRestrictHelper;

public class BreakableNodeEvaluator extends WalkNodeEvaluator {
    @Override
    public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z, Mob mob) {
        // 先获取原版的寻路判定类型
        BlockPathTypes type = super.getBlockPathType(level, x, y, z, mob);

        // 如果原版认为是死路 (BLOCKED) 或者是栅栏、树叶等障碍物
//        if (type == BlockPathTypes.BLOCKED || type == BlockPathTypes.FENCE || type == BlockPathTypes.LEAVES) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);

            // 检查僵尸当前手持的工具是否能够破坏该方块
            if (!state.isAir() && DigRestrictHelper.canBreak(mob.getMainHandItem().getItem(), state)) {
                // DANGER_OTHER 是原版的一种路径类型，代表“可以走，但默认有 8.0 的寻路惩罚”
                // 这意味着僵尸会优先走正常的绕行路线；但如果是完全封死的庇护所，它就会规划出一条穿墙路线。
                return BlockPathTypes.DANGER_OTHER;
            }
//        }
        return type;
    }
}