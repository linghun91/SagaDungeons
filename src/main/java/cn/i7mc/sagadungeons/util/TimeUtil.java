package cn.i7mc.sagadungeons.util;

/**
 * 时间工具类
 * 提供时间处理相关的工具方法
 */
public class TimeUtil {

    /**
     * 格式化时间
     * @param seconds 秒数
     * @return 格式化后的时间字符串 (HH:mm:ss)
     */
    public static String formatTime(int seconds) {
        if (seconds < 0) {
            seconds = 0;
        }
        
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /**
     * 格式化时间（简短版）
     * @param seconds 秒数
     * @return 格式化后的时间字符串
     */
    public static String formatTimeShort(int seconds) {
        if (seconds < 0) {
            seconds = 0;
        }
        
        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            int secs = seconds % 60;
            return minutes + "分" + (secs > 0 ? secs + "秒" : "");
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            return hours + "小时" + (minutes > 0 ? minutes + "分" : "");
        }
    }

    /**
     * 获取当前时间戳（毫秒）
     * @return 当前时间戳
     */
    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前时间戳（秒）
     * @return 当前时间戳
     */
    public static long getCurrentTimeSeconds() {
        return System.currentTimeMillis() / 1000L;
    }

    /**
     * 计算剩余时间（秒）
     * @param expirationTime 过期时间（毫秒）
     * @return 剩余时间（秒）
     */
    public static int getRemainingSeconds(long expirationTime) {
        long remainingMillis = expirationTime - System.currentTimeMillis();
        return remainingMillis > 0 ? (int) (remainingMillis / 1000) : 0;
    }
}
