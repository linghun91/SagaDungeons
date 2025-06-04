package cn.i7mc.sagadungeons.manager;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * 副本安全管理器
 * 负责管理副本的安全进入控制
 */
public class DungeonSecurityManager {

    private final SagaDungeons plugin;

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public DungeonSecurityManager(SagaDungeons plugin) {
        this.plugin = plugin;
    }

    /**
     * 授予玩家合法副本进入权限
     * 当玩家通过合法方式（如sd create、sd join、邀请等）进入副本时调用
     * @param player 玩家
     */
    public void grantLegalAccess(Player player) {
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());
        playerData.setLegalDungeonAccess(true);
        
        // 如果启用调试模式，输出调试信息
        if (plugin.getConfigManager().isDebugEnabled()) {
            cn.i7mc.sagadungeons.util.DebugUtil.debug("security.access.granted",
                    cn.i7mc.sagadungeons.util.MessageUtil.createPlaceholders("player", player.getName()));
        }
    }

    /**
     * 撤销玩家合法副本进入权限
     * 当玩家离开副本时调用
     * @param player 玩家
     */
    public void revokeLegalAccess(Player player) {
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());
        playerData.setLegalDungeonAccess(false);
        
        // 如果启用调试模式，输出调试信息
        if (plugin.getConfigManager().isDebugEnabled()) {
            cn.i7mc.sagadungeons.util.DebugUtil.debug("security.access.revoked",
                    cn.i7mc.sagadungeons.util.MessageUtil.createPlaceholders("player", player.getName()));
        }
    }

    /**
     * 检查玩家是否拥有合法副本进入权限
     * @param player 玩家
     * @return 是否拥有合法权限
     */
    public boolean hasLegalAccess(Player player) {
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());
        return playerData.hasLegalDungeonAccess();
    }

    /**
     * 检查玩家是否拥有合法副本进入权限（通过UUID）
     * @param playerUUID 玩家UUID
     * @return 是否拥有合法权限
     */
    public boolean hasLegalAccess(UUID playerUUID) {
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(playerUUID);
        return playerData.hasLegalDungeonAccess();
    }

    /**
     * 检查传送是否为合法的副本进入
     * @param player 玩家
     * @param targetWorldName 目标世界名称
     * @return 是否为合法传送
     */
    public boolean isLegalDungeonTeleport(Player player, String targetWorldName) {
        // 检查目标世界是否为副本世界
        if (!plugin.getWorldManager().isDungeonWorld(targetWorldName)) {
            return true; // 不是副本世界，允许传送
        }

        // 检查玩家是否拥有合法进入权限
        if (hasLegalAccess(player)) {
            return true; // 拥有合法权限，允许传送
        }

        // 检查是否为管理员
        if (player.hasPermission("sagadungeons.admin")) {
            return true; // 管理员权限，允许传送
        }

        return false; // 非法传送
    }

    /**
     * 处理非法传送尝试
     * @param player 玩家
     * @param targetWorldName 目标世界名称
     */
    public void handleIllegalTeleport(Player player, String targetWorldName) {
        // 发送阻止消息
        plugin.getConfigManager().getMessageManager().sendMessage(player, "security.teleport.blocked",
                cn.i7mc.sagadungeons.util.MessageUtil.createPlaceholders("world", targetWorldName));

        // 如果启用调试模式，输出调试信息
        if (plugin.getConfigManager().isDebugEnabled()) {
            cn.i7mc.sagadungeons.util.DebugUtil.debug("security.teleport.blocked-debug",
                    cn.i7mc.sagadungeons.util.MessageUtil.createPlaceholders(
                            "player", player.getName(),
                            "world", targetWorldName,
                            "hasAccess", String.valueOf(hasLegalAccess(player)),
                            "isAdmin", String.valueOf(player.hasPermission("sagadungeons.admin"))
                    ));
        }
    }

    /**
     * 处理非法进入副本
     * 将玩家传送回主世界出生点并发送提示消息
     * @param player 玩家
     * @param dungeonWorldName 副本世界名称
     */
    public void handleIllegalDungeonEntry(Player player, String dungeonWorldName) {
        // 获取主世界
        World mainWorld = Bukkit.getWorlds().get(0);
        if (mainWorld == null) {
            return;
        }

        // 获取主世界出生点
        Location spawnLocation = mainWorld.getSpawnLocation();

        // 传送玩家回主世界出生点
        player.teleport(spawnLocation);

        // 发送提示消息
        plugin.getConfigManager().getMessageManager().sendMessage(player, "security.entry.blocked",
                cn.i7mc.sagadungeons.util.MessageUtil.createPlaceholders("world", dungeonWorldName));

        // 如果启用调试模式，输出调试信息
        if (plugin.getConfigManager().isDebugEnabled()) {
            cn.i7mc.sagadungeons.util.DebugUtil.debug("security.entry.blocked-debug",
                    cn.i7mc.sagadungeons.util.MessageUtil.createPlaceholders(
                            "player", player.getName(),
                            "world", dungeonWorldName,
                            "hasAccess", String.valueOf(hasLegalAccess(player)),
                            "isAdmin", String.valueOf(player.hasPermission("sagadungeons.admin"))
                    ));
        }
    }

    /**
     * 清理玩家的安全状态
     * 当玩家离线或离开副本时调用
     * @param player 玩家
     */
    public void cleanupPlayerSecurity(Player player) {
        revokeLegalAccess(player);
    }

    /**
     * 清理玩家的安全状态（通过UUID）
     * @param playerUUID 玩家UUID
     */
    public void cleanupPlayerSecurity(UUID playerUUID) {
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(playerUUID);
        playerData.setLegalDungeonAccess(false);
    }
}
