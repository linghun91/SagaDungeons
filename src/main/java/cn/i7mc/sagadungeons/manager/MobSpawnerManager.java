package cn.i7mc.sagadungeons.manager;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.model.MobSpawner;
import cn.i7mc.sagadungeons.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 怪物生成管理器
 * 负责在副本世界中生成怪物
 */
public class MobSpawnerManager {

    private final SagaDungeons plugin;
    private final Map<String, Map<String, SpawnerData>> dungeonSpawners = new HashMap<>();
    private final Map<String, BukkitTask> spawnerTasks = new HashMap<>();

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public MobSpawnerManager(SagaDungeons plugin) {
        this.plugin = plugin;
    }

    /**
     * 初始化副本的刷怪点
     * @param dungeonId 副本ID
     * @param templateName 模板名称
     * @param world 副本世界
     */
    public void initializeSpawners(String dungeonId, String templateName, World world) {
        // 获取模板
        DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(templateName);
        if (template == null) {
            return;
        }

        // 创建刷怪点数据
        Map<String, SpawnerData> spawners = new HashMap<>();
        for (Map.Entry<String, MobSpawner> entry : template.getMobSpawners().entrySet()) {
            String spawnerId = entry.getKey();
            MobSpawner spawner = entry.getValue();

            // 创建位置
            String locationStr = spawner.getLocation();

            // 使用不包含世界名的方法解析位置（标准格式）
            Location location = LocationUtil.stringToLocationWithoutWorld(locationStr, world);

            // 如果无法解析，可能是旧格式，尝试去除第一部分
            if (location == null && locationStr.contains(",")) {
                String[] parts = locationStr.split(",", 2);
                if (parts.length > 1) {
                    // 去除第一部分（可能是世界名或模板名）
                    locationStr = parts[1];
                    location = LocationUtil.stringToLocationWithoutWorld(locationStr, world);
                }
            }

            // 如果仍然无法解析，记录错误并跳过
            if (location == null) {
                continue;
            }

            // 确保区块已加载
            if (!location.getChunk().isLoaded()) {
                location.getChunk().load();
            }

            // 创建刷怪点数据
            SpawnerData spawnerData = new SpawnerData(
                    spawnerId,
                    spawner.getMobType(),
                    location,
                    spawner.getAmount(),
                    spawner.getCooldown()
            );

            // 添加到刷怪点列表
            spawners.put(spawnerId, spawnerData);
        }

        // 保存刷怪点数据
        dungeonSpawners.put(dungeonId, spawners);

        // 启动刷怪任务
        startSpawnerTask(dungeonId);
    }

