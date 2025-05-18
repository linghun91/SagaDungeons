package cn.i7mc.sagadungeons.hook;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.util.DebugUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * MythicMobs集成
 * 负责与MythicMobs怪物系统交互
 */
public class MythicMobsHook {

    private final SagaDungeons plugin;
    private Object mythicMobsInstance;
    private boolean available = false;

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public MythicMobsHook(SagaDungeons plugin) {
        this.plugin = plugin;
        try {
            Plugin mythicMobs = Bukkit.getPluginManager().getPlugin("MythicMobs");
            if (mythicMobs != null && mythicMobs.isEnabled()) {
                // 获取MythicBukkit实例
                Class<?> mythicBukkitClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
                Method instMethod = mythicBukkitClass.getMethod("inst");
                mythicMobsInstance = instMethod.invoke(null);
                available = true;
                DebugUtil.debug("hook.mythicmobs.connected");
            } else {
                DebugUtil.debug("hook.mythicmobs.not-found");
            }
        } catch (Exception e) {
            DebugUtil.debug("hook.mythicmobs.connect-error");
        }
    }

    /**
     * 检查MythicMobs是否可用
     * @return 是否可用
     */
    public boolean isAvailable() {
        return available && mythicMobsInstance != null;
    }

    /**
     * 检查怪物类型是否存在
     * @param mobType 怪物类型
     * @return 是否存在
     */
    public boolean isMobTypeExists(String mobType) {
        if (!isAvailable()) return false;

        try {
            // 获取MobManager
            Method getMobManagerMethod = mythicMobsInstance.getClass().getMethod("getMobManager");
            Object mobManager = getMobManagerMethod.invoke(mythicMobsInstance);

            // 获取MythicMob
            Method getMythicMobMethod = mobManager.getClass().getMethod("getMythicMob", String.class);
            Object optional = getMythicMobMethod.invoke(mobManager, mobType);

            // 检查是否存在
            Method isPresentMethod = optional.getClass().getMethod("isPresent");
            return (boolean) isPresentMethod.invoke(optional);
        } catch (Exception e) {
            DebugUtil.debug("hook.mythicmobs.check-mob-error");
            return false;
        }
    }

    /**
     * 创建刷怪点
     * @param name 刷怪点名称
     * @param location 位置
     * @param mobType 怪物类型
     * @return 是否成功
     */
    public boolean createSpawner(String name, Location location, String mobType) {
        if (!isAvailable()) return false;

        try {
            // 获取SpawnerManager
            Method getSpawnerManagerMethod = mythicMobsInstance.getClass().getMethod("getSpawnerManager");
            Object spawnerManager = getSpawnerManagerMethod.invoke(mythicMobsInstance);

            // 创建刷怪点
            Method createSpawnerMethod = spawnerManager.getClass().getMethod("createSpawner", String.class, Location.class, String.class);
            Object spawner = createSpawnerMethod.invoke(spawnerManager, name, location, mobType);

            return spawner != null;
        } catch (Exception e) {
            DebugUtil.debug("hook.mythicmobs.create-spawner-error");
            return false;
        }
    }

    /**
     * 移除刷怪点
     * @param name 刷怪点名称
     * @return 是否成功
     */
    public boolean removeSpawner(String name) {
        if (!isAvailable()) return false;

        try {
            // 获取SpawnerManager
            Method getSpawnerManagerMethod = mythicMobsInstance.getClass().getMethod("getSpawnerManager");
            Object spawnerManager = getSpawnerManagerMethod.invoke(mythicMobsInstance);

            // 获取刷怪点
            Method getSpawnerByNameMethod = spawnerManager.getClass().getMethod("getSpawnerByName", String.class);
            Object spawner = getSpawnerByNameMethod.invoke(spawnerManager, name);

            if (spawner == null) return false;

            // 移除刷怪点
            Method removeSpawnerMethod = spawnerManager.getClass().getMethod("removeSpawner", spawner.getClass());
            return (boolean) removeSpawnerMethod.invoke(spawnerManager, spawner);
        } catch (Exception e) {
            DebugUtil.debug("hook.mythicmobs.remove-spawner-error");
            return false;
        }
    }

