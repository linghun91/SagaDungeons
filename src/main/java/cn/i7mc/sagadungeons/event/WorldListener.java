package cn.i7mc.sagadungeons.event;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 * 世界事件监听器
 * 处理世界相关的事件
 */
public class WorldListener extends AbstractListener {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public WorldListener(SagaDungeons plugin) {
        super(plugin);
    }

    /**
     * 处理世界初始化事件
     * @param event 世界初始化事件
     */
    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        World world = event.getWorld();
        
        // 检查是否为副本世界
        if (plugin.getWorldManager().isDungeonWorld(world.getName())) {
            // 设置世界属性
            world.setAutoSave(false);
        }
    }

    /**
     * 处理世界加载事件
     * @param event 世界加载事件
     */
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        
        // 检查是否为副本世界
        if (plugin.getWorldManager().isDungeonWorld(world.getName())) {
            // 设置世界属性
            world.setAutoSave(false);
            
            // 获取副本ID
            String dungeonId = plugin.getWorldManager().getDungeonIdFromWorldName(world.getName());
            if (dungeonId != null) {
                // 获取副本实例
                if (plugin.getDungeonManager().getDungeon(dungeonId) != null) {
                    // 设置副本世界
                    plugin.getDungeonManager().getDungeon(dungeonId).setWorld(world);
                }
            }
        }
    }

    /**
     * 处理世界卸载事件
     * @param event 世界卸载事件
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        World world = event.getWorld();
        
        // 检查是否为副本世界
        if (plugin.getWorldManager().isDungeonWorld(world.getName())) {
            // 获取副本ID
            String dungeonId = plugin.getWorldManager().getDungeonIdFromWorldName(world.getName());
            if (dungeonId != null) {
                // 获取副本实例
                if (plugin.getDungeonManager().getDungeon(dungeonId) != null) {
                    // 检查是否正在删除
                    if (plugin.getDungeonManager().getDungeon(dungeonId).getState() == cn.i7mc.sagadungeons.dungeon.DungeonState.DELETING) {
                        // 允许卸载
                        return;
                    }
                }
            }
            
            // 取消卸载
            event.setCancelled(true);
        }
    }

    /**
     * 处理区块卸载事件
     * @param event 区块卸载事件
     */
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        World world = event.getWorld();
        
        // 检查是否为副本世界
        if (plugin.getWorldManager().isDungeonWorld(world.getName())) {
            // 设置不保存
            event.setSaveChunk(false);
        }
    }
}
