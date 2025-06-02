package cn.i7mc.sagadungeons.hook;

import cn.i7mc.sagadungeons.SagaDungeons;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Vault集成
 * 负责与Vault经济系统交互
 */
public class VaultHook {

    private final SagaDungeons plugin;
    private Economy economy;
    private boolean enabled;

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public VaultHook(SagaDungeons plugin) {
        this.plugin = plugin;
        this.enabled = false;
    }

    /**
     * 设置经济系统
     * @return 是否成功
     */
    public boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        
        economy = rsp.getProvider();
        enabled = (economy != null);
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
     * 获取经济系统
     * @return 经济系统
     */
    public Economy getEconomy() {
        return economy;
    }

    /**
     * 获取玩家余额
     * @param player 玩家
     * @return 余额
     */
    public double getBalance(OfflinePlayer player) {
        if (!enabled || economy == null) {
            return 0;
        }
        
        return economy.getBalance(player);
    }

    /**
     * 扣除玩家金币
     * @param player 玩家
     * @param amount 金额
     * @return 是否成功
     */
    public boolean withdrawMoney(OfflinePlayer player, double amount) {
        if (!enabled || economy == null) {
            return false;
        }
        
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    /**
     * 给予玩家金币
     * @param player 玩家
     * @param amount 金额
     * @return 是否成功
     */
    public boolean depositMoney(OfflinePlayer player, double amount) {
        if (!enabled || economy == null) {
            return false;
        }
        
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    /**
     * 检查玩家是否有足够的金币
     * @param player 玩家
     * @param amount 金额
     * @return 是否有足够的金币
     */
    public boolean hasMoney(OfflinePlayer player, double amount) {
        if (!enabled || economy == null) {
            return false;
        }
        
        return economy.has(player, amount);
    }
}
