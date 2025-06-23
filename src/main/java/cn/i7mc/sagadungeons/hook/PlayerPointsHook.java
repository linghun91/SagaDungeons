package cn.i7mc.sagadungeons.hook;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;


/**
 * PlayerPoints集成
 * 负责与PlayerPoints点券系统交互
 */
public class PlayerPointsHook {

    private final SagaDungeons plugin;
    private PlayerPointsAPI pointsAPI;
    private boolean enabled;

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public PlayerPointsHook(SagaDungeons plugin) {
        this.plugin = plugin;
        this.enabled = false;
    }

    /**
     * 设置PlayerPoints
     * @return 是否成功
     */
    public boolean setupPlayerPoints() {
        Plugin playerPointsPlugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (playerPointsPlugin == null) {
            return false;
        }
        
        pointsAPI = ((PlayerPoints) playerPointsPlugin).getAPI();
        enabled = (pointsAPI != null);
        return enabled;
    }

    /**
     * 检查是否启用
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 获取点券API
     * @return 点券API
     */
    public PlayerPointsAPI getPointsAPI() {
        return pointsAPI;
    }

    /**
     * 获取玩家点券
     * @param player 玩家
     * @return 点券数量
     */
    public int getPoints(OfflinePlayer player) {
        if (!enabled || pointsAPI == null) {
            return 0;
        }
        
        return pointsAPI.look(player.getUniqueId());
    }

    /**
     * 扣除玩家点券
     * @param player 玩家
     * @param amount 数量
     * @return 是否成功
     */
    public boolean takePoints(OfflinePlayer player, int amount) {
        if (!enabled || pointsAPI == null) {
            return false;
        }
        
        return pointsAPI.take(player.getUniqueId(), amount);
    }

    /**
     * 给予玩家点券
     * @param player 玩家
     * @param amount 数量
     * @return 是否成功
     */
    public boolean givePoints(OfflinePlayer player, int amount) {
        if (!enabled || pointsAPI == null) {
            return false;
        }
        
        return pointsAPI.give(player.getUniqueId(), amount);
    }

    /**
     * 设置玩家点券
     * @param player 玩家
     * @param amount 数量
     * @return 是否成功
     */
    public boolean setPoints(OfflinePlayer player, int amount) {
        if (!enabled || pointsAPI == null) {
            return false;
        }
        
        return pointsAPI.set(player.getUniqueId(), amount);
    }

    /**
     * 检查玩家是否有足够的点券
     * @param player 玩家
     * @param amount 数量
     * @return 是否有足够的点券
     */
    public boolean hasPoints(OfflinePlayer player, int amount) {
        if (!enabled || pointsAPI == null) {
            return false;
        }
        
        return pointsAPI.look(player.getUniqueId()) >= amount;
    }
}
