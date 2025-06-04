package cn.i7mc.sagadungeons.event;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.event.AbstractListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * 传送安全监听器
 * 负责监听玩家传送事件，阻止非法传送进入副本
 */
public class TeleportSecurityListener extends AbstractListener {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public TeleportSecurityListener(SagaDungeons plugin) {
        super(plugin);
    }

    /**
     * 处理玩家传送事件
     * @param event 玩家传送事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        
        // 检查目标位置是否有效
        if (to == null || to.getWorld() == null) {
            return;
        }

        String targetWorldName = to.getWorld().getName();
        
        // 检查是否为副本世界
        if (!plugin.getWorldManager().isDungeonWorld(targetWorldName)) {
            return; // 不是副本世界，不需要检查
        }

        // 获取传送原因
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        
        // 检查是否为插件传送（我们项目的合法传送）
        if (cause == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            return; // 插件传送，允许通过
        }

        // 检查是否为合法传送
        if (!plugin.getDungeonSecurityManager().isLegalDungeonTeleport(player, targetWorldName)) {
            // 阻止非法传送
            event.setCancelled(true);
            
            // 处理非法传送尝试
            plugin.getDungeonSecurityManager().handleIllegalTeleport(player, targetWorldName);
            
            return;
        }

        // 如果是合法传送，记录调试信息
        if (plugin.getConfigManager().isDebugEnabled()) {
            cn.i7mc.sagadungeons.util.DebugUtil.debug("security.teleport.allowed-debug",
                    cn.i7mc.sagadungeons.util.MessageUtil.createPlaceholders(
                            "player", player.getName(),
                            "world", targetWorldName,
                            "cause", cause.name(),
                            "hasAccess", String.valueOf(plugin.getDungeonSecurityManager().hasLegalAccess(player))
                    ));
        }
    }

    /**
     * 处理玩家世界变更事件
     * 监听玩家进入副本世界，检查是否为合法进入
     * @param event 玩家世界变更事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String currentWorldName = player.getWorld().getName();

        // 检查是否进入副本世界
        if (!plugin.getWorldManager().isDungeonWorld(currentWorldName)) {
            return; // 不是副本世界，不需要检查
        }

        // 检查是否为合法进入
        if (!plugin.getDungeonSecurityManager().isLegalDungeonTeleport(player, currentWorldName)) {
            // 处理非法进入副本
            plugin.getDungeonSecurityManager().handleIllegalDungeonEntry(player, currentWorldName);
        }
    }
}
