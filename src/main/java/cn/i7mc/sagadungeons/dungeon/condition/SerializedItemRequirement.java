package cn.i7mc.sagadungeons.dungeon.condition;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.util.ItemStackUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 序列化物品条件
 * 检查玩家是否有指定的序列化物品
 */
public class SerializedItemRequirement implements DungeonRequirement {

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
    public SerializedItemRequirement(SagaDungeons plugin, String serializedItem, int amount) {
        this.plugin = plugin;
        this.serializedItem = serializedItem;
        this.amount = amount;
        this.cachedItem = ItemStackUtil.deserializeItemStack(serializedItem);
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
        }
        return cachedItem != null ? cachedItem.clone() : null;
    }

    @Override
    public boolean check(Player player) {
        // 如果物品数量为0或负数，直接返回true
        if (amount <= 0) {
            return true;
        }
        
        // 检查玩家是否有足够的物品
        return ItemStackUtil.hasEnoughSerializedItem(player, serializedItem, amount);
    }

    @Override
    public String getFailMessage(Player player) {
        ItemStack item = getItem();
        if (item == null) {
            return plugin.getConfigManager().getMessageManager().getMessage("dungeon.requirement.item.fail", 
                    plugin.getConfigManager().getMessageManager().createPlaceholders("amount", String.valueOf(amount), 
                            "item", "未知物品"));
        }
        
        String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? 
                item.getItemMeta().getDisplayName() : item.getType().name();
        
        return plugin.getConfigManager().getMessageManager().getMessage("dungeon.requirement.item.name.fail", 
                plugin.getConfigManager().getMessageManager().createPlaceholders("amount", String.valueOf(amount), 
                        "item", item.getType().name(), "name", itemName));
    }

    @Override
    public boolean take(Player player) {
        // 如果物品数量为0或负数，直接返回true
        if (amount <= 0) {
            return true;
        }
        
        // 检查玩家是否有足够的物品
        if (!check(player)) {
            return false;
        }
        
        // 移除物品
        return ItemStackUtil.removeSerializedItem(player, serializedItem, amount);
    }

    @Override
    public RequirementType getType() {
        return RequirementType.ITEM;
    }
}
