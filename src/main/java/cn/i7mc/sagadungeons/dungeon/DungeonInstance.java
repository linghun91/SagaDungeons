package cn.i7mc.sagadungeons.dungeon;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.PlayerData;
import cn.i7mc.sagadungeons.util.MessageUtil;
import cn.i7mc.sagadungeons.util.TimeUtil;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Random;
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

        // 获取配置的延迟删除时间
        int delaySeconds = plugin.getConfigManager().getCompletionDeleteDelay();

        // 发送完成消息和Title
        for (Player player : world.getPlayers()) {
            // 发送完成消息
            MessageUtil.sendMessage(player, "dungeon.completion.success",
                    MessageUtil.createPlaceholders("dungeon", displayName,
                            "time", cn.i7mc.sagadungeons.util.TimeUtil.formatTimeShort(completionTimeSeconds)));

            // 显示Title
            showCompletionTitle(player, delaySeconds);

            // 原奖励系统已移除，保留触发器功能

            // 更新玩家统计数据
            PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());
            if (playerData != null) {
                playerData.incrementCompletedCount(templateName);
            }
        }

        // 启动烟花效果
        startFireworkEffect();

        // 启动倒计时任务
        startCompletionCountdown(delaySeconds);
    }

    /**
     * 启动通关后倒计时
     * @param delaySeconds 延迟秒数
     */
    private void startCompletionCountdown(int delaySeconds) {
        SagaDungeons plugin = SagaDungeons.getInstance();

        // 创建倒计时任务
        new BukkitRunnable() {
            private int remainingSeconds = delaySeconds;

            @Override
            public void run() {
                // 检查副本世界是否还存在
                if (world == null || world.getPlayers().isEmpty()) {
                    // 如果没有玩家了，直接删除副本
                    plugin.getDungeonManager().deleteDungeon(id);
                    this.cancel();
                    return;
                }

                // 如果倒计时结束，删除副本
                if (remainingSeconds <= 0) {
                    plugin.getDungeonManager().deleteDungeon(id);
                    this.cancel();
                    return;
                }

                // 发送倒计时消息
                String messageKey = null;

                if (remainingSeconds <= 3) {
                    // 最后3秒每秒提示
                    messageKey = "dungeon.completion.countdown.urgent";
                } else if (remainingSeconds % 5 == 0) {
                    // 每5秒提示一次
                    messageKey = "dungeon.completion.countdown.normal";
                }

                if (messageKey != null) {
                    for (Player player : world.getPlayers()) {
                        // 发送聊天消息
                        MessageUtil.sendMessage(player, messageKey,
                                MessageUtil.createPlaceholders("time", String.valueOf(remainingSeconds)));

                        // 显示倒计时Title
                        showCountdownTitle(player, remainingSeconds);
                    }
                }

                remainingSeconds--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // 每秒执行一次
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

    /**
     * 显示通关Title
     * @param player 玩家
     * @param delaySeconds 延迟秒数
     */
    private void showCompletionTitle(Player player, int delaySeconds) {
        SagaDungeons plugin = SagaDungeons.getInstance();

        // 获取Title消息
        String mainTitle = plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.title.main");
        String subTitle = plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.title.sub",
                MessageUtil.createPlaceholders("time", String.valueOf(delaySeconds)));

        // 使用Bukkit原生API发送Title
        try {
            // 尝试使用新版本的sendTitle方法
            player.sendTitle(mainTitle, subTitle, 10, 70, 20);
        } catch (Exception e) {
            // 如果新版本方法不可用，使用旧版本方法
            try {
                player.getClass().getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class)
                        .invoke(player, mainTitle, subTitle, 10, 70, 20);
            } catch (Exception ex) {
                // 如果都不可用，只发送聊天消息
                player.sendMessage(mainTitle);
                player.sendMessage(subTitle);
            }
        }
    }

    /**
     * 启动烟花效果
     */
    private void startFireworkEffect() {
        if (world == null) {
            return;
        }

        SagaDungeons plugin = SagaDungeons.getInstance();

        // 创建烟花效果任务
        new BukkitRunnable() {
            @Override
            public void run() {
                // 检查副本世界是否还存在
                if (world == null || world.getPlayers().isEmpty()) {
                    this.cancel();
                    return;
                }

                // 为每个玩家生成烟花
                for (Player player : world.getPlayers()) {
                    spawnFireworksForPlayer(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 60L); // 每3秒执行一次 (60 ticks = 3秒)
    }

    /**
     * 为玩家生成烟花
     * @param player 玩家
     */
    private void spawnFireworksForPlayer(Player player) {
        Random random = new Random();

        // 生成3个随机样式的烟花
        for (int i = 0; i < 3; i++) {
            // 在玩家位置生成烟花
            Firework firework = world.spawn(player.getLocation(), Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();

            // 创建随机烟花效果
            FireworkEffect.Builder builder = FireworkEffect.builder();

            // 随机烟花类型
            FireworkEffect.Type[] types = FireworkEffect.Type.values();
            builder.with(types[random.nextInt(types.length)]);

            // 随机颜色
            Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PURPLE, Color.ORANGE, Color.WHITE};
            builder.withColor(colors[random.nextInt(colors.length)]);
            builder.withFade(colors[random.nextInt(colors.length)]);

            // 随机效果
            if (random.nextBoolean()) {
                builder.flicker(true);
            }
            if (random.nextBoolean()) {
                builder.trail(true);
            }

            // 应用效果
            meta.addEffect(builder.build());
            meta.setPower(random.nextInt(2) + 1); // 1-2的随机威力
            firework.setFireworkMeta(meta);
        }
    }

    /**
     * 显示倒计时Title
     * @param player 玩家
     * @param remainingSeconds 剩余秒数
     */
    private void showCountdownTitle(Player player, int remainingSeconds) {
        SagaDungeons plugin = SagaDungeons.getInstance();

        // 获取Title消息
        String mainTitle = plugin.getConfigManager().getMessageManager().getMessage("dungeon.completion.title.main");
        String subTitleKey = remainingSeconds <= 3 ? "dungeon.completion.title.sub-urgent" : "dungeon.completion.title.sub";
        String subTitle = plugin.getConfigManager().getMessageManager().getMessage(subTitleKey,
                MessageUtil.createPlaceholders("time", String.valueOf(remainingSeconds)));

        // 使用Bukkit原生API发送Title
        try {
            // 尝试使用新版本的sendTitle方法
            player.sendTitle(mainTitle, subTitle, 5, 25, 5);
        } catch (Exception e) {
            // 如果新版本方法不可用，使用旧版本方法
            try {
                player.getClass().getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class)
                        .invoke(player, mainTitle, subTitle, 5, 25, 5);
            } catch (Exception ex) {
                // 如果都不可用，只发送聊天消息
                player.sendMessage(mainTitle);
                player.sendMessage(subTitle);
            }
        }
    }
}
