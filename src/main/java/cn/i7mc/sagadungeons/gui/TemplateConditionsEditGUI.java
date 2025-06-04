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

/**
 * 模板创建条件编辑界面
 * 用于编辑模板的创建条件
 */
public class TemplateConditionsEditGUI extends AbstractGUI {

    private final String templateName;
    private final DungeonTemplate template;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     * @param templateName 模板名称
     */
    public TemplateConditionsEditGUI(SagaDungeons plugin, Player player, String templateName) {
        super(plugin, player, "&6创建条件编辑 - " + templateName, 54);
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

        // 添加条件编辑选项
        addConditionOptions();

        // 添加功能按钮
        addFunctionButtons();
    }

    /**
     * 添加装饰边框
     */
    private void addBorder() {
        ItemStack borderItem = new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE)
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
     * 添加条件编辑选项
     */
    private void addConditionOptions() {
        // 金币条件
        ItemStack moneyItem = new ItemBuilder(Material.GOLD_NUGGET)
                .setName("&e金币条件")
                .setLore(createMoneyLore())
                .build();
        inventory.setItem(10, moneyItem);

        // 点券条件
        ItemStack pointsItem = new ItemBuilder(Material.EMERALD)
                .setName("&e点券条件")
                .setLore(createPointsLore())
                .build();
        inventory.setItem(12, pointsItem);

        // 等级条件
        ItemStack levelItem = new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setName("&e等级条件")
                .setLore(createLevelLore())
                .build();
        inventory.setItem(14, levelItem);

        // 物品条件
        ItemStack itemsItem = new ItemBuilder(Material.CHEST)
                .setName("&e物品条件")
                .setLore(createItemsLore())
                .build();
        inventory.setItem(16, itemsItem);

        // 金币开关
        ItemStack moneyToggleItem = new ItemBuilder(
                template.isMoneyEnabled() ? Material.LIME_DYE : Material.GRAY_DYE)
                .setName("&e金币条件开关")
                .setLore(
                        "&7当前状态: " + (template.isMoneyEnabled() ? "&a启用" : "&c禁用"),
                        "",
                        "&a左键点击切换",
                        "&7控制是否启用金币条件检查"
                )
                .build();
        inventory.setItem(28, moneyToggleItem);

        // 点券开关
        ItemStack pointsToggleItem = new ItemBuilder(
                template.isPointsEnabled() ? Material.LIME_DYE : Material.GRAY_DYE)
                .setName("&e点券条件开关")
                .setLore(
                        "&7当前状态: " + (template.isPointsEnabled() ? "&a启用" : "&c禁用"),
                        "",
                        "&a左键点击切换",
                        "&7控制是否启用点券条件检查"
                )
                .build();
        inventory.setItem(30, pointsToggleItem);

        // 等级开关
        ItemStack levelToggleItem = new ItemBuilder(
                template.isLevelEnabled() ? Material.LIME_DYE : Material.GRAY_DYE)
                .setName("&e等级条件开关")
                .setLore(
                        "&7当前状态: " + (template.isLevelEnabled() ? "&a启用" : "&c禁用"),
                        "",
                        "&a左键点击切换",
                        "&7控制是否启用等级条件检查"
                )
                .build();
        inventory.setItem(32, levelToggleItem);

        // 物品开关
        ItemStack itemsToggleItem = new ItemBuilder(
                template.isItemsEnabled() ? Material.LIME_DYE : Material.GRAY_DYE)
                .setName("&e物品条件开关")
                .setLore(
                        "&7当前状态: " + (template.isItemsEnabled() ? "&a启用" : "&c禁用"),
                        "",
                        "&a左键点击切换",
                        "&7控制是否启用物品条件检查"
                )
                .build();
        inventory.setItem(34, itemsToggleItem);
    }

    /**
     * 创建金币条件说明
     */
    private List<String> createMoneyLore() {
        List<String> lore = new ArrayList<>();
        double moneyCost = getMoneyConditionFromConfig();
        boolean moneyEnabled = getMoneyEnabledFromConfig();
        lore.add("&7当前金币花费: &6" + moneyCost);
        lore.add("&7状态: " + (moneyEnabled ? "&a启用" : "&c禁用"));
        lore.add("");
        lore.add("&a左键点击编辑金币数量");
        lore.add("&7在聊天框输入新的金币数量");
        return lore;
    }

