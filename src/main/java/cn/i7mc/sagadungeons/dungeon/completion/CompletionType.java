package cn.i7mc.sagadungeons.dungeon.completion;

/**
 * 通关条件类型枚举
 * 定义所有可用的通关条件类型
 */
public enum CompletionType {
    /**
     * 全部击杀
     */
    KILL_ALL,
    
    /**
     * 到达区域
     */
    REACH_AREA,
    
    /**
     * 击杀特定怪物
     */
    KILL_SPECIFIC,
    
    /**
     * 击杀数量
     */
    KILL_COUNT
}
