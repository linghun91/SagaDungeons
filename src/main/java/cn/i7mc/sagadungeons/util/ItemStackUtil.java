package cn.i7mc.sagadungeons.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 物品工具类
 * 提供ItemStack序列化和反序列化功能
 * 使用Paper API 1.20.1的NBT序列化方法
 */
public class ItemStackUtil {

    private static final Logger logger = Logger.getLogger("SagaDungeons");

    /**
     * 将ItemStack序列化为Base64字符串
     * 使用Paper API的serializeAsBytes()方法，更安全和高效
     * @param itemStack 物品堆
     * @return 序列化后的Base64字符串，如果失败则返回null
     */
    public static String serializeItemStack(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }

        try {
            // 使用Paper API的NBT序列化方法
            byte[] bytes = itemStack.serializeAsBytes();

            // 转换为Base64字符串
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "序列化物品时出错", e);
            return null;
        }
    }

    /**
     * 将Base64字符串反序列化为ItemStack
     * 使用Paper API的deserializeBytes()方法，更安全和高效
     * @param serializedItem 序列化后的Base64字符串
     * @return 反序列化后的物品堆，如果失败则返回null
     */
    public static ItemStack deserializeItemStack(String serializedItem) {
        if (serializedItem == null || serializedItem.isEmpty()) {
            return null;
        }

        try {
            // 解码Base64字符串
            byte[] bytes = Base64.getDecoder().decode(serializedItem);

            // 使用Paper API的NBT反序列化方法
            return ItemStack.deserializeBytes(bytes);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "反序列化物品时出错", e);
            return null;
        }
    }

    /**
     * 检查物品是否与序列化物品匹配
     * @param itemStack 物品堆
     * @param serializedItem 序列化后的Base64字符串
     * @return 是否匹配
     */
    public static boolean matchesSerializedItem(ItemStack itemStack, String serializedItem) {
        if (itemStack == null || serializedItem == null || serializedItem.isEmpty()) {
            return false;
        }

        // 反序列化物品
        ItemStack deserializedItem = deserializeItemStack(serializedItem);
        if (deserializedItem == null) {
            return false;
        }

        // 比较物品类型和元数据
        return itemStack.isSimilar(deserializedItem);
    }

    /**
     * 检查玩家是否有足够的序列化物品
     * @param itemStack 物品堆
     * @param serializedItem 序列化后的Base64字符串
     * @param amount 所需数量
     * @return 是否有足够的物品
     */
    public static boolean hasEnoughSerializedItem(ItemStack itemStack, String serializedItem, int amount) {
        if (itemStack == null || serializedItem == null || serializedItem.isEmpty()) {
            return false;
        }

        // 检查物品是否匹配
        if (!matchesSerializedItem(itemStack, serializedItem)) {
            return false;
        }

        // 检查数量是否足够
        return itemStack.getAmount() >= amount;
    }

    /**
     * 检查玩家背包中是否有足够的序列化物品
     * @param player 玩家
     * @param serializedItem 序列化后的Base64字符串
     * @param amount 所需数量
     * @return 是否有足够的物品
     */
    public static boolean hasEnoughSerializedItem(Player player, String serializedItem, int amount) {
        if (player == null || serializedItem == null || serializedItem.isEmpty()) {
            return false;
        }

        return hasEnoughSerializedItem(player.getInventory(), serializedItem, amount);
    }

    /**
     * 检查背包中是否有足够的序列化物品
     * @param inventory 背包
     * @param serializedItem 序列化后的Base64字符串
     * @param amount 所需数量
     * @return 是否有足够的物品
     */
    public static boolean hasEnoughSerializedItem(Inventory inventory, String serializedItem, int amount) {
        if (inventory == null || serializedItem == null || serializedItem.isEmpty()) {
            return false;
        }

        // 反序列化物品
        ItemStack deserializedItem = deserializeItemStack(serializedItem);
        if (deserializedItem == null) {
            return false;
        }

        // 计算背包中匹配物品的总数
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.isSimilar(deserializedItem)) {
                count += item.getAmount();
                if (count >= amount) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 从玩家背包中移除指定数量的序列化物品
     * @param player 玩家
     * @param serializedItem 序列化后的Base64字符串
     * @param amount 要移除的数量
     * @return 是否成功移除
     */
    public static boolean removeSerializedItem(Player player, String serializedItem, int amount) {
        if (player == null || serializedItem == null || serializedItem.isEmpty()) {
            return false;
        }

        return removeSerializedItem(player.getInventory(), serializedItem, amount);
    }

    /**
     * 从背包中移除指定数量的序列化物品
     * @param inventory 背包
     * @param serializedItem 序列化后的Base64字符串
     * @param amount 要移除的数量
     * @return 是否成功移除
     */
    public static boolean removeSerializedItem(Inventory inventory, String serializedItem, int amount) {
        if (inventory == null || serializedItem == null || serializedItem.isEmpty()) {
            return false;
        }

        // 检查是否有足够的物品
        if (!hasEnoughSerializedItem(inventory, serializedItem, amount)) {
            return false;
        }

        // 反序列化物品
        ItemStack deserializedItem = deserializeItemStack(serializedItem);
        if (deserializedItem == null) {
            return false;
        }

        // 移除物品
        int remaining = amount;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.isSimilar(deserializedItem)) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    inventory.setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                }

                if (remaining <= 0) {
                    break;
                }
            }
        }

        return true;
    }
}
