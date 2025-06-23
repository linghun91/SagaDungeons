package cn.i7mc.sagadungeons.util;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
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

        try {
            // 计算总文件数和大小
            FileCounter counter = countFilesAndSize(source);
            int totalFiles = counter.getFileCount();
            long totalSize = counter.getTotalSize();

            // 记录已复制的文件数和大小
            final int[] copiedFiles = {0};
            final long[] copiedSize = {0};

            // 使用Java 17的新特性进行文件复制
            // 首先创建所有目录结构
            try (var paths = Files.walk(source.toPath())) {
                paths.filter(Files::isDirectory)
                    .forEach(dir -> {
                        try {
                            Path targetDir = target.toPath().resolve(source.toPath().relativize(dir));
                            Files.createDirectories(targetDir);
                        } catch (IOException e) {
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("dir", dir.toString());
                            placeholders.put("message", e.getMessage());
                            DebugUtil.debug("file.copy.dir-create-fail", placeholders);
                        }
                    });
            }

            // 然后复制所有文件，忽略session.lock文件
            try (var paths = Files.walk(source.toPath())) {
                paths.filter(Files::isRegularFile)
                    .filter(file -> {
                        String fileName = file.getFileName().toString();
                        if (fileName.equals("session.lock")) {
                            // 记录跳过session.lock文件的日志
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("file", file.toString());
                            DebugUtil.debug("file.copy.skip-session-lock", placeholders);
                            return false;
                        }
                        if (fileName.equals("uid.dat")) {
                            // 记录跳过uid.dat文件的日志
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("file", file.toString());
                            DebugUtil.debug("file.copy.skip-uid-dat", placeholders);
                            return false;
                        }
                        return true;
                    }) // 忽略session.lock和uid.dat文件
                    .forEach(file -> {
                        try {
                            Path targetFile = target.toPath().resolve(source.toPath().relativize(file));
                            Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);

                            // 更新计数器
                            copiedFiles[0]++;
                            try {
                                long fileSize = Files.size(file);
                                copiedSize[0] += fileSize;

                                // 更新进度
                                if (progressCallback != null && totalFiles > 0) {
                                    // 使用文件大小作为权重计算进度，更准确
                                    double progress;
                                    if (totalSize > 0) {
                                        progress = (double) copiedSize[0] / totalSize;
                                    } else {
                                        progress = (double) copiedFiles[0] / totalFiles;
                                    }
                                    progressCallback.accept(progress);
                                }
                            } catch (IOException e) {
                                // 忽略无法获取大小的文件
                            }
                        } catch (IOException e) {
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("file", file.toString());
                            placeholders.put("message", e.getMessage());
                            DebugUtil.debug("file.copy.fail", placeholders);
                        }
                    });
            }

            return true;
        } catch (IOException e) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("message", e.getMessage());
            DebugUtil.debug("file.copy.dir-error", placeholders);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 文件计数器类
     * 用于统计文件数量和总大小
     */
    private static class FileCounter {
        private int fileCount = 0;
        private long totalSize = 0;

        public int getFileCount() {
            return fileCount;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public void incrementFileCount() {
            fileCount++;
        }

        public void addSize(long size) {
            totalSize += size;
        }
    }

    /**
     * 统计文件夹中的文件数量和总大小
     * @param directory 文件夹
     * @return 文件计数器
     */
    public static FileCounter countFilesAndSize(File directory) throws IOException {
        final FileCounter counter = new FileCounter();

        // 使用Java 8+的Files.walk方法和Stream API，忽略session.lock文件
        try (var paths = Files.walk(directory.toPath())) {
            paths.filter(Files::isRegularFile)
                .filter(path -> {
                    String fileName = path.getFileName().toString();
                    if (fileName.equals("session.lock")) {
                        // 记录跳过session.lock文件的日志
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("file", path.toString());
                        DebugUtil.debug("file.count.skip-session-lock", placeholders);
                        return false;
                    }
                    if (fileName.equals("uid.dat")) {
                        // 记录跳过uid.dat文件的日志
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("file", path.toString());
                        DebugUtil.debug("file.count.skip-uid-dat", placeholders);
                        return false;
                    }
                    return true;
                }) // 忽略session.lock和uid.dat文件
                .forEach(path -> {
                    counter.incrementFileCount();
                    try {
                        counter.addSize(Files.size(path));
                    } catch (IOException e) {
                        // 忽略无法获取大小的文件
                    }
                });
        }

        return counter;
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
            try {
                // 记录开始时间
                final long startTime = System.currentTimeMillis();

                // 执行复制
                boolean success = copyDirectory(source, target, progressCallback);

                // 计算耗时
                final long copyTime = System.currentTimeMillis() - startTime;
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("time", String.valueOf(copyTime));
                DebugUtil.debug("file.copy.async-complete", placeholders);

                // 在主线程中执行完成回调
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (completionCallback != null) {
                        completionCallback.accept(success);
                    }
                });
            } catch (Exception e) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("message", e.getMessage());
                DebugUtil.debug("file.copy.async-error", placeholders);
                e.printStackTrace();

                // 在主线程中执行完成回调，报告失败
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (completionCallback != null) {
                        completionCallback.accept(false);
                    }
                });
            }
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

        try {
            // 使用Java 16+的Files.walk方法和Stream API，倒序删除（先删文件后删目录）
            try (var paths = Files.walk(directory.toPath())) {
                paths.sorted((a, b) -> -a.compareTo(b)) // 倒序，确保先删除文件和子目录
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("path", path.toString());
                            placeholders.put("message", e.getMessage());
                            DebugUtil.debug("file.delete.fail", placeholders);
                        }
                    });
            }
            return true;
        } catch (IOException e) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("message", e.getMessage());
            DebugUtil.debug("file.delete.dir-error", placeholders);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 异步删除文件夹及其内容
     * @param directory 要删除的文件夹
     * @param completionCallback 完成回调
     */
    public static void deleteDirectoryAsync(File directory, Consumer<Boolean> completionCallback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // 记录开始时间
                final long startTime = System.currentTimeMillis();

                // 执行删除
                boolean success = deleteDirectory(directory);

                // 计算耗时
                final long deleteTime = System.currentTimeMillis() - startTime;
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("time", String.valueOf(deleteTime));
                DebugUtil.debug("file.delete.async-complete", placeholders);

                // 在主线程中执行完成回调
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (completionCallback != null) {
                        completionCallback.accept(success);
                    }
                });
            } catch (Exception e) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("message", e.getMessage());
                DebugUtil.debug("file.delete.async-error", placeholders);
                e.printStackTrace();

                // 在主线程中执行完成回调，报告失败
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (completionCallback != null) {
                        completionCallback.accept(false);
                    }
                });
            }
        });
    }

    /**
     * 计算文件夹中的文件数量
     * @param directory 文件夹
     * @return 文件数量
     */
    public static int countFiles(File directory) {
        try {
            // 使用Files.walk计算文件数量，忽略session.lock文件
            return (int) Files.walk(directory.toPath())
                    .filter(path -> Files.isRegularFile(path))
                    .filter(path -> {
                        String fileName = path.getFileName().toString();
                        if (fileName.equals("session.lock")) {
                            // 记录跳过session.lock文件的日志
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("file", path.toString());
                            DebugUtil.debug("file.count.skip-session-lock", placeholders);
                            return false;
                        }
                        if (fileName.equals("uid.dat")) {
                            // 记录跳过uid.dat文件的日志
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("file", path.toString());
                            DebugUtil.debug("file.count.skip-uid-dat", placeholders);
                            return false;
                        }
                        return true;
                    }) // 忽略session.lock和uid.dat文件
                    .count();
        } catch (IOException e) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("message", e.getMessage());
            DebugUtil.debug("file.count.error", placeholders);

            // 回退到旧方法
            int count = 0;
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        count += countFiles(file);
                    } else if (!file.getName().equals("session.lock") && !file.getName().equals("uid.dat")) { // 忽略session.lock和uid.dat文件
                        count++;
                    } else {
                        // 记录跳过特殊文件的日志
                        String fileName = file.getName();
                        Map<String, String> fileSkipPlaceholders = new HashMap<>();
                        fileSkipPlaceholders.put("file", file.getAbsolutePath());
                        if (fileName.equals("session.lock")) {
                            DebugUtil.debug("file.count.skip-session-lock", fileSkipPlaceholders);
                        } else if (fileName.equals("uid.dat")) {
                            DebugUtil.debug("file.count.skip-uid-dat", fileSkipPlaceholders);
                        }
                    }
                }
            }
            return count;
        }
    }


}
