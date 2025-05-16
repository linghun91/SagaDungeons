package cn.i7mc.sagadungeons.config;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息管理器
 * 负责管理插件的所有消息
 */
public class MessageManager {

    private final SagaDungeons plugin;
    private FileConfiguration messagesConfig;
    private String prefix;

    public MessageManager(SagaDungeons plugin) {
        this.plugin = plugin;
    }

    /**
     * 加载消息配置文件
     */
    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        // 如果消息文件不存在，则保存默认文件
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        // 加载消息文件
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
        // 获取消息前缀
        prefix = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("prefix", "&6[&bSagaDungeons&6] "));
    }

    /**
     * 获取消息
     * @param path 消息路径
     * @return 格式化后的消息
     */
    public String getMessage(String path) {
        String message = messagesConfig.getString(path);
        if (message == null) {
            return "§c消息未找到: " + path;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * 获取带前缀的消息
     * @param path 消息路径
     * @return 带前缀的格式化消息
     */
    public String getPrefixedMessage(String path) {
        return prefix + getMessage(path);
    }

    /**
     * 获取带变量的消息
     * @param path 消息路径
     * @param placeholders 变量映射
     * @return 替换变量后的消息
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
     * 获取带前缀和变量的消息
     * @param path 消息路径
     * @param placeholders 变量映射
     * @return 带前缀并替换变量后的消息
     */
    public String getPrefixedMessage(String path, Map<String, String> placeholders) {
        return prefix + getMessage(path, placeholders);
    }

    /**
     * 向玩家发送消息
     * @param player 玩家
     * @param path 消息路径
     */
    public void sendMessage(Player player, String path) {
        player.sendMessage(getPrefixedMessage(path));
    }

    /**
     * 向玩家发送带变量的消息
     * @param player 玩家
     * @param path 消息路径
     * @param placeholders 变量映射
     */
    public void sendMessage(Player player, String path, Map<String, String> placeholders) {
        player.sendMessage(getPrefixedMessage(path, placeholders));
    }

    /**
     * 创建变量映射
     * @param key 变量名
     * @param value 变量值
     * @return 变量映射
     */
    public Map<String, String> createPlaceholders(String key, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(key, value);
        return placeholders;
    }

    /**
     * 创建变量映射
     * @param key1 变量名1
     * @param value1 变量值1
     * @param key2 变量名2
     * @param value2 变量值2
     * @return 变量映射
     */
    public Map<String, String> createPlaceholders(String key1, String value1, String key2, String value2) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(key1, value1);
        placeholders.put(key2, value2);
        return placeholders;
    }

    /**
     * 创建变量映射
     * @param key1 变量名1
     * @param value1 变量值1
     * @param key2 变量名2
     * @param value2 变量值2
     * @param key3 变量名3
     * @param value3 变量值3
     * @return 变量映射
     */
    public Map<String, String> createPlaceholders(String key1, String value1, String key2, String value2, String key3, String value3) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(key1, value1);
        placeholders.put(key2, value2);
        placeholders.put(key3, value3);
        return placeholders;
    }
}
