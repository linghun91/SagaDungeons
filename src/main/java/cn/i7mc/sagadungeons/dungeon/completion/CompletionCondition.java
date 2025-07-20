package cn.i7mc.sagadungeons.dungeon.completion;

import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import org.bukkit.entity.Player;

/**
 * 通关条件接口
 * 所有通关条件的基础接口
 */
public interface CompletionCondition {
    
    /**
     * 检查条件是否满足
     * @param instance 副本实例
     * @return 是否满足
     */
    boolean check(DungeonInstance instance);
    
    /**
     * 获取条件描述
     * @return 条件描述
     */
    String getDescription();
    
    /**
     * 获取条件类型
     * @return 条件类型
     */
    CompletionType getType();
    
    /**
     * 重置条件
     */
    void reset();
    
    /**
     * 获取进度
     * @return 进度 (0.0 - 1.0)
     */
    double getProgress();
    
    /**
     * 获取进度描述
     * @return 进度描述
     */
    String getProgressDescription();
    
    /**
     * 处理玩家事件
     * @param player 玩家
     * @param event 事件类型
     * @param data 事件数据
     */
    void handleEvent(Player player, String event, Object data);
}
