package cn.i7mc.sagadungeons.dungeon.condition;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.entity.Player;

/**
 * 等级条件
 * 检查玩家是否达到指定等级
 */
public class LevelRequirement implements DungeonRequirement {

    private final int level;
    private final SagaDungeons plugin;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param level 等级要求
     */
    public LevelRequirement(SagaDungeons plugin, int level) {
        this.plugin = plugin;
        this.level = level;
    }

    /**
     * 获取等级要求
     * @return 等级要求
     */
    public int getLevel() {
        return level;
    }

    @Override
    public boolean check(Player player) {
        // 如果等级要求为0或负数，直接返回true
        if (level <= 0) {
            return true;
        }
        
        // 检查玩家等级是否达到要求
        return player.getLevel() >= level;
    }

    @Override
    public String getFailMessage(Player player) {
        return plugin.getConfigManager().getMessageManager().getMessage("dungeon.requirement.level.fail", 
                plugin.getConfigManager().getMessageManager().createPlaceholders("level", String.valueOf(level)));
    }

    @Override
    public boolean take(Player player) {
        // 等级条件不需要扣除任何东西，只需要检查
        return check(player);
    }

    @Override
    public RequirementType getType() {
        return RequirementType.LEVEL;
    }
}
