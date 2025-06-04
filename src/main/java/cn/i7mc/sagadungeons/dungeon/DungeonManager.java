package cn.i7mc.sagadungeons.dungeon;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.completion.CompletionManager;
import cn.i7mc.sagadungeons.dungeon.cooldown.CooldownManager;
import cn.i7mc.sagadungeons.dungeon.condition.RequirementManager;
import cn.i7mc.sagadungeons.dungeon.death.DeathManager;
import cn.i7mc.sagadungeons.dungeon.reward.RewardManager;
import cn.i7mc.sagadungeons.dungeon.trigger.TriggerManager;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.model.PlayerData;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 副本管理器
 * 负责管理副本实例和玩家数据
 */
public class DungeonManager {

    private final SagaDungeons plugin;
    private final Map<String, DungeonInstance> activeDungeons = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();
    private final CooldownManager cooldownManager;
    private final DeathManager deathManager;
    private final CompletionManager completionManager;
    private final RewardManager rewardManager;
    private final TriggerManager triggerManager;
    private int nextDungeonNumber = 1;

    public DungeonManager(SagaDungeons plugin) {
        this.plugin = plugin;
        this.cooldownManager = new CooldownManager(plugin);
        this.deathManager = new DeathManager(plugin);
        // 使用TemplateManager中的CompletionManager实例，而不是创建新的
        this.completionManager = plugin.getConfigManager().getTemplateManager().getCompletionManager();
        this.rewardManager = new RewardManager(plugin);
        this.triggerManager = new TriggerManager(plugin);

        // 加载副本数据
        loadDungeonData();

        // 加载玩家数据
        loadPlayerData();

        // 加载冷却数据
        cooldownManager.loadCooldowns();

        // 启动自动保存任务
        startAutoSaveTask();
    }

