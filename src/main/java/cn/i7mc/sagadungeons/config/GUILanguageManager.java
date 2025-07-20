package cn.i7mc.sagadungeons.config;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.util.DebugUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

/**
 * GUI语言管理器
 * 负责管理GUI界面的多语言支持
 */
public class GUILanguageManager {

    private final SagaDungeons plugin;
    private YamlConfiguration guiLanguageConfig;
    private String language;

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public GUILanguageManager(SagaDungeons plugin) {
        this.plugin = plugin;
    }

    /**
     * 加载GUI语言配置文件
     */
    public void loadGUILanguage() {
        // 获取配置的语言
        language = plugin.getConfig().getString("lang", "zh");

        // 确定GUI语言文件名
        String guiLanguageFileName = "lang_gui.yml";
        if (!"zh".equals(language)) {
            guiLanguageFileName = "lang_gui_" + language + ".yml";
        }

        File guiLanguageFile = new File(plugin.getDataFolder(), guiLanguageFileName);

        // 如果GUI语言文件不存在，尝试回退到默认中文
        if (!guiLanguageFile.exists()) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("file", guiLanguageFileName);
            DebugUtil.debug("config.gui-language.lang-not-found", placeholders);
            language = "zh";
            guiLanguageFile = new File(plugin.getDataFolder(), "lang_gui.yml");

            // 如果默认中文文件也不存在，这是一个严重错误
            if (!guiLanguageFile.exists()) {
                DebugUtil.debug("config.gui-language.default-not-found");
                return;
            }
        }

        // 加载GUI语言文件
        guiLanguageConfig = YamlConfiguration.loadConfiguration(guiLanguageFile);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("language", language);
        DebugUtil.debug("config.gui-language.loaded", placeholders);
    }

    /**
     * 获取GUI文本
     * @param path 文本路径
     * @return 格式化后的文本
     */
    public String getGUIText(String path) {
        if (guiLanguageConfig == null) {
            return "§c文本未找到: " + path;
        }
        
        String text = guiLanguageConfig.getString(path);
        if (text == null) {
            return "§c文本未找到: " + path;
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * 获取GUI文本（带占位符替换）
     * @param path 文本路径
     * @param placeholders 占位符映射
     * @return 格式化后的文本
     */
    public String getGUIText(String path, Map<String, String> placeholders) {
        String text = getGUIText(path);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                text = text.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return text;
    }

    /**
     * 获取GUI文本列表
     * @param path 文本路径
     * @return 格式化后的文本列表
     */
    public List<String> getGUITextList(String path) {
        if (guiLanguageConfig == null) {
            return Arrays.asList("§c文本未找到: " + path);
        }

        List<String> textList = guiLanguageConfig.getStringList(path);
        if (textList == null || textList.isEmpty()) {
            return Arrays.asList("§c文本未找到: " + path);
        }

        List<String> coloredList = new ArrayList<>();
        for (String text : textList) {
            coloredList.add(ChatColor.translateAlternateColorCodes('&', text));
        }
        return coloredList;
    }

    /**
     * 获取GUI文本列表（带占位符替换）
     * @param path 文本路径
     * @param placeholders 占位符映射
     * @return 格式化后的文本列表
     */
    public List<String> getGUITextList(String path, Map<String, String> placeholders) {
        List<String> textList = getGUITextList(path);

        if (placeholders != null) {
            List<String> replacedList = new ArrayList<>();
            for (String text : textList) {
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    text = text.replace("{" + entry.getKey() + "}", entry.getValue());
                }
                replacedList.add(text);
            }
            return replacedList;
        }

        return textList;
    }

    /**
     * 获取当前语言
     * @return 当前语言代码
     */
    public String getLanguage() {
        return language;
    }

    /**
     * 检查GUI语言配置是否已加载
     * @return 是否已加载
     */
    public boolean isLoaded() {
        return guiLanguageConfig != null;
    }
}
