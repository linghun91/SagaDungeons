package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 模板奖励系统编辑界面
 * 用于编辑模板的奖励系统
 */
public class TemplateRewardsEditGUI extends AbstractGUI {

    private final String templateName;
    private final DungeonTemplate template;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     * @param templateName 模板名称
     */
    public TemplateRewardsEditGUI(SagaDungeons plugin, Player player, String templateName) {
        super(plugin, player, "&6奖励系统编辑 - " + templateName, 54);
        this.templateName = templateName;
        this.template = plugin.getConfigManager().getTemplateManager().getTemplates().get(templateName);
    }

    @Override
    public void init() {
        // 检查模板是否存在
        if (template == null) {
            MessageUtil.sendMessage(player, "command.admin.edit.template-not-found",
                    MessageUtil.createPlaceholders("template", templateName));
            close();
            return;
        }

        // 清空界面
        inventory.clear();

        // 添加装饰边框
        addBorder();

        // 添加奖励编辑选项
        addRewardOptions();

        // 添加功能按钮
        addFunctionButtons();
    }

    /**
     * 添加装饰边框
     */
    private void addBorder() {
        ItemStack borderItem = new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE)
                .setName("&7")
                .build();

        // 上下边框
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, borderItem);
            inventory.setItem(45 + i, borderItem);
        }

        // 左右边框
        for (int i = 1; i < 5; i++) {
            inventory.setItem(i * 9, borderItem);
            inventory.setItem(i * 9 + 8, borderItem);
        }
    }

    /**
     * 添加奖励编辑选项
     */
    private void addRewardOptions() {
        // 基础奖励区域 (第二行)
        addBasicRewards();

        // 高级奖励区域 (第三行)
        addAdvancedRewards();

        // 时间奖励区域 (第四行)
        addTimeRewards();
    }

    /**
     * 添加基础奖励选项
     */
    private void addBasicRewards() {
        // 金币奖励
        ItemStack moneyRewardItem = new ItemBuilder(Material.GOLD_NUGGET)
                .setName("&e金币奖励")
                .setLore(createBasicRewardLore("money", "金币"))
                .build();
        inventory.setItem(10, moneyRewardItem);

        // 点券奖励
        ItemStack pointsRewardItem = new ItemBuilder(Material.EMERALD)
                .setName("&e点券奖励")
                .setLore(createBasicRewardLore("points", "点券"))
                .build();
        inventory.setItem(12, pointsRewardItem);

        // 经验奖励
        ItemStack expRewardItem = new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setName("&e经验奖励")
                .setLore(createBasicRewardLore("experience", "经验"))
                .build();
        inventory.setItem(14, expRewardItem);
    }

    /**
     * 添加高级奖励选项
     */
    private void addAdvancedRewards() {
        // 物品奖励
        ItemStack itemRewardItem = new ItemBuilder(Material.CHEST)
                .setName("&e物品奖励")
                .setLore(
                        "&7管理副本完成后给予的物品奖励",
                        "&7当前物品数量: &f" + getItemRewardCount(),
                        "",
                        "&a左键点击管理物品奖励",
                        "&7添加、删除或编辑物品奖励"
                )
                .build();
        inventory.setItem(19, itemRewardItem);

        // 命令奖励
        ItemStack commandRewardItem = new ItemBuilder(Material.COMMAND_BLOCK)
                .setName("&e命令奖励")
                .setLore(
                        "&7管理副本完成后执行的命令奖励",
                        "&7当前命令数量: &f" + getCommandRewardCount(),
                        "",
                        "&a左键点击管理命令奖励",
                        "&7添加、删除或编辑命令奖励"
                )
                .build();
        inventory.setItem(21, commandRewardItem);
    }

    /**
     * 添加时间奖励选项
     */
    private void addTimeRewards() {
        // 时间奖励管理
        ItemStack timeRewardItem = new ItemBuilder(Material.CLOCK)
                .setName("&e时间奖励")
                .setLore(createTimeRewardLore())
                .build();
        inventory.setItem(31, timeRewardItem);
    }

    /**
     * 创建基础奖励说明
     */
    private List<String> createBasicRewardLore(String rewardType, String displayName) {
        List<String> lore = new ArrayList<>();
        lore.add("&7设置副本完成后给予的" + displayName + "奖励");
        
        // 获取当前奖励值
        String currentValue = getCurrentRewardValue(rewardType);
        lore.add("&7当前值: &f" + currentValue);
        lore.add("");
        lore.add("&a左键点击编辑" + displayName + "数量");
        lore.add("&7在聊天框输入新的" + displayName + "数量");
        lore.add("&c右键点击清除" + displayName + "奖励");
        
        return lore;
    }

    /**
     * 创建时间奖励说明
     */
    private List<String> createTimeRewardLore() {
        List<String> lore = new ArrayList<>();
        lore.add("&7管理基于完成时间的奖励系统");
        lore.add("&7根据玩家完成副本的时间给予不同奖励");
        lore.add("");
        
        if (template.hasTimeRewards()) {
            lore.add("&7当前时间奖励配置:");
            for (Map.Entry<Integer, List<String>> entry : template.getTimeRewards().entrySet()) {
                int timeSeconds = entry.getKey();
                int commandCount = entry.getValue().size();
                lore.add("&8- &f" + timeSeconds + "秒内: &e" + commandCount + "个命令");
            }
        } else {
            lore.add("&7当前无时间奖励配置");
        }
        
        lore.add("");
        lore.add("&a左键点击管理时间奖励");
        lore.add("&7添加、删除或编辑时间奖励配置");
        
        return lore;
    }

    /**
     * 获取当前奖励值
     */
    private String getCurrentRewardValue(String rewardType) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return "未配置";
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection rewardsSection = config.getConfigurationSection("rewards");

            if (rewardsSection == null) {
                return "未配置";
            }

            switch (rewardType) {
                case "money":
                    if (rewardsSection.contains("money")) {
                        double money = rewardsSection.getDouble("money", 0.0);
                        return money > 0 ? String.valueOf(money) : "未配置";
                    }
                    return "未配置";

                case "points":
                    if (rewardsSection.contains("points")) {
                        int points = rewardsSection.getInt("points", 0);
                        return points > 0 ? String.valueOf(points) : "未配置";
                    }
                    return "未配置";

                case "experience":
                    if (rewardsSection.contains("experience")) {
                        int experience = rewardsSection.getInt("experience", 0);
                        return experience > 0 ? String.valueOf(experience) : "未配置";
                    }
                    return "未配置";

                default:
                    return "未知";
            }
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的奖励配置时发生错误: " + e.getMessage());
            return "读取失败";
        }
    }

    /**
     * 获取物品奖励数量
     */
    private int getItemRewardCount() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return 0;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection rewardsSection = config.getConfigurationSection("rewards");

            if (rewardsSection == null) {
                return 0;
            }

            ConfigurationSection itemsSection = rewardsSection.getConfigurationSection("items");
            if (itemsSection != null) {
                Set<String> keys = itemsSection.getKeys(false);
                return keys.size();
            }

            return 0;
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的物品奖励数量时发生错误: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 获取命令奖励数量
     */
    private int getCommandRewardCount() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return 0;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection rewardsSection = config.getConfigurationSection("rewards");

            if (rewardsSection == null) {
                return 0;
            }

            if (rewardsSection.contains("commands")) {
                List<String> commands = rewardsSection.getStringList("commands");
                return commands.size();
            }

            return 0;
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的命令奖励数量时发生错误: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 添加功能按钮
     */
    private void addFunctionButtons() {
        // 保存按钮
        ItemStack saveButton = new ItemBuilder(Material.EMERALD)
                .setName("&a保存更改")
                .setLore(
                        "&7保存当前的所有更改",
                        "",
                        "&a左键点击保存"
                )
                .build();
        inventory.setItem(49, saveButton);

        // 返回按钮
        ItemStack backButton = new ItemBuilder(Material.ARROW)
                .setName("&c返回")
                .setLore("&7返回基础信息编辑界面")
                .build();
        inventory.setItem(45, backButton);

        // 刷新按钮
        ItemStack refreshButton = new ItemBuilder(Material.LIME_DYE)
                .setName("&a刷新")
                .setLore("&7刷新界面显示")
                .build();
        inventory.setItem(53, refreshButton);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        int slot = event.getSlot();

        // 处理功能按钮
        switch (slot) {
            case 49: // 保存
                handleSave();
                return;
            case 45: // 返回
                handleBack();
                return;
            case 53: // 刷新
                init();
                return;
            case 10: // 金币奖励
                handleMoneyReward(event);
                return;
            case 12: // 点券奖励
                handlePointsReward(event);
                return;
            case 14: // 经验奖励
                handleExperienceReward(event);
                return;
            case 19: // 物品奖励
                handleItemReward();
                return;
            case 21: // 命令奖励
                handleCommandReward();
                return;
            case 31: // 时间奖励
                handleTimeReward();
                return;
        }
    }

    /**
     * 处理保存
     */
    private void handleSave() {
        plugin.getConfigManager().getTemplateManager().saveTemplate(template);
        MessageUtil.sendMessage(player, "command.admin.edit.save-success",
                MessageUtil.createPlaceholders("template", templateName));
    }

    /**
     * 处理返回
     */
    private void handleBack() {
        close();
        plugin.getGUIManager().openTemplateBasicEditGUI(player, templateName);
    }

    /**
     * 处理金币奖励
     */
    private void handleMoneyReward(InventoryClickEvent event) {
        if (event.isLeftClick()) {
            close();
            plugin.getChatInputListener().requestDecimalInput(player, "command.admin.edit.input-money-reward", input -> {
                // 使用BukkitScheduler确保在主线程中执行
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    // 设置金币奖励
                    double moneyReward = Double.parseDouble(input);
                    // TODO: 设置金币奖励到模板配置
                    MessageUtil.sendMessage(player, "command.admin.edit.money-reward-updated",
                            MessageUtil.createPlaceholders("reward", input));
                    // 重新打开界面
                    plugin.getGUIManager().openTemplateRewardsEditGUI(player, templateName);
                });
            });
        } else if (event.isRightClick()) {
            // 清除金币奖励
            // TODO: 清除金币奖励配置
            MessageUtil.sendMessage(player, "command.admin.edit.money-reward-cleared");
            init(); // 刷新界面
        }
    }

    /**
     * 处理点券奖励
     */
    private void handlePointsReward(InventoryClickEvent event) {
        if (event.isLeftClick()) {
            close();
            plugin.getChatInputListener().requestNumberInput(player, "command.admin.edit.input-points-reward", input -> {
                // 使用BukkitScheduler确保在主线程中执行
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    // 设置点券奖励
                    int pointsReward = Integer.parseInt(input);
                    // TODO: 设置点券奖励到模板配置
                    MessageUtil.sendMessage(player, "command.admin.edit.points-reward-updated",
                            MessageUtil.createPlaceholders("reward", input));
                    // 重新打开界面
                    plugin.getGUIManager().openTemplateRewardsEditGUI(player, templateName);
                });
            });
        } else if (event.isRightClick()) {
            // 清除点券奖励
            // TODO: 清除点券奖励配置
            MessageUtil.sendMessage(player, "command.admin.edit.points-reward-cleared");
            init(); // 刷新界面
        }
    }

    /**
     * 处理经验奖励
     */
    private void handleExperienceReward(InventoryClickEvent event) {
        if (event.isLeftClick()) {
            close();
            plugin.getChatInputListener().requestNumberInput(player, "command.admin.edit.input-experience-reward", input -> {
                // 使用BukkitScheduler确保在主线程中执行
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    // 设置经验奖励
                    int experienceReward = Integer.parseInt(input);
                    // TODO: 设置经验奖励到模板配置
                    MessageUtil.sendMessage(player, "command.admin.edit.experience-reward-updated",
                            MessageUtil.createPlaceholders("reward", input));
                    // 重新打开界面
                    plugin.getGUIManager().openTemplateRewardsEditGUI(player, templateName);
                });
            });
        } else if (event.isRightClick()) {
            // 清除经验奖励
            // TODO: 清除经验奖励配置
            MessageUtil.sendMessage(player, "command.admin.edit.experience-reward-cleared");
            init(); // 刷新界面
        }
    }

    /**
     * 处理物品奖励
     */
    private void handleItemReward() {
        close();
        // 打开物品奖励管理界面
        plugin.getGUIManager().openItemRewardManageGUI(player, templateName);
    }

    /**
     * 处理命令奖励
     */
    private void handleCommandReward() {
        close();
        // 打开命令奖励管理界面
        plugin.getGUIManager().openCommandRewardManageGUI(player, templateName);
    }

    /**
     * 处理时间奖励
     */
    private void handleTimeReward() {
        close();
        // 打开时间奖励管理界面
        plugin.getGUIManager().openTimeRewardManageGUI(player, templateName);
    }
}