    /**
     * 设置刷怪点属性
     * @param name 刷怪点名称
     * @param cooldown 冷却时间
     * @param amount 生成数量
     * @return 是否成功
     */
    public boolean setupSpawner(String name, int cooldown, int amount) {
        if (!isAvailable()) return false;

        try {
            // 获取SpawnerManager
            Method getSpawnerManagerMethod = mythicMobsInstance.getClass().getMethod("getSpawnerManager");
            Object spawnerManager = getSpawnerManagerMethod.invoke(mythicMobsInstance);

            // 获取刷怪点
            Method getSpawnerByNameMethod = spawnerManager.getClass().getMethod("getSpawnerByName", String.class);
            Object spawner = getSpawnerByNameMethod.invoke(spawnerManager, name);

            if (spawner == null) return false;

            // 设置属性
            Method setCooldownSecondsMethod = spawner.getClass().getMethod("setCooldownSeconds", int.class);
            setCooldownSecondsMethod.invoke(spawner, cooldown);

            Method setMobsPerSpawnMethod = spawner.getClass().getMethod("setMobsPerSpawn", int.class);
            setMobsPerSpawnMethod.invoke(spawner, amount);

            Method setSpawnRadiusMethod = spawner.getClass().getMethod("setSpawnRadius", double.class);
            setSpawnRadiusMethod.invoke(spawner, 5.0);

            Method setLeashRangeMethod = spawner.getClass().getMethod("setLeashRange", double.class);
            setLeashRangeMethod.invoke(spawner, 32.0);

            Method setActivationRangeMethod = spawner.getClass().getMethod("setActivationRange", double.class);
            setActivationRangeMethod.invoke(spawner, 40.0);

            Method saveMethod = spawner.getClass().getMethod("save");
            saveMethod.invoke(spawner);

            return true;
        } catch (Exception e) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", name);
            DebugUtil.debug("hook.mythicmobs.setup-spawner-error", placeholders);
            return false;
        }
    }

    /**
     * 获取所有怪物类型
     * @return 怪物类型列表
     */
    public List<String> getMobTypes() {
        List<String> mobTypes = new ArrayList<>();

        if (!isAvailable()) return mobTypes;

        try {
            // 获取MobManager
            Method getMobManagerMethod = mythicMobsInstance.getClass().getMethod("getMobManager");
            Object mobManager = getMobManagerMethod.invoke(mythicMobsInstance);

            // 获取所有怪物类型
            Method getMobTypesMethod = mobManager.getClass().getMethod("getMobTypes");
            Collection<?> mythicMobs = (Collection<?>) getMobTypesMethod.invoke(mobManager);

            // 获取内部名称
            for (Object mythicMob : mythicMobs) {
                Method getInternalNameMethod = mythicMob.getClass().getMethod("getInternalName");
                String internalName = (String) getInternalNameMethod.invoke(mythicMob);
                mobTypes.add(internalName);
            }
        } catch (Exception e) {
            DebugUtil.debug("hook.mythicmobs.get-mob-types-error");
        }

        return mobTypes;
    }

    /**
     * 直接在指定位置生成MythicMobs怪物
     * @param mobType 怪物类型
     * @param location 生成位置
     * @param amount 生成数量
     * @return 生成的实体列表，如果失败则返回空列表
     */
    public List<LivingEntity> spawnMob(String mobType, Location location, int amount) {
        List<LivingEntity> entities = new ArrayList<>();

        if (!isAvailable() || mobType == null || mobType.isEmpty() ||
            location == null || amount <= 0) {
            return entities;
        }

        try {
            // 获取MobManager
            Method getMobManagerMethod = mythicMobsInstance.getClass().getMethod("getMobManager");
            Object mobManager = getMobManagerMethod.invoke(mythicMobsInstance);

            // 获取MythicMob
            Method getMythicMobMethod = mobManager.getClass().getMethod("getMythicMob", String.class);
            Object optional = getMythicMobMethod.invoke(mobManager, mobType);

            // 检查是否存在
            Method isPresentMethod = optional.getClass().getMethod("isPresent");
            boolean isPresent = (boolean) isPresentMethod.invoke(optional);

            if (!isPresent) {
                return entities;
            }

            // 获取MythicMob实例
            Method getMethod = optional.getClass().getMethod("get");
            Object mythicMob = getMethod.invoke(optional);

            // 确保区块已加载
            if (!location.getChunk().isLoaded()) {
                location.getChunk().load();
            }

            // 创建AbstractLocation
            Class<?> bukkitAdapterClass = Class.forName("io.lumine.mythic.bukkit.BukkitAdapter");
            Method adaptMethod = bukkitAdapterClass.getMethod("adapt", Location.class);
            Object abstractLocation = adaptMethod.invoke(null, location);

            // 生成怪物
            Method spawnMethod = mythicMob.getClass().getMethod("spawn", Class.forName("io.lumine.mythic.api.adapters.AbstractLocation"), double.class);

            for (int i = 0; i < amount; i++) {
                Object activeMob = spawnMethod.invoke(mythicMob, abstractLocation, 1.0);

                // 获取实体
                Method getEntityMethod = activeMob.getClass().getMethod("getEntity");
                Object entity = getEntityMethod.invoke(activeMob);

                Method getBukkitEntityMethod = entity.getClass().getMethod("getBukkitEntity");
                LivingEntity livingEntity = (LivingEntity) getBukkitEntityMethod.invoke(entity);

                entities.add(livingEntity);
            }
        } catch (Exception e) {
            // 忽略异常
        }

        return entities;
    }
}
