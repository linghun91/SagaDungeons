package cn.i7mc.sagadungeons.hook;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;

/**
 * Multiverse-Core插件事件监听器
 * 用于处理与Multiverse-Core插件的交互
 */
public class MultiverseWorldListener implements Listener {

    private final SagaDungeons plugin;
    private final String worldPrefix;

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public MultiverseWorldListener(SagaDungeons plugin) {
        this.plugin = plugin;
        this.worldPrefix = plugin.getConfigManager().getWorldPrefix();
        
        // 检查Multiverse-Core插件是否存在
        Plugin mvPlugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if (mvPlugin != null && mvPlugin.isEnabled()) {
            // 注册事件监听器
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("已注册Multiverse-Core事件监听器");
        }
    }

    /**
     * 处理服务器命令事件
     * 用于拦截Multiverse-Core的导入命令
     * @param event 服务器命令事件
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerCommand(ServerCommandEvent event) {
        String command = event.getCommand();
        
        // 检查是否为Multiverse-Core的导入命令
        if (command.startsWith("mv import ")) {
            // 获取世界名称
            String[] args = command.split(" ");
            if (args.length >= 3) {
                String worldName = args[2];
                
                // 检查是否为副本世界
                if (worldName.startsWith(worldPrefix)) {
                    // 取消命令执行
                    event.setCancelled(true);
                    plugin.getLogger().info("已阻止Multiverse-Core导入临时副本世界: " + worldName);
                }
            }
        }
    }
}
