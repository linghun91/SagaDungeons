package cn.i7mc.sagadungeons.dungeon.completion;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

/**
 * 击杀数量条件
 * 需要击杀指定数量的怪物
 */
public class KillCountCondition implements CompletionCondition {

    private final SagaDungeons plugin;
    private final int targetCount;
    private int currentCount = 0;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param targetCount 目标数量
     */
    public KillCountCondition(SagaDungeons plugin, int targetCount) {
        this.plugin = plugin;
        this.targetCount = targetCount;
    }

    @Override
    public boolean check(DungeonInstance instance) {
        return currentCount >= targetCount;
    }

    @Override
    public String getDescription() {
        return plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.kill-count.description", 
                plugin.getConfigManager().getMessageManager().createPlaceholders("count", String.valueOf(targetCount)));
    }

    @Override
    public CompletionType getType() {
        return CompletionType.KILL_COUNT;
    }

    @Override
    public void reset() {
        currentCount = 0;
    }

    @Override
    public double getProgress() {
        return Math.min(1.0, (double) currentCount / targetCount);
    }

    @Override
    public String getProgressDescription() {
        return plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.kill-count.progress", 
                plugin.getConfigManager().getMessageManager().createPlaceholders("current", String.valueOf(currentCount), 
                        "target", String.valueOf(targetCount)));
    }

    @Override
    public void handleEvent(Player player, String event, Object data) {
        if ("kill".equals(event) && data instanceof Entity) {
            Entity entity = (Entity) data;
            
            // 检查是否为怪物
            if (entity instanceof Monster) {
                // 增加击杀数量
                currentCount++;
            }
        }
    }
}
