package cn.i7mc.sagadungeons.config;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * 配置管理器
 * 负责管理插件的所有配置文件
 */
public class ConfigManager {

    private final SagaDungeons plugin;
    private FileConfiguration config;
    private MessageManager messageManager;
    private TemplateManager templateManager;
    private boolean debug;
    private String language;

    public ConfigManager(SagaDungeons plugin) {
        this.plugin = plugin;
    }

    /**
     * 加载所有配置文件
     */
    public void loadConfigs() {
        // 加载主配置文件
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        debug = config.getBoolean("debug", false);
        language = config.getString("lang", "zh");

        // 加载消息配置文件
        messageManager = new MessageManager(plugin);
        messageManager.loadMessages();

        // 加载模板配置文件
        templateManager = new TemplateManager(plugin);
        templateManager.loadTemplates();
    }

    /**
     * 重载所有配置文件
     */
    public void reloadConfigs() {
        // 重载主配置文件
        plugin.reloadConfig();
        config = plugin.getConfig();
        debug = config.getBoolean("debug", false);
        language = config.getString("lang", "zh");

        // 重载消息配置文件
        messageManager.loadMessages();

        // 重载模板配置文件
        templateManager.loadTemplates();
    }

    /**
     * 重载所有配置文件（别名）
     */
    public void reload() {
        reloadConfigs();
    }

    /**
     * 获取主配置文件
     * @return 主配置文件
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * 获取消息管理器
     * @return 消息管理器
     */
    public MessageManager getMessageManager() {
        return messageManager;
    }

    /**
     * 获取模板管理器
     * @return 模板管理器
     */
    public TemplateManager getTemplateManager() {
        return templateManager;
    }

    /**
     * 检查是否启用调试模式
     * @return 是否启用调试模式
     */
    public boolean isDebugEnabled() {
        return debug;
    }

    /**
     * 获取当前语言设置
     * @return 当前语言代码
     */
    public String getLanguage() {
        return language;
    }

    /**
     * 获取副本世界前缀
     * @return 副本世界前缀
     */
    public String getWorldPrefix() {
        return config.getString("dungeon.world-prefix", "sd_");
    }

    /**
     * 获取默认副本超时时间(秒)
     * @return 默认副本超时时间
     */
    public int getDefaultTimeout() {
        return config.getInt("dungeon.default-timeout", 3600);
    }

    /**
     * 获取副本创建冷却时间(秒)
     * @return 副本创建冷却时间
     */
    public int getCreationCooldown() {
        return config.getInt("dungeon.creation-cooldown", 300);
    }

    /**
     * 获取通关后延迟删除时间(秒)
     * @return 通关后延迟删除时间
     */
    public int getCompletionDeleteDelay() {
        return config.getInt("dungeon.completion-delete-delay", 10);
    }

    /**
     * 检查是否在服务器启动时清理残留副本
     * @return 是否清理残留副本
     */
    public boolean isCleanupOnStartup() {
        return config.getBoolean("dungeon.cleanup-on-startup", true);
    }

    /**
     * 检查是否启用Vault经济
     * @return 是否启用Vault经济
     */
    public boolean isVaultEnabled() {
        return config.getBoolean("economy.use-vault", true);
    }

    /**
     * 检查是否启用PlayerPoints点券
     * @return 是否启用PlayerPoints点券
     */
    public boolean isPlayerPointsEnabled() {
        return config.getBoolean("economy.use-playerpoints", true);
    }

    /**
     * 检查是否启用MythicMobs集成
     * @return 是否启用MythicMobs集成
     */
    public boolean isMythicMobsEnabled() {
        return config.getBoolean("integration.mythicmobs", true);
    }

    /**
     * 检查是否启用PlaceholderAPI集成
     * @return 是否启用PlaceholderAPI集成
     */
    public boolean isPlaceholderAPIEnabled() {
        return config.getBoolean("integration.placeholderapi", true);
    }
}
