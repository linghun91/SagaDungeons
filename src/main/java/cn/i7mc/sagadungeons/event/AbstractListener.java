package cn.i7mc.sagadungeons.event;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.event.Listener;

/**
 * 监听器抽象基类
 * 所有事件监听器的基类
 */
public abstract class AbstractListener implements Listener {

    protected final SagaDungeons plugin;

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public AbstractListener(SagaDungeons plugin) {
        this.plugin = plugin;
    }
}
