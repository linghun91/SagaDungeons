package cn.i7mc.sagadungeons.dungeon.trigger;

import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * 副本触发器接口
 * 用于定义触发器的基本行为
 */
public interface DungeonTrigger {
    
    /**
     * 获取触发器ID
     * @return 触发器ID
     */
    String getId();
    
    /**
     * 获取触发器类型
     * @return 触发器类型
     */
    String getType();
    
    /**
     * 检查触发条件
     * @param instance 副本实例
     * @param player 触发玩家
     * @return 是否满足触发条件
     */
    boolean checkCondition(DungeonInstance instance, Player player);
    
    /**
     * 执行触发器
     * @param instance 副本实例
     * @param player 触发玩家
     */
    void execute(DungeonInstance instance, Player player);
    
    /**
     * 获取触发器配置
     * @return 触发器配置
     */
    ConfigurationSection getConfig();
} 