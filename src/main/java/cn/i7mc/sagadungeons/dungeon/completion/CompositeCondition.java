package cn.i7mc.sagadungeons.dungeon.completion;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 组合条件
 * 支持AND/OR组合模式的条件组合
 */
public class CompositeCondition implements CompletionCondition {

    private final SagaDungeons plugin;
    private final CompletionType type;
    private final List<CompletionCondition> conditions;
    private final int priority;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param type 组合类型（AND/OR）
     * @param priority 优先级（数字越大优先级越高）
     */
    public CompositeCondition(SagaDungeons plugin, CompletionType type, int priority) {
        this.plugin = plugin;
        this.type = type;
        this.conditions = new ArrayList<>();
        this.priority = priority;
    }

    /**
     * 添加子条件
     * @param condition 子条件
     */
    public void addCondition(CompletionCondition condition) {
        conditions.add(condition);
    }

    /**
     * 获取子条件列表
     * @return 子条件列表
     */
    public List<CompletionCondition> getConditions() {
        return conditions;
    }

    /**
     * 获取优先级
     * @return 优先级
     */
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean check(DungeonInstance instance) {
        if (conditions.isEmpty()) {
            return false;
        }

        if (type == CompletionType.AND) {
            // 所有条件都必须满足
            for (CompletionCondition condition : conditions) {
                if (!condition.check(instance)) {
                    return false;
                }
            }
            return true;
        } else if (type == CompletionType.OR) {
            // 满足任意一个条件即可
            for (CompletionCondition condition : conditions) {
                if (condition.check(instance)) {
                    return true;
                }
            }
            return false;
        }

        return false;
    }

    @Override
    public String getDescription() {
        StringBuilder description = new StringBuilder();
        if (type == CompletionType.AND) {
            description.append(plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.and.description"));
        } else {
            description.append(plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.or.description"));
        }

        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) {
                description.append("\n");
            }
            description.append("- ").append(conditions.get(i).getDescription());
        }

        return description.toString();
    }

    @Override
    public CompletionType getType() {
        return type;
    }

    @Override
    public void reset() {
        for (CompletionCondition condition : conditions) {
            condition.reset();
        }
    }

    @Override
    public double getProgress() {
        if (conditions.isEmpty()) {
            return 0.0;
        }

        if (type == CompletionType.AND) {
            // 计算所有条件的平均进度
            double totalProgress = 0.0;
            for (CompletionCondition condition : conditions) {
                totalProgress += condition.getProgress();
            }
            return totalProgress / conditions.size();
        } else if (type == CompletionType.OR) {
            // 返回最高进度
            double maxProgress = 0.0;
            for (CompletionCondition condition : conditions) {
                maxProgress = Math.max(maxProgress, condition.getProgress());
            }
            return maxProgress;
        }

        return 0.0;
    }

    @Override
    public String getProgressDescription() {
        StringBuilder description = new StringBuilder();
        if (type == CompletionType.AND) {
            description.append(plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.and.progress"));
        } else {
            description.append(plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.or.progress"));
        }

        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) {
                description.append("\n");
            }
            description.append("- ").append(conditions.get(i).getProgressDescription());
        }

        return description.toString();
    }

    @Override
    public void handleEvent(Player player, String event, Object data) {
        for (CompletionCondition condition : conditions) {
            condition.handleEvent(player, event, data);
        }
    }
} 