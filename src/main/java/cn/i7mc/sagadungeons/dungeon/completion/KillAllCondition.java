package cn.i7mc.sagadungeons.dungeon.completion;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 全部击杀条件
 * 需要击杀副本中的所有怪物
 */
public class KillAllCondition implements CompletionCondition {

    private final SagaDungeons plugin;
    private final Set<UUID> killedMonsters = new HashSet<>();
    private final Set<UUID> allMonsters = new HashSet<>();
    private boolean initialized = false;

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public KillAllCondition(SagaDungeons plugin) {
        this.plugin = plugin;
    }

    /**
     * 初始化条件
     * @param instance 副本实例
     */
    private void initialize(DungeonInstance instance) {
        if (initialized) {
            return;
        }
        
        World world = instance.getWorld();
        if (world == null) {
            return;
        }
        
        // 获取世界中的所有怪物
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Monster) {
                allMonsters.add(entity.getUniqueId());
            }
        }
        
        initialized = true;
    }

    @Override
    public boolean check(DungeonInstance instance) {
        // 初始化
        initialize(instance);
        
        // 如果没有怪物，直接返回true
        if (allMonsters.isEmpty()) {
            return true;
        }
        
        // 检查是否所有怪物都已被击杀
        return killedMonsters.size() >= allMonsters.size();
    }

    @Override
    public String getDescription() {
        return plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.kill-all.description");
    }

    @Override
    public CompletionType getType() {
        return CompletionType.KILL_ALL;
    }

    @Override
    public void reset() {
        killedMonsters.clear();
        allMonsters.clear();
        initialized = false;
    }

    @Override
    public double getProgress() {
        if (allMonsters.isEmpty()) {
            return 1.0;
        }
        
        return (double) killedMonsters.size() / allMonsters.size();
    }

    @Override
    public String getProgressDescription() {
        return plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.kill-all.progress", 
                plugin.getConfigManager().getMessageManager().createPlaceholders("killed", String.valueOf(killedMonsters.size()), 
                        "total", String.valueOf(allMonsters.size())));
    }

    @Override
    public void handleEvent(Player player, String event, Object data) {
        if ("kill".equals(event) && data instanceof Entity) {
            Entity entity = (Entity) data;
            
            // 检查是否为怪物
            if (entity instanceof Monster) {
                // 添加到已击杀列表
                killedMonsters.add(entity.getUniqueId());
            }
        }
    }
}
