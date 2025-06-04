package cn.i7mc.sagadungeons.dungeon.condition;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.hook.VaultHook;
import org.bukkit.entity.Player;

/**
 * 金币条件
 * 检查玩家是否有足够的金币
 */
public class MoneyRequirement implements DungeonRequirement {

    private final double amount;
    private final SagaDungeons plugin;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param amount 金币数量
     */
    public MoneyRequirement(SagaDungeons plugin, double amount) {
        this.plugin = plugin;
        this.amount = amount;
    }

    /**
     * 获取金币数量
     * @return 金币数量
     */
    public double getAmount() {
        return amount;
    }

    @Override
    public boolean check(Player player) {
        // 如果金币数量为0或负数，直接返回true
        if (amount <= 0) {
            return true;
        }
        
        // 检查Vault是否可用
        VaultHook vaultHook = plugin.getHookManager().getVaultHook();
        if (vaultHook == null || !vaultHook.isEnabled()) {
            return false;
        }
        
        // 检查玩家是否有足够的金币
        return vaultHook.hasMoney(player, amount);
    }

    @Override
    public String getFailMessage(Player player) {
        return plugin.getConfigManager().getMessageManager().getMessage("dungeon.requirement.money.fail", 
                plugin.getConfigManager().getMessageManager().createPlaceholders("amount", String.format("%.2f", amount)));
    }

    @Override
    public boolean take(Player player) {
        // 如果金币数量为0或负数，直接返回true
        if (amount <= 0) {
            return true;
        }
        
        // 检查Vault是否可用
        VaultHook vaultHook = plugin.getHookManager().getVaultHook();
        if (vaultHook == null || !vaultHook.isEnabled()) {
            return false;
        }
        
        // 检查玩家是否有足够的金币
        if (!vaultHook.hasMoney(player, amount)) {
            return false;
        }
        
        // 扣除金币
        return vaultHook.withdrawMoney(player, amount);
    }

    @Override
    public RequirementType getType() {
        return RequirementType.MONEY;
    }
}
