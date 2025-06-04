package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.hook.MythicMobsHook;
import cn.i7mc.sagadungeons.model.PlayerData;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 怪物刷怪点编辑界面
 * 用于编辑怪物刷怪点
 */
public class MobSpawnerGUI extends AbstractGUI {

    private final MythicMobsHook mythicMobsHook;
    private final List<String> mobTypes = new ArrayList<>();
    private int page = 0;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     */
    public MobSpawnerGUI(SagaDungeons plugin, Player player) {
        super(plugin, player, "&6怪物刷怪点编辑", 54);
        this.mythicMobsHook = plugin.getHookManager().getMythicMobsHook();

        // 检查MythicMobs是否可用
        if (mythicMobsHook == null) {
            close();
            MessageUtil.sendMessage(player, "hook.mythicmobs.not-available");
            return;
        }

        // 获取所有怪物类型
        mobTypes.addAll(mythicMobsHook.getMobTypes());
    }

    /**
     * 初始化界面
     */
    @Override
    public void init() {
        // 检查玩家是否在副本中
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());
        if (!playerData.isInDungeon()) {
            close();
            MessageUtil.sendMessage(player, "dungeon.spawner.not-in-dungeon");
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

        // 添加怪物类型
        int startIndex = page * 28;
        int endIndex = Math.min(startIndex + 28, mobTypes.size());

        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            String mobType = mobTypes.get(i);

            // 创建物品
            ItemStack item = new ItemBuilder(Material.ZOMBIE_SPAWN_EGG)
                    .setName("&e" + mobType)
                    .setLore("&7点击在当前位置创建刷怪点")
                    .build();

            // 添加到界面
            inventory.setItem(slot, item);

            // 更新槽位
            slot++;
            if (slot % 9 == 8) {
                slot += 2;
            }
        }

        // 添加翻页按钮
        if (page > 0) {
            inventory.setItem(48, new ItemBuilder(Material.ARROW).setName("&a上一页").build());
        }

        if (endIndex < mobTypes.size()) {
            inventory.setItem(50, new ItemBuilder(Material.ARROW).setName("&a下一页").build());
        }

        // 添加关闭按钮
        inventory.setItem(49, new ItemBuilder(Material.BARRIER).setName("&c关闭").build());
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
        if (slot == 49) {
            close();
            return;
        }

        // 处理上一页按钮
        if (slot == 48 && page > 0) {
            page--;
            init();
            return;
        }

        // 处理下一页按钮
        if (slot == 50 && (page + 1) * 28 < mobTypes.size()) {
            page++;
            init();
            return;
        }

        // 处理怪物类型
        if (slot >= 10 && slot < 45 && slot % 9 != 0 && slot % 9 != 8) {
            // 获取物品元数据
            if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
                return;
            }

            // 获取怪物类型
            String displayName = item.getItemMeta().getDisplayName();
            String mobType = MessageUtil.colorize(displayName).substring(2);

            // 关闭界面
            close();

            // 创建刷怪点
            createSpawner(mobType);
        }
    }

    /**
     * 创建刷怪点
     * @param mobType 怪物类型
     */
    private void createSpawner(String mobType) {
        // 检查玩家是否在副本中
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());
        if (!playerData.isInDungeon()) {
            MessageUtil.sendMessage(player, "dungeon.spawner.not-in-dungeon");
            return;
        }

        // 获取副本ID
        String dungeonId = playerData.getCurrentDungeonId();

        // 获取副本实例
        if (plugin.getDungeonManager().getDungeon(dungeonId) == null) {
            MessageUtil.sendMessage(player, "dungeon.not-found");
            return;
        }

        // 检查是否为副本创建者
        if (!player.getUniqueId().equals(plugin.getDungeonManager().getDungeon(dungeonId).getOwnerUUID())) {
            MessageUtil.sendMessage(player, "dungeon.spawner.not-owner");
            return;
        }

        // 获取玩家位置
        Location location = player.getLocation();

        // 创建刷怪点名称
        String spawnerName = "sd_" + dungeonId + "_" + System.currentTimeMillis();

        // 创建刷怪点
        boolean success = mythicMobsHook.createSpawner(spawnerName, location, mobType);

        // 设置刷怪点属性
        if (success) {
            mythicMobsHook.setupSpawner(spawnerName, 30, 1);
        } else {
            MessageUtil.sendMessage(player, "dungeon.spawner.set-fail");
            return;
        }

        // 发送消息
        MessageUtil.sendMessage(player, "dungeon.spawner.set-success");
    }
}
