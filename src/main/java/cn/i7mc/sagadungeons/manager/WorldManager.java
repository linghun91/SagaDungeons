package cn.i7mc.sagadungeons.manager;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.model.PlayerData;
import cn.i7mc.sagadungeons.util.BukkitFileUtil;
import cn.i7mc.sagadungeons.util.DebugUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 世界管理器
 * 负责管理副本世界的创建、复制、卸载和删除
 */
public class WorldManager {

    private final SagaDungeons plugin;
    private final String worldPrefix;
    private boolean createLock = false; // 创建锁，防止并发创建副本
    private final Set<String> unloadTracker = new HashSet<>(); // 世界卸载跟踪器，防止递归卸载

    public WorldManager(SagaDungeons plugin) {
        this.plugin = plugin;
        this.worldPrefix = plugin.getConfigManager().getWorldPrefix();
    }

    /**
     * 检查是否可以创建副本
     * @return 是否可以创建
     */
    public boolean canCreate() {
        return !createLock;
    }

    /**
     * 创建副本世界
     * @param templateName 模板名称
     * @param dungeonId 副本ID
     * @param progressCallback 进度回调
     * @param completionCallback 完成回调
     */
    public void createDungeonWorld(String templateName, String dungeonId, Consumer<Double> progressCallback, Consumer<Boolean> completionCallback) {
        // 检查创建锁
        if (createLock) {
            plugin.getLogger().warning("另一个副本正在创建中，请稍后再试");
            if (completionCallback != null) {
                completionCallback.accept(false);
            }
            return;
        }

        // 设置创建锁
        createLock = true;

        // 在异步线程中执行世界复制操作
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // 获取模板
                DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(templateName);
                if (template == null) {
                    plugin.getLogger().warning("找不到模板: " + templateName);
                    if (completionCallback != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            createLock = false;
                            completionCallback.accept(false);
                        });
                    } else {
                        Bukkit.getScheduler().runTask(plugin, () -> createLock = false);
                    }
                    return;
                }

                // 获取模板目录
                File templateDir;

                // 检查是否有指定的世界路径
                if (template.hasWorldPath()) {
                    templateDir = new File(plugin.getDataFolder(), template.getWorldPath());
                } else {
                    templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
                }

                // 检查模板目录是否存在
                if (!templateDir.exists() || !templateDir.isDirectory()) {
                    plugin.getLogger().warning("模板目录不存在: " + templateDir.getAbsolutePath());
                    if (completionCallback != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            createLock = false;
                            completionCallback.accept(false);
                        });
                    } else {
                        Bukkit.getScheduler().runTask(plugin, () -> createLock = false);
                    }
                    return;
                }

                // 创建副本世界名称
                String worldName = worldPrefix + dungeonId;

                // 获取服务器世界目录
                File worldsDir = new File(Bukkit.getWorldContainer(), worldName);

                // 确保目标目录存在
                if (!worldsDir.exists()) {
                    worldsDir.mkdirs();
                }

                // 检查源目录结构
                File sourceDir;
                File regionDir = new File(templateDir, "region");

                if (regionDir.exists() && regionDir.isDirectory()) {
                    sourceDir = templateDir;
                } else {
                    File worldSubDir = new File(templateDir, "world");
                    File worldRegionDir = new File(worldSubDir, "region");

                    if (worldSubDir.exists() && worldSubDir.isDirectory() && worldRegionDir.exists() && worldRegionDir.isDirectory()) {
                        sourceDir = worldSubDir;
                    } else {
                        plugin.getLogger().warning("找不到有效的世界目录: " + templateDir.getAbsolutePath());
                        if (completionCallback != null) {
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                createLock = false;
                                completionCallback.accept(false);
                            });
                        } else {
                            Bukkit.getScheduler().runTask(plugin, () -> createLock = false);
                        }
                        return;
                    }
                }

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("source", sourceDir.getAbsolutePath());
                placeholders.put("target", worldsDir.getAbsolutePath());
                DebugUtil.debug("world.copy.file-copy-start", placeholders);

                // 复制文件
                final long startTime = System.currentTimeMillis();
                boolean success = BukkitFileUtil.copyDirectory(sourceDir, worldsDir, progressCallback);
                final long copyTime = System.currentTimeMillis() - startTime;

                if (!success) {
                    DebugUtil.debug("world.copy.file-copy-fail");
                    if (completionCallback != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            createLock = false;
                            completionCallback.accept(false);
                        });
                    } else {
                        Bukkit.getScheduler().runTask(plugin, () -> createLock = false);
                    }
                    return;
                }

                placeholders.clear();
                placeholders.put("time", String.valueOf(copyTime));
                DebugUtil.debug("world.copy.file-copy-complete", placeholders);

                // 文件复制完成后，切换到主线程创建世界
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        final long loadStartTime = System.currentTimeMillis();

                        // 创建并加载世界
                        WorldCreator creator = new WorldCreator(worldName);
                        creator.generateStructures(false);
                        creator.environment(World.Environment.NORMAL);

                        World world = creator.createWorld();

                        final long loadTime = System.currentTimeMillis() - loadStartTime;
                        Map<String, String> loadPlaceholders = new HashMap<>();
                        loadPlaceholders.put("time", String.valueOf(loadTime));
                        loadPlaceholders.put("world", worldName);

                        if (world != null) {
                            DebugUtil.debug("world.load.complete", loadPlaceholders);

                            // 设置世界属性
                            world.setAutoSave(false);
                            world.setKeepSpawnInMemory(false);

                            // 设置游戏规则
                            world.setGameRule(GameRule.KEEP_INVENTORY, true);
                            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                            world.setGameRule(GameRule.DO_FIRE_TICK, false);
                            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
                            world.setGameRule(GameRule.DO_ENTITY_DROPS, false);

                            // 调用完成回调
                            if (completionCallback != null) {
                                completionCallback.accept(true);
                            }
                        } else {
                            DebugUtil.debug("world.load.fail", loadPlaceholders);
                            if (completionCallback != null) {
                                completionCallback.accept(false);
                            }
                        }
                    } catch (Exception worldCreateException) {
                        Map<String, String> worldErrorPlaceholders = new HashMap<>();
                        worldErrorPlaceholders.put("message", worldCreateException.getMessage());
                        DebugUtil.debug("world.error.create", worldErrorPlaceholders);
                        worldCreateException.printStackTrace();

                        if (completionCallback != null) {
                            completionCallback.accept(false);
                        }
                    } finally {
                        // 释放创建锁
                        createLock = false;
                    }
                });
            } catch (Exception e) {
                Map<String, String> errorPlaceholders = new HashMap<>();
                errorPlaceholders.put("message", e.getMessage());
                DebugUtil.debug("world.error.create", errorPlaceholders);
                e.printStackTrace();

                // 释放创建锁并调用完成回调
                Bukkit.getScheduler().runTask(plugin, () -> {
                    createLock = false;
                    if (completionCallback != null) {
                        completionCallback.accept(false);
                    }
                });
            }
        });
    }

    /**
     * 将所有玩家传送出世界
     * @param world 要传送玩家的世界
     */
    private void teleportPlayersOutOfWorld(World world) {
        // 获取世界中的所有玩家
        for (Player player : world.getPlayers()) {
            // 获取玩家数据
            PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());

            // 传送玩家回上次位置
            Location lastLocation = playerData.getLastLocation();
            if (lastLocation != null) {
                player.teleport(lastLocation);
            } else {
                // 如果没有上次位置，传送到主世界出生点
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            }

            // 恢复玩家游戏模式
            plugin.getDungeonManager().restorePlayerGameMode(player);

            // 清除床重生位置，避免残留的床重生位置影响后续游戏
            player.setBedSpawnLocation(null, true);

            // 清除玩家当前副本
            playerData.setCurrentDungeonId(null);
        }
    }

    /**
     * 卸载并删除副本世界
     * @param worldName 世界名称
     * @param completionCallback 完成回调
     */
    public void deleteDungeonWorld(String worldName, Consumer<Boolean> completionCallback) {
        // 确保在主线程中执行
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> deleteDungeonWorld(worldName, completionCallback));
            return;
        }

        // 检查是否已经在卸载中
        if (unloadTracker.contains(worldName)) {
            plugin.getLogger().info("世界已经在卸载中: " + worldName);
            if (completionCallback != null) {
                completionCallback.accept(false);
            }
            return;
        }

        // 添加到卸载跟踪器
        unloadTracker.add(worldName);

        // 获取世界
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            // 世界已经卸载，直接删除文件
            unloadTracker.remove(worldName);
            deleteWorldFolder(worldName, completionCallback);
            return;
        }

        Map<String, String> unloadPlaceholders = new HashMap<>();
        unloadPlaceholders.put("world", worldName);
        DebugUtil.debug("world.unload.start", unloadPlaceholders);

        // 1. 将所有玩家传送出世界
        teleportPlayersOutOfWorld(world);

        // 2. 移除所有实体
        removeAllEntities(world);

        // 3. 保存世界数据
        try {
            world.save();
        } catch (Exception e) {
        }

        // 4. 延迟20tick后卸载世界
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                // 5. 卸载世界
                boolean unloaded = false;

                // 首先尝试卸载所有区块
                try {
                    // 确保所有区块都被保存和卸载
                    for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                        chunk.unload(true);
                    }
                } catch (Exception e) {
                }

                // 再次尝试移除所有实体
                removeAllEntities(world);

                // 使用Bukkit API卸载世界
                try {
                    unloaded = Bukkit.unloadWorld(world, false);
                } catch (Exception e) {
                }

                // 最终检查世界是否已卸载
                World finalCheckWorld = Bukkit.getWorld(worldName);
                boolean finalUnloaded = finalCheckWorld == null;

                if (finalUnloaded) {
                } else {
                }

                // 6. 再延迟20tick后删除世界文件
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // 7. 删除世界文件夹
                    File worldDir = new File(Bukkit.getWorldContainer(), worldName);
                    BukkitFileUtil.deleteDirectoryAsync(worldDir, success -> {
                        // 从卸载跟踪器中移除
                        unloadTracker.remove(worldName);

                        if (completionCallback != null) {
                            completionCallback.accept(success);
                        }

                        Map<String, String> deletePlaceholders = new HashMap<>();
                        deletePlaceholders.put("world", worldName);
                        if (success) {
                            DebugUtil.debug("world.delete.success", deletePlaceholders);
                        } else {
                            DebugUtil.debug("world.delete.fail", deletePlaceholders);
                        }
                    });
                }, 20L);
        } finally {
            // 确保在异常情况下也能从卸载跟踪器中移除
            if (unloadTracker.contains(worldName)) {
                unloadTracker.remove(worldName);
            }
        }
    }, 20L);
    }

    /**
     * 删除世界文件夹
     * @param worldName 世界名称
     * @param completionCallback 完成回调
     */
    private void deleteWorldFolder(String worldName, Consumer<Boolean> completionCallback) {
        // 确保在主线程中执行
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> deleteWorldFolder(worldName, completionCallback));
            return;
        }

        // 检查是否已经在卸载中
        if (unloadTracker.contains(worldName)) {
            if (completionCallback != null) {
                completionCallback.accept(false);
            }
            return;
        }

        // 添加到卸载跟踪器
        unloadTracker.add(worldName);

        File worldDir = new File(Bukkit.getWorldContainer(), worldName);

        // 检查文件夹是否存在
        if (!worldDir.exists()) {
            Map<String, String> notExistPlaceholders = new HashMap<>();
            notExistPlaceholders.put("world", worldName);
            DebugUtil.debug("world.delete.not-exist", notExistPlaceholders);
            unloadTracker.remove(worldName);
            if (completionCallback != null) {
                completionCallback.accept(true);
            }
            return;
        }

        // 检查是否有世界正在使用该文件夹
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            Map<String, String> inUsePlaceholders = new HashMap<>();
            inUsePlaceholders.put("world", worldName);
            DebugUtil.debug("world.delete.in-use", inUsePlaceholders);

            // 将所有玩家传送出世界
            teleportPlayersOutOfWorld(world);

            // 移除所有实体
            removeAllEntities(world);

            // 保存世界数据
            try {
                world.save();
            } catch (Exception e) {
            }

            // 延迟20tick后卸载世界
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    // 卸载世界
                    boolean unloaded = false;

                    // 首先尝试卸载所有区块
                    try {
                        // 确保所有区块都被保存和卸载
                        for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                            chunk.unload(true);
                        }
                    } catch (Exception e) {
                    }

                    // 再次尝试移除所有实体
                    removeAllEntities(world);

                    // 使用Bukkit API卸载世界
                    try {
                        unloaded = Bukkit.unloadWorld(world, false);
                    } catch (Exception e) {
                    }

                    // 最终检查世界是否已卸载
                    World finalCheckWorld = Bukkit.getWorld(worldName);
                    boolean finalUnloaded = finalCheckWorld == null;

                    if (finalUnloaded) {
                    } else {
                    }

                // 再延迟20tick后删除世界文件
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // 异步删除文件夹
                    BukkitFileUtil.deleteDirectoryAsync(worldDir, success -> {
                        // 从卸载跟踪器中移除
                        unloadTracker.remove(worldName);

                        if (completionCallback != null) {
                            completionCallback.accept(success);
                        }

                        Map<String, String> deletePlaceholders = new HashMap<>();
                        deletePlaceholders.put("world", worldName);
                        if (success) {
                            DebugUtil.debug("world.delete.success", deletePlaceholders);
                        } else {
                            DebugUtil.debug("world.delete.fail", deletePlaceholders);
                        }
                    });
                }, 20L);
            } finally {
                // 确保在异常情况下也能从卸载跟踪器中移除
                if (unloadTracker.contains(worldName)) {
                    unloadTracker.remove(worldName);
                }
            }
        }, 20L);
        } else {
            // 世界未加载，直接删除文件夹
            BukkitFileUtil.deleteDirectoryAsync(worldDir, success -> {
                // 从卸载跟踪器中移除
                unloadTracker.remove(worldName);

                if (completionCallback != null) {
                    completionCallback.accept(success);
                }

                Map<String, String> deletePlaceholders = new HashMap<>();
                deletePlaceholders.put("world", worldName);
                if (success) {
                    DebugUtil.debug("world.delete.success", deletePlaceholders);
                } else {
                    DebugUtil.debug("world.delete.fail", deletePlaceholders);
                }
            });
        }
    }

    /**
     * 移除世界中的所有实体
     * @param world 要移除实体的世界
     */
    private void removeAllEntities(World world) {
        // 获取世界中的所有实体
        for (org.bukkit.entity.Entity entity : world.getEntities()) {
            // 跳过玩家
            if (entity instanceof Player) {
                continue;
            }
            // 移除实体
            entity.remove();
        }
    }



    /**
     * 卸载所有副本世界
     */
    public void unloadAllDungeonWorlds() {
        // 获取所有世界
        List<World> worlds = new ArrayList<>(Bukkit.getWorlds());

        // 遍历所有世界
        for (World world : worlds) {
            String worldName = world.getName();

            // 检查是否为副本世界
            if (worldName.startsWith(worldPrefix)) {
                Map<String, String> unloadPlaceholders = new HashMap<>();
                unloadPlaceholders.put("world", worldName);
                DebugUtil.debug("world.unload.start", unloadPlaceholders);

                // 使用deleteDungeonWorld方法卸载和删除世界
                deleteDungeonWorld(worldName, success -> {
                    Map<String, String> resultPlaceholders = new HashMap<>();
                    resultPlaceholders.put("world", worldName);
                    if (success) {
                        DebugUtil.debug("world.unload.complete", resultPlaceholders);
                    } else {
                        DebugUtil.debug("world.unload.fail", resultPlaceholders);
                    }
                });
            }
        }
    }

    /**
     * 清理残留副本世界
     */
    public void cleanupRemnantWorlds() {
        // 确保在主线程中执行
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, this::cleanupRemnantWorlds);
            return;
        }

        DebugUtil.debug("world.cleanup.start");

        // 1. 清理已加载的副本世界
        cleanupLoadedDungeonWorlds();

        // 2. 清理未加载的副本世界文件夹
        cleanupUnloadedDungeonFolders();

        DebugUtil.debug("world.cleanup.complete");
    }

    /**
     * 清理已加载的副本世界
     */
    private void cleanupLoadedDungeonWorlds() {
        // 获取所有已加载的世界
        List<World> worlds = new ArrayList<>(Bukkit.getWorlds());

        // 遍历所有已加载的世界
        for (World world : worlds) {
            String worldName = world.getName();

            // 检查是否为副本世界
            if (worldName.startsWith(worldPrefix)) {
                Map<String, String> cleanupPlaceholders = new HashMap<>();
                cleanupPlaceholders.put("world", worldName);
                DebugUtil.debug("world.cleanup.found", cleanupPlaceholders);
                // 卸载并删除世界
                deleteDungeonWorld(worldName, null);
            }
        }
    }

    /**
     * 清理未加载的副本世界文件夹
     */
    private void cleanupUnloadedDungeonFolders() {
        // 确保在主线程中执行
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, this::cleanupUnloadedDungeonFolders);
            return;
        }

        // 检查服务器目录中的所有文件夹
        File worldContainer = Bukkit.getWorldContainer();
        File[] files = worldContainer.listFiles();
        if (files == null) {
            return;
        }

        DebugUtil.debug("world.cleanup.start-unloaded");
        int count = 0;

        for (File file : files) {
            // 检查是否为副本世界文件夹
            if (!file.isDirectory() || !file.getName().startsWith(worldPrefix)) {
                continue;
            }

            String worldName = file.getName();

            // 检查世界是否已加载
            if (Bukkit.getWorld(worldName) != null) {
                continue; // 已加载的世界由cleanupLoadedDungeonWorlds处理
            }

            Map<String, String> cleanupPlaceholders = new HashMap<>();
            cleanupPlaceholders.put("world", worldName);
            DebugUtil.debug("world.cleanup.found", cleanupPlaceholders);

            // 直接删除世界文件夹，不尝试加载
            deleteWorldFolder(worldName, file);
            count++;
        }

        Map<String, String> completePlaceholders = new HashMap<>();
        completePlaceholders.put("count", String.valueOf(count));
        DebugUtil.debug("world.cleanup.complete", completePlaceholders);
    }



    /**
     * 删除世界文件夹
     * @param worldName 世界名称
     * @param worldFolder 世界文件夹
     */
    public void deleteWorldFolder(String worldName, File worldFolder) {
        // 确保在主线程中执行
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> deleteWorldFolder(worldName, worldFolder));
            return;
        }

        // 检查是否已经在卸载中
        if (unloadTracker.contains(worldName)) {
            return;
        }

        // 添加到卸载跟踪器
        unloadTracker.add(worldName);

        // 检查文件夹是否存在
        if (!worldFolder.exists()) {
            Map<String, String> notExistPlaceholders = new HashMap<>();
            notExistPlaceholders.put("world", worldName);
            DebugUtil.debug("world.delete.not-exist", notExistPlaceholders);
            unloadTracker.remove(worldName);
            return;
        }

        // 检查是否有世界正在使用该文件夹
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            Map<String, String> inUsePlaceholders = new HashMap<>();
            inUsePlaceholders.put("world", worldName);
            DebugUtil.debug("world.delete.in-use", inUsePlaceholders);

            // 将所有玩家传送出世界
            teleportPlayersOutOfWorld(world);

            // 移除所有实体
            removeAllEntities(world);

            // 保存世界数据
            try {
                world.save();
            } catch (Exception e) {
            }

            // 延迟20tick后卸载世界
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    // 卸载世界
                    boolean unloaded = false;

                    // 首先尝试卸载所有区块
                    try {
                        // 确保所有区块都被保存和卸载
                        for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                            chunk.unload(true);
                        }
                    } catch (Exception e) {
                    }

                    // 再次尝试移除所有实体
                    removeAllEntities(world);

                    // 使用Bukkit API卸载世界
                    try {
                        unloaded = Bukkit.unloadWorld(world, false);
                    } catch (Exception e) {
                    }

                    // 最终检查世界是否已卸载
                    World finalCheckWorld = Bukkit.getWorld(worldName);
                    boolean finalUnloaded = finalCheckWorld == null;

                    Map<String, String> unloadResultPlaceholders = new HashMap<>();
                    unloadResultPlaceholders.put("world", worldName);
                    if (finalUnloaded) {
                        DebugUtil.debug("world.unload.success", unloadResultPlaceholders);
                    } else {
                        DebugUtil.debug("world.unload.fail", unloadResultPlaceholders);
                    }

                    // 再延迟20tick后删除世界文件
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        // 异步删除文件夹
                        BukkitFileUtil.deleteDirectoryAsync(worldFolder, success -> {
                            // 从卸载跟踪器中移除
                            unloadTracker.remove(worldName);

                            Map<String, String> deletePlaceholders = new HashMap<>();
                            deletePlaceholders.put("world", worldName);
                            if (success) {
                                DebugUtil.debug("world.delete.success", deletePlaceholders);
                            } else {
                                DebugUtil.debug("world.delete.fail", deletePlaceholders);
                            }
                        });
                    }, 20L);
            } finally {
                // 确保在异常情况下也能从卸载跟踪器中移除
                if (unloadTracker.contains(worldName)) {
                    unloadTracker.remove(worldName);
                }
            }
        }, 20L);
        } else {
            // 世界未加载，直接删除文件夹
            BukkitFileUtil.deleteDirectoryAsync(worldFolder, success -> {
                // 从卸载跟踪器中移除
                unloadTracker.remove(worldName);

                Map<String, String> deletePlaceholders = new HashMap<>();
                deletePlaceholders.put("world", worldName);
                if (success) {
                    DebugUtil.debug("world.delete.success", deletePlaceholders);
                } else {
                    DebugUtil.debug("world.delete.fail", deletePlaceholders);
                }
            });
        }
    }

    /**
     * 检查世界是否为副本世界
     * @param worldName 世界名称
     * @return 是否为副本世界
     */
    public boolean isDungeonWorld(String worldName) {
        return worldName != null && worldName.startsWith(worldPrefix);
    }

    /**
     * 检查世界是否正在卸载中
     * @param worldName 世界名称
     * @return 是否正在卸载
     */
    public boolean isWorldUnloading(String worldName) {
        return unloadTracker.contains(worldName);
    }

    /**
     * 从副本世界名称中提取副本ID
     * @param worldName 副本世界名称
     * @return 副本ID，如果不是副本世界则返回null
     */
    public String getDungeonIdFromWorldName(String worldName) {
        if (!isDungeonWorld(worldName)) {
            return null;
        }

        return worldName.substring(worldPrefix.length());
    }
}