    /**
     * 创建点券条件说明
     */
    private List<String> createPointsLore() {
        List<String> lore = new ArrayList<>();
        int pointsCost = getPointsConditionFromConfig();
        boolean pointsEnabled = getPointsEnabledFromConfig();
        lore.add("&7当前点券花费: &b" + pointsCost);
        lore.add("&7状态: " + (pointsEnabled ? "&a启用" : "&c禁用"));
        lore.add("");
        lore.add("&a左键点击编辑点券数量");
        lore.add("&7在聊天框输入新的点券数量");
        return lore;
    }

    /**
     * 创建等级条件说明
     */
    private List<String> createLevelLore() {
        List<String> lore = new ArrayList<>();
        int levelRequirement = getLevelConditionFromConfig();
        boolean levelEnabled = getLevelEnabledFromConfig();
        lore.add("&7当前等级要求: &d" + levelRequirement);
        lore.add("&7状态: " + (levelEnabled ? "&a启用" : "&c禁用"));
        lore.add("");
        lore.add("&a左键点击编辑等级要求");
        lore.add("&7在聊天框输入新的等级要求");
        return lore;
    }

    /**
     * 创建物品条件说明
     */
    private List<String> createItemsLore() {
        List<String> lore = new ArrayList<>();
        lore.add("&7当前物品条件数量: &f" + template.getRequirements().size());
        lore.add("&7状态: " + (template.isItemsEnabled() ? "&a启用" : "&c禁用"));
        lore.add("");
        lore.add("&a左键点击管理物品条件");
        lore.add("&7添加、删除或编辑物品条件");
        return lore;
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
            case 10: // 金币条件编辑
                handleMoneyEdit();
                return;
            case 12: // 点券条件编辑
                handlePointsEdit();
                return;
            case 14: // 等级条件编辑
                handleLevelEdit();
                return;
            case 16: // 物品条件编辑
                handleItemsEdit();
                return;
            case 28: // 金币开关
                handleMoneyToggle();
                return;
            case 30: // 点券开关
                handlePointsToggle();
                return;
            case 32: // 等级开关
                handleLevelToggle();
                return;
            case 34: // 物品开关
                handleItemsToggle();
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
     * 处理金币条件编辑
     */
    private void handleMoneyEdit() {
        close();
        plugin.getChatInputListener().requestDecimalInput(player, "command.admin.edit.input-money-cost", input -> {
            // 使用BukkitScheduler确保在主线程中执行
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // 设置金币花费
                double moneyCost = Double.parseDouble(input);
                template.setMoneyCost(moneyCost);
                MessageUtil.sendMessage(player, "command.admin.edit.money-cost-updated",
                        MessageUtil.createPlaceholders("cost", input));
                // 重新打开界面
                plugin.getGUIManager().openTemplateConditionsEditGUI(player, templateName);
            });
        });
    }

    /**
     * 处理点券条件编辑
     */
    private void handlePointsEdit() {
        close();
        plugin.getChatInputListener().requestNumberInput(player, "command.admin.edit.input-points-cost", input -> {
            // 使用BukkitScheduler确保在主线程中执行
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // 设置点券花费
                int pointsCost = Integer.parseInt(input);
                template.setPointsCost(pointsCost);
                MessageUtil.sendMessage(player, "command.admin.edit.points-cost-updated",
                        MessageUtil.createPlaceholders("cost", input));
                // 重新打开界面
                plugin.getGUIManager().openTemplateConditionsEditGUI(player, templateName);
            });
        });
    }

    /**
     * 处理等级条件编辑
     */
    private void handleLevelEdit() {
        close();
        plugin.getChatInputListener().requestNumberInput(player, "command.admin.edit.input-level-requirement", input -> {
            // 使用BukkitScheduler确保在主线程中执行
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // 设置等级要求
                int levelRequirement = Integer.parseInt(input);
                template.setLevelRequirement(levelRequirement);
                MessageUtil.sendMessage(player, "command.admin.edit.level-requirement-updated",
                        MessageUtil.createPlaceholders("level", input));
                // 重新打开界面
                plugin.getGUIManager().openTemplateConditionsEditGUI(player, templateName);
            });
        });
    }

    /**
     * 处理物品条件编辑
     */
    private void handleItemsEdit() {
        close();
        plugin.getGUIManager().openItemConditionManageGUI(player, templateName);
    }

    /**
     * 处理金币开关切换
     */
    private void handleMoneyToggle() {
        template.setMoneyEnabled(!template.isMoneyEnabled());
        init(); // 刷新界面
        MessageUtil.sendMessage(player, "command.admin.edit.money-toggle",
                MessageUtil.createPlaceholders("status", 
                        template.isMoneyEnabled() ? "启用" : "禁用"));
    }

    /**
     * 处理点券开关切换
     */
    private void handlePointsToggle() {
        template.setPointsEnabled(!template.isPointsEnabled());
        init(); // 刷新界面
        MessageUtil.sendMessage(player, "command.admin.edit.points-toggle",
                MessageUtil.createPlaceholders("status", 
                        template.isPointsEnabled() ? "启用" : "禁用"));
    }

    /**
     * 处理等级开关切换
     */
    private void handleLevelToggle() {
        template.setLevelEnabled(!template.isLevelEnabled());
        init(); // 刷新界面
        MessageUtil.sendMessage(player, "command.admin.edit.level-toggle",
                MessageUtil.createPlaceholders("status", 
                        template.isLevelEnabled() ? "启用" : "禁用"));
    }

    /**
     * 处理物品开关切换
     */
    private void handleItemsToggle() {
        template.setItemsEnabled(!template.isItemsEnabled());
        init(); // 刷新界面
        MessageUtil.sendMessage(player, "command.admin.edit.items-toggle",
                MessageUtil.createPlaceholders("status",
                        template.isItemsEnabled() ? "启用" : "禁用"));
    }

    /**
     * 从配置文件中读取金币条件
     */
    private double getMoneyConditionFromConfig() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return 0.0;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection conditionsSection = config.getConfigurationSection("creationConditions");

            if (conditionsSection != null && conditionsSection.contains("money")) {
                return conditionsSection.getDouble("money", 0.0);
            }

            return 0.0;
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的金币条件时发生错误: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * 从配置文件中读取金币条件启用状态
     */
    private boolean getMoneyEnabledFromConfig() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return true;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection conditionsSection = config.getConfigurationSection("creationConditions");

            if (conditionsSection != null && conditionsSection.contains("moneyEnabled")) {
                return conditionsSection.getBoolean("moneyEnabled", true);
            }

            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的金币条件启用状态时发生错误: " + e.getMessage());
            return true;
        }
    }

    /**
     * 从配置文件中读取点券条件
     */
    private int getPointsConditionFromConfig() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return 0;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection conditionsSection = config.getConfigurationSection("creationConditions");

            if (conditionsSection != null && conditionsSection.contains("points")) {
                return conditionsSection.getInt("points", 0);
            }

            return 0;
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的点券条件时发生错误: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 从配置文件中读取点券条件启用状态
     */
    private boolean getPointsEnabledFromConfig() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return true;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection conditionsSection = config.getConfigurationSection("creationConditions");

            if (conditionsSection != null && conditionsSection.contains("pointsEnabled")) {
                return conditionsSection.getBoolean("pointsEnabled", true);
            }

            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的点券条件启用状态时发生错误: " + e.getMessage());
            return true;
        }
    }

    /**
     * 从配置文件中读取等级条件
     */
    private int getLevelConditionFromConfig() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return 0;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection conditionsSection = config.getConfigurationSection("creationConditions");

            if (conditionsSection != null && conditionsSection.contains("level")) {
                return conditionsSection.getInt("level", 0);
            }

            return 0;
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的等级条件时发生错误: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 从配置文件中读取等级条件启用状态
     */
    private boolean getLevelEnabledFromConfig() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return true;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection conditionsSection = config.getConfigurationSection("creationConditions");

            if (conditionsSection != null && conditionsSection.contains("levelEnabled")) {
                return conditionsSection.getBoolean("levelEnabled", true);
            }

            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的等级条件启用状态时发生错误: " + e.getMessage());
            return true;
        }
    }

    /**
     * 从配置文件中读取物品条件启用状态
     */
    private boolean getItemsEnabledFromConfig() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return true; // 默认值
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection conditionsSection = config.getConfigurationSection("creationConditions");

            if (conditionsSection != null && conditionsSection.contains("itemsEnabled")) {
                return conditionsSection.getBoolean("itemsEnabled", true);
            }

            return true; // 默认值
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的物品条件启用状态时发生错误: " + e.getMessage());
            return true; // 默认值
        }
    }
}
