package cn.i7mc.sagadungeons.dungeon.death;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.model.PlayerData;
import cn.i7mc.sagadungeons.util.ItemStackUtil;
import cn.i7mc.sagadungeons.util.LocationUtil;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 死亡管理器
 * 负责管理副本中的玩家死亡
 */
public class DeathManager {

    private final SagaDungeons plugin;
    // 按副本ID分组存储每个玩家的死亡次数: Map<副本ID, Map<玩家UUID, 死亡次数>>
    private final Map<String, Map<UUID, Integer>> dungeonDeathCounts = new HashMap<>();

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public DeathManager(SagaDungeons plugin) {
        this.plugin = plugin;
    }

    /**
     * 处理玩家死亡
     * @param player 玩家
     * @return 是否允许继续游戏
     */
    public boolean handleDeath(Player player) {
        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());

        // 检查玩家是否在副本中
        if (!playerData.isInDungeon()) {
            return true;
        }

        // 获取副本ID
        String dungeonId = playerData.getCurrentDungeonId();

        // 获取副本实例
        DungeonInstance instance = plugin.getDungeonManager().getDungeon(dungeonId);
        if (instance == null) {
            return true;
        }

        // 获取副本模板
        DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(instance.getTemplateName());
        if (template == null) {
            return true;
        }

        // 检查死亡限制设置
        int deathLimit = template.getDeathLimit();

        // 如果死亡限制为0，表示无死亡限制，直接在副本内重生
        if (deathLimit == 0) {
            // 传送到副本出生点
            respawnInDungeon(player, instance);
            return true;
        }

        // 有死亡限制的情况，增加死亡次数
        int deathCount = incrementDeathCount(dungeonId, player.getUniqueId());

