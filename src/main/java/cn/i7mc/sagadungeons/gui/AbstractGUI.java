package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * GUI抽象基类
 * 所有GUI界面的基类
 */
public abstract class AbstractGUI implements InventoryHolder {

    protected final SagaDungeons plugin;
    protected final Player player;
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
}
