package cn.i7mc.sagadungeons;

import cn.i7mc.sagadungeons.command.CommandManager;
import cn.i7mc.sagadungeons.config.ConfigManager;
import cn.i7mc.sagadungeons.dungeon.DungeonManager;
import cn.i7mc.sagadungeons.event.ChatInputListener;
import cn.i7mc.sagadungeons.event.CommandBlockListener;
import cn.i7mc.sagadungeons.event.CompletionListener;
import cn.i7mc.sagadungeons.event.InventoryListener;
import cn.i7mc.sagadungeons.event.PlayerListener;
import cn.i7mc.sagadungeons.event.WorldListener;
import cn.i7mc.sagadungeons.gui.GUIManager;
import cn.i7mc.sagadungeons.hook.HookManager;
import cn.i7mc.sagadungeons.manager.MobSpawnerManager;
import cn.i7mc.sagadungeons.manager.WorldManager;
import cn.i7mc.sagadungeons.metrics.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * SagaDungeons插件主类
 * 副本系统主类，负责初始化和管理插件生命周期
 */
public class SagaDungeons extends JavaPlugin {

    private static SagaDungeons instance;
    private ConfigManager configManager;
    private CommandManager commandManager;
    private DungeonManager dungeonManager;
    private WorldManager worldManager;
    private HookManager hookManager;
    private GUIManager guiManager;
    private MobSpawnerManager mobSpawnerManager;
    private ChatInputListener chatInputListener;
    private cn.i7mc.sagadungeons.manager.DungeonSecurityManager dungeonSecurityManager;

    /**
     * 获取插件实例
     * @return 插件实例
     */
    public static SagaDungeons getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        // 保存插件实例
        instance = this;

        // 保存默认配置文件
        saveDefaultConfig();

        // 保存所有语言文件
        saveLanguageFiles();

        // 初始化配置管理器
        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        // 初始化世界管理器
        worldManager = new WorldManager(this);

        // 初始化副本管理器
        dungeonManager = new DungeonManager(this);

        // 初始化外部插件集成管理器
        hookManager = new HookManager(this);

        // 初始化GUI管理器
        guiManager = new GUIManager(this);

        // 初始化怪物生成管理器
        mobSpawnerManager = new MobSpawnerManager(this);

        // 初始化聊天输入监听器
        chatInputListener = new ChatInputListener(this);

        // 初始化副本安全管理器
        dungeonSecurityManager = new cn.i7mc.sagadungeons.manager.DungeonSecurityManager(this);

        // 初始化命令管理器
        commandManager = new CommandManager(this);
        commandManager.registerCommands();

        // 注册事件监听器
        registerListeners();

        // 清理残留副本世界
        if (configManager.isCleanupOnStartup()) {
            worldManager.cleanupRemnantWorlds();
        }

        // 初始化bStats统计
        initializeMetrics();
    }

    /**
     * 初始化bStats统计
     */
    private void initializeMetrics() {
        // SagaDungeons的插件ID是26069
        int pluginId = 26069;
        Metrics metrics = new Metrics(this, pluginId);

        // 添加自定义统计图表

        // 统计活跃副本数量
        metrics.addCustomChart(new Metrics.SingleLineChart("active_dungeons", () -> {
            return dungeonManager != null ? dungeonManager.getActiveDungeonCount() : 0;
        }));

        // 统计可用模板数量
        metrics.addCustomChart(new Metrics.SingleLineChart("available_templates", () -> {
            return configManager != null ? configManager.getTemplateManager().getTemplateCount() : 0;
        }));

        // 统计集成的外部插件
        metrics.addCustomChart(new Metrics.SimplePie("integrated_plugins", () -> {
            if (hookManager == null) return "无";

            StringBuilder plugins = new StringBuilder();
            if (hookManager.isVaultAvailable()) plugins.append("Vault,");
            if (hookManager.isPlayerPointsAvailable()) plugins.append("PlayerPoints,");
            if (hookManager.isMythicMobsAvailable()) plugins.append("MythicMobs,");
            if (hookManager.isPlaceholderAPIAvailable()) plugins.append("PlaceholderAPI,");

            if (plugins.length() == 0) return "无";

            // 移除最后的逗号
            return plugins.substring(0, plugins.length() - 1);
        }));

        // 统计使用的语言
        metrics.addCustomChart(new Metrics.SimplePie("language", () -> {
            return configManager != null ? configManager.getLanguage() : "zh";
        }));

        getLogger().info("bStats统计已启用");
    }

    /**
     * 保存所有语言文件
     */
    private void saveLanguageFiles() {
        // 保存中文消息文件
        if (!new File(getDataFolder(), "messages.yml").exists()) {
            saveResource("messages.yml", false);
        }

        // 保存英文消息文件
        if (!new File(getDataFolder(), "messages_en.yml").exists()) {
            saveResource("messages_en.yml", false);
        }

        // 这里可以添加更多语言文件
    }

    @Override
    public void onDisable() {

        // 保存数据
        if (dungeonManager != null) {
            dungeonManager.saveAllData();
        }

        // 卸载所有副本世界
        if (worldManager != null) {
            worldManager.unloadAllDungeonWorlds();
        }

        // 清除插件实例
        instance = null;
    }

    /**
     * 注册事件监听器
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new CompletionListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new cn.i7mc.sagadungeons.event.TeleportSecurityListener(this), this);
        getServer().getPluginManager().registerEvents(chatInputListener, this);
    }

    /**
     * 获取配置管理器
     * @return 配置管理器
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * 获取命令管理器
     * @return 命令管理器
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * 获取副本管理器
     * @return 副本管理器
     */
    public DungeonManager getDungeonManager() {
        return dungeonManager;
    }

    /**
     * 获取世界管理器
     * @return 世界管理器
     */
    public WorldManager getWorldManager() {
        return worldManager;
    }

    /**
     * 获取外部插件集成管理器
     * @return 外部插件集成管理器
     */
    public HookManager getHookManager() {
        return hookManager;
    }

    /**
     * 获取GUI管理器
     * @return GUI管理器
     */
    public GUIManager getGUIManager() {
        return guiManager;
    }

    /**
     * 获取怪物生成管理器
     * @return 怪物生成管理器
     */
    public MobSpawnerManager getMobSpawnerManager() {
        return mobSpawnerManager;
    }

    /**
     * 获取聊天输入监听器
     * @return 聊天输入监听器
     */
    public ChatInputListener getChatInputListener() {
        return chatInputListener;
    }

    /**
     * 获取副本安全管理器
     * @return 副本安全管理器
     */
    public cn.i7mc.sagadungeons.manager.DungeonSecurityManager getDungeonSecurityManager() {
        return dungeonSecurityManager;
    }
}