        // 检查是否达到死亡次数限制
        if (deathCount >= deathLimit) {
            // 检查是否有复活道具
            if (template.hasReviveItem() && checkReviveItem(player, template)) {
                // 消耗复活道具
                consumeReviveItem(player, template);

                // 发送消息
                if (template.hasSerializedReviveItem()) {
                    // 获取物品名称
                    ItemStack item = ItemStackUtil.deserializeItemStack(template.getSerializedReviveItem());
                    String itemName = item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() ?
                            item.getItemMeta().getDisplayName() : (item != null ? item.getType().name() : "未知物品");

                    MessageUtil.sendMessage(player, "dungeon.death.revive.item",
                            MessageUtil.createPlaceholders("item", itemName));
                } else {
                    MessageUtil.sendMessage(player, "dungeon.death.revive.item",
                            MessageUtil.createPlaceholders("item", template.getReviveItemMaterial()));
                }

                // 传送到副本出生点
                respawnInDungeon(player, instance);

                return true;
            }

            // 发送消息
            MessageUtil.sendMessage(player, "dungeon.death.limit.reached",
                    MessageUtil.createPlaceholders("limit", String.valueOf(deathLimit)));

            // 踢出副本
            plugin.getDungeonManager().leaveDungeon(player);

            return false;
        } else {
            // 发送消息
            MessageUtil.sendMessage(player, "dungeon.death.count",
                    MessageUtil.createPlaceholders("count", String.valueOf(deathCount),
                            "limit", String.valueOf(deathLimit)));

            // 传送到副本出生点
            respawnInDungeon(player, instance);

            return true;
        }
    }

    /**
     * 增加玩家在指定副本中的死亡次数
     * @param dungeonId 副本ID
     * @param playerUUID 玩家UUID
     * @return 当前死亡次数
     */
    private int incrementDeathCount(String dungeonId, UUID playerUUID) {
        // 获取或创建该副本的死亡次数映射
        Map<UUID, Integer> dungeonCounts = dungeonDeathCounts.computeIfAbsent(dungeonId, k -> new HashMap<>());

        // 增加死亡次数
        int count = dungeonCounts.getOrDefault(playerUUID, 0) + 1;
        dungeonCounts.put(playerUUID, count);
        return count;
    }

    /**
     * 重置玩家在指定副本中的死亡次数
     * @param dungeonId 副本ID
     * @param playerUUID 玩家UUID
     */
    public void resetDeathCount(String dungeonId, UUID playerUUID) {
        Map<UUID, Integer> dungeonCounts = dungeonDeathCounts.get(dungeonId);
        if (dungeonCounts != null) {
            dungeonCounts.remove(playerUUID);
        }
    }

    /**
     * 获取玩家在指定副本中的死亡次数
     * @param dungeonId 副本ID
     * @param playerUUID 玩家UUID
     * @return 死亡次数
     */
    public int getDeathCount(String dungeonId, UUID playerUUID) {
        Map<UUID, Integer> dungeonCounts = dungeonDeathCounts.get(dungeonId);
        if (dungeonCounts == null) {
            return 0;
        }
        return dungeonCounts.getOrDefault(playerUUID, 0);
    }

    /**
     * 清理指定副本的所有死亡次数记录
     * @param dungeonId 副本ID
     */
    public void cleanupDungeonDeathCounts(String dungeonId) {
        dungeonDeathCounts.remove(dungeonId);
    }

    /**
     * 检查玩家是否有复活道具
     * @param player 玩家
     * @param template 副本模板
     * @return 是否有复活道具
     */
    private boolean checkReviveItem(Player player, DungeonTemplate template) {
        // 优先检查序列化复活道具
        if (template.hasSerializedReviveItem()) {
            return ItemStackUtil.hasEnoughSerializedItem(player, template.getSerializedReviveItem(), 1);
        }

        // 如果没有序列化复活道具，则检查传统复活道具
        // 获取复活道具材质
        String materialName = template.getReviveItemMaterial();
        if (materialName == null || materialName.isEmpty()) {
            return false;
        }

        // 解析材质
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return false;
        }

        // 获取复活道具名称
        String itemName = template.getReviveItemName();

        // 检查玩家背包中是否有复活道具
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                // 如果需要检查名称
                if (itemName != null && !itemName.isEmpty()) {
                    if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                            item.getItemMeta().getDisplayName().equals(itemName)) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 消耗复活道具
     * @param player 玩家
     * @param template 副本模板
     */
    private void consumeReviveItem(Player player, DungeonTemplate template) {
        // 优先检查序列化复活道具
        if (template.hasSerializedReviveItem()) {
            ItemStackUtil.removeSerializedItem(player, template.getSerializedReviveItem(), 1);
            return;
        }

        // 如果没有序列化复活道具，则检查传统复活道具
        // 获取复活道具材质
        String materialName = template.getReviveItemMaterial();
        if (materialName == null || materialName.isEmpty()) {
            return;
        }

        // 解析材质
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return;
        }

        // 获取复活道具名称
        String itemName = template.getReviveItemName();

        // 遍历玩家背包
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == material) {
                // 如果需要检查名称
                if (itemName != null && !itemName.isEmpty()) {
                    if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                            item.getItemMeta().getDisplayName().equals(itemName)) {
                        // 减少数量
                        if (item.getAmount() > 1) {
                            item.setAmount(item.getAmount() - 1);
                        } else {
                            player.getInventory().setItem(i, null);
                        }
                        return;
                    }
                } else {
                    // 减少数量
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        player.getInventory().setItem(i, null);
                    }
                    return;
                }
            }
        }
    }

    /**
     * 在副本中重生
     * @param player 玩家
     * @param instance 副本实例
     */
    private void respawnInDungeon(Player player, DungeonInstance instance) {
        // 获取副本世界
        World world = instance.getWorld();
        if (world == null) {
            return;
        }

        // 获取副本模板
        DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(instance.getTemplateName());

        // 获取重生点
        final Location spawnLocation;

        // 检查模板是否有指定重生点
        if (template != null && template.hasSpawnLocation()) {
            // 使用模板中的重生点（不包含世界名）
            Location tempLocation = LocationUtil.stringToLocationWithoutWorld(template.getSpawnLocation(), world);

            // 如果重生点不可用，使用世界默认出生点
            if (tempLocation == null) {
                spawnLocation = world.getSpawnLocation();
            } else {
                spawnLocation = tempLocation;
            }
        } else {
            // 使用世界默认出生点
            spawnLocation = world.getSpawnLocation();
        }

        // 传送玩家
        Bukkit.getScheduler().runTask(plugin, () -> player.teleport(spawnLocation));
    }
}
