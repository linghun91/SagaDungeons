package cn.i7mc.sagadungeons.util;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息工具类
 * 提供消息处理相关的工具方法
 */
public class MessageUtil {

    /**
     * 获取插件实例
     * @return 插件实例
     */
    private static SagaDungeons getPlugin() {
        return SagaDungeons.getInstance();
    }

    /**
     * 向发送者发送消息
     * @param sender 发送者
     * @param path 消息路径
     */
    public static void sendMessage(CommandSender sender, String path) {
        SagaDungeons plugin = getPlugin();
        if (plugin == null || plugin.getConfigManager() == null || plugin.getConfigManager().getMessageManager() == null) {
            sender.sendMessage("§c[SagaDungeons] 消息系统未初始化: " + path);
            return;
        }
        String message = plugin.getConfigManager().getMessageManager().getPrefixedMessage(path);
        sender.sendMessage(message);
    }

    /**
     * 向发送者发送带变量的消息
     * @param sender 发送者
     * @param path 消息路径
     * @param placeholders 变量映射
     */
    public static void sendMessage(CommandSender sender, String path, Map<String, String> placeholders) {
        SagaDungeons plugin = getPlugin();
        if (plugin == null || plugin.getConfigManager() == null || plugin.getConfigManager().getMessageManager() == null) {
            sender.sendMessage("§c[SagaDungeons] 消息系统未初始化: " + path);
            return;
        }
        String message = plugin.getConfigManager().getMessageManager().getPrefixedMessage(path, placeholders);
        sender.sendMessage(message);
    }

    /**
     * 向玩家发送消息
     * @param player 玩家
     * @param path 消息路径
     */
    public static void sendMessage(Player player, String path) {
        SagaDungeons plugin = getPlugin();
        if (plugin == null || plugin.getConfigManager() == null || plugin.getConfigManager().getMessageManager() == null) {
            player.sendMessage("§c[SagaDungeons] 消息系统未初始化: " + path);
            return;
        }
        plugin.getConfigManager().getMessageManager().sendMessage(player, path);
    }

    /**
     * 向玩家发送带变量的消息
     * @param player 玩家
     * @param path 消息路径
     * @param placeholders 变量映射
     */
    public static void sendMessage(Player player, String path, Map<String, String> placeholders) {
        SagaDungeons plugin = getPlugin();
        if (plugin == null || plugin.getConfigManager() == null || plugin.getConfigManager().getMessageManager() == null) {
            player.sendMessage("§c[SagaDungeons] 消息系统未初始化: " + path);
            return;
        }
        plugin.getConfigManager().getMessageManager().sendMessage(player, path, placeholders);
    }

    /**
     * 创建变量映射
     * @param key 变量名
     * @param value 变量值
     * @return 变量映射
     */
    public static Map<String, String> createPlaceholders(String key, String value) {
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
    public static Map<String, String> createPlaceholders(String key1, String value1, String key2, String value2) {
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
    public static Map<String, String> createPlaceholders(String key1, String value1, String key2, String value2, String key3, String value3) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(key1, value1);
        placeholders.put(key2, value2);
        placeholders.put(key3, value3);
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
     * @param key4 变量名4
     * @param value4 变量值4
     * @return 变量映射
     */
    public static Map<String, String> createPlaceholders(String key1, String value1, String key2, String value2,
                                                        String key3, String value3, String key4, String value4) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(key1, value1);
        placeholders.put(key2, value2);
        placeholders.put(key3, value3);
        placeholders.put(key4, value4);
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
     * @param key4 变量名4
     * @param value4 变量值4
     * @param key5 变量名5
     * @param value5 变量值5
     * @param key6 变量名6
     * @param value6 变量值6
     * @return 变量映射
     */
    public static Map<String, String> createPlaceholders(String key1, String value1, String key2, String value2,
                                                        String key3, String value3, String key4, String value4,
                                                        String key5, String value5, String key6, String value6) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(key1, value1);
        placeholders.put(key2, value2);
        placeholders.put(key3, value3);
        placeholders.put(key4, value4);
        placeholders.put(key5, value5);
        placeholders.put(key6, value6);
        return placeholders;
    }

    /**
     * 格式化颜色代码
     * @param text 文本
     * @return 格式化后的文本
     */
    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * 格式化颜色代码
     * @param texts 文本列表
     * @return 格式化后的文本列表
     */
    public static java.util.List<String> colorize(java.util.List<String> texts) {
        java.util.List<String> result = new java.util.ArrayList<>();
        for (String text : texts) {
            result.add(colorize(text));
        }
        return result;
    }
}
