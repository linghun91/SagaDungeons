package cn.i7mc.sagadungeons.event;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * 通关条件监听器
 * 处理与通关条件相关的事件
 */
public class CompletionListener extends AbstractListener {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public CompletionListener(SagaDungeons plugin) {
        super(plugin);
    }

    /**
     * 处理实体死亡事件
     * @param event 实体死亡事件
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // 获取击杀者
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }

        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(killer.getUniqueId());

        // 检查玩家是否在副本中
        if (!playerData.isInDungeon()) {
            return;
        }

        // 处理击杀事件
        plugin.getDungeonManager().getCompletionManager().handleEvent(killer, "kill", event.getEntity());
    }

    /**
     * 处理玩家移动事件
     * @param event 玩家移动事件
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // 获取玩家
        Player player = event.getPlayer();

        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());

        // 检查玩家是否在副本中
        if (!playerData.isInDungeon()) {
            return;
        }

        // 检查是否真的移动了（不仅仅是转头）
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())) {
            return;
        }

        // 处理移动事件
        plugin.getDungeonManager().getCompletionManager().handleEvent(player, "move", to);
    }
}
