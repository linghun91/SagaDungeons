package cn.i7mc.sagadungeons.event;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.gui.AbstractGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * 物品栏事件监听器
 * 处理物品栏相关的事件
 */
public class InventoryListener extends AbstractListener {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public InventoryListener(SagaDungeons plugin) {
        super(plugin);
    }

    /**
     * 处理物品栏点击事件
     * @param event 物品栏点击事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // 获取物品栏持有者
        InventoryHolder holder = event.getInventory().getHolder();
        
        // 检查是否为插件GUI
        if (holder instanceof AbstractGUI) {
            // 处理点击事件
            ((AbstractGUI) holder).handleClick(event);
        }
    }

    /**
     * 处理物品栏拖拽事件
     * @param event 物品栏拖拽事件
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // 获取物品栏持有者
        InventoryHolder holder = event.getInventory().getHolder();
        
        // 检查是否为插件GUI
        if (holder instanceof AbstractGUI) {
            // 取消拖拽
            event.setCancelled(true);
        }
    }

    /**
     * 处理物品栏关闭事件
     * @param event 物品栏关闭事件
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // 获取物品栏持有者
        InventoryHolder holder = event.getInventory().getHolder();
        
        // 检查是否为插件GUI
        if (holder instanceof AbstractGUI) {
            // 处理关闭事件
            // 暂时不需要特殊处理
        }
    }
}
