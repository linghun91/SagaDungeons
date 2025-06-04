package cn.i7mc.sagadungeons.hook;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.Bukkit;

/**
 * 外部插件集成管理器
 * 负责管理与其他插件的集成
 */
public class HookManager {

    private final SagaDungeons plugin;
    private VaultHook vaultHook;
    private PlayerPointsHook playerPointsHook;
    private MythicMobsHook mythicMobsHook;
    private PlaceholderAPIHook placeholderAPIHook;

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public HookManager(SagaDungeons plugin) {
        this.plugin = plugin;

        // 初始化各个集成
        setupVault();
        setupPlayerPoints();
        setupMythicMobs();
        setupPlaceholderAPI();
    }

    /**
     * 设置Vault集成
     */
    private void setupVault() {
        // 检查是否启用Vault集成
        if (!plugin.getConfigManager().isVaultEnabled()) {
            return;
        }

        // 检查Vault插件是否存在
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return;
        }

        // 初始化Vault集成
        vaultHook = new VaultHook(plugin);
        if (!vaultHook.setupEconomy()) {
            vaultHook = null;
        }
    }

    /**
     * 设置PlayerPoints集成
     */
    private void setupPlayerPoints() {
        // 检查是否启用PlayerPoints集成
        if (!plugin.getConfigManager().isPlayerPointsEnabled()) {
            return;
        }

        // 检查PlayerPoints插件是否存在
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") == null) {
            return;
        }

        // 初始化PlayerPoints集成
        playerPointsHook = new PlayerPointsHook(plugin);
        if (!playerPointsHook.setupPlayerPoints()) {
            playerPointsHook = null;
        }
    }

    /**
     * 设置MythicMobs集成
     */
    private void setupMythicMobs() {
        // 检查是否启用MythicMobs集成
        if (!plugin.getConfigManager().isMythicMobsEnabled()) {
            return;
        }

        // 检查MythicMobs插件是否存在
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null) {
            return;
        }

        // 初始化MythicMobs集成
        mythicMobsHook = new MythicMobsHook(plugin);
    }

    /**
     * 设置PlaceholderAPI集成
     */
    private void setupPlaceholderAPI() {
        // 检查是否启用PlaceholderAPI集成
        if (!plugin.getConfigManager().isPlaceholderAPIEnabled()) {
            return;
        }

        // 检查PlaceholderAPI插件是否存在
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }

        // 初始化PlaceholderAPI集成
        placeholderAPIHook = new PlaceholderAPIHook(plugin);
        placeholderAPIHook.register();
    }

    /**
     * 获取Vault集成
     * @return Vault集成
     */
    public VaultHook getVaultHook() {
        return vaultHook;
    }

    /**
     * 获取PlayerPoints集成
     * @return PlayerPoints集成
     */
    public PlayerPointsHook getPlayerPointsHook() {
        return playerPointsHook;
    }

    /**
     * 获取MythicMobs集成
     * @return MythicMobs集成
     */
    public MythicMobsHook getMythicMobsHook() {
        return mythicMobsHook;
    }

    /**
     * 获取PlaceholderAPI集成
     * @return PlaceholderAPI集成
     */
    public PlaceholderAPIHook getPlaceholderAPIHook() {
        return placeholderAPIHook;
    }

    /**
     * 检查Vault是否可用
     * @return 是否可用
     */
    public boolean isVaultAvailable() {
        return vaultHook != null && vaultHook.isEnabled();
    }

    /**
     * 检查PlayerPoints是否可用
     * @return 是否可用
     */
    public boolean isPlayerPointsAvailable() {
        return playerPointsHook != null && playerPointsHook.isEnabled();
    }

    /**
     * 检查MythicMobs是否可用
     * @return 是否可用
     */
    public boolean isMythicMobsAvailable() {
        return mythicMobsHook != null && mythicMobsHook.isAvailable();
    }

    /**
     * 检查PlaceholderAPI是否可用
     * @return 是否可用
     */
    public boolean isPlaceholderAPIAvailable() {
        return placeholderAPIHook != null;
    }
}
