package cn.i7mc.sagadungeons.dungeon.reward;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.hook.VaultHook;
import org.bukkit.entity.Player;

/**
 * 金币奖励
 * 给予玩家金币
 */
public class MoneyReward implements DungeonReward {

    private final SagaDungeons plugin;
    private final double amount;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param amount 金币数量
     */
    public MoneyReward(SagaDungeons plugin, double amount) {
        this.plugin = plugin;
        this.amount = amount;
    }

    @Override
    public boolean give(Player player) {
        // 检查Vault是否可用
        VaultHook vaultHook = plugin.getHookManager().getVaultHook();
        if (vaultHook == null || !vaultHook.isEnabled()) {
            return false;
        }
        
        // 给予金币
        return vaultHook.depositMoney(player, amount);
    }

    @Override
    public String getDescription() {
        return plugin.getConfigManager().getMessageManager().getMessage("dungeon.reward.money.description", 
                plugin.getConfigManager().getMessageManager().createPlaceholders("amount", String.format("%.2f", amount)));
    }

    @Override
    public RewardType getType() {
        return RewardType.MONEY;
    }
}
