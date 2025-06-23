package cn.i7mc.sagadungeons.dungeon.cooldown;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 冷却管理器
 * 负责管理副本创建冷却时间
 */
public class CooldownManager {

    private final SagaDungeons plugin;
    private final Map<UUID, Long> lastCreationTimes = new ConcurrentHashMap<>();
    private final File cooldownFile;

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public CooldownManager(SagaDungeons plugin) {
        this.plugin = plugin;
        this.cooldownFile = new File(plugin.getDataFolder(), "player_cooldowns.yml");
    }

    /**
     * 加载冷却数据
     */
    public void loadCooldowns() {
        // 清空现有数据
        lastCreationTimes.clear();

        // 删除旧的冷却文件
        if (cooldownFile.exists()) {
            cooldownFile.delete();
            plugin.getLogger().info("已重置玩家冷却时间记录");
        }

        // 不需要加载旧的冷却数据，因为我们希望在服务器重启后重置所有冷却时间
    }

    /**
     * 保存冷却数据
     */
    public void saveCooldowns() {
        // 创建配置
        FileConfiguration config = new YamlConfiguration();

        // 保存所有玩家的冷却时间
        for (Map.Entry<UUID, Long> entry : lastCreationTimes.entrySet()) {
            config.set(entry.getKey().toString(), entry.getValue());
        }

        // 保存到文件
        try {
            config.save(cooldownFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置玩家上次创建副本的时间
     * @param playerUUID 玩家UUID
     * @param time 时间戳
     */
    public void setLastCreationTime(UUID playerUUID, long time) {
        lastCreationTimes.put(playerUUID, time);
    }

    /**
     * 获取玩家上次创建副本的时间
     * @param playerUUID 玩家UUID
     * @return 时间戳，如果没有记录则返回0
     */
    public long getLastCreationTime(UUID playerUUID) {
        return lastCreationTimes.getOrDefault(playerUUID, 0L);
    }

    /**
     * 检查玩家是否可以创建副本
     * @param playerUUID 玩家UUID
     * @param cooldownSeconds 冷却时间(秒)
     * @return 是否可以创建副本
     */
    public boolean canCreateDungeon(UUID playerUUID, int cooldownSeconds) {
        long lastTime = getLastCreationTime(playerUUID);
        if (lastTime == 0) {
            return true;
        }

        long cooldownMillis = cooldownSeconds * 1000L;
        return System.currentTimeMillis() - lastTime >= cooldownMillis;
    }

    /**
     * 获取玩家剩余冷却时间(秒)
     * @param playerUUID 玩家UUID
     * @param cooldownSeconds 冷却时间(秒)
     * @return 剩余冷却时间(秒)
     */
    public int getRemainingCooldown(UUID playerUUID, int cooldownSeconds) {
        long lastTime = getLastCreationTime(playerUUID);
        if (lastTime == 0) {
            return 0;
        }

        long cooldownMillis = cooldownSeconds * 1000L;
        long elapsedMillis = System.currentTimeMillis() - lastTime;
        long remainingMillis = cooldownMillis - elapsedMillis;

        return remainingMillis > 0 ? (int) (remainingMillis / 1000) : 0;
    }

    /**
     * 重置玩家冷却时间
     * @param playerUUID 玩家UUID
     */
    public void resetCooldown(UUID playerUUID) {
        lastCreationTimes.remove(playerUUID);
    }
}
