package cn.i7mc.sagadungeons.manager;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.util.BukkitFileUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

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

    public WorldManager(SagaDungeons plugin) {
        this.plugin = plugin;
        this.worldPrefix = plugin.getConfigManager().getWorldPrefix();
    }

    /**
     * 创建副本世界
     * @param templateName 模板名称
     * @param dungeonId 副本ID
     * @param progressCallback 进度回调
     * @param completionCallback 完成回调
     */
    public void createDungeonWorld(String templateName, String dungeonId, Consumer<Double> progressCallback, Consumer<Boolean> completionCallback) {
        // 获取模板
        DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(templateName);
        if (template == null) {
            if (completionCallback != null) {
                completionCallback.accept(false);
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
                completionCallback.accept(false);
            }
            return;
        }

        // 创建副本世界名称
        String worldName = worldPrefix + dungeonId;

        // 获取服务器世界目录
        // Bukkit.getWorldContainer()返回的是服务器主目录
        // 我们需要在服务器主目录下创建一个与世界名称相同的目录
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
            // 直接使用源目录
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
                    completionCallback.accept(false);
                }
                return;
            }
        }

        plugin.getLogger().info("复制世界文件从 " + sourceDir.getAbsolutePath() + " 到 " + worldsDir.getAbsolutePath());

        // 异步复制世界文件
        BukkitFileUtil.copyDirectoryAsync(sourceDir, worldsDir, progressCallback, success -> {
            if (!success) {
                if (completionCallback != null) {
                    completionCallback.accept(false);
                }
                return;
            }

            // 在主线程中加载世界
            Bukkit.getScheduler().runTask(plugin, () -> {
                // 创建并加载世界
                WorldCreator creator = new WorldCreator(worldName);
                World world = creator.createWorld();

                if (world != null) {
                    // 设置世界属性
                    world.setAutoSave(false);

                    // 调用完成回调
                    if (completionCallback != null) {
                        completionCallback.accept(true);
                    }
                } else {
                    // 加载失败
                    if (completionCallback != null) {
                        completionCallback.accept(false);
                    }
                }
            });
        });
    }

    /**
     * 卸载并删除副本世界
     * @param worldName 世界名称
     * @param completionCallback 完成回调
     */
    public void deleteDungeonWorld(String worldName, Consumer<Boolean> completionCallback) {
        // 获取世界
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            // 世界已经卸载，直接删除文件
            File worldDir = new File(Bukkit.getWorldContainer(), worldName);
            BukkitFileUtil.deleteDirectoryAsync(worldDir, completionCallback);
            return;
        }

        // 在主线程中卸载世界
        Bukkit.getScheduler().runTask(plugin, () -> {
            // 卸载世界
            boolean unloaded = Bukkit.unloadWorld(world, false);

            if (unloaded) {
                // 异步删除世界文件
                File worldDir = new File(Bukkit.getWorldContainer(), worldName);
                BukkitFileUtil.deleteDirectoryAsync(worldDir, completionCallback);
            } else {
                // 卸载失败
                if (completionCallback != null) {
                    completionCallback.accept(false);
                }
            }
        });
    }

    /**
     * 卸载所有副本世界
     */
    public void unloadAllDungeonWorlds() {
        // 获取所有世界
        List<World> worlds = Bukkit.getWorlds();

        // 遍历所有世界
        for (World world : worlds) {
            String worldName = world.getName();

            // 检查是否为副本世界
            if (worldName.startsWith(worldPrefix)) {
                // 卸载世界
                Bukkit.unloadWorld(world, false);
            }
        }
    }

    /**
     * 清理残留副本世界
     */
    public void cleanupRemnantWorlds() {
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

        // 检查服务器目录中的所有文件夹
        File worldContainer = Bukkit.getWorldContainer();
        File[] files = worldContainer.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && file.getName().startsWith(worldPrefix)) {
                    String worldName = file.getName();

                    // 检查世界是否已加载
                    if (Bukkit.getWorld(worldName) == null) {
                        plugin.getLogger().info("清理未加载的副本世界文件夹: " + worldName);
                        // 直接删除文件夹
                        BukkitFileUtil.deleteDirectoryAsync(file, success -> {
                            if (success) {
                                plugin.getLogger().info("成功删除副本世界文件夹: " + worldName);
                            } else {
                                plugin.getLogger().warning("删除副本世界文件夹失败: " + worldName);
                            }
                        });
                    }
                }
            }
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
