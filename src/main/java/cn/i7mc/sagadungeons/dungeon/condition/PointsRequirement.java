package cn.i7mc.sagadungeons.dungeon.condition;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.hook.PlayerPointsHook;
import org.bukkit.entity.Player;

/**
 * 点券条件
 * 检查玩家是否有足够的点券
 */
public class PointsRequirement implements DungeonRequirement {

    private final int amount;
    private final SagaDungeons plugin;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param amount 点券数量
     */
    public PointsRequirement(SagaDungeons plugin, int amount) {
        this.plugin = plugin;
        this.amount = amount;
    }

    /**
     * 获取点券数量
     * @return 点券数量
     */
    public int getAmount() {
        return amount;
    }

    @Override
    public boolean check(Player player) {
        // 如果点券数量为0或负数，直接返回true
        if (amount <= 0) {
            return true;
        }
        
        // 检查PlayerPoints是否可用
        PlayerPointsHook playerPointsHook = plugin.getHookManager().getPlayerPointsHook();
        if (playerPointsHook == null || !playerPointsHook.isEnabled()) {
            return false;
        }
        
        // 检查玩家是否有足够的点券
        return playerPointsHook.hasPoints(player, amount);
    }

    @Override
    public String getFailMessage(Player player) {
        return plugin.getConfigManager().getMessageManager().getMessage("dungeon.requirement.points.fail", 
                plugin.getConfigManager().getMessageManager().createPlaceholders("amount", String.valueOf(amount)));
    }

    @Override
    public boolean take(Player player) {
        // 如果点券数量为0或负数，直接返回true
        if (amount <= 0) {
            return true;
        }
        
        // 检查PlayerPoints是否可用
        PlayerPointsHook playerPointsHook = plugin.getHookManager().getPlayerPointsHook();
        if (playerPointsHook == null || !playerPointsHook.isEnabled()) {
            return false;
        }
        
        // 检查玩家是否有足够的点券
        if (!playerPointsHook.hasPoints(player, amount)) {
            return false;
        }
        
        // 扣除点券
        return playerPointsHook.takePoints(player, amount);
    }

    @Override
    public RequirementType getType() {
        return RequirementType.POINTS;
    }
}
