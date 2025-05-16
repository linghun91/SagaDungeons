package cn.i7mc.sagadungeons.dungeon.reward;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.util.ItemStackUtil;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 奖励管理器
 * 负责管理副本奖励
 */
public class RewardManager {

    private final SagaDungeons plugin;
    private final Map<String, List<DungeonReward>> dungeonRewards = new HashMap<>();

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public RewardManager(SagaDungeons plugin) {
        this.plugin = plugin;
    }

    /**
     * 加载副本奖励
     * @param template 副本模板
     * @param section 配置部分
     */
    public void loadRewards(DungeonTemplate template, ConfigurationSection section) {
        if (section == null) {
            return;
        }

        List<DungeonReward> rewards = new ArrayList<>();

        // 加载金币奖励
        if (section.contains("money")) {
            double money = section.getDouble("money");
            if (money > 0) {
                rewards.add(new MoneyReward(plugin, money));
            }
        }

        // 加载点券奖励
        if (section.contains("points")) {
            int points = section.getInt("points");
            if (points > 0) {
                rewards.add(new PointsReward(plugin, points));
            }
        }

        // 加载经验奖励
        if (section.contains("experience")) {
            int experience = section.getInt("experience");
            if (experience > 0) {
                rewards.add(new ExperienceReward(plugin, experience));
            }
        }

        // 加载物品奖励
        ConfigurationSection itemsSection = section.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection != null) {
                    // 检查是否是序列化物品
                    if (key.equals("serialized-item")) {
                        String serializedItem = itemSection.getString("serialized-item");
                        if (serializedItem != null && !serializedItem.isEmpty()) {
                            int amount = itemSection.getInt("amount", 1);
                            rewards.add(new SerializedItemReward(plugin, serializedItem, amount));
                        }
                        continue;
                    }

                    String materialName = itemSection.getString("material");
                    int amount = itemSection.getInt("amount", 1);
                    String name = itemSection.getString("name");
                    List<String> lore = itemSection.getStringList("lore");

                    try {
                        Material material = Material.valueOf(materialName.toUpperCase());
                        rewards.add(new ItemReward(plugin, material, amount, name, lore));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid material name: " + materialName);
                    }
                }
            }
        }

        // 加载命令奖励
        ConfigurationSection commandsSection = section.getConfigurationSection("commands");
        if (commandsSection != null) {
            for (String key : commandsSection.getKeys(false)) {
                String command = commandsSection.getString(key + ".command");
                String description = commandsSection.getString(key + ".description");

                if (command != null && !command.isEmpty()) {
                    rewards.add(new CommandReward(plugin, command, description));
                }
            }
        }

        // 保存奖励列表
        if (!rewards.isEmpty()) {
            dungeonRewards.put(template.getName(), rewards);
        }
    }

    /**
     * 获取副本奖励
     * @param templateName 模板名称
     * @return 奖励列表
     */
    public List<DungeonReward> getRewards(String templateName) {
        return dungeonRewards.getOrDefault(templateName, new ArrayList<>());
    }

    /**
     * 给予奖励
     * @param player 玩家
     * @param templateName 模板名称
     */
    public void giveRewards(Player player, String templateName) {
        // 获取奖励列表
        List<DungeonReward> rewards = getRewards(templateName);

        // 如果没有奖励，直接返回
        if (rewards.isEmpty()) {
            return;
        }

        // 发送奖励标题
        MessageUtil.sendMessage(player, "dungeon.reward.header");

        // 给予奖励
        for (DungeonReward reward : rewards) {
            // 给予奖励
            boolean success = reward.give(player);

            // 发送奖励消息
            if (success) {
                MessageUtil.sendMessage(player, "dungeon.reward.entry",
                        MessageUtil.createPlaceholders("description", reward.getDescription()));
            }
        }

        // 发送奖励页脚
        MessageUtil.sendMessage(player, "dungeon.reward.footer");
    }
}
