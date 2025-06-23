package cn.i7mc.sagadungeons.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * 位置工具类
 * 提供位置处理相关的工具方法
 */
public class LocationUtil {

    /**
     * 将位置转换为字符串
     * @param location 位置
     * @return 位置字符串
     */
    public static String locationToString(Location location) {
        if (location == null) {
            return null;
        }

        World world = location.getWorld();
        if (world == null) {
            return null;
        }

        return world.getName() + "," +
                location.getX() + "," +
                location.getY() + "," +
                location.getZ() + "," +
                location.getYaw() + "," +
                location.getPitch();
    }

    /**
     * 将位置转换为字符串（不包含世界名）
     * 用于副本模板的重生点等不需要世界名的场景
     * @param location 位置
     * @return 位置字符串（不包含世界名）
     */
    public static String locationToStringWithoutWorld(Location location) {
        if (location == null) {
            return null;
        }

        return location.getX() + "," +
                location.getY() + "," +
                location.getZ() + "," +
                location.getYaw() + "," +
                location.getPitch();
    }

    /**
     * 将字符串转换为位置
     * @param locationString 位置字符串
     * @return 位置
     */
    public static Location stringToLocation(String locationString) {
        if (locationString == null || locationString.isEmpty()) {
            return null;
        }

        String[] parts = locationString.split(",");
        if (parts.length < 6) {
            return null;
        }

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            return null;
        }

        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);

            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 将不包含世界名的字符串转换为位置
     * @param locationString 位置字符串（不包含世界名）
     * @param world 世界
     * @return 位置
     */
    public static Location stringToLocationWithoutWorld(String locationString, World world) {
        if (locationString == null || locationString.isEmpty() || world == null) {
            return null;
        }

        String[] parts = locationString.split(",");
        if (parts.length < 5) {
            return null;
        }

        try {
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            float yaw = Float.parseFloat(parts[3]);
            float pitch = Float.parseFloat(parts[4]);

            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 查找安全的传送位置
     * @param location 原始位置
     * @return 安全的传送位置
     */
    public static Location findSafeLocation(Location location) {
        if (location == null) {
            return null;
        }

        World world = location.getWorld();
        if (world == null) {
            return null;
        }

        // 复制位置
        Location safeLocation = location.clone();

        // 检查当前位置是否安全
        if (isLocationSafe(safeLocation)) {
            return safeLocation;
        }

        // 向上查找安全位置
        for (int y = 0; y < 10; y++) {
            safeLocation.setY(location.getY() + y);
            if (isLocationSafe(safeLocation)) {
                return safeLocation;
            }
        }

        // 向下查找安全位置
        for (int y = 0; y < 10; y++) {
            safeLocation.setY(location.getY() - y);
            if (isLocationSafe(safeLocation)) {
                return safeLocation;
            }
        }

        // 找不到安全位置，返回世界出生点
        return world.getSpawnLocation();
    }

    /**
     * 检查位置是否安全
     * @param location 位置
     * @return 是否安全
     */
    public static boolean isLocationSafe(Location location) {
        if (location == null) {
            return false;
        }

        World world = location.getWorld();
        if (world == null) {
            return false;
        }

        Block feet = world.getBlockAt(location);
        Block head = world.getBlockAt(location.clone().add(0, 1, 0));
        Block ground = world.getBlockAt(location.clone().add(0, -1, 0));

        // 检查脚部和头部是否为空气
        return feet.getType().isAir() && head.getType().isAir() && ground.getType().isSolid();
    }
}
