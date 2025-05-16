package cn.i7mc.sagadungeons.dungeon.condition;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * 物品条件
 * 检查玩家是否有指定物品
 */
public class ItemRequirement implements DungeonRequirement {

    private final Material material;
    private final int amount;
    private final SagaDungeons plugin;
    private final String displayName;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param material 物品材质
     * @param amount 物品数量
     */
    public ItemRequirement(SagaDungeons plugin, Material material, int amount) {
        this(plugin, material, amount, null);
    }

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param material 物品材质
     * @param amount 物品数量
     * @param displayName 物品显示名称
     */
    public ItemRequirement(SagaDungeons plugin, Material material, int amount, String displayName) {
        this.plugin = plugin;
        this.material = material;
        this.amount = amount;
        this.displayName = displayName;
    }

    /**
     * 获取物品材质
     * @return 物品材质
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * 获取物品数量
     * @return 物品数量
     */
    public int getAmount() {
        return amount;
    }

    /**
     * 获取物品显示名称
     * @return 物品显示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean check(Player player) {
        // 如果物品数量为0或负数，直接返回true
        if (amount <= 0) {
            return true;
        }
        
        // 获取玩家背包
        PlayerInventory inventory = player.getInventory();
        
        // 如果需要检查物品名称
        if (displayName != null && !displayName.isEmpty()) {
            int count = 0;
            
            // 遍历背包中的所有物品
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getType() == material && item.hasItemMeta() && item.getItemMeta().hasDisplayName() 
                        && item.getItemMeta().getDisplayName().equals(displayName)) {
                    count += item.getAmount();
                }
            }
            
            return count >= amount;
        } else {
            // 只检查物品类型和数量
            return inventory.contains(material, amount);
        }
    }

    @Override
    public String getFailMessage(Player player) {
        if (displayName != null && !displayName.isEmpty()) {
            return plugin.getConfigManager().getMessageManager().getMessage("dungeon.requirement.item.name.fail", 
                    plugin.getConfigManager().getMessageManager().createPlaceholders("amount", String.valueOf(amount), 
                            "item", material.name(), "name", displayName));
        } else {
            return plugin.getConfigManager().getMessageManager().getMessage("dungeon.requirement.item.fail", 
                    plugin.getConfigManager().getMessageManager().createPlaceholders("amount", String.valueOf(amount), 
                            "item", material.name()));
        }
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
        
        // 获取玩家背包
        PlayerInventory inventory = player.getInventory();
        
        // 如果需要检查物品名称
        if (displayName != null && !displayName.isEmpty()) {
            int remaining = amount;
            
            // 遍历背包中的所有物品
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getType() == material && item.hasItemMeta() && item.getItemMeta().hasDisplayName() 
                        && item.getItemMeta().getDisplayName().equals(displayName)) {
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
            
            return remaining <= 0;
        } else {
            // 只检查物品类型和数量
            inventory.removeItem(new ItemStack(material, amount));
            return true;
        }
    }

    @Override
    public RequirementType getType() {
        return RequirementType.ITEM;
    }
}
