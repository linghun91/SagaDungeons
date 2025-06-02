package cn.i7mc.sagadungeons.dungeon.reward;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.util.ItemStackUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 序列化物品奖励
 * 给予玩家序列化物品
 */
public class SerializedItemReward implements DungeonReward {

    private final SagaDungeons plugin;
    private final String serializedItem;
    private final int amount;
    private ItemStack cachedItem; // 缓存反序列化后的物品，提高性能

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param serializedItem 序列化物品字符串
     * @param amount 物品数量
     */
    public SerializedItemReward(SagaDungeons plugin, String serializedItem, int amount) {
        this.plugin = plugin;
        this.serializedItem = serializedItem;
        this.amount = amount;
        this.cachedItem = ItemStackUtil.deserializeItemStack(serializedItem);
        if (this.cachedItem != null) {
            this.cachedItem.setAmount(amount);
        }
    }

    /**
     * 获取序列化物品字符串
     * @return 序列化物品字符串
     */
    public String getSerializedItem() {
        return serializedItem;
    }

    /**
     * 获取物品数量
     * @return 物品数量
     */
    public int getAmount() {
        return amount;
    }

    /**
     * 获取反序列化后的物品
     * @return 反序列化后的物品
     */
    public ItemStack getItem() {
        if (cachedItem == null) {
            cachedItem = ItemStackUtil.deserializeItemStack(serializedItem);
            if (cachedItem != null) {
                cachedItem.setAmount(amount);
            }
        }
        return cachedItem != null ? cachedItem.clone() : null;
    }

    @Override
    public boolean give(Player player) {
        // 获取物品
        ItemStack item = getItem();
        if (item == null) {
            return false;
        }
        
        // 给予物品
        if (player.getInventory().firstEmpty() != -1) {
            // 如果背包有空位，直接添加
            player.getInventory().addItem(item.clone());
            return true;
        } else {
            // 如果背包满了，掉落在地上
            player.getWorld().dropItem(player.getLocation(), item.clone());
            return true;
        }
    }

    @Override
    public String getDescription() {
        ItemStack item = getItem();
        if (item == null) {
            return plugin.getConfigManager().getMessageManager().getMessage("dungeon.reward.item.description", 
                    plugin.getConfigManager().getMessageManager().createPlaceholders("item", "未知物品", 
                            "amount", String.valueOf(amount)));
        }
        
        String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? 
                item.getItemMeta().getDisplayName() : item.getType().name();
        
        return plugin.getConfigManager().getMessageManager().getMessage("dungeon.reward.item.description", 
                plugin.getConfigManager().getMessageManager().createPlaceholders("item", itemName, 
                        "amount", String.valueOf(amount)));
    }

    @Override
    public RewardType getType() {
        return RewardType.ITEM;
    }
}
