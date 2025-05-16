package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import cn.i7mc.sagadungeons.model.DungeonInvitation;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 玩家邀请界面
 * 用于邀请玩家加入副本
 */
public class PlayerInviteGUI extends AbstractGUI {

    private final String dungeonId;
    private final DungeonInstance dungeon;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     * @param dungeonId 副本ID
     */
    public PlayerInviteGUI(SagaDungeons plugin, Player player, String dungeonId) {
        super(plugin, player, "&6邀请玩家", 54);
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
            inventory.setItem(45 + i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build());
        }
        
        for (int i = 0; i < 5; i++) {
            inventory.setItem(i * 9, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build());
            inventory.setItem(i * 9 + 8, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build());
        }
        
        // 添加在线玩家
        int slot = 10;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            // 跳过自己和已经在副本中的玩家
            if (onlinePlayer.equals(player) || dungeon.getWorld().getPlayers().contains(onlinePlayer)) {
                continue;
            }
            
            // 创建玩家头颅
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(onlinePlayer);
            meta.setDisplayName(MessageUtil.colorize("&e" + onlinePlayer.getName()));
            
            // 创建描述
            List<String> lore = new ArrayList<>();
            lore.add("&7点击邀请该玩家");
            
            // 检查是否已经被邀请
            if (dungeon.isAllowed(onlinePlayer.getUniqueId())) {
                lore.add("&a已被邀请");
            }
            
            meta.setLore(MessageUtil.colorize(lore));
            skull.setItemMeta(meta);
            
            // 添加到界面
            inventory.setItem(slot, skull);
            
            // 更新槽位
            slot++;
            if (slot % 9 == 8) {
                slot += 2;
            }
            
            // 检查是否超出界面
            if (slot >= 45) {
                break;
            }
        }
        
        // 添加返回按钮
        inventory.setItem(49, new ItemBuilder(Material.ARROW).setName("&a返回").build());
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
        
        // 处理返回按钮
        if (slot == 49) {
            close();
            plugin.getGUIManager().openDungeonManageGUI(player, dungeonId);
            return;
        }
        
        // 处理玩家头颅
        if (item.getType() == Material.PLAYER_HEAD && item.hasItemMeta() && item.getItemMeta() instanceof SkullMeta) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta.getOwningPlayer() != null) {
                // 获取目标玩家
                Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
                if (target != null && target.isOnline()) {
                    // 邀请玩家
                    invitePlayer(target);
                }
            }
        }
    }

    /**
     * 邀请玩家
     * @param target 目标玩家
     */
    private void invitePlayer(Player target) {
        // 检查是否为副本创建者
        if (!player.getUniqueId().equals(dungeon.getOwnerUUID())) {
            close();
            MessageUtil.sendMessage(player, "dungeon.invite.not-owner");
            return;
        }
        
        // 添加到允许列表
        dungeon.addAllowedPlayer(target.getUniqueId());
        
        // 发送邀请消息
        MessageUtil.sendMessage(player, "dungeon.invite.sent", 
                MessageUtil.createPlaceholders("player", target.getName()));
        
        MessageUtil.sendMessage(target, "dungeon.invite.received", 
                MessageUtil.createPlaceholders("player", player.getName()));
        
        // 刷新界面
        close();
        plugin.getGUIManager().openPlayerInviteGUI(player, dungeonId);
    }
}