    /**
     * 启动刷怪任务
     * @param dungeonId 副本ID
     */
    private void startSpawnerTask(String dungeonId) {
        // 取消已有的任务
        stopSpawnerTask(dungeonId);

        // 创建新任务
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // 获取副本实例
            DungeonInstance dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
            if (dungeon == null || dungeon.getWorld() == null) {
                plugin.getLogger().warning("副本实例或世界不存在，停止刷怪任务: " + dungeonId);
                stopSpawnerTask(dungeonId);
                return;
            }

            // 获取刷怪点数据
            Map<String, SpawnerData> spawners = dungeonSpawners.get(dungeonId);
            if (spawners == null) {
                plugin.getLogger().warning("找不到副本的刷怪点数据: " + dungeonId);
                return;
            }

            // 检查每个刷怪点
            for (SpawnerData spawner : spawners.values()) {
                try {
                    // 检查冷却时间
                    if (spawner.isOnCooldown()) {
                        continue;
                    }

                    // 检查是否有玩家在附近
                    boolean playerNearby = false;
                    World world = dungeon.getWorld();

                    // 首先检查副本创建者
                    Player owner = Bukkit.getPlayer(dungeon.getOwnerUUID());
                    if (owner != null && owner.isOnline() &&
                        owner.getWorld().equals(world) &&
                        owner.getLocation().distance(spawner.getLocation()) <= 40) {
                        playerNearby = true;
                    }

                    // 然后检查其他允许的玩家
                    if (!playerNearby) {
                        for (Player player : world.getPlayers()) {
                            if (player.getLocation().distance(spawner.getLocation()) <= 40) {
                                playerNearby = true;
                                break;
                            }
                        }
                    }

                    if (!playerNearby) {
                        continue;
                    }
                } catch (Exception e) {
                    continue;
                }

                // 生成怪物
                if (plugin.getHookManager().isMythicMobsAvailable()) {
                    List<LivingEntity> entities = plugin.getHookManager().getMythicMobsHook().spawnMob(
                            spawner.getMobType(),
                            spawner.getLocation(),
                            spawner.getAmount()
                    );

                    if (!entities.isEmpty()) {
                        // 记录生成的实体
                        spawner.addSpawnedEntities(entities);
                    }
                }

                // 设置冷却时间
                spawner.startCooldown();
            }
        }, 20L, 20L);

        // 保存任务
        spawnerTasks.put(dungeonId, task);
    }

    /**
     * 停止刷怪任务
     * @param dungeonId 副本ID
     */
    public void stopSpawnerTask(String dungeonId) {
        BukkitTask task = spawnerTasks.remove(dungeonId);
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * 清理副本的刷怪点
     * @param dungeonId 副本ID
     */
    public void cleanupSpawners(String dungeonId) {
        // 停止刷怪任务
        stopSpawnerTask(dungeonId);

        // 移除刷怪点数据
        Map<String, SpawnerData> spawners = dungeonSpawners.remove(dungeonId);
        if (spawners != null) {
            // 移除所有生成的实体
            for (SpawnerData spawner : spawners.values()) {
                spawner.removeAllEntities();
            }
        }
    }

    /**
     * 刷怪点数据类
     */
    private static class SpawnerData {
        private final String id;
        private final String mobType;
        private final Location location;
        private final int amount;
        private final int cooldown;
        private long lastSpawnTime;
        private final List<LivingEntity> spawnedEntities = new ArrayList<>();

        /**
         * 构造函数
         * @param id 刷怪点ID
         * @param mobType 怪物类型
         * @param location 位置
         * @param amount 数量
         * @param cooldown 冷却时间
         */
        public SpawnerData(String id, String mobType, Location location, int amount, int cooldown) {
            this.id = id;
            this.mobType = mobType;
            this.location = location;
            this.amount = amount;
            this.cooldown = cooldown;
            this.lastSpawnTime = 0;
        }

        /**
         * 获取刷怪点ID
         * @return 刷怪点ID
         */
        public String getId() {
            return id;
        }

        /**
         * 获取怪物类型
         * @return 怪物类型
         */
        public String getMobType() {
            return mobType;
        }

        /**
         * 获取位置
         * @return 位置
         */
        public Location getLocation() {
            // 确保区块已加载
            if (!location.getChunk().isLoaded()) {
                location.getChunk().load();
            }
            return location;
        }

        /**
         * 获取数量
         * @return 数量
         */
        public int getAmount() {
            return amount;
        }

        /**
         * 检查是否在冷却中
         * @return 是否在冷却中
         */
        public boolean isOnCooldown() {
            // 如果冷却时间为0，表示一次性刷怪点，只有在没有生成过实体时才能生成
            if (cooldown == 0) {
                return !spawnedEntities.isEmpty();
            }

            // 检查冷却时间
            return System.currentTimeMillis() - lastSpawnTime < cooldown * 1000L;
        }

        /**
         * 开始冷却
         */
        public void startCooldown() {
            lastSpawnTime = System.currentTimeMillis();
        }

        /**
         * 添加生成的实体
         * @param entities 实体列表
         */
        public void addSpawnedEntities(List<LivingEntity> entities) {
            // 清理已死亡的实体
            cleanupDeadEntities();

            // 添加新实体
            spawnedEntities.addAll(entities);
        }

        /**
         * 清理已死亡的实体
         */
        private void cleanupDeadEntities() {
            spawnedEntities.removeIf(entity -> entity == null || entity.isDead());
        }

        /**
         * 移除所有实体
         */
        public void removeAllEntities() {
            for (LivingEntity entity : spawnedEntities) {
                if (entity != null && !entity.isDead()) {
                    entity.remove();
                }
            }
            spawnedEntities.clear();
        }
    }
}
