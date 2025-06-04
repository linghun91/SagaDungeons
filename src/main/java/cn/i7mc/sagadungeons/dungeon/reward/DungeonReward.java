package cn.i7mc.sagadungeons.dungeon.reward;

import org.bukkit.entity.Player;

/**
 * 副本奖励接口
 * 所有副本奖励的基础接口
 */
public interface DungeonReward {
    
    /**
     * 给予奖励
     * @param player 玩家
     * @return 是否成功
     */
    boolean give(Player player);
    
    /**
     * 获取奖励描述
     * @return 奖励描述
     */
    String getDescription();
    
    /**
     * 获取奖励类型
     * @return 奖励类型
     */
    RewardType getType();
}
