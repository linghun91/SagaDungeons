package cn.i7mc.sagadungeons.dungeon.completion;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 击杀特定怪物条件
 * 需要击杀指定名称的怪物特定数量
 */
public class KillSpecificCondition implements CompletionCondition {

    private final SagaDungeons plugin;
    private final String targetMobName;
    private final int targetCount;
    private int currentCount = 0;
    private final Set<UUID> killedTargets = new HashSet<>();

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param targetMobName 目标怪物名称
     * @param targetCount 目标击杀数量
     */
    public KillSpecificCondition(SagaDungeons plugin, String targetMobName, int targetCount) {
        this.plugin = plugin;
        this.targetMobName = targetMobName;
        this.targetCount = Math.max(1, targetCount); // 确保数量至少为1
    }

    @Override
    public boolean check(DungeonInstance instance) {
        return currentCount >= targetCount;
    }

    @Override
    public String getDescription() {
        return plugin.getConfigManager().getMessageManager().getMessage(
                "dungeon.completion.kill-specific.description",
                plugin.getConfigManager().getMessageManager()
                        .createPlaceholders("mob", targetMobName, "count", String.valueOf(targetCount)));
    }

    @Override
    public CompletionType getType() {
        return CompletionType.KILL_SPECIFIC;
    }

    @Override
    public void reset() {
        currentCount = 0;
        killedTargets.clear();
    }

    @Override
    public double getProgress() {
        return Math.min(1.0, ((double) currentCount) / targetCount);
    }

    @Override
    public String getProgressDescription() {
        return plugin.getConfigManager().getMessageManager().getMessage(
                "dungeon.completion.kill-specific.progress",
                plugin.getConfigManager().getMessageManager()
                        .createPlaceholders(
                                "mob", targetMobName,
                                "current", String.valueOf(currentCount),
                                "target", String.valueOf(targetCount)
                        ));
    }

    @Override
    public void handleEvent(Player player, String event, Object data) {
        if ("kill".equals(event) && data instanceof Entity) {
            Entity entity = (Entity) data;

            // 检查是否为目标怪物
            boolean isTargetMob = false;

            // 首先检查MythicMobs怪物类型
            if (plugin.getHookManager().isMythicMobsAvailable()) {
                String mythicMobType = plugin.getHookManager().getMythicMobsHook().getMythicMobType(entity);
                if (mythicMobType != null && mythicMobType.equals(targetMobName)) {
                    isTargetMob = true;
                }
            }

            // 如果不是MythicMobs怪物，检查实体名称和自定义名称
            if (!isTargetMob) {
                if (entity.getName().equals(targetMobName) ||
                        (entity.getCustomName() != null && entity.getCustomName().equals(targetMobName))) {
                    isTargetMob = true;
                }
            }

            // 如果是目标怪物，记录击杀
            if (isTargetMob && killedTargets.add(entity.getUniqueId())) {
                currentCount = Math.min(currentCount + 1, targetCount); // 防止溢出
            }
        }
    }
}