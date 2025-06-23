package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.config.GUILanguageManager;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GUI抽象基类
 * 所有GUI界面的基类
 */
public abstract class AbstractGUI implements InventoryHolder {

    protected final SagaDungeons plugin;
    protected final Player player;
    protected final GUILanguageManager guiLang;
    protected Inventory inventory;
    protected String title;
    protected int size;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     * @param title 标题
     * @param size 大小
     */
    public AbstractGUI(SagaDungeons plugin, Player player, String title, int size) {
        this.plugin = plugin;
        this.player = player;
        this.guiLang = plugin.getConfigManager().getGUILanguageManager();
        this.title = MessageUtil.colorize(title);
        this.size = size;
    }

    /**
     * 初始化界面
     */
    public abstract void init();

    /**
     * 处理点击事件
     * @param event 点击事件
     */
    public abstract void handleClick(InventoryClickEvent event);

    /**
     * 打开界面
     */
    public void open() {
        // 创建物品栏
        inventory = Bukkit.createInventory(this, size, title);
        
        // 初始化界面
        init();
        
        // 打开界面
        player.openInventory(inventory);
    }

    /**
     * 关闭界面
     */
    public void close() {
        player.closeInventory();
    }

    /**
     * 获取物品栏
     * @return 物品栏
     */
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * 获取玩家
     * @return 玩家
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * 获取标题
     * @return 标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取大小
     * @return 大小
     */
    public int getSize() {
        return size;
    }

    /**
     * 获取GUI文本
     * @param path 文本路径
     * @return 格式化后的文本
     */
    protected String getGUIText(String path) {
        return guiLang.getGUIText(path);
    }

    /**
     * 获取GUI文本（带占位符替换）
     * @param path 文本路径
     * @param placeholders 占位符映射
     * @return 格式化后的文本
     */
    protected String getGUIText(String path, Map<String, String> placeholders) {
        return guiLang.getGUIText(path, placeholders);
    }

    /**
     * 获取GUI文本列表
     * @param path 文本路径
     * @return 格式化后的文本列表
     */
    protected List<String> getGUITextList(String path) {
        return guiLang.getGUITextList(path);
    }

    /**
     * 获取GUI文本列表（带占位符替换）
     * @param path 文本路径
     * @param placeholders 占位符映射
     * @return 格式化后的文本列表
     */
    protected List<String> getGUITextList(String path, Map<String, String> placeholders) {
        return guiLang.getGUITextList(path, placeholders);
    }

    /**
     * 创建单个占位符的映射
     * @param key 占位符键
     * @param value 占位符值
     * @return 占位符映射
     */
    protected Map<String, String> createPlaceholder(String key, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(key, value);
        return placeholders;
    }

    /**
     * 创建标题的静态方法
     * @param plugin 插件实例
     * @param titleKey 标题键
     * @return 格式化后的标题
     */
    protected static String createGUITitle(SagaDungeons plugin, String titleKey) {
        return plugin.getConfigManager().getGUILanguageManager().getGUIText(titleKey);
    }

    /**
     * 创建带占位符的标题的静态方法
     * @param plugin 插件实例
     * @param titleKey 标题键
     * @param placeholders 占位符映射
     * @return 格式化后的标题
     */
    protected static String createGUITitle(SagaDungeons plugin, String titleKey, Map<String, String> placeholders) {
        return plugin.getConfigManager().getGUILanguageManager().getGUIText(titleKey, placeholders);
    }
}
