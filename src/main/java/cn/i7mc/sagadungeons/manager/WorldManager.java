package cn.i7mc.sagadungeons.manager;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.model.PlayerData;
import cn.i7mc.sagadungeons.util.BukkitFileUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 世界管理器
 * 负责管理副本世界的创建、复制、卸载和删除
 */
public class WorldManager {

    private final SagaDungeons plugin;
    private final String worldPrefix;
    private boolean createLock = false; // 创建锁，防止并发创建副本

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
                    // 使用指定的世界路径
                    templateDir = new File(plugin.getDataFolder(), template.getWorldPath());
                } else {
                    // 使用默认模板目录
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
                    // 如果源目录包含region文件夹，说明源目录就是世界目录
                    sourceDir = templateDir;
                } else {
                    // 如果源目录不包含region文件夹，可能是因为region文件夹在源目录的world子目录中
                    File worldSubDir = new File(templateDir, "world");
                    File worldRegionDir = new File(worldSubDir, "region");

                    if (worldSubDir.exists() && worldSubDir.isDirectory() && worldRegionDir.exists() && worldRegionDir.isDirectory()) {
                        // 使用world子目录作为源目录
                        sourceDir = worldSubDir;
                    } else {
                        // 找不到有效的世界目录
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

                plugin.getLogger().info("复制世界文件从 " + sourceDir.getAbsolutePath() + " 到 " + worldsDir.getAbsolutePath());

                // 直接在当前异步线程中复制文件，避免嵌套异步任务
                final long startTime = System.currentTimeMillis();
                boolean success = BukkitFileUtil.copyDirectory(sourceDir, worldsDir, progressCallback);
                final long copyTime = System.currentTimeMillis() - startTime;

                if (!success) {
                    plugin.getLogger().warning("复制世界文件失败");
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

                plugin.getLogger().info("世界文件复制完成，耗时: " + copyTime + "ms");

                // 在主线程中加载世界
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        final long loadStartTime = System.currentTimeMillis();

                        // 创建并加载世界
                        WorldCreator creator = new WorldCreator(worldName);
                        // 设置世界类型为FLAT，减少生成开销
                        creator.generateStructures(false);
                        // 设置环境类型
                        creator.environment(World.Environment.NORMAL);
                        // 设置不保持出生点加载
                        creator.keepSpawnLoaded(net.kyori.adventure.util.TriState.FALSE);

                        World world = creator.createWorld();

                        if (world != null) {
                            // 设置世界属性
                            world.setAutoSave(false);
                            world.setKeepSpawnInMemory(false);

                            // 设置游戏规则 - 使用最新的GameRule API
                            world.setGameRule(GameRule.KEEP_INVENTORY, true);
                            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                            world.setGameRule(GameRule.DO_FIRE_TICK, false);
                            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
                            world.setGameRule(GameRule.DO_ENTITY_DROPS, false);

                            final long loadTime = System.currentTimeMillis() - loadStartTime;
                            plugin.getLogger().info("世界加载完成，耗时: " + loadTime + "ms");

                            // 调用完成回调
                            if (completionCallback != null) {
                                completionCallback.accept(true);
                            }
                        } else {
                            // 加载失败
                            plugin.getLogger().warning("世界加载失败: " + worldName);
                            if (completionCallback != null) {
                                completionCallback.accept(false);
                            }
                        }
                    } finally {
                        // 释放创建锁
                        createLock = false;
                    }
                });
            } catch (Exception e) {
                plugin.getLogger().severe("创建副本世界时发生错误: " + e.getMessage());
                e.printStackTrace();

                // 释放创建锁
                Bukkit.getScheduler().runTask(plugin, () -> createLock = false);

                // 调用完成回调
                if (completionCallback != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> completionCallback.accept(false));
                }
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

        // 获取世界
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            // 世界已经卸载，直接删除文件
            deleteWorldFolder(worldName, completionCallback);
            return;
        }

        plugin.getLogger().info("正在卸载副本世界: " + worldName);

        // 1. 将所有玩家传送出世界
        teleportPlayersOutOfWorld(world);

        // 2. 移除所有实体
        removeAllEntities(world);

        // 3. 直接删除文件夹（即使卸载失败，删除文件夹也能成功）
        File worldDir = new File(Bukkit.getWorldContainer(), worldName);
        BukkitFileUtil.deleteDirectoryAsync(worldDir, success -> {
            if (completionCallback != null) {
                completionCallback.accept(success);
            }

            if (success) {
                plugin.getLogger().info("成功删除副本世界文件夹: " + worldName);
            } else {
                plugin.getLogger().warning("删除副本世界文件夹失败: " + worldName);
            }
        });
    }

    /**
     * 删除世界文件夹
     * @param worldName 世界名称
     * @param completionCallback 完成回调
     */
    private void deleteWorldFolder(String worldName, Consumer<Boolean> completionCallback) {
        File worldDir = new File(Bukkit.getWorldContainer(), worldName);

        // 检查文件夹是否存在
        if (!worldDir.exists()) {
            plugin.getLogger().info("副本世界文件夹不存在，无需删除: " + worldName);
            if (completionCallback != null) {
                completionCallback.accept(true);
            }
            return;
        }

        // 异步删除文件夹
        BukkitFileUtil.deleteDirectoryAsync(worldDir, success -> {
            if (completionCallback != null) {
                completionCallback.accept(success);
            }

            if (success) {
                plugin.getLogger().info("成功删除副本世界文件夹: " + worldName);
            } else {
                plugin.getLogger().warning("删除副本世界文件夹失败: " + worldName);
            }
        });
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
                plugin.getLogger().info("正在卸载副本世界: " + worldName);

                // 使用deleteDungeonWorld方法卸载和删除世界
                deleteDungeonWorld(worldName, success -> {
                    if (success) {
                        plugin.getLogger().info("成功卸载和删除副本世界: " + worldName);
                    } else {
                        plugin.getLogger().severe("卸载和删除副本世界失败: " + worldName);
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

        plugin.getLogger().info("开始清理残留副本世界...");

        // 1. 清理已加载的副本世界
        cleanupLoadedDungeonWorlds();

        // 2. 清理未加载的副本世界文件夹
        cleanupUnloadedDungeonFolders();

        plugin.getLogger().info("残留副本世界清理完成");
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
                plugin.getLogger().info("清理已加载的副本世界: " + worldName);
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

        plugin.getLogger().info("开始清理未加载的副本世界文件夹...");
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

            plugin.getLogger().info("清理未加载的副本世界文件夹: " + worldName);

            // 直接删除世界文件夹，不尝试加载
            deleteWorldFolder(worldName, file);
            count++;
        }

        plugin.getLogger().info("共清理 " + count + " 个未加载的副本世界文件夹");
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

        // 检查文件夹是否存在
        if (!worldFolder.exists()) {
            plugin.getLogger().info("副本世界文件夹不存在，无需删除: " + worldName);
            return;
        }

        // 检查是否有世界正在使用该文件夹
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            plugin.getLogger().warning("世界 " + worldName + " 仍在使用中，尝试先传送玩家出去");
            // 将所有玩家传送出世界
            teleportPlayersOutOfWorld(world);
            // 移除所有实体
            removeAllEntities(world);
        }

        // 异步删除文件夹
        BukkitFileUtil.deleteDirectoryAsync(worldFolder, success -> {
            if (success) {
                plugin.getLogger().info("成功删除副本世界文件夹: " + worldName);
            } else {
                plugin.getLogger().warning("删除副本世界文件夹失败: " + worldName);
            }
        });
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
