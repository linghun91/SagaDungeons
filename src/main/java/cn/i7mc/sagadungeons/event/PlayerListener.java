package cn.i7mc.sagadungeons.event;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * 玩家事件监听器
 * 处理玩家相关的事件
 */
public class PlayerListener extends AbstractListener {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public PlayerListener(SagaDungeons plugin) {
        super(plugin);
    }

    /**
     * 处理玩家加入事件
     * @param event 玩家加入事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 加载玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());

        // 延迟1tick检查玩家状态，确保玩家完全加载
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            handlePlayerReconnection(player, playerData);
        }, 1L);
    }

    /**
     * 处理玩家重新连接时的副本状态检查
     * @param player 玩家
     * @param playerData 玩家数据
     */
    private void handlePlayerReconnection(Player player, PlayerData playerData) {
        String currentWorldName = player.getWorld().getName();

        // 检查玩家是否在副本世界
        if (!plugin.getWorldManager().isDungeonWorld(currentWorldName)) {
            return; // 不在副本世界，无需处理
        }

        // 玩家在副本世界，检查状态
        if (playerData.isInDungeon()) {
            // 玩家数据显示在副本中，验证副本是否仍然存在
            String dungeonId = playerData.getCurrentDungeonId();
            DungeonInstance instance = plugin.getDungeonManager().getDungeon(dungeonId);

            if (instance != null && instance.getWorld() != null &&
                instance.getWorld().getName().equals(currentWorldName)) {
                // 副本存在且世界匹配，恢复玩家状态
                restorePlayerDungeonState(player, playerData, instance);
            } else {
                // 副本不存在或世界不匹配，清理状态并移除玩家
                handleOrphanedPlayer(player, playerData, currentWorldName);
            }
        } else {
            // 玩家数据显示不在副本中，但实际在副本世界
            // 尝试根据世界名称查找对应的副本
            DungeonInstance instance = plugin.getDungeonManager().findDungeonByWorldName(currentWorldName);

            if (instance != null) {
                // 找到对应副本，检查玩家是否有权限在此副本中
                if (instance.isAllowed(player.getUniqueId()) ||
                    player.getUniqueId().equals(instance.getOwnerUUID()) ||
                    instance.isPublic()) {
                    // 玩家有权限，恢复副本状态
                    restorePlayerToDungeon(player, playerData, instance);
                } else {
                    // 玩家无权限，移除
                    handleUnauthorizedPlayer(player, currentWorldName);
                }
            } else {
                // 找不到对应副本，可能是残留世界，移除玩家
                handleOrphanedPlayer(player, playerData, currentWorldName);
            }
        }
    }

    /**
     * 恢复玩家的副本状态
     * 当玩家重新连接到已存在的副本时调用
     * @param player 玩家
     * @param playerData 玩家数据
     * @param instance 副本实例
     */
    private void restorePlayerDungeonState(Player player, PlayerData playerData, DungeonInstance instance) {
        // 授予合法副本进入权限
        plugin.getDungeonSecurityManager().grantLegalAccess(player);

        // 获取副本模板并设置游戏模式
        DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(instance.getTemplateName());
        if (template != null) {
            plugin.getDungeonManager().setPlayerGameMode(player, template);
        }

        // 发送重新连接消息
        plugin.getConfigManager().getMessageManager().sendMessage(player, "dungeon.reconnect.restored",
                cn.i7mc.sagadungeons.util.MessageUtil.createPlaceholders(
                        "dungeon", instance.getDisplayName(),
                        "id", instance.getId()
                ));
    }

    /**
     * 恢复玩家到副本中
     * 当玩家重新连接且有权限时调用
     * @param player 玩家
     * @param playerData 玩家数据
     * @param instance 副本实例
     */
    private void restorePlayerToDungeon(Player player, PlayerData playerData, DungeonInstance instance) {
        // 设置玩家当前副本
        playerData.setCurrentDungeonId(instance.getId());

        // 授予合法副本进入权限
        plugin.getDungeonSecurityManager().grantLegalAccess(player);

        // 获取副本模板并设置游戏模式
        DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(instance.getTemplateName());
        if (template != null) {
            plugin.getDungeonManager().setPlayerGameMode(player, template);
        }

        // 发送重新连接消息
        plugin.getConfigManager().getMessageManager().sendMessage(player, "dungeon.reconnect.rejoined",
                cn.i7mc.sagadungeons.util.MessageUtil.createPlaceholders(
                        "dungeon", instance.getDisplayName(),
                        "id", instance.getId()
                ));
    }

    /**
     * 处理无权限的玩家
     * @param player 玩家
     * @param worldName 世界名称
     */
    private void handleUnauthorizedPlayer(Player player, String worldName) {
        // 使用安全管理器处理非法进入
        plugin.getDungeonSecurityManager().handleIllegalDungeonEntry(player, worldName);
    }

