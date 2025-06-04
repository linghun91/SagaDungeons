package cn.i7mc.sagadungeons.dungeon.reward;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.hook.PlayerPointsHook;
import org.bukkit.entity.Player;

/**
 * 点券奖励
 * 给予玩家点券
 */
public class PointsReward implements DungeonReward {

    private final SagaDungeons plugin;
    private final int amount;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param amount 点券数量
     */
    public PointsReward(SagaDungeons plugin, int amount) {
        this.plugin = plugin;
        this.amount = amount;
    }

    @Override
    public boolean give(Player player) {
        // 检查PlayerPoints是否可用
        PlayerPointsHook playerPointsHook = plugin.getHookManager().getPlayerPointsHook();
        if (playerPointsHook == null || !playerPointsHook.isEnabled()) {
            return false;
        }
        
        // 给予点券
        return playerPointsHook.givePoints(player, amount);
    }

    @Override
    public String getDescription() {
        return plugin.getConfigManager().getMessageManager().getMessage("dungeon.reward.points.description", 
                plugin.getConfigManager().getMessageManager().createPlaceholders("amount", String.valueOf(amount)));
    }

    @Override
    public RewardType getType() {
        return RewardType.POINTS;
    }
}
