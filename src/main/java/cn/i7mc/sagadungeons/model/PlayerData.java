package cn.i7mc.sagadungeons.model;

import cn.i7mc.sagadungeons.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 玩家数据模型
 * 存储玩家在副本系统中的数据
 */
public class PlayerData {

    private final UUID playerUUID;
    private String currentDungeonId;
    private Location lastLocation;
    private long lastCreationTime;
    private final Map<String, Integer> completedDungeons = new HashMap<>();
    private int totalCompleted = 0;
    private int totalCreated = 0;
    private int totalJoined = 0;

    /**
     * 构造函数
     * @param playerUUID 玩家UUID
     */
    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    /**
     * 获取玩家UUID
     * @return 玩家UUID
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * 获取当前副本ID
     * @return 当前副本ID，如果不在副本中则为null
     */
    public String getCurrentDungeonId() {
        return currentDungeonId;
    }

    /**
     * 设置当前副本ID
     * @param currentDungeonId 当前副本ID
     */
    public void setCurrentDungeonId(String currentDungeonId) {
        this.currentDungeonId = currentDungeonId;
    }

    /**
     * 获取上次位置
     * @return 上次位置
     */
    public Location getLastLocation() {
        return lastLocation;
    }

    /**
     * 设置上次位置
     * @param lastLocation 上次位置
     */
    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    /**
     * 获取上次创建副本的时间
     * @return 上次创建副本的时间
     */
    public long getLastCreationTime() {
        return lastCreationTime;
    }

    /**
     * 设置上次创建副本的时间
     * @param lastCreationTime 上次创建副本的时间
     */
    public void setLastCreationTime(long lastCreationTime) {
        this.lastCreationTime = lastCreationTime;
    }

    /**
     * 检查玩家是否在副本中
     * @return 是否在副本中
     */
    public boolean isInDungeon() {
        return currentDungeonId != null;
    }

    /**
     * 检查玩家是否可以创建副本
     * @param cooldownSeconds 冷却时间(秒)
     * @return 是否可以创建副本
     */
    public boolean canCreateDungeon(int cooldownSeconds) {
        if (lastCreationTime == 0) {
            return true;
        }
        long cooldownMillis = cooldownSeconds * 1000L;
        return System.currentTimeMillis() - lastCreationTime >= cooldownMillis;
    }

    /**
     * 获取剩余冷却时间(秒)
     * @param cooldownSeconds 冷却时间(秒)
     * @return 剩余冷却时间(秒)
     */
    public int getRemainingCooldown(int cooldownSeconds) {
        if (lastCreationTime == 0) {
            return 0;
        }
        long cooldownMillis = cooldownSeconds * 1000L;
        long elapsedMillis = System.currentTimeMillis() - lastCreationTime;
        long remainingMillis = cooldownMillis - elapsedMillis;
        return remainingMillis > 0 ? (int) (remainingMillis / 1000) : 0;
    }

    /**
     * 获取已完成副本次数
     * @param templateName 模板名称
     * @return 完成次数
     */
    public int getCompletedCount(String templateName) {
        return completedDungeons.getOrDefault(templateName, 0);
    }

    /**
     * 增加已完成副本次数
     * @param templateName 模板名称
     */
    public void incrementCompletedCount(String templateName) {
        int count = getCompletedCount(templateName) + 1;
        completedDungeons.put(templateName, count);
        totalCompleted++;
    }

    /**
     * 获取总完成次数
     * @return 总完成次数
     */
    public int getTotalCompleted() {
        return totalCompleted;
    }

    /**
     * 设置总完成次数
     * @param totalCompleted 总完成次数
     */
    public void setTotalCompleted(int totalCompleted) {
        this.totalCompleted = totalCompleted;
    }

    /**
     * 获取总创建次数
     * @return 总创建次数
     */
    public int getTotalCreated() {
        return totalCreated;
    }

    /**
     * 增加总创建次数
     */
    public void incrementTotalCreated() {
        totalCreated++;
    }

    /**
     * 设置总创建次数
     * @param totalCreated 总创建次数
     */
    public void setTotalCreated(int totalCreated) {
        this.totalCreated = totalCreated;
    }

    /**
     * 获取总加入次数
     * @return 总加入次数
     */
    public int getTotalJoined() {
        return totalJoined;
    }

    /**
     * 增加总加入次数
     */
    public void incrementTotalJoined() {
        totalJoined++;
    }

    /**
     * 设置总加入次数
     * @param totalJoined 总加入次数
     */
    public void setTotalJoined(int totalJoined) {
        this.totalJoined = totalJoined;
    }

    /**
     * 获取已完成副本映射
     * @return 已完成副本映射
     */
    public Map<String, Integer> getCompletedDungeons() {
        return completedDungeons;
    }

    /**
     * 保存到配置部分
     * @param section 配置部分
     */
    public void saveToConfig(ConfigurationSection section) {
        // 保存基本数据
        section.set("uuid", playerUUID.toString());
        section.set("lastCreationTime", lastCreationTime);

        // 保存上次位置
        if (lastLocation != null) {
            section.set("lastLocation", LocationUtil.locationToString(lastLocation));
        }

        // 保存统计数据
        section.set("stats.totalCompleted", totalCompleted);
        section.set("stats.totalCreated", totalCreated);
        section.set("stats.totalJoined", totalJoined);

        // 保存已完成副本
        ConfigurationSection completedSection = section.createSection("completed");
        for (Map.Entry<String, Integer> entry : completedDungeons.entrySet()) {
            completedSection.set(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 从配置部分加载
     * @param section 配置部分
     */
    public void loadFromConfig(ConfigurationSection section) {
        // 加载基本数据
        lastCreationTime = section.getLong("lastCreationTime", 0);

        // 加载上次位置
        String locationString = section.getString("lastLocation");
        if (locationString != null) {
            lastLocation = LocationUtil.stringToLocation(locationString);
        }

        // 加载统计数据
        ConfigurationSection statsSection = section.getConfigurationSection("stats");
        if (statsSection != null) {
            totalCompleted = statsSection.getInt("totalCompleted", 0);
            totalCreated = statsSection.getInt("totalCreated", 0);
            totalJoined = statsSection.getInt("totalJoined", 0);
        }

        // 加载已完成副本
        ConfigurationSection completedSection = section.getConfigurationSection("completed");
        if (completedSection != null) {
            for (String key : completedSection.getKeys(false)) {
                int count = completedSection.getInt(key, 0);
                if (count > 0) {
                    completedDungeons.put(key, count);
                }
            }
        }
    }
}
