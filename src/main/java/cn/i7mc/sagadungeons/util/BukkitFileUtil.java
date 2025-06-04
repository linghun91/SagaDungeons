package cn.i7mc.sagadungeons.util;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

/**
 * 文件操作工具类
 * 提供文件操作相关的工具方法
 */
public class BukkitFileUtil {

    private static final SagaDungeons plugin = SagaDungeons.getInstance();

    /**
     * 复制文件夹
     * @param source 源文件夹
     * @param target 目标文件夹
     * @param progressCallback 进度回调
     * @return 是否成功
     */
    public static boolean copyDirectory(File source, File target, Consumer<Double> progressCallback) {
        if (!source.exists() || !source.isDirectory()) {
            return false;
        }
        
        // 创建目标文件夹
        if (!target.exists()) {
            target.mkdirs();
        }
        
        // 获取源文件夹中的所有文件和子文件夹
        File[] files = source.listFiles();
        if (files == null) {
            return true;
        }
        
        // 计算总文件数
        int totalFiles = countFiles(source);
        int copiedFiles = 0;
        
        // 复制每个文件和子文件夹
        for (File file : files) {
            File destFile = new File(target, file.getName());
            
            if (file.isDirectory()) {
                // 递归复制子文件夹
                copyDirectory(file, destFile, null);
            } else {
                // 复制文件
                try {
                    Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    copiedFiles++;
                    
                    // 更新进度
                    if (progressCallback != null && totalFiles > 0) {
                        double progress = (double) copiedFiles / totalFiles;
                        progressCallback.accept(progress);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * 异步复制文件夹
     * @param source 源文件夹
     * @param target 目标文件夹
     * @param progressCallback 进度回调
     * @param completionCallback 完成回调
     */
    public static void copyDirectoryAsync(File source, File target, Consumer<Double> progressCallback, Consumer<Boolean> completionCallback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = copyDirectory(source, target, progressCallback);
            
            // 在主线程中执行完成回调
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (completionCallback != null) {
                    completionCallback.accept(success);
                }
            });
        });
    }

    /**
     * 删除文件夹及其内容
     * @param directory 要删除的文件夹
     * @return 是否成功
     */
    public static boolean deleteDirectory(File directory) {
        if (!directory.exists()) {
            return true;
        }
        
        // 删除所有文件和子文件夹
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        
        // 删除文件夹本身
        return directory.delete();
    }

    /**
     * 异步删除文件夹及其内容
     * @param directory 要删除的文件夹
     * @param completionCallback 完成回调
     */
    public static void deleteDirectoryAsync(File directory, Consumer<Boolean> completionCallback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = deleteDirectory(directory);
            
            // 在主线程中执行完成回调
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (completionCallback != null) {
                    completionCallback.accept(success);
                }
            });
        });
    }

    /**
     * 计算文件夹中的文件数量
     * @param directory 文件夹
     * @return 文件数量
     */
    public static int countFiles(File directory) {
        int count = 0;
        
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    count += countFiles(file);
                } else {
                    count++;
                }
            }
        }
        
        return count;
    }
}
