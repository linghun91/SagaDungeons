package cn.i7mc.sagadungeons.dungeon.completion;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * 到达区域条件
 * 需要玩家到达指定区域
 */
public class ReachAreaCondition implements CompletionCondition {

    private final SagaDungeons plugin;
    private final Location targetLocation;
    private final double radius;
    private boolean reached = false;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param targetLocation 目标位置
     * @param radius 半径
     */
    public ReachAreaCondition(SagaDungeons plugin, Location targetLocation, double radius) {
        this.plugin = plugin;
        this.targetLocation = targetLocation;
        this.radius = radius;
    }

    @Override
    public boolean check(DungeonInstance instance) {
        // 如果已经到达，直接返回true
        if (reached) {
            return true;
        }
        
        // 获取副本世界
        if (instance.getWorld() == null) {
            return false;
        }
        
        // 检查是否有玩家到达目标区域
        for (Player player : instance.getWorld().getPlayers()) {
            if (isInTargetArea(player.getLocation())) {
                reached = true;
                return true;
            }
        }
        
        return false;
    }

    /**
     * 检查位置是否在目标区域内
     * @param location 位置
     * @return 是否在目标区域内
     */
    private boolean isInTargetArea(Location location) {
        // 检查世界是否相同
        if (!location.getWorld().equals(targetLocation.getWorld())) {
            return false;
        }
        
        // 检查距离是否在半径内
        return location.distance(targetLocation) <= radius;
    }

    @Override
    public String getDescription() {
        return plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.reach-area.description", 
                plugin.getConfigManager().getMessageManager().createPlaceholders("x", String.valueOf(targetLocation.getBlockX()), 
                        "y", String.valueOf(targetLocation.getBlockY()), 
                        "z", String.valueOf(targetLocation.getBlockZ())));
    }

    @Override
    public CompletionType getType() {
        return CompletionType.REACH_AREA;
    }

    @Override
    public void reset() {
        reached = false;
    }

    @Override
    public double getProgress() {
        return reached ? 1.0 : 0.0;
    }

    @Override
    public String getProgressDescription() {
        if (reached) {
            return plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.reach-area.reached");
        } else {
            return plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.reach-area.not-reached");
        }
    }

    @Override
    public void handleEvent(Player player, String event, Object data) {
        if ("move".equals(event) && data instanceof Location) {
            Location location = (Location) data;
            
            // 检查是否到达目标区域
            if (isInTargetArea(location)) {
                reached = true;
            }
        }
    }
}
