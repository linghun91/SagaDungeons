package cn.i7mc.sagadungeons.util;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.config.DebugMessageManager;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

/**
 * 调试工具类
 * 提供调试相关的工具方法
 */
public class DebugUtil {

    private static final SagaDungeons plugin = SagaDungeons.getInstance();

    /**
     * 输出调试消息
     * @param path 消息路径
     */
    public static void debug(String path) {
        if (plugin.getConfigManager().isDebugEnabled()) {
            DebugMessageManager debugMessageManager = new DebugMessageManager(plugin);
            String message = debugMessageManager.getMessage(path);
            plugin.getLogger().info("[Debug] " + message);
        }
    }

    /**
     * 输出带变量的调试消息
     * @param path 消息路径
     * @param placeholders 变量映射
     */
    public static void debug(String path, Map<String, String> placeholders) {
        if (plugin.getConfigManager().isDebugEnabled()) {
            DebugMessageManager debugMessageManager = new DebugMessageManager(plugin);
            String message = debugMessageManager.getMessage(path, placeholders);
            plugin.getLogger().info("[Debug] " + message);
        }
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
}