    /**
     * 创建副本
     * @param player 创建者
     * @param templateName 模板名称
     * @return 是否成功
     */
    public boolean createDungeon(Player player, String templateName) {
        // 检查模板是否存在
        if (!plugin.getConfigManager().getTemplateManager().hasTemplate(templateName)) {
            return false;
        }

        // 获取玩家数据
        PlayerData playerData = getPlayerData(player.getUniqueId());

        // 检查玩家是否已经在副本中
        if (playerData.isInDungeon()) {
            return false;
        }

        // 检查冷却时间
        int cooldownSeconds = plugin.getConfigManager().getCreationCooldown();
        if (!cooldownManager.canCreateDungeon(player.getUniqueId(), cooldownSeconds)) {
            return false;
        }

        // 检查是否有其他副本正在创建中
        if (!plugin.getWorldManager().canCreate()) {
            plugin.getConfigManager().getMessageManager().sendMessage(player, "dungeon.creation.locked");
            return false;
        }

        // 获取模板
        DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(templateName);

        // 检查创建条件
        if (!checkCreationConditions(player, template)) {
            return false;
        }

        // 生成副本ID
        String dungeonId = generateDungeonId(templateName);

        // 创建副本实例
        DungeonInstance instance = new DungeonInstance(dungeonId, templateName, player.getUniqueId());
        instance.setDisplayName(template.getDisplayName());
        instance.setExpirationTime(System.currentTimeMillis() + template.getDefaultTimeout() * 1000L);

        // 保存玩家当前位置
        playerData.setLastLocation(player.getLocation());

        // 创建副本世界
        plugin.getWorldManager().createDungeonWorld(templateName, dungeonId, progress -> {
            // 进度回调
        }, success -> {
            if (success) {
                // 世界创建成功
                String worldName = plugin.getConfigManager().getWorldPrefix() + dungeonId;
                World world = Bukkit.getWorld(worldName);

                if (world != null) {
                    // 设置副本世界
                    instance.setWorld(world);

                    // 添加到活动副本列表
                    activeDungeons.put(dungeonId, instance);

                    // 设置玩家当前副本
                    playerData.setCurrentDungeonId(dungeonId);

                    // 授予合法副本进入权限
                    plugin.getDungeonSecurityManager().grantLegalAccess(player);

                    // 更新玩家创建时间
                    playerData.setLastCreationTime(System.currentTimeMillis());
                    cooldownManager.setLastCreationTime(player.getUniqueId(), System.currentTimeMillis());

                    // 更新玩家统计数据
                    playerData.incrementTotalCreated();

                    // 初始化副本刷怪点 - 延迟20tick执行，确保世界完全加载
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        plugin.getMobSpawnerManager().initializeSpawners(dungeonId, templateName, world);
                    }, 20L);

                    // 为副本实例创建独立的通关条件
                    completionManager.createConditionsForDungeon(dungeonId, templateName);

                    // 传送玩家到副本
                    Location spawnLocation;

                    // 检查模板是否有指定重生点
                    if (template.hasSpawnLocation()) {
                        // 使用模板中的重生点（不包含世界名）
                        spawnLocation = cn.i7mc.sagadungeons.util.LocationUtil.stringToLocationWithoutWorld(template.getSpawnLocation(), world);

                        // 如果重生点不可用，使用世界默认出生点
                        if (spawnLocation == null) {
                            spawnLocation = world.getSpawnLocation();
                        }
                    } else {
                        // 使用世界默认出生点
                        spawnLocation = world.getSpawnLocation();
                    }

                    player.teleport(spawnLocation);

                    // 设置游戏模式
                    setPlayerGameMode(player, template);

                    // 启动超时任务
                    instance.startTimeoutTask();
                }
            }
        });

        return true;
    }

    /**
     * 删除副本
     * @param dungeonId 副本ID
     * @return 是否成功
     */
    public boolean deleteDungeon(String dungeonId) {
        // 获取副本实例
        DungeonInstance instance = activeDungeons.get(dungeonId);
        if (instance == null) {
            // 尝试直接删除世界文件，可能是副本实例已经被移除但世界文件仍然存在
            String worldName = plugin.getConfigManager().getWorldPrefix() + dungeonId;
            plugin.getLogger().info("副本实例不存在，尝试直接删除世界文件: " + worldName);

            // 使用清理残留副本世界的方法删除
            cleanupDungeonWorld(worldName);
            return true;
        }

        // 设置副本状态为正在删除
        instance.setState(DungeonState.DELETING);

        // 获取副本世界
        World world = instance.getWorld();
        if (world == null) {
            // 如果世界为空，尝试通过ID构建世界名称
            String worldName = plugin.getConfigManager().getWorldPrefix() + dungeonId;
            plugin.getLogger().info("副本世界为空，尝试通过ID构建世界名称: " + worldName);

            // 从活动副本列表中移除
            activeDungeons.remove(dungeonId);

            // 使用清理残留副本世界的方法删除
            cleanupDungeonWorld(worldName);
            return true;
        }

        // 取消超时任务
        instance.cancelTimeoutTask();

        // 清理副本刷怪点
        plugin.getMobSpawnerManager().cleanupSpawners(dungeonId);

        // 清理副本通关条件
        completionManager.cleanupDungeonConditions(dungeonId);

        // 清理副本死亡次数记录
        deathManager.cleanupDungeonDeathCounts(dungeonId);

        // 将所有玩家传送出副本
        for (Player player : world.getPlayers()) {
            // 获取玩家数据
            PlayerData playerData = getPlayerData(player.getUniqueId());

            // 传送玩家回上次位置
            Location lastLocation = playerData.getLastLocation();
            if (lastLocation != null) {
                player.teleport(lastLocation);
            } else {
                // 如果没有上次位置，传送到主世界出生点
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            }

            // 恢复玩家游戏模式
            restorePlayerGameMode(player);

            // 清除床重生位置，避免残留的床重生位置影响后续游戏
            player.setBedSpawnLocation(null, true);

            // 清除玩家当前副本
            playerData.setCurrentDungeonId(null);

            // 发送消息通知玩家副本被管理员关闭
            plugin.getConfigManager().getMessageManager().sendMessage(player, "dungeon.death.admin-close",
                    MessageUtil.createPlaceholders("id", dungeonId));
        }

        // 立即从活动副本列表中移除，防止玩家加入正在删除的副本
        activeDungeons.remove(dungeonId);

        // 延迟10tick后删除世界
        final String worldName = world.getName();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // 使用清理残留副本世界的方法删除
            cleanupDungeonWorld(worldName);
        }, 10L);

        return true;
    }

    /**
     * 清理副本世界
     * 使用与服务端启动时相同的方法清理副本世界
     * @param worldName 世界名称
     */
    private void cleanupDungeonWorld(String worldName) {
        // 检查世界是否存在
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            // 世界存在，使用WorldManager的方法卸载并删除
            plugin.getWorldManager().deleteDungeonWorld(worldName, success -> {
                if (success) {
                } else {
                }
            });
        } else {
            // 检查世界文件夹是否存在
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            if (worldFolder.exists() && worldFolder.isDirectory()) {
                // 世界文件夹存在，直接删除
                plugin.getWorldManager().deleteWorldFolder(worldName, worldFolder);
            }
        }
    }

    /**
     * 加入副本
     * @param player 玩家
     * @param dungeonId 副本ID
     * @return 是否成功
     */
    public boolean joinDungeon(Player player, String dungeonId) {
        // 获取副本实例
        DungeonInstance instance = activeDungeons.get(dungeonId);
        if (instance == null) {
            return false;
        }

        // 检查副本状态
        if (instance.getState() == DungeonState.DELETING ||
            instance.getState() == DungeonState.COMPLETED ||
            instance.getState() == DungeonState.TIMEOUT) {
            return false;
        }

        // 获取玩家数据
        PlayerData playerData = getPlayerData(player.getUniqueId());

        // 检查玩家是否已经在副本中
        if (playerData.isInDungeon()) {
            return false;
        }

        // 检查玩家是否有权限加入
        if (!instance.isPublic() && !instance.isAllowed(player.getUniqueId()) && !player.getUniqueId().equals(instance.getOwnerUUID())) {
            return false;
        }

        // 获取副本世界
        World world = instance.getWorld();
        if (world == null) {
            return false;
        }

        // 保存玩家当前位置
        playerData.setLastLocation(player.getLocation());

        // 设置玩家当前副本
        playerData.setCurrentDungeonId(dungeonId);
        playerData.incrementTotalJoined();

        // 授予合法副本进入权限
        plugin.getDungeonSecurityManager().grantLegalAccess(player);

        // 传送玩家到副本
        Location spawnLocation;

        // 获取模板
        DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(instance.getTemplateName());

        // 检查模板是否有指定重生点
        if (template != null && template.hasSpawnLocation()) {
            // 使用模板中的重生点（不包含世界名）
            spawnLocation = cn.i7mc.sagadungeons.util.LocationUtil.stringToLocationWithoutWorld(template.getSpawnLocation(), world);

            // 如果重生点不可用，使用世界默认出生点
            if (spawnLocation == null) {
                spawnLocation = world.getSpawnLocation();
            }
        } else {
            // 使用世界默认出生点
            spawnLocation = world.getSpawnLocation();
        }

        player.teleport(spawnLocation);

        // 设置游戏模式
        setPlayerGameMode(player, template);

        return true;
    }

    /**
     * 离开副本
     * @param player 玩家
     * @return 是否成功
     */
    public boolean leaveDungeon(Player player) {
        // 获取玩家数据
        PlayerData playerData = getPlayerData(player.getUniqueId());

        // 检查玩家是否在副本中
        if (!playerData.isInDungeon()) {
            return false;
        }

        // 获取玩家当前副本
        String dungeonId = playerData.getCurrentDungeonId();

        // 传送玩家回上次位置
        Location lastLocation = playerData.getLastLocation();
        if (lastLocation != null) {
            player.teleport(lastLocation);
        } else {
            // 如果没有上次位置，传送到主世界出生点
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }

        // 恢复玩家游戏模式
        restorePlayerGameMode(player);

        // 清除床重生位置，避免残留的床重生位置影响后续游戏
        player.setBedSpawnLocation(null, true);

        // 清除玩家当前副本
        playerData.setCurrentDungeonId(null);

        // 撤销合法副本进入权限
        plugin.getDungeonSecurityManager().revokeLegalAccess(player);

        // 检查副本是否为空
        DungeonInstance instance = activeDungeons.get(dungeonId);
        if (instance != null && instance.getWorld() != null && instance.getWorld().getPlayers().isEmpty()) {
            // 如果副本为空，删除副本
            deleteDungeon(dungeonId);
        }

        return true;
    }

    /**
     * 检查创建条件
     * @param player 玩家
     * @param template 模板
     * @return 是否满足条件
     */
    private boolean checkCreationConditions(Player player, DungeonTemplate template) {
        // 检查金币条件
        if (template.hasMoneyCost() && template.isMoneyEnabled()) {
            // 检查Vault是否可用
            if (!plugin.getHookManager().isVaultAvailable()) {
                plugin.getConfigManager().getMessageManager().sendMessage(player, "dungeon.requirement.vault.unavailable");
                return false;
            }

            // 检查玩家是否有足够的金币
            if (!plugin.getHookManager().getVaultHook().hasMoney(player, template.getMoneyCost())) {
                plugin.getConfigManager().getMessageManager().sendMessage(player, "dungeon.requirement.money.fail",
                        plugin.getConfigManager().getMessageManager().createPlaceholders("amount", String.format("%.2f", template.getMoneyCost())));
                return false;
            }
        }

        // 检查点券条件
        if (template.hasPointsCost() && template.isPointsEnabled()) {
            // 检查PlayerPoints是否可用
            if (!plugin.getHookManager().isPlayerPointsAvailable()) {
                plugin.getConfigManager().getMessageManager().sendMessage(player, "dungeon.requirement.playerpoints.unavailable");
                return false;
            }

            // 检查玩家是否有足够的点券
            if (!plugin.getHookManager().getPlayerPointsHook().hasPoints(player, template.getPointsCost())) {
                plugin.getConfigManager().getMessageManager().sendMessage(player, "dungeon.requirement.points.fail",
                        plugin.getConfigManager().getMessageManager().createPlaceholders("amount", String.valueOf(template.getPointsCost())));
                return false;
            }
        }

        // 检查等级条件
        if (template.hasLevelRequirement() && template.isLevelEnabled() && player.getLevel() < template.getLevelRequirement()) {
            plugin.getConfigManager().getMessageManager().sendMessage(player, "dungeon.requirement.level.fail",
                    plugin.getConfigManager().getMessageManager().createPlaceholders("level", String.valueOf(template.getLevelRequirement())));
            return false;
        }

        // 检查其他条件
        if (!template.getRequirements().isEmpty()) {
            // 使用条件管理器检查条件
            RequirementManager requirementManager = new RequirementManager(plugin);
            if (!requirementManager.checkRequirements(player, template.getRequirements())) {
                return false;
            }
        }

        return true;
    }

    /**
     * 生成副本ID
     * @param templateName 模板名称
     * @return 副本ID
     */
    private String generateDungeonId(String templateName) {
        // 生成编号
        String number = String.format("%03d", nextDungeonNumber++);

        // 生成ID
        return number + "-" + templateName;
    }

    /**
     * 获取玩家数据
     * @param playerUUID 玩家UUID
     * @return 玩家数据
     */
    public PlayerData getPlayerData(UUID playerUUID) {
        // 获取玩家数据，如果不存在则创建
        return playerDataMap.computeIfAbsent(playerUUID, uuid -> new PlayerData(uuid));
    }

    /**
     * 获取活动副本
     * @param dungeonId 副本ID
     * @return 副本实例
     */
    public DungeonInstance getDungeon(String dungeonId) {
        return activeDungeons.get(dungeonId);
    }

    /**
     * 获取所有活动副本
     * @return 活动副本映射
     */
    public Map<String, DungeonInstance> getActiveDungeons() {
        return activeDungeons;
    }

    /**
     * 获取活动副本数量
     * @return 活动副本数量
     */
    public int getActiveDungeonCount() {
        return activeDungeons.size();
    }

    /**
     * 获取冷却管理器
     * @return 冷却管理器
     */
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    /**
     * 获取死亡管理器
     * @return 死亡管理器
     */
    public DeathManager getDeathManager() {
        return deathManager;
    }

    /**
     * 获取通关管理器
     * @return 通关管理器
     */
    public CompletionManager getCompletionManager() {
        return completionManager;
    }

    /**
     * 获取玩家当前所在副本ID
     * @param player 玩家
     * @return 副本ID，如果不在副本中则返回null
     */
    public String getCurrentDungeonId(Player player) {
        PlayerData playerData = getPlayerData(player.getUniqueId());
        return playerData.getCurrentDungeonId();
    }

    /**
     * 获取奖励管理器
     * @return 奖励管理器
     */
    public RewardManager getRewardManager() {
        return rewardManager;
    }

    /**
     * 获取触发器管理器
     * @return 触发器管理器
     */
    public TriggerManager getTriggerManager() {
        return triggerManager;
    }

    /**
     * 加载副本数据
     */
    private void loadDungeonData() {
        // 由于我们在服务器启动时清理所有残留副本，所以不需要加载之前的副本ID记录
        // 直接重置nextDungeonNumber为1
        nextDungeonNumber = 1;

        // 删除旧的dungeons.yml文件
        File dungeonDataFile = new File(plugin.getDataFolder(), "dungeons.yml");
        if (dungeonDataFile.exists()) {
            dungeonDataFile.delete();
        }

        // 不需要加载旧的副本数据，因为所有残留副本都会被清理
        // 活动副本列表将保持为空，直到有新的副本被创建
    }

    /**
     * 加载玩家数据
     */
    private void loadPlayerData() {
        // 获取玩家数据文件
        File playerDataFile = new File(plugin.getDataFolder(), "playerdata.yml");

        // 检查文件是否存在
        if (!playerDataFile.exists()) {
            return;
        }

        // 加载配置
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerDataFile);

        // 加载玩家数据
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        if (playersSection == null) {
            return;
        }

        // 遍历所有玩家
        for (String uuidString : playersSection.getKeys(false)) {
            try {
                // 解析UUID
                UUID playerUUID = UUID.fromString(uuidString);

                // 获取玩家数据部分
                ConfigurationSection playerSection = playersSection.getConfigurationSection(uuidString);
                if (playerSection != null) {
                    // 创建玩家数据
                    PlayerData playerData = new PlayerData(playerUUID);

                    // 加载数据
                    playerData.loadFromConfig(playerSection);

                    // 添加到映射
                    playerDataMap.put(playerUUID, playerData);
                }
            } catch (IllegalArgumentException e) {
            }
        }
    }

    /**
     * 保存所有数据
     */
    public void saveAllData() {
        // 保存副本数据
        saveDungeonData();

        // 保存玩家数据
        savePlayerData();

        // 保存冷却数据
        cooldownManager.saveCooldowns();
    }

    /**
     * 保存副本数据
     */
    private void saveDungeonData() {
        // 创建副本数据文件
        File dungeonDataFile = new File(plugin.getDataFolder(), "dungeons.yml");

        // 创建配置
        FileConfiguration config = new YamlConfiguration();

        // 保存下一个副本编号
        config.set("nextDungeonNumber", nextDungeonNumber);

        // 创建副本数据部分
        ConfigurationSection dungeonsSection = config.createSection("dungeons");

        // 保存所有副本数据
        for (Map.Entry<String, DungeonInstance> entry : activeDungeons.entrySet()) {
            // 获取副本ID和实例
            String dungeonId = entry.getKey();
            DungeonInstance instance = entry.getValue();

            // 创建副本数据部分
            ConfigurationSection dungeonSection = dungeonsSection.createSection(dungeonId);

            // 保存基本数据
            dungeonSection.set("templateName", instance.getTemplateName());
            dungeonSection.set("ownerUUID", instance.getOwnerUUID().toString());

            // 保存世界数据
            if (instance.getWorld() != null) {
                dungeonSection.set("worldName", instance.getWorld().getName());
            }

            // 保存其他数据
            dungeonSection.set("isPublic", instance.isPublic());
            dungeonSection.set("creationTime", instance.getCreationTime());
            dungeonSection.set("expirationTime", instance.getExpirationTime());
            dungeonSection.set("displayName", instance.getDisplayName());
            dungeonSection.set("state", instance.getState().name());

            // 保存允许的玩家
            ConfigurationSection allowedPlayersSection = dungeonSection.createSection("allowedPlayers");
            for (UUID playerUUID : instance.getAllowedPlayers()) {
                allowedPlayersSection.set(playerUUID.toString(), true);
            }
        }

        // 保存配置
        try {
            config.save(dungeonDataFile);
        } catch (IOException e) {
        }
    }

    /**
     * 保存玩家数据
     */
    private void savePlayerData() {
        // 创建玩家数据文件
        File playerDataFile = new File(plugin.getDataFolder(), "playerdata.yml");

        // 创建配置
        FileConfiguration config = new YamlConfiguration();

        // 创建玩家数据部分
        ConfigurationSection playersSection = config.createSection("players");

        // 保存所有玩家数据
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            // 获取玩家UUID和数据
            UUID playerUUID = entry.getKey();
            PlayerData playerData = entry.getValue();

            // 创建玩家数据部分
            ConfigurationSection playerSection = playersSection.createSection(playerUUID.toString());

            // 保存数据
            playerData.saveToConfig(playerSection);
        }

        // 保存配置
        try {
            config.save(playerDataFile);
        } catch (IOException e) {
        }
    }

    /**
     * 启动自动保存任务
     */
    private void startAutoSaveTask() {
        // 每5分钟自动保存一次数据
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveAllData, 6000L, 6000L);
    }

    /**
     * 设置玩家游戏模式
     * @param player 玩家
     * @param template 副本模板
     */
    private void setPlayerGameMode(Player player, DungeonTemplate template) {
        // 检查是否启用强制游戏模式
        if (!template.isForceGameMode()) {
            return;
        }

        // 获取玩家数据
        PlayerData playerData = getPlayerData(player.getUniqueId());

        // 保存玩家当前游戏模式
        playerData.setOriginalGameMode(player.getGameMode());

        // 设置新的游戏模式
        try {
            org.bukkit.GameMode gameMode = org.bukkit.GameMode.valueOf(template.getGameMode().toUpperCase());
            player.setGameMode(gameMode);
        } catch (IllegalArgumentException e) {
            // 如果游戏模式无效，默认使用冒险模式
            player.setGameMode(org.bukkit.GameMode.ADVENTURE);
        }
    }

    /**
     * 恢复玩家游戏模式
     * @param player 玩家
     */
    public void restorePlayerGameMode(Player player) {
        // 获取玩家数据
        PlayerData playerData = getPlayerData(player.getUniqueId());

        // 获取原始游戏模式
        org.bukkit.GameMode originalGameMode = playerData.getOriginalGameMode();

        // 如果有保存的原始游戏模式，则恢复
        if (originalGameMode != null) {
            player.setGameMode(originalGameMode);
            // 清除保存的游戏模式
            playerData.setOriginalGameMode(null);
        }
    }

}
