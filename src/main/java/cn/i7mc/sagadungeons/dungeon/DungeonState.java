package cn.i7mc.sagadungeons.dungeon;

/**
 * 副本状态枚举
 * 表示副本的当前状态
 */
public enum DungeonState {
    /**
     * 正在创建
     */
    CREATING,
    
    /**
     * 正在运行
     */
    RUNNING,
    
    /**
     * 已完成
     */
    COMPLETED,
    
    /**
     * 已超时
     */
    TIMEOUT,
    
    /**
     * 正在删除
     */
    DELETING
}
