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
import java.util.HashMap;
import java.util.Map;

/**
 * 模板基础信息编辑界面
 * 用于编辑模板的基础信息
 */
public class TemplateBasicEditGUI extends AbstractGUI {

    private final String templateName;
    private final DungeonTemplate template;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     * @param templateName 模板名称
     */
    public TemplateBasicEditGUI(SagaDungeons plugin, Player player, String templateName) {
        super(plugin, player, createTitle(plugin, templateName), 54);
        this.templateName = templateName;
        this.template = plugin.getConfigManager().getTemplateManager().getTemplates().get(templateName);
    }

    private static String createTitle(SagaDungeons plugin, String templateName) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("template", templateName);
        return createGUITitle(plugin, "template-basic-edit.title", placeholders);
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

        // 添加编辑选项
        addEditOptions();

        // 添加功能按钮
        addFunctionButtons();
    }

    /**
     * 添加装饰边框
     */
    private void addBorder() {
        ItemStack borderItem = new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE)
                .setName(getGUIText("common.border"))
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
     * 添加编辑选项
     */
    private void addEditOptions() {
        // 显示名称编辑
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("value", template.getDisplayName());
        ItemStack displayNameItem = new ItemBuilder(Material.NAME_TAG)
                .setName(getGUIText("template-basic-edit.display-name-edit"))
                .setLore(
                        getGUIText("common.current-value", placeholders),
                        "",
                        getGUIText("common.left-click") + " " + getGUIText("common.edit"),
                        getGUIText("template-basic-edit.display-name-lore")
                )
                .build();
        inventory.setItem(10, displayNameItem);

        // 世界显示名称编辑
        placeholders.clear();
        placeholders.put("value", template.getWorldDisplay());
        ItemStack worldDisplayItem = new ItemBuilder(Material.COMPASS)
                .setName(getGUIText("template-basic-edit.world-display-edit"))
                .setLore(
                        getGUIText("common.current-value", placeholders),
                        getGUIText("template-basic-edit.world-display-lore"),
                        "",
                        getGUIText("common.left-click") + " " + getGUIText("common.edit"),
                        getGUIText("template-basic-edit.world-display-input-lore")
                )
                .build();
        inventory.setItem(12, worldDisplayItem);

        // 超时时间编辑
        placeholders.clear();
        placeholders.put("timeout", String.valueOf(template.getDefaultTimeout()));
        String timeoutCurrent = getGUIText("template-basic-edit.timeout-current", placeholders);

        placeholders.clear();
        placeholders.put("minutes", String.valueOf(template.getDefaultTimeout() / 60));
        String timeoutMinutes = getGUIText("template-basic-edit.timeout-minutes", placeholders);

        ItemStack timeoutItem = new ItemBuilder(Material.CLOCK)
                .setName(getGUIText("template-basic-edit.timeout-edit"))
                .setLore(
                        timeoutCurrent,
                        timeoutMinutes,
                        "",
                        getGUIText("common.left-click") + " " + getGUIText("common.edit"),
                        getGUIText("template-basic-edit.timeout-lore")
                )
                .build();
        inventory.setItem(14, timeoutItem);

        // 死亡限制编辑
        String deathLimitDisplay;
        if (template.getDeathLimit() > 0) {
            placeholders.clear();
            placeholders.put("limit", String.valueOf(template.getDeathLimit()));
            deathLimitDisplay = getGUIText("template-basic-edit.death-limit-times", placeholders);
        } else {
            deathLimitDisplay = getGUIText("template-basic-edit.death-limit-unlimited");
        }

        placeholders.clear();
        placeholders.put("limit", deathLimitDisplay);

        ItemStack deathLimitItem = new ItemBuilder(Material.SKELETON_SKULL)
                .setName(getGUIText("template-basic-edit.death-limit-edit"))
                .setLore(
                        getGUIText("template-basic-edit.death-limit-current", placeholders),
                        "",
                        getGUIText("common.left-click") + " " + getGUIText("common.edit"),
                        getGUIText("template-basic-edit.death-limit-input-lore"),
                        getGUIText("template-basic-edit.death-limit-zero-lore")
                )
                .build();
        inventory.setItem(16, deathLimitItem);

        // 强制游戏模式开关
        boolean actualForceGameMode = getForceGameModeFromConfig();
        String statusText = actualForceGameMode ?
                getGUIText("template-basic-edit.force-gamemode-enabled") :
                getGUIText("template-basic-edit.force-gamemode-disabled");

        placeholders.clear();
        placeholders.put("status", statusText);

        ItemStack gameModeToggleItem = new ItemBuilder(
                actualForceGameMode ? Material.LIME_DYE : Material.GRAY_DYE)
                .setName(getGUIText("template-basic-edit.force-gamemode-edit"))
                .setLore(
                        getGUIText("common.current-value", createPlaceholder("value", statusText)),
                        "",
                        getGUIText("common.left-click") + " " + getGUIText("common.edit"),
                        getGUIText("template-basic-edit.force-gamemode-toggle-lore")
                )
                .build();
        inventory.setItem(28, gameModeToggleItem);

        // 游戏模式选择
        placeholders.clear();
        placeholders.put("mode", getGameModeDisplayName(template.getGameMode()));

        ItemStack gameModeItem = new ItemBuilder(getGameModeIcon(template.getGameMode()))
                .setName(getGUIText("template-basic-edit.gamemode-edit"))
                .setLore(
                        getGUIText("template-basic-edit.gamemode-current", placeholders),
                        "",
                        getGUIText("common.left-click") + " " + getGUIText("common.edit"),
                        getGUIText("template-basic-edit.gamemode-cycle-lore"),
                        getGUIText("template-basic-edit.gamemode-force-only")
                )
                .build();
        inventory.setItem(30, gameModeItem);

        // 创建条件编辑按钮
        ItemStack conditionsItem = new ItemBuilder(Material.GOLD_INGOT)
                .setName(getGUIText("template-basic-edit.conditions-edit"))
                .setLore(
                        getGUIText("template-basic-edit.conditions-lore"),
                        getGUIText("template-basic-edit.conditions-desc"),
                        "",
                        getGUIText("common.left-click") + " " + getGUIText("common.edit")
                )
                .build();
        inventory.setItem(32, conditionsItem);

        // 奖励系统编辑按钮
        ItemStack rewardsItem = new ItemBuilder(Material.DIAMOND)
                .setName(getGUIText("template-basic-edit.rewards-edit"))
                .setLore(
                        getGUIText("template-basic-edit.rewards-lore"),
                        getGUIText("template-basic-edit.rewards-desc1"),
                        getGUIText("template-basic-edit.rewards-desc2"),
                        "",
                        getGUIText("common.left-click") + " " + getGUIText("common.edit")
                )
                .build();
        inventory.setItem(34, rewardsItem);

        // 通关条件编辑按钮
        placeholders.clear();
        placeholders.put("count", String.valueOf(getCompletionConditionCount()));

        ItemStack completionItem = new ItemBuilder(Material.BEACON)
                .setName(getGUIText("template-basic-edit.completion-edit"))
                .setLore(
                        getGUIText("template-basic-edit.completion-lore"),
                        getGUIText("template-basic-edit.completion-desc"),
                        getGUIText("template-basic-edit.completion-count", placeholders),
                        "",
                        getGUIText("common.left-click") + " " + getGUIText("common.edit")
                )
                .build();
        inventory.setItem(37, completionItem);

        // 刷怪点管理按钮
        placeholders.clear();
        placeholders.put("count", String.valueOf(template.getMobSpawners().size()));

        ItemStack spawnersItem = new ItemBuilder(Material.SPAWNER)
                .setName(getGUIText("template-basic-edit.spawners-edit"))
                .setLore(
                        getGUIText("template-basic-edit.spawners-lore"),
                        getGUIText("template-basic-edit.spawners-desc"),
                        getGUIText("template-basic-edit.spawners-count", placeholders),
                        "",
                        getGUIText("common.left-click") + " " + getGUIText("common.edit")
                )
                .build();
        inventory.setItem(39, spawnersItem);
    }

    /**
     * 获取游戏模式图标
     * @param gameMode 游戏模式字符串
     * @return 对应的材质
     */
    private Material getGameModeIcon(String gameMode) {
        switch (gameMode.toUpperCase()) {
            case "SURVIVAL":
                return Material.WOODEN_SWORD;
            case "CREATIVE":
                return Material.COMMAND_BLOCK;
            case "ADVENTURE":
                return Material.MAP;
            case "SPECTATOR":
                return Material.ENDER_EYE;
            default:
                return Material.PAPER;
        }
    }

    /**
     * 获取游戏模式显示名称
     * @param gameMode 游戏模式字符串
     * @return 显示名称
     */
    private String getGameModeDisplayName(String gameMode) {
        switch (gameMode.toUpperCase()) {
            case "SURVIVAL":
                return getGUIText("template-basic-edit.gamemode-survival");
            case "CREATIVE":
                return getGUIText("template-basic-edit.gamemode-creative");
            case "ADVENTURE":
                return getGUIText("template-basic-edit.gamemode-adventure");
            case "SPECTATOR":
                return getGUIText("template-basic-edit.gamemode-spectator");
            default:
                return gameMode;
        }
    }

    /**
     * 添加功能按钮
     */
    private void addFunctionButtons() {
        // 保存按钮
        ItemStack saveButton = new ItemBuilder(Material.EMERALD)
                .setName(getGUIText("template-basic-edit.save-changes"))
                .setLore(
                        getGUIText("template-basic-edit.save-changes-lore"),
                        "",
                        getGUIText("common.left-click") + " " + getGUIText("common.save")
                )
                .build();
        inventory.setItem(49, saveButton);

        // 返回按钮
        ItemStack backButton = new ItemBuilder(Material.ARROW)
                .setName(getGUIText("common.back"))
                .setLore(getGUIText("template-basic-edit.back-to-main"))
                .build();
        inventory.setItem(45, backButton);

        // 刷新按钮
        ItemStack refreshButton = new ItemBuilder(Material.LIME_DYE)
                .setName(getGUIText("common.refresh"))
                .setLore(getGUIText("common.refresh-lore"))
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
            case 10: // 显示名称编辑
                handleDisplayNameEdit();
                return;
            case 12: // 世界显示名称编辑
                handleWorldDisplayEdit();
                return;
            case 14: // 超时时间编辑
                handleTimeoutEdit();
                return;
            case 16: // 死亡限制编辑
                handleDeathLimitEdit();
                return;
            case 28: // 强制游戏模式切换
                handleGameModeToggle();
                return;
            case 30: // 游戏模式切换
                handleGameModeSwitch();
                return;
            case 32: // 创建条件编辑
                handleConditionsEdit();
                return;
            case 34: // 奖励系统编辑
                handleRewardsEdit();
                return;
            case 37: // 通关条件编辑
                handleCompletionEdit();
                return;
            case 39: // 刷怪点管理
                handleSpawnersEdit();
                return;
        }
    }

    /**
     * 处理保存
     */
    private void handleSave() {
        plugin.getConfigManager().getTemplateManager().saveTemplate(template);

        // 保存后重新加载模板，确保内存中的模板与配置文件同步
        plugin.getConfigManager().getTemplateManager().reloadTemplate(templateName);

        MessageUtil.sendMessage(player, "command.admin.edit.save-success",
                MessageUtil.createPlaceholders("template", templateName));
    }

    /**
     * 处理返回
     */
    private void handleBack() {
        close();
        plugin.getGUIManager().openTemplateEditMainGUI(player);
    }

    /**
     * 处理显示名称编辑
     */
    private void handleDisplayNameEdit() {
        close();
        plugin.getChatInputListener().requestTextInput(player, "command.admin.edit.input-display-name", input -> {
            // 使用BukkitScheduler确保在主线程中执行
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // 设置显示名称
                template.setDisplayName(input);
                MessageUtil.sendMessage(player, "command.admin.edit.display-name-updated",
                        MessageUtil.createPlaceholders("name", input));
                // 重新打开界面
                plugin.getGUIManager().openTemplateBasicEditGUI(player, templateName);
            });
        });
    }

    /**
     * 处理世界显示名称编辑
     */
    private void handleWorldDisplayEdit() {
        close();
        plugin.getChatInputListener().requestTextInput(player, "command.admin.edit.input-world-display", input -> {
            // 使用BukkitScheduler确保在主线程中执行
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // 设置世界显示名称
                template.setWorldDisplay(input);
                MessageUtil.sendMessage(player, "command.admin.edit.world-display-updated",
                        MessageUtil.createPlaceholders("name", input));
                // 重新打开界面
                plugin.getGUIManager().openTemplateBasicEditGUI(player, templateName);
            });
        });
    }

    /**
     * 处理超时时间编辑
     */
    private void handleTimeoutEdit() {
        close();
        plugin.getChatInputListener().requestNumberInput(player, "command.admin.edit.input-timeout", input -> {
            // 使用BukkitScheduler确保在主线程中执行
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // 设置超时时间
                int timeout = Integer.parseInt(input);
                template.setDefaultTimeout(timeout);
                MessageUtil.sendMessage(player, "command.admin.edit.timeout-updated",
                        MessageUtil.createPlaceholders("timeout", input));
                // 重新打开界面
                plugin.getGUIManager().openTemplateBasicEditGUI(player, templateName);
            });
        });
    }

    /**
     * 处理死亡限制编辑
     */
    private void handleDeathLimitEdit() {
        close();
        plugin.getChatInputListener().requestNumberInput(player, "command.admin.edit.input-death-limit", input -> {
            // 使用BukkitScheduler确保在主线程中执行
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // 设置死亡限制
                int deathLimit = Integer.parseInt(input);
                template.setDeathLimit(deathLimit);
                MessageUtil.sendMessage(player, "command.admin.edit.death-limit-updated",
                        MessageUtil.createPlaceholders("limit", input));
                // 重新打开界面
                plugin.getGUIManager().openTemplateBasicEditGUI(player, templateName);
            });
        });
    }

    /**
     * 处理强制游戏模式切换
     */
    private void handleGameModeToggle() {
        template.setForceGameMode(!template.isForceGameMode());
        init(); // 刷新界面

        String statusKey = template.isForceGameMode() ?
                "template-basic-edit.status-enabled" :
                "template-basic-edit.status-disabled";
        String statusText = getGUIText(statusKey);

        MessageUtil.sendMessage(player, "command.admin.edit.gamemode-toggle",
                MessageUtil.createPlaceholders("status", statusText));
    }

    /**
     * 处理游戏模式切换
     */
    private void handleGameModeSwitch() {
        String currentMode = template.getGameMode();
        String nextMode;

        switch (currentMode.toUpperCase()) {
            case "SURVIVAL":
                nextMode = "CREATIVE";
                break;
            case "CREATIVE":
                nextMode = "ADVENTURE";
                break;
            case "ADVENTURE":
                nextMode = "SPECTATOR";
                break;
            case "SPECTATOR":
                nextMode = "SURVIVAL";
                break;
            default:
                nextMode = "ADVENTURE";
                break;
        }

        template.setGameMode(nextMode);
        init(); // 刷新界面
        MessageUtil.sendMessage(player, "command.admin.edit.gamemode-switch",
                MessageUtil.createPlaceholders("mode", getGameModeDisplayName(nextMode)));
    }

    /**
     * 处理创建条件编辑
     */
    private void handleConditionsEdit() {
        close();
        plugin.getGUIManager().openTemplateConditionsEditGUI(player, templateName);
    }

    /**
     * 处理奖励系统编辑 - 已移除
     */
    private void handleRewardsEdit() {
        // 奖励系统已移除
        MessageUtil.sendMessage(player, "command.admin.edit.feature-removed");
        close();
    }

    /**
     * 处理通关条件编辑
     */
    private void handleCompletionEdit() {
        close();
        plugin.getGUIManager().openTemplateCompletionEditGUI(player, templateName);
    }

    /**
     * 处理刷怪点管理
     */
    private void handleSpawnersEdit() {
        close();
        plugin.getGUIManager().openTemplateSpawnersEditGUI(player, templateName);
    }

    /**
     * 获取通关条件数量
     */
    private int getCompletionConditionCount() {
        // 从配置文件中读取通关条件数量
        File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
        File configFile = new File(templateDir, "config.yml");

        if (!configFile.exists()) {
            return 0;
        }

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection completionSection = config.getConfigurationSection("completion");

            if (completionSection == null) {
                return 0;
            }

            int count = 0;

            // 检查基础条件
            if (completionSection.getBoolean("killAll", false)) {
                count++;
            }

            if (completionSection.contains("killCount")) {
                count++;
            }

            if (completionSection.contains("killSpecific")) {
                count++;
            }

            if (completionSection.contains("reachArea")) {
                count++;
            }

            // 检查组合条件
            ConfigurationSection compositeSection = completionSection.getConfigurationSection("composite");
            if (compositeSection != null) {
                ConfigurationSection conditionsSection = compositeSection.getConfigurationSection("conditions");
                if (conditionsSection != null) {
                    count += conditionsSection.getKeys(false).size();
                }
            }

            return count;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to read completion conditions for template " + templateName + ": " + e.getMessage());
            return 0;
        }
    }

    // ==================== 开关状态配置读取方法 ====================

    /**
     * 从配置文件读取强制游戏模式开关状态
     * @return 强制游戏模式开关状态，如果读取失败返回默认值true
     */
    private boolean getForceGameModeFromConfig() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return true; // 默认值
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            return config.getBoolean("forceGameMode", true);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to read force game mode setting: " + e.getMessage());
        }
        return true; // 默认值
    }
}
