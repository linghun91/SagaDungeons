package cn.i7mc.sagadungeons.dungeon.reward;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 物品奖励
 * 给予玩家物品
 */
public class ItemReward implements DungeonReward {

    private final SagaDungeons plugin;
    private final ItemStack item;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param material 物品材质
     * @param amount 物品数量
     */
    public ItemReward(SagaDungeons plugin, Material material, int amount) {
        this(plugin, material, amount, null, null);
    }

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param material 物品材质
     * @param amount 物品数量
     * @param name 物品名称
     * @param lore 物品描述
     */
    public ItemReward(SagaDungeons plugin, Material material, int amount, String name, List<String> lore) {
        this.plugin = plugin;
        this.item = new ItemStack(material, amount);
        
        // 设置物品元数据
        if (name != null || lore != null) {
            ItemMeta meta = item.getItemMeta();
            
            if (name != null) {
                meta.setDisplayName(name);
            }
            
            if (lore != null) {
                meta.setLore(lore);
            }
            
            item.setItemMeta(meta);
        }
    }

    @Override
    public boolean give(Player player) {
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
        String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? 
                item.getItemMeta().getDisplayName() : item.getType().name();
        
        return plugin.getConfigManager().getMessageManager().getMessage("dungeon.reward.item.description", 
                plugin.getConfigManager().getMessageManager().createPlaceholders("item", itemName, 
                        "amount", String.valueOf(item.getAmount())));
    }

    @Override
    public RewardType getType() {
        return RewardType.ITEM;
    }
}
