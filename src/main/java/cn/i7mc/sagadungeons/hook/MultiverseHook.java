package cn.i7mc.sagadungeons.hook;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Multiverse-Core插件兼容钩子
 * 用于处理与Multiverse-Core插件的交互
 */
public class MultiverseHook {

    private final SagaDungeons plugin;
    private boolean multiverseEnabled = false;

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public MultiverseHook(SagaDungeons plugin) {
        this.plugin = plugin;
        initialize();
    }

    /**
     * 初始化Multiverse钩子
     */
    private void initialize() {
        // 检查Multiverse-Core插件是否存在
        Plugin mvPlugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if (mvPlugin != null && mvPlugin.isEnabled()) {
            multiverseEnabled = true;
            plugin.getLogger().info("成功挂钩Multiverse-Core插件");
        }
    }

    /**
     * 检查Multiverse-Core插件是否可用
     * @return 是否可用
     */
    public boolean isEnabled() {
        return multiverseEnabled;
    }
}
