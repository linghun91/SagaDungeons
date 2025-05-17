package cn.i7mc.sagadungeons;

import cn.i7mc.sagadungeons.command.CommandManager;
import cn.i7mc.sagadungeons.config.ConfigManager;
import cn.i7mc.sagadungeons.dungeon.DungeonManager;
import cn.i7mc.sagadungeons.event.CompletionListener;
import cn.i7mc.sagadungeons.event.InventoryListener;
import cn.i7mc.sagadungeons.event.PlayerListener;
import cn.i7mc.sagadungeons.event.WorldListener;
import cn.i7mc.sagadungeons.gui.GUIManager;
import cn.i7mc.sagadungeons.hook.HookManager;
import cn.i7mc.sagadungeons.manager.WorldManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SagaDungeons插件主类
 * 副本系统主类，负责初始化和管理插件生命周期
 */
public class SagaDungeons extends JavaPlugin {

    private static SagaDungeons instance;
    private ConfigManager configManager;
    private CommandManager commandManager;
    private DungeonManager dungeonManager;
    private WorldManager worldManager;
    private HookManager hookManager;
    private GUIManager guiManager;

    /**
     * 获取插件实例
     * @return 插件实例
     */
    public static SagaDungeons getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        // 保存插件实例
        instance = this;

        // 初始化配置管理器
        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        // 初始化世界管理器
        worldManager = new WorldManager(this);

        // 初始化副本管理器
        dungeonManager = new DungeonManager(this);

        // 初始化外部插件集成管理器
        hookManager = new HookManager(this);

        // 初始化GUI管理器
        guiManager = new GUIManager(this);

        // 初始化命令管理器
        commandManager = new CommandManager(this);
        commandManager.registerCommands();

        // 注册事件监听器
        registerListeners();

        // 清理残留副本世界
        if (configManager.isCleanupOnStartup()) {
            worldManager.cleanupRemnantWorlds();
        }
    }

    @Override
    public void onDisable() {
        
        // 保存数据
        if (dungeonManager != null) {
            dungeonManager.saveAllData();
        }

        // 卸载所有副本世界
        if (worldManager != null) {
            worldManager.unloadAllDungeonWorlds();
        }

        // 清除插件实例
        instance = null;
    }

    /**
     * 注册事件监听器
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new CompletionListener(this), this);
    }

    /**
     * 获取配置管理器
     * @return 配置管理器
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * 获取命令管理器
     * @return 命令管理器
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * 获取副本管理器
     * @return 副本管理器
     */
    public DungeonManager getDungeonManager() {
        return dungeonManager;
    }

    /**
     * 获取世界管理器
     * @return 世界管理器
     */
    public WorldManager getWorldManager() {
        return worldManager;
    }

    /**
     * 获取外部插件集成管理器
     * @return 外部插件集成管理器
     */
    public HookManager getHookManager() {
        return hookManager;
    }

    /**
     * 获取GUI管理器
     * @return GUI管理器
     */
    public GUIManager getGUIManager() {
        return guiManager;
    }
}
