package cn.i7mc.sagadungeons.dungeon.trigger;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 触发器管理器
 * 负责管理所有触发器
 */
public class TriggerManager {
    
    private final SagaDungeons plugin;
    private final Map<String, List<DungeonTrigger>> dungeonTriggers = new HashMap<>();
    
    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public TriggerManager(SagaDungeons plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 加载触发器配置
     * @param templateName 模板名称
     * @param section 配置部分
     */
    public void loadTriggers(String templateName, ConfigurationSection section) {
        List<DungeonTrigger> triggers = new ArrayList<>();
        
        // 加载所有触发器
        if (section != null) {
            // 加载关卡跳转触发器
            ConfigurationSection levelJumpSection = section.getConfigurationSection("levelJump");
            if (levelJumpSection != null) {
                DungeonTrigger trigger = LevelJumpTrigger.fromConfig(levelJumpSection);
                triggers.add(trigger);
            }
            
            // 加载关卡返回触发器
            ConfigurationSection levelBackSection = section.getConfigurationSection("levelback");
            if (levelBackSection != null) {
                DungeonTrigger trigger = LevelJumpTrigger.fromConfig(levelBackSection);
                triggers.add(trigger);
            }
        }
        
        // 保存触发器列表
        dungeonTriggers.put(templateName, triggers);
    }
    
    /**
     * 检查并执行触发器
     * @param instance 副本实例
     * @param player 触发玩家
     */
    public void checkAndExecuteTriggers(DungeonInstance instance, Player player) {
        String templateName = instance.getTemplateName();
        List<DungeonTrigger> triggers = dungeonTriggers.get(templateName);
        
        if (triggers != null) {
            for (DungeonTrigger trigger : triggers) {
                if (trigger.checkCondition(instance, player)) {
                    trigger.execute(instance, player);
                }
            }
        }
    }
    
    /**
     * 获取触发器的配置
     * @param templateName 模板名称
     * @return 触发器配置
     */
    public ConfigurationSection getTriggerConfig(String templateName) {
        List<DungeonTrigger> triggers = dungeonTriggers.get(templateName);
        if (triggers == null || triggers.isEmpty()) {
            return null;
        }
        
        // 创建一个新的配置部分来存储所有触发器的配置
        ConfigurationSection config = plugin.getConfig().createSection(templateName + "_triggers");
        for (DungeonTrigger trigger : triggers) {
            config.set(trigger.getId(), trigger.getConfig());
        }
        return config;
    }
} 