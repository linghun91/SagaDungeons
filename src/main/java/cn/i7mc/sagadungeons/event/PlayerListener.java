package cn.i7mc.sagadungeons.event;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.model.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
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
            // 设置重生位置为上次位置
            player.setBedSpawnLocation(playerData.getLastLocation(), true);

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
            if (plugin.getDungeonManager().getDungeon(dungeonId) != null) {
                // 获取副本模板
                String templateName = plugin.getDungeonManager().getDungeon(dungeonId).getTemplateName();

                // 获取副本模板
                if (plugin.getConfigManager().getTemplateManager().hasTemplate(templateName)) {
                    // 获取模板
                    DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(templateName);

                    // 检查是否有死亡次数限制
                    if (template.hasDeathLimit()) {
                        // 获取玩家死亡次数
                        int deathCount = plugin.getDungeonManager().getDeathManager().getDeathCount(player.getUniqueId());

                        // 检查是否达到死亡次数限制
                        if (deathCount >= template.getDeathLimit()) {
                            // 检查是否有复活道具
                            if (template.hasReviveItem()) {
                                // 不做处理，由死亡管理器处理
                                return;
                            }

                            // 离开副本
                            plugin.getDungeonManager().leaveDungeon(player);
                            return;
                        }
                    }
                }
            }
        }
    }
}
