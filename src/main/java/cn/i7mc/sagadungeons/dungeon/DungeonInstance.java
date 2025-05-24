package cn.i7mc.sagadungeons.dungeon;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.PlayerData;
import cn.i7mc.sagadungeons.util.MessageUtil;
import cn.i7mc.sagadungeons.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 副本实例
 * 表示一个正在运行的副本
 */
public class DungeonInstance {

    private final String id;
    private final String templateName;
    private final UUID ownerUUID;
    private final Set<UUID> allowedPlayers;
    private final long creationTime;
    private String displayName;
    private World world;
    private boolean isPublic;
    private long expirationTime;
    private DungeonState state;
    private BukkitTask timeoutTask;

    /**
     * 构造函数
     * @param id 副本ID
     * @param templateName 模板名称
     * @param ownerUUID 创建者UUID
     */
    public DungeonInstance(String id, String templateName, UUID ownerUUID) {
        this.id = id;
        this.templateName = templateName;
        this.ownerUUID = ownerUUID;
        this.allowedPlayers = new HashSet<>();
        this.creationTime = System.currentTimeMillis();
        this.displayName = templateName;
        this.isPublic = false;
        this.state = DungeonState.CREATING;
    }

    /**
     * 获取副本ID
     * @return 副本ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取模板名称
     * @return 模板名称
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * 获取创建者UUID
     * @return 创建者UUID
     */
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    /**
     * 获取副本世界
     * @return 副本世界
     */
    public World getWorld() {
        return world;
    }

    /**
     * 设置副本世界
     * @param world 副本世界
     */
    public void setWorld(World world) {
        this.world = world;
        this.state = DungeonState.RUNNING;
    }

    /**
     * 获取显示名称
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 设置显示名称
     * @param displayName 显示名称
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 检查是否为公开副本
     * @return 是否为公开副本
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * 设置是否为公开副本
     * @param isPublic 是否为公开副本
     */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * 获取创建时间
     * @return 创建时间
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * 获取过期时间
     * @return 过期时间
     */
    public long getExpirationTime() {
        return expirationTime;
    }

    /**
     * 设置过期时间
     * @param expirationTime 过期时间
     */
    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * 获取副本状态
     * @return 副本状态
     */
    public DungeonState getState() {
        return state;
    }

    /**
     * 设置副本状态
     * @param state 副本状态
     */
    public void setState(DungeonState state) {
        this.state = state;

        // 如果状态为已完成，处理完成事件
        if (state == DungeonState.COMPLETED) {
            handleCompletion();
        }
    }

    /**
     * 处理副本完成
     */
    private void handleCompletion() {
        // 获取插件实例
        SagaDungeons plugin = SagaDungeons.getInstance();

        // 获取副本世界
        if (world == null) {
            return;
        }

        // 计算完成时间
        long completionTime = System.currentTimeMillis();
        int completionTimeSeconds = (int) ((completionTime - creationTime) / 1000);

        // 发送完成消息
        for (Player player : world.getPlayers()) {
            // 发送完成消息
            MessageUtil.sendMessage(player, "dungeon.completion.success",
                    MessageUtil.createPlaceholders("dungeon", displayName,
                            "time", cn.i7mc.sagadungeons.util.TimeUtil.formatTimeShort(completionTimeSeconds)));

            // 给予基础奖励
            plugin.getDungeonManager().getRewardManager().giveRewards(player, templateName);

            // 给予时间奖励
            plugin.getDungeonManager().getRewardManager().giveTimeRewards(player, templateName, completionTimeSeconds);

            // 更新玩家统计数据
            PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());
            if (playerData != null) {
                playerData.incrementCompletedCount(templateName);
            }
        }

        // 延迟删除副本
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // 删除副本
            plugin.getDungeonManager().deleteDungeon(id);
        }, 200L); // 10秒后删除
    }

    /**
     * 获取允许进入的玩家列表
     * @return 允许进入的玩家列表
     */
    public Set<UUID> getAllowedPlayers() {
        return allowedPlayers;
    }

    /**
     * 添加允许进入的玩家
     * @param playerUUID 玩家UUID
     */
    public void addAllowedPlayer(UUID playerUUID) {
        allowedPlayers.add(playerUUID);
    }

    /**
     * 移除允许进入的玩家
     * @param playerUUID 玩家UUID
     */
    public void removeAllowedPlayer(UUID playerUUID) {
        allowedPlayers.remove(playerUUID);
    }

    /**
     * 检查玩家是否允许进入
     * @param playerUUID 玩家UUID
     * @return 是否允许进入
     */
    public boolean isAllowed(UUID playerUUID) {
        return allowedPlayers.contains(playerUUID);
    }

    /**
     * 获取剩余时间（秒）
     * @return 剩余时间
     */
    public int getRemainingTime() {
        return TimeUtil.getRemainingSeconds(expirationTime);
    }

    /**
     * 启动超时任务
     */
    public void startTimeoutTask() {
        // 取消已有的任务
        cancelTimeoutTask();

        // 计算剩余时间
        long remainingMillis = expirationTime - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            // 已经超时，直接删除副本
            SagaDungeons.getInstance().getDungeonManager().deleteDungeon(id);
            return;
        }

        // 计算警告时间点
        long warningMillis = Math.min(remainingMillis - 60000, remainingMillis / 2);

        // 创建警告任务
        if (warningMillis > 0) {
            Bukkit.getScheduler().runTaskLater(SagaDungeons.getInstance(), () -> {
                // 发送警告消息
                if (world != null) {
                    for (Player player : world.getPlayers()) {
                        MessageUtil.sendMessage(player, "dungeon.timeout.warning",
                                MessageUtil.createPlaceholders("time", String.valueOf(60)));
                    }
                }
            }, warningMillis / 50);
        }

        // 创建超时任务
        timeoutTask = Bukkit.getScheduler().runTaskLater(SagaDungeons.getInstance(), () -> {
            // 发送超时消息
            if (world != null) {
                for (Player player : world.getPlayers()) {
                    MessageUtil.sendMessage(player, "dungeon.timeout.expired");
                }
            }

            // 设置状态为超时
            state = DungeonState.TIMEOUT;

            // 删除副本
            SagaDungeons.getInstance().getDungeonManager().deleteDungeon(id);
        }, remainingMillis / 50);
    }

    /**
     * 取消超时任务
     */
    public void cancelTimeoutTask() {
        if (timeoutTask != null) {
            timeoutTask.cancel();
            timeoutTask = null;
        }
    }

    /**
     * 获取副本中的玩家数量
     * @return 玩家数量
     */
    public int getPlayerCount() {
        return world != null ? world.getPlayers().size() : 0;
    }
}
