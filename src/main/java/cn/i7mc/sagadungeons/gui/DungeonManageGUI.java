package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import cn.i7mc.sagadungeons.util.MessageUtil;
import cn.i7mc.sagadungeons.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 副本管理界面
 * 用于管理副本
 */
public class DungeonManageGUI extends AbstractGUI {

    private final String dungeonId;
    private final DungeonInstance dungeon;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     * @param dungeonId 副本ID
     */
    public DungeonManageGUI(SagaDungeons plugin, Player player, String dungeonId) {
        super(plugin, player, "&6副本管理", 36);
        this.dungeonId = dungeonId;
        this.dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
    }

    /**
     * 初始化界面
     */
    @Override
    public void init() {
        // 检查副本是否存在
        if (dungeon == null) {
            close();
            MessageUtil.sendMessage(player, "dungeon.not-found");
            return;
        }
        
        // 填充边框
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build());
            inventory.setItem(27 + i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build());
        }
        
        for (int i = 0; i < 3; i++) {
            inventory.setItem(i * 9, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build());
            inventory.setItem(i * 9 + 8, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build());
        }
        
        // 添加副本信息
        inventory.setItem(4, createDungeonInfoItem());
        
        // 添加功能按钮
        inventory.setItem(11, new ItemBuilder(Material.ENDER_PEARL).setName("&a传送到副本").build());
        inventory.setItem(13, new ItemBuilder(Material.PLAYER_HEAD).setName("&a邀请玩家").build());
        
        // 添加公开/私有切换按钮
        if (dungeon.isPublic()) {
            inventory.setItem(15, new ItemBuilder(Material.REDSTONE_TORCH).setName("&c设为私有").build());
        } else {
            inventory.setItem(15, new ItemBuilder(Material.TORCH).setName("&a设为公开").build());
        }
        
        // 添加关闭按钮
        inventory.setItem(31, new ItemBuilder(Material.BARRIER).setName("&c关闭").build());
    }

    /**
     * 处理点击事件
     * @param event 点击事件
     */
    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        // 获取点击的物品
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || item.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            return;
        }
        
        // 获取点击的槽位
        int slot = event.getRawSlot();
        
        // 处理关闭按钮
        if (slot == 31) {
            close();
            return;
        }
        
        // 处理传送按钮
        if (slot == 11) {
            close();
            plugin.getDungeonManager().joinDungeon(player, dungeonId);
            return;
        }
        
        // 处理邀请按钮
        if (slot == 13) {
            close();
            plugin.getGUIManager().openPlayerInviteGUI(player, dungeonId);
            return;
        }
        
        // 处理公开/私有切换按钮
        if (slot == 15) {
            // 检查是否为副本创建者
            if (!player.getUniqueId().equals(dungeon.getOwnerUUID())) {
                close();
                MessageUtil.sendMessage(player, "dungeon.public.not-owner");
                return;
            }
            
            // 切换公开/私有状态
            dungeon.setPublic(!dungeon.isPublic());
            
            // 发送消息
            if (dungeon.isPublic()) {
                MessageUtil.sendMessage(player, "dungeon.public.enabled");
            } else {
                MessageUtil.sendMessage(player, "dungeon.public.disabled");
            }
            
            // 刷新界面
            close();
            plugin.getGUIManager().openDungeonManageGUI(player, dungeonId);
        }
    }

    /**
     * 创建副本信息物品
     * @return 物品
     */
    private ItemStack createDungeonInfoItem() {
        // 创建物品构建器
        ItemBuilder builder = new ItemBuilder(Material.BOOK);
        
        // 设置名称
        builder.setName(dungeon.getDisplayName());
        
        // 创建描述
        List<String> lore = new ArrayList<>();
        lore.add("&7副本ID: &e" + dungeon.getId());
        lore.add("&7创建者: &e" + Bukkit.getOfflinePlayer(dungeon.getOwnerUUID()).getName());
        lore.add("&7状态: &e" + dungeon.getState().name());
        lore.add("&7剩余时间: &e" + TimeUtil.formatTimeShort(dungeon.getRemainingTime()));
        lore.add("&7玩家数量: &e" + dungeon.getPlayerCount());
        lore.add("&7公开状态: &e" + (dungeon.isPublic() ? "公开" : "私有"));
        
        // 添加允许的玩家列表
        if (!dungeon.getAllowedPlayers().isEmpty()) {
            lore.add("");
            lore.add("&7允许的玩家:");
            for (UUID uuid : dungeon.getAllowedPlayers()) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                lore.add("&7- &e" + (offlinePlayer.getName() != null ? offlinePlayer.getName() : uuid.toString()));
            }
        }
        
        // 设置描述
        builder.setLore(lore);
        
        // 构建物品
        return builder.build();
    }
}
