package cn.i7mc.sagadungeons.dungeon;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.completion.CompletionManager;
import cn.i7mc.sagadungeons.dungeon.cooldown.CooldownManager;
import cn.i7mc.sagadungeons.dungeon.condition.RequirementManager;
import cn.i7mc.sagadungeons.dungeon.death.DeathManager;
import cn.i7mc.sagadungeons.dungeon.reward.RewardManager;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.model.PlayerData;
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
    private int nextDungeonNumber = 1;

    public DungeonManager(SagaDungeons plugin) {
        this.plugin = plugin;
        this.cooldownManager = new CooldownManager(plugin);
        this.deathManager = new DeathManager(plugin);
        this.completionManager = new CompletionManager(plugin);
        this.rewardManager = new RewardManager(plugin);

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

                    // 更新玩家创建时间
                    playerData.setLastCreationTime(System.currentTimeMillis());
                    cooldownManager.setLastCreationTime(player.getUniqueId(), System.currentTimeMillis());

                    // 更新玩家统计数据
                    playerData.incrementTotalCreated();

                    // 传送玩家到副本
                    Location spawnLocation = world.getSpawnLocation();
                    player.teleport(spawnLocation);

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
            return false;
        }

        // 获取副本世界
        World world = instance.getWorld();
        if (world == null) {
            return false;
        }

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

            // 清除玩家当前副本
            playerData.setCurrentDungeonId(null);
        }

        // 取消超时任务
        instance.cancelTimeoutTask();

        // 卸载并删除副本世界
        plugin.getWorldManager().deleteDungeonWorld(world.getName(), success -> {
            if (success) {
                // 从活动副本列表中移除
                activeDungeons.remove(dungeonId);
            }
        });

        return true;
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

        // 传送玩家到副本
        Location spawnLocation = world.getSpawnLocation();
        player.teleport(spawnLocation);

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

        // 清除玩家当前副本
        playerData.setCurrentDungeonId(null);

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
        if (template.hasMoneyCost()) {
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
        if (template.hasPointsCost()) {
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
        if (template.hasLevelRequirement() && player.getLevel() < template.getLevelRequirement()) {
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
     * 获取奖励管理器
     * @return 奖励管理器
     */
    public RewardManager getRewardManager() {
        return rewardManager;
    }

    /**
     * 加载副本数据
     */
    private void loadDungeonData() {
        // 获取副本数据文件
        File dungeonDataFile = new File(plugin.getDataFolder(), "dungeons.yml");

        // 检查文件是否存在
        if (!dungeonDataFile.exists()) {
            return;
        }

        // 加载配置
        FileConfiguration config = YamlConfiguration.loadConfiguration(dungeonDataFile);

        // 加载副本数据
        ConfigurationSection dungeonsSection = config.getConfigurationSection("dungeons");
        if (dungeonsSection == null) {
            return;
        }

        // 获取下一个副本编号
        nextDungeonNumber = config.getInt("nextDungeonNumber", 1);

        // 遍历所有副本
        for (String dungeonId : dungeonsSection.getKeys(false)) {
            ConfigurationSection dungeonSection = dungeonsSection.getConfigurationSection(dungeonId);
            if (dungeonSection == null) {
                continue;
            }

            try {
                // 获取副本数据
                String templateName = dungeonSection.getString("templateName");
                UUID ownerUUID = UUID.fromString(dungeonSection.getString("ownerUUID"));
                String worldName = dungeonSection.getString("worldName");
                boolean isPublic = dungeonSection.getBoolean("isPublic", false);
                long expirationTime = dungeonSection.getLong("expirationTime", 0);
                String displayName = dungeonSection.getString("displayName", templateName);
                String stateStr = dungeonSection.getString("state", "ACTIVE");
                DungeonState state = DungeonState.valueOf(stateStr);

                // 检查世界是否存在
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().warning("副本世界不存在: " + worldName);
                    continue;
                }

                // 创建副本实例
                DungeonInstance instance = new DungeonInstance(dungeonId, templateName, ownerUUID);
                instance.setWorld(world);
                instance.setPublic(isPublic);
                instance.setExpirationTime(expirationTime);
                instance.setDisplayName(displayName);
                instance.setState(state);

                // 加载允许的玩家
                ConfigurationSection allowedPlayersSection = dungeonSection.getConfigurationSection("allowedPlayers");
                if (allowedPlayersSection != null) {
                    for (String playerUUIDStr : allowedPlayersSection.getKeys(false)) {
                        try {
                            UUID playerUUID = UUID.fromString(playerUUIDStr);
                            instance.addAllowedPlayer(playerUUID);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("无效的玩家UUID: " + playerUUIDStr);
                        }
                    }
                }

                // 添加到活动副本列表
                activeDungeons.put(dungeonId, instance);

                // 启动超时任务
                instance.startTimeoutTask();
            } catch (Exception e) {
                plugin.getLogger().warning("加载副本数据失败: " + dungeonId);
                e.printStackTrace();
            }
        }
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
                plugin.getLogger().warning("Invalid UUID in player data: " + uuidString);
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
            plugin.getLogger().severe("Failed to save dungeon data: " + e.getMessage());
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
            plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
        }
    }

    /**
     * 启动自动保存任务
     */
    private void startAutoSaveTask() {
        // 每5分钟自动保存一次数据
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveAllData, 6000L, 6000L);
    }
}