    /**
     * 处理孤立的玩家
     * 当玩家在副本世界但副本不存在时调用
     * @param player 玩家
     * @param playerData 玩家数据
     * @param worldName 世界名称
     */
    private void handleOrphanedPlayer(Player player, PlayerData playerData, String worldName) {
        // 清理玩家副本状态
        playerData.setCurrentDungeonId(null);

        // 撤销合法副本进入权限
        plugin.getDungeonSecurityManager().revokeLegalAccess(player);

        // 恢复玩家游戏模式
        plugin.getDungeonManager().restorePlayerGameMode(player);

        // 清除床重生位置
        player.setBedSpawnLocation(null, true);

        // 传送玩家到安全位置
        Location kickbackLocation = plugin.getDungeonSecurityManager().getKickbackLocation();
        if (kickbackLocation != null) {
            player.teleport(kickbackLocation);
        }

        // 发送消息
        plugin.getConfigManager().getMessageManager().sendMessage(player, "dungeon.reconnect.orphaned",
                cn.i7mc.sagadungeons.util.MessageUtil.createPlaceholders("world", worldName));
    }

    /**
     * 处理玩家退出事件
     * @param event 玩家退出事件
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());

        // 检查玩家是否在副本中
        if (playerData.isInDungeon()) {
            // 离开副本
            plugin.getDungeonManager().leaveDungeon(player);
        } else {
            // 清理玩家安全状态
            plugin.getDungeonSecurityManager().cleanupPlayerSecurity(player);
        }
    }

    /**
     * 处理玩家死亡事件
     * @param event 玩家死亡事件
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());

        // 检查玩家是否在副本中
        if (playerData.isInDungeon()) {
            // 获取副本ID
            String dungeonId = playerData.getCurrentDungeonId();

            // 获取副本实例
            DungeonInstance instance = plugin.getDungeonManager().getDungeon(dungeonId);
            if (instance != null) {
                // 获取副本模板
                DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(instance.getTemplateName());
                if (template != null) {
                    // 获取副本内的重生位置
                    Location spawnLocation = getSpawnLocation(template, instance.getWorld());
                    if (spawnLocation != null) {
                        // 设置重生位置为副本内的重生点
                        player.setBedSpawnLocation(spawnLocation, true);
                    }
                }
            }

            // 使用死亡管理器处理死亡
            plugin.getDungeonManager().getDeathManager().handleDeath(player);
        }
    }

    /**
     * 处理玩家重生事件
     * @param event 玩家重生事件
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());

        // 检查玩家是否在副本中
        if (playerData.isInDungeon()) {
            // 获取副本ID
            String dungeonId = playerData.getCurrentDungeonId();

            // 获取副本实例
            DungeonInstance instance = plugin.getDungeonManager().getDungeon(dungeonId);
            if (instance != null) {
                // 获取副本模板
                DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(instance.getTemplateName());
                if (template != null) {
                    // 设置重生位置
                    Location spawnLocation = getSpawnLocation(template, instance.getWorld());
                    if (spawnLocation != null) {
                        event.setRespawnLocation(spawnLocation);
                    }
                }
            }
        }
    }

    /**
     * 获取重生位置
     * @param template 副本模板
     * @param world 副本世界
     * @return 重生位置
     */
    private Location getSpawnLocation(DungeonTemplate template, World world) {
        // 检查模板是否有指定重生点
        if (template.hasSpawnLocation()) {
            // 使用模板中的重生点（不包含世界名）
            Location spawnLocation = cn.i7mc.sagadungeons.util.LocationUtil.stringToLocationWithoutWorld(template.getSpawnLocation(), world);

            // 如果重生点可用，返回该位置
            if (spawnLocation != null) {
                return spawnLocation;
            }
        }

        // 使用世界默认出生点
        return world.getSpawnLocation();
    }

    /**
     * 处理玩家世界切换事件
     * @param event 玩家世界切换事件
     */
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFrom();
        World toWorld = player.getWorld();

        // 检查玩家是否从副本世界离开
        if (plugin.getWorldManager().isDungeonWorld(fromWorld.getName())) {
            // 获取玩家数据
            PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());

            // 检查玩家是否在副本中且目标世界不是副本世界
            if (playerData.isInDungeon() && !plugin.getWorldManager().isDungeonWorld(toWorld.getName())) {
                // 玩家通过其他方式离开了副本世界，自动清理副本状态
                String dungeonId = playerData.getCurrentDungeonId();

                // 清除玩家当前副本状态
                playerData.setCurrentDungeonId(null);

                // 撤销合法副本进入权限
                plugin.getDungeonSecurityManager().revokeLegalAccess(player);

                // 恢复玩家游戏模式
                plugin.getDungeonManager().restorePlayerGameMode(player);

                // 清除床重生位置，避免残留的床重生位置影响后续游戏
                player.setBedSpawnLocation(null, true);

                // 检查副本是否为空，如果为空则删除副本
                if (dungeonId != null) {
                    cn.i7mc.sagadungeons.dungeon.DungeonInstance instance = plugin.getDungeonManager().getDungeon(dungeonId);
                    if (instance != null && instance.getWorld() != null && instance.getWorld().getPlayers().isEmpty()) {
                        // 如果副本为空，删除副本
                        plugin.getDungeonManager().deleteDungeon(dungeonId);
                    }
                }
            } else if (!playerData.isInDungeon()) {
                // 如果玩家不再在副本中，恢复游戏模式
                plugin.getDungeonManager().restorePlayerGameMode(player);
            }
        }
    }
}
