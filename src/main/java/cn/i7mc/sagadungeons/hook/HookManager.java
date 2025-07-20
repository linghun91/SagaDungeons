package cn.i7mc.sagadungeons.hook;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.Bukkit;

/**
 * 外部插件集成管理器
 * 负责管理与其他插件的集成
 */
public class HookManager {

    private final SagaDungeons plugin;
    private MythicMobsHook mythicMobsHook;
    private PlaceholderAPIHook placeholderAPIHook;

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public HookManager(SagaDungeons plugin) {
        this.plugin = plugin;

        // 初始化各个集成
        setupMythicMobs();
        setupPlaceholderAPI();
    }


    /**
     * 设置MythicMobs集成
     */
    private void setupMythicMobs() {
        // 检查是否启用MythicMobs集成
        if (!plugin.getConfigManager().isMythicMobsEnabled()) {
            return;
        }

        // 检查MythicMobs插件是否存在
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null) {
            return;
        }

        // 初始化MythicMobs集成
        mythicMobsHook = new MythicMobsHook(plugin);
    }

    /**
     * 设置PlaceholderAPI集成
     */
    private void setupPlaceholderAPI() {
        // 检查是否启用PlaceholderAPI集成
        if (!plugin.getConfigManager().isPlaceholderAPIEnabled()) {
            return;
        }

        // 检查PlaceholderAPI插件是否存在
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }

        // 初始化PlaceholderAPI集成
        placeholderAPIHook = new PlaceholderAPIHook(plugin);
        placeholderAPIHook.register();
    }


    /**
     * 获取MythicMobs集成
     * @return MythicMobs集成
     */
    public MythicMobsHook getMythicMobsHook() {
        return mythicMobsHook;
    }

    /**
     * 获取PlaceholderAPI集成
     * @return PlaceholderAPI集成
     */
    public PlaceholderAPIHook getPlaceholderAPIHook() {
        return placeholderAPIHook;
    }


    /**
     * 检查MythicMobs是否可用
     * @return 是否可用
     */
    public boolean isMythicMobsAvailable() {
        return mythicMobsHook != null && mythicMobsHook.isAvailable();
    }

    /**
     * 检查PlaceholderAPI是否可用
     * @return 是否可用
     */
    public boolean isPlaceholderAPIAvailable() {
        return placeholderAPIHook != null;
    }
}
