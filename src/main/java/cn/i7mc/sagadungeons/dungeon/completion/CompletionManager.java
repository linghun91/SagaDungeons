package cn.i7mc.sagadungeons.dungeon.completion;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 通关条件管理器
 * 负责管理副本通关条件
 */
public class CompletionManager {

    private final SagaDungeons plugin;
    private final Map<String, List<CompletionCondition>> dungeonConditions = new HashMap<>();

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public CompletionManager(SagaDungeons plugin) {
        this.plugin = plugin;
    }

    /**
     * 加载副本通关条件
     * @param template 副本模板
     * @param section 配置部分
     */
    public void loadCompletionConditions(DungeonTemplate template, ConfigurationSection section) {
        if (section == null) {
            return;
        }

        List<CompletionCondition> conditions = new ArrayList<>();

        // 加载组合条件
        ConfigurationSection compositeSection = section.getConfigurationSection("composite");
        if (compositeSection != null) {
            String type = compositeSection.getString("type", "AND");
            int priority = compositeSection.getInt("priority", 0);
            
            CompletionType compositeType = type.equalsIgnoreCase("OR") ? CompletionType.OR : CompletionType.AND;
            CompositeCondition compositeCondition = new CompositeCondition(plugin, compositeType, priority);
            
            // 加载子条件
            ConfigurationSection conditionsSection = compositeSection.getConfigurationSection("conditions");
            if (conditionsSection != null) {
                for (String key : conditionsSection.getKeys(false)) {
                    ConfigurationSection conditionSection = conditionsSection.getConfigurationSection(key);
                    if (conditionSection != null) {
                        CompletionCondition condition = loadCondition(conditionSection);
                        if (condition != null) {
                            compositeCondition.addCondition(condition);
                        }
                    }
                }
            }
            
            if (!compositeCondition.getConditions().isEmpty()) {
                conditions.add(compositeCondition);
            }
        }

        // 加载单个条件
        CompletionCondition singleCondition = loadCondition(section);
        if (singleCondition != null) {
            conditions.add(singleCondition);
        }

        // 保存条件列表
        if (!conditions.isEmpty()) {
            // 按优先级排序
            conditions.sort((c1, c2) -> {
                int p1 = c1 instanceof CompositeCondition ? ((CompositeCondition) c1).getPriority() : 0;
                int p2 = c2 instanceof CompositeCondition ? ((CompositeCondition) c2).getPriority() : 0;
                return Integer.compare(p2, p1); // 优先级高的排在前面
            });

            dungeonConditions.put(template.getName(), conditions);
        }
    }

    /**
     * 加载单个条件
     * @param section 配置部分
     * @return 条件实例
     */
    private CompletionCondition loadCondition(ConfigurationSection section) {
        // 加载全部击杀条件
        if (section.getBoolean("killAll", false)) {
            return new KillAllCondition(plugin);
        }

        // 加载到达区域条件
        ConfigurationSection reachAreaSection = section.getConfigurationSection("reachArea");
        if (reachAreaSection != null) {
            String locationString = reachAreaSection.getString("location");
            double radius = reachAreaSection.getDouble("radius", 3.0);

            if (locationString != null) {
                Location location = LocationUtil.stringToLocation(locationString);
                if (location != null) {
                    return new ReachAreaCondition(plugin, location, radius);
                }
            }
        }

        // 加载击杀特定怪物条件
        ConfigurationSection killSpecificSection = section.getConfigurationSection("killSpecific");
        if (killSpecificSection != null) {
            String mobName = killSpecificSection.getString("mobName");

            if (mobName != null && !mobName.isEmpty()) {
                return new KillSpecificCondition(plugin, mobName);
            }
        }

        // 加载击杀数量条件
        ConfigurationSection killCountSection = section.getConfigurationSection("killCount");
        if (killCountSection != null) {
            int count = killCountSection.getInt("count", 10);

            if (count > 0) {
                return new KillCountCondition(plugin, count);
            }
        }

        return null;
    }

    /**
     * 获取副本通关条件
     * @param templateName 模板名称
     * @return 通关条件列表
     */
    public List<CompletionCondition> getCompletionConditions(String templateName) {
        return dungeonConditions.getOrDefault(templateName, new ArrayList<>());
    }

    /**
     * 检查副本是否完成
     * @param instance 副本实例
     * @return 是否完成
     */
    public boolean checkCompletion(DungeonInstance instance) {
        // 获取副本模板名称
        String templateName = instance.getTemplateName();

        // 获取通关条件
        List<CompletionCondition> conditions = getCompletionConditions(templateName);

        // 如果没有条件，直接返回false
        if (conditions.isEmpty()) {
            return false;
        }

        // 检查所有条件
        for (CompletionCondition condition : conditions) {
            if (!condition.check(instance)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 处理玩家事件
     * @param player 玩家
     * @param event 事件类型
     * @param data 事件数据
     */
    public void handleEvent(Player player, String event, Object data) {
        // 获取玩家数据
        if (player == null) {
            return;
        }

        // 获取玩家所在副本
        String dungeonId = plugin.getDungeonManager().getPlayerData(player.getUniqueId()).getCurrentDungeonId();
        if (dungeonId == null) {
            return;
        }

        // 获取副本实例
        DungeonInstance instance = plugin.getDungeonManager().getDungeon(dungeonId);
        if (instance == null) {
            return;
        }

        // 检查副本是否已经完成，避免重复处理
        if (instance.getState() == cn.i7mc.sagadungeons.dungeon.DungeonState.COMPLETED) {
            return;
        }

        // 获取副本模板名称
        String templateName = instance.getTemplateName();

        // 获取通关条件
        List<CompletionCondition> conditions = getCompletionConditions(templateName);

        // 处理事件
        for (CompletionCondition condition : conditions) {
            condition.handleEvent(player, event, data);
        }

        // 检查是否完成
        if (checkCompletion(instance)) {
            // 设置副本状态为已完成
            instance.setState(cn.i7mc.sagadungeons.dungeon.DungeonState.COMPLETED);
        }
    }

    /**
     * 重置副本通关条件
     * @param templateName 模板名称
     */
    public void resetCompletionConditions(String templateName) {
        List<CompletionCondition> conditions = getCompletionConditions(templateName);

        for (CompletionCondition condition : conditions) {
            condition.reset();
        }
    }
}
