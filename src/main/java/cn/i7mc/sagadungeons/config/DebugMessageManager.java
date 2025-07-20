package cn.i7mc.sagadungeons.config;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;

/**
 * 调试消息管理器
 * 负责管理插件的调试消息
 */
public class DebugMessageManager {

    private final SagaDungeons plugin;
    private FileConfiguration debugMessagesConfig;

    public DebugMessageManager(SagaDungeons plugin) {
        this.plugin = plugin;
        loadDebugMessages();
    }

    /**
     * 加载调试消息配置文件
     */
    public void loadDebugMessages() {
        File debugMessagesFile = new File(plugin.getDataFolder(), "debugmessage.yml");
        
        // 如果调试消息文件不存在，则保存默认文件
        if (!debugMessagesFile.exists()) {
            plugin.saveResource("debugmessage.yml", false);
        }
        
        // 加载调试消息文件
        debugMessagesConfig = YamlConfiguration.loadConfiguration(debugMessagesFile);
    }

    /**
     * 获取调试消息
     * @param path 消息路径
     * @return 调试消息
     */
    public String getMessage(String path) {
        String message = debugMessagesConfig.getString(path);
        if (message == null) {
            return "调试消息未找到: " + path;
        }
        return message;
    }

    /**
     * 获取带变量的调试消息
     * @param path 消息路径
     * @param placeholders 变量映射
     * @return 替换变量后的调试消息
     */
    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);
        
        // 替换变量
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        
        return message;
    }

    /**
     * 输出调试消息
     * @param path 消息路径
     */
    public void debug(String path) {
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("[Debug] " + getMessage(path));
        }
    }

    /**
     * 输出带变量的调试消息
     * @param path 消息路径
     * @param placeholders 变量映射
     */
    public void debug(String path, Map<String, String> placeholders) {
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("[Debug] " + getMessage(path, placeholders));
        }
    }
}
