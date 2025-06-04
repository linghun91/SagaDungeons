package cn.i7mc.sagadungeons.event;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.model.PlayerData;
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
        plugin.getDungeonManager().getPlayerData(player.getUniqueId());
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
