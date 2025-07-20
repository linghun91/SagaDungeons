package cn.i7mc.sagadungeons.dungeon.condition;

import org.bukkit.entity.Player;

/**
 * 副本创建条件接口
 * 所有副本创建条件的基础接口
 */
public interface DungeonRequirement {
    
    /**
     * 检查玩家是否满足条件
     * @param player 玩家
     * @return 是否满足条件
     */
    boolean check(Player player);
    
    /**
     * 获取条件不满足时的提示消息
     * @param player 玩家
     * @return 提示消息
     */
    String getFailMessage(Player player);
    
    /**
     * 扣除条件所需的资源
     * @param player 玩家
     * @return 是否成功扣除
     */
    boolean take(Player player);
    
    /**
     * 获取条件类型
     * @return 条件类型
     */
    RequirementType getType();
}
