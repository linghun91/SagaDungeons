package cn.i7mc.sagadungeons.dungeon.trigger;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import cn.i7mc.sagadungeons.dungeon.DungeonState;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;

/**
 * 关卡跳转触发器
 * 用于实现关卡之间的自动跳转
 */
public class LevelJumpTrigger implements DungeonTrigger {
    
    private final String id;
    private final String targetLevel;
    private final String condition;
    private final ConfigurationSection config;
    private final int delay;
    private final String message;
    
    /**
     * 构造函数
     * @param id 触发器ID
     * @param targetLevel 目标关卡
     * @param condition 触发条件
     * @param config 配置部分
     */
    public LevelJumpTrigger(String id, String targetLevel, String condition, ConfigurationSection config) {
        this.id = id;
        this.targetLevel = targetLevel;
        this.condition = condition;
        this.config = config;
        
        // 从配置中读取参数
        this.delay = config.getInt("delay", 0);
        this.message = config.getString("message", "");
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getType() {
        return config.getString("type", "LEVEL_JUMP");
    }
    
    @Override
    public boolean checkCondition(DungeonInstance instance, Player player) {
        // 根据条件类型检查
        switch (condition.toUpperCase()) {
            case "COMPLETION":
                return instance.getState() == DungeonState.COMPLETED;
            case "DEATH":
                return instance.getState() == DungeonState.DELETING;
            default:
                return false;
        }
    }
    
    @Override
    public void execute(DungeonInstance instance, Player player) {
        // 获取插件实例
        SagaDungeons plugin = SagaDungeons.getInstance();
        
        // 发送跳转消息
        if (!message.isEmpty()) {
            MessageUtil.sendMessage(player, message);
        }
        
        // 如果有延迟，使用延迟任务
        if (delay > 0) {
            BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                createNewDungeon(player);
            }, delay * 20L); // 转换为tick
        } else {
            // 立即创建新副本
            createNewDungeon(player);
        }
    }
    
    /**
     * 创建新的副本
     * @param player 玩家
     */
    private void createNewDungeon(Player player) {
        SagaDungeons plugin = SagaDungeons.getInstance();
        plugin.getDungeonManager().createDungeon(player, targetLevel);
    }
    
    @Override
    public ConfigurationSection getConfig() {
        return config;
    }
    
    /**
     * 从配置部分创建触发器
     * @param section 配置部分
     * @return 触发器实例
     */
    public static LevelJumpTrigger fromConfig(ConfigurationSection section) {
        String id = section.getString("id");
        String targetLevel = section.getString("targetLevel");
        String condition = section.getString("condition", "COMPLETION");
        ConfigurationSection config = section.getConfigurationSection("config");
        
        return new LevelJumpTrigger(id, targetLevel, condition, config);
    }
} 