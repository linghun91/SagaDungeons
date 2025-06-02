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
 * 需要击杀指定的怪物
 */
public class KillSpecificCondition implements CompletionCondition {

    private final SagaDungeons plugin;
    private final String targetMobName;
    private final Set<UUID> killedTargets = new HashSet<>();
    private boolean killed = false;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param targetMobName 目标怪物名称
     */
    public KillSpecificCondition(SagaDungeons plugin, String targetMobName) {
        this.plugin = plugin;
        this.targetMobName = targetMobName;
    }

    @Override
    public boolean check(DungeonInstance instance) {
        return killed;
    }

    @Override
    public String getDescription() {
        return plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.kill-specific.description", 
                plugin.getConfigManager().getMessageManager().createPlaceholders("mob", targetMobName));
    }

    @Override
    public CompletionType getType() {
        return CompletionType.KILL_SPECIFIC;
    }

    @Override
    public void reset() {
        killedTargets.clear();
        killed = false;
    }

    @Override
    public double getProgress() {
        return killed ? 1.0 : 0.0;
    }

    @Override
    public String getProgressDescription() {
        if (killed) {
            return plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.kill-specific.killed", 
                    plugin.getConfigManager().getMessageManager().createPlaceholders("mob", targetMobName));
        } else {
            return plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.kill-specific.not-killed", 
                    plugin.getConfigManager().getMessageManager().createPlaceholders("mob", targetMobName));
        }
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
                        (entity.customName() != null && entity.customName().toString().equals(targetMobName))) {
                    isTargetMob = true;
                }
            }

            // 如果是目标怪物，标记为已击杀
            if (isTargetMob) {
                killedTargets.add(entity.getUniqueId());
                killed = true;
            }
        }
    }
}
