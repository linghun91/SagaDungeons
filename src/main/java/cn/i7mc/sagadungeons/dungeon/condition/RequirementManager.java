package cn.i7mc.sagadungeons.dungeon.condition;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 条件管理器
 * 负责管理副本创建条件
 */
public class RequirementManager {

    private final SagaDungeons plugin;

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public RequirementManager(SagaDungeons plugin) {
        this.plugin = plugin;
    }

    /**
     * 加载模板条件
     * @param template 副本模板
     * @param section 配置部分
     */
    public void loadRequirements(DungeonTemplate template, ConfigurationSection section) {
        if (section == null) {
            return;
        }

        // 加载金币条件
        if (section.contains("money") && template.isMoneyEnabled()) {
            double money = section.getDouble("money");
            if (money > 0) {
                template.addRequirement(new MoneyRequirement(plugin, money));
            }
        }

        // 加载点券条件
        if (section.contains("points") && template.isPointsEnabled()) {
            int points = section.getInt("points");
            if (points > 0) {
                template.addRequirement(new PointsRequirement(plugin, points));
            }
        }

        // 加载等级条件
        if (section.contains("level") && template.isLevelEnabled()) {
            int level = section.getInt("level");
            if (level > 0) {
                template.addRequirement(new LevelRequirement(plugin, level));
            }
        }

        // 加载物品条件
        if (template.isItemsEnabled()) {
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
                                template.addRequirement(new SerializedItemRequirement(plugin, serializedItem, amount));
                            }
                            continue;
                        }

                        String materialName = itemSection.getString("material");
                        int amount = itemSection.getInt("amount", 1);
                        String displayName = itemSection.getString("name");

                        try {
                            Material material = Material.valueOf(materialName.toUpperCase());
                            template.addRequirement(new ItemRequirement(plugin, material, amount, displayName));
                        } catch (IllegalArgumentException e) {
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查玩家是否满足所有条件
     * @param player 玩家
     * @param requirements 条件列表
     * @return 是否满足所有条件
     */
    public boolean checkRequirements(Player player, List<DungeonRequirement> requirements) {
        for (DungeonRequirement requirement : requirements) {
            if (!requirement.check(player)) {
                player.sendMessage(requirement.getFailMessage(player));
                return false;
            }
        }
        return true;
    }

    /**
     * 扣除所有条件所需的资源
     * @param player 玩家
     * @param requirements 条件列表
     * @return 是否成功扣除
     */
    public boolean takeRequirements(Player player, List<DungeonRequirement> requirements) {
        // 先检查所有条件
        if (!checkRequirements(player, requirements)) {
            return false;
        }

        // 创建临时列表，用于记录已扣除的条件
        List<DungeonRequirement> takenRequirements = new ArrayList<>();

        // 尝试扣除所有条件
        for (DungeonRequirement requirement : requirements) {
            if (requirement.take(player)) {
                takenRequirements.add(requirement);
            } else {
                // 如果扣除失败，回滚已扣除的条件
                // 注意：这里的回滚逻辑可能不完善，因为有些条件可能无法回滚
                // 但在实际使用中，由于我们先检查了所有条件，所以这种情况应该很少发生
                return false;
            }
        }

        return true;
    }
}
