package cn.i7mc.sagadungeons.manager;

import cn.i7mc.sagadungeons.SagaDungeons;
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
        // 获取模板目录
        File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
        if (!templateDir.exists() || !templateDir.isDirectory()) {
            if (completionCallback != null) {
                completionCallback.accept(false);
            }
            return;
        }

        // 创建副本世界名称
        String worldName = worldPrefix + dungeonId;

        // 获取服务器世界目录
        File worldsDir = new File(Bukkit.getWorldContainer(), worldName);

        // 异步复制世界文件
        BukkitFileUtil.copyDirectoryAsync(templateDir, worldsDir, progressCallback, success -> {
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
        // 获取所有世界
        List<World> worlds = new ArrayList<>(Bukkit.getWorlds());

        // 遍历所有世界
        for (World world : worlds) {
            String worldName = world.getName();

            // 检查是否为副本世界
            if (worldName.startsWith(worldPrefix)) {
                // 卸载并删除世界
                deleteDungeonWorld(worldName, null);
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
