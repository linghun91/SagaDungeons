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
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final double range;
    private boolean reached = false;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     * @param targetZ 目标Z坐标
     * @param range 检测范围
     */
    public ReachAreaCondition(SagaDungeons plugin, double targetX, double targetY, double targetZ, double range) {
        this.plugin = plugin;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.range = range;
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
     * @param location 玩家位置
     * @return 是否在目标区域内
     */
    private boolean isInTargetArea(Location location) {
        // 直接检查坐标范围，不检查世界名
        double deltaX = Math.abs(location.getX() - targetX);
        double deltaY = Math.abs(location.getY() - targetY);
        double deltaZ = Math.abs(location.getZ() - targetZ);

        // 检查是否在range范围内
        return deltaX <= range && deltaY <= range && deltaZ <= range;
    }

    @Override
    public String getDescription() {
        return plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.reach-area.description",
                plugin.getConfigManager().getMessageManager().createPlaceholders("x", String.valueOf((int)targetX),
                        "y", String.valueOf((int)targetY),
                        "z", String.valueOf((int)targetZ)));
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
