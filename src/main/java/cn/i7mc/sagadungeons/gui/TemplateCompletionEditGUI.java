package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.util.LocationUtil;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Set;

/**
 * 模板通关条件编辑界面
 * 用于编辑模板的通关条件
 */
public class TemplateCompletionEditGUI extends AbstractGUI {

    private final String templateName;
    private final DungeonTemplate template;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     * @param templateName 模板名称
     */
    public TemplateCompletionEditGUI(SagaDungeons plugin, Player player, String templateName) {
        super(plugin, player, createGUITitle(plugin, "template-completion-edit.title",
                createPlaceholderMap("template", templateName)), 54);
        this.templateName = templateName;
        this.template = plugin.getConfigManager().getTemplateManager().getTemplates().get(templateName);
    }

    /**
     * 创建占位符映射的辅助方法
     */
    private static java.util.Map<String, String> createPlaceholderMap(String key, String value) {
        java.util.Map<String, String> placeholders = new java.util.HashMap<>();
        placeholders.put(key, value);
        return placeholders;
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

        // 添加通关条件选项
        addCompletionConditions();

        // 添加功能按钮
        addFunctionButtons();
    }

    /**
     * 添加装饰边框
     */
    private void addBorder() {
        ItemStack borderItem = new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE)
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
     * 添加通关条件选项
     */
    private void addCompletionConditions() {
        // 基础条件区域 (第二行)
        addBasicConditions();

        // 高级条件区域 (第三行)
        addAdvancedConditions();

        // 组合条件已移除
    }

    /**
     * 添加基础条件选项
     */
    private void addBasicConditions() {
        // 全部击杀条件
        ItemStack killAllItem = new ItemBuilder(Material.IRON_SWORD)
                .setName(getGUIText("template-completion-edit.kill-all"))
                .setLore(
                        getGUIText("template-completion-edit.kill-all-desc1"),
                        getGUIText("template-completion-edit.kill-all-desc2"),
                        getGUIText("template-completion-edit.kill-all-status",
                                createPlaceholder("status", getConditionStatus("killAll"))),
                        "",
                        getGUIText("template-completion-edit.kill-all-left-click"),
                        getGUIText("template-completion-edit.kill-all-right-click")
                )
                .build();
        inventory.setItem(10, killAllItem);

        // 击杀数量条件
        ItemStack killCountItem = new ItemBuilder(Material.DIAMOND_SWORD)
                .setName(getGUIText("template-completion-edit.kill-count"))
                .setLore(
                        getGUIText("template-completion-edit.kill-count-desc"),
                        getGUIText("template-completion-edit.kill-count-current",
                                createPlaceholder("count", String.valueOf(getKillCountTarget()))),
                        getGUIText("template-completion-edit.kill-count-status",
                                createPlaceholder("status", getConditionStatus("killCount"))),
                        "",
                        getGUIText("template-completion-edit.kill-count-left-click"),
                        getGUIText("template-completion-edit.kill-count-right-click")
                )
                .build();
        inventory.setItem(12, killCountItem);

        // 击杀特定怪物条件
        ItemStack killSpecificItem = new ItemBuilder(Material.GOLDEN_SWORD)
                .setName(getGUIText("template-completion-edit.kill-specific"))
                .setLore(
                        getGUIText("template-completion-edit.kill-specific-desc"),
                        getGUIText("template-completion-edit.kill-specific-target",
                                createPlaceholder("target", getSpecificMobTarget())),
                        getGUIText("template-completion-edit.kill-specific-status",
                                createPlaceholder("status", getConditionStatus("killSpecific"))),
                        "",
                        getGUIText("template-completion-edit.kill-specific-left-click"),
                        getGUIText("template-completion-edit.kill-specific-right-click")
                )
                .build();
        inventory.setItem(14, killSpecificItem);
    }

    /**
     * 添加高级条件选项
     */
    private void addAdvancedConditions() {
        // 到达区域条件
        ItemStack reachAreaItem = new ItemBuilder(Material.COMPASS)
                .setName(getGUIText("template-completion-edit.reach-area"))
                .setLore(
                        getGUIText("template-completion-edit.reach-area-desc"),
                        getGUIText("template-completion-edit.reach-area-location",
                                createPlaceholder("location", getReachAreaLocation())),
                        getGUIText("template-completion-edit.reach-area-radius",
                                createPlaceholder("radius", String.valueOf(getReachAreaRadius()))),
                        getGUIText("template-completion-edit.reach-area-status",
                                createPlaceholder("status", getConditionStatus("reachArea"))),
                        "",
                        getGUIText("template-completion-edit.reach-area-left-click"),
                        getGUIText("template-completion-edit.reach-area-shift-left-click"),
                        getGUIText("template-completion-edit.reach-area-right-click")
                )
                .build();
        inventory.setItem(21, reachAreaItem);
    }

    // 组合条件选项已移除

    /**
     * 获取条件状态
     */
    private String getConditionStatus(String conditionType) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return getGUIText("template-completion-edit.status-not-configured");
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection completionSection = config.getConfigurationSection("completion");

            if (completionSection == null) {
                return getGUIText("template-completion-edit.status-not-configured");
            }

            switch (conditionType) {
                case "killAll":
                    if (completionSection.contains("killAll")) {
                        boolean enabled = completionSection.getBoolean("killAll", false);
                        return enabled ? getGUIText("template-completion-edit.status-enabled") :
                                        getGUIText("template-completion-edit.status-disabled");
                    }
                    return getGUIText("template-completion-edit.status-not-configured");

                case "killCount":
                    if (completionSection.contains("killCount")) {
                        return getGUIText("template-completion-edit.status-configured");
                    }
                    return getGUIText("template-completion-edit.status-not-configured");

                case "killSpecific":
                    if (completionSection.contains("killSpecific")) {
                        return getGUIText("template-completion-edit.status-configured");
                    }
                    return getGUIText("template-completion-edit.status-not-configured");

                case "reachArea":
                    if (completionSection.contains("reachArea")) {
                        return getGUIText("template-completion-edit.status-configured");
                    }
                    return getGUIText("template-completion-edit.status-not-configured");

                // 组合条件状态检查已移除

                default:
                    return getGUIText("template-completion-edit.status-unknown");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的通关条件状态时发生错误: " + e.getMessage());
            return getGUIText("template-completion-edit.status-read-failed");
        }
    }

    /**
     * 获取击杀数量目标
     */
    private int getKillCountTarget() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return 0;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection completionSection = config.getConfigurationSection("completion");

            if (completionSection == null) {
                return 0;
            }

            if (completionSection.contains("killCount")) {
                return completionSection.getInt("killCount", 0);
            }

            return 0;
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的击杀数量目标时发生错误: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 获取特定怪物目标
     */
    private String getSpecificMobTarget() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return getGUIText("template-completion-edit.not-set");
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection completionSection = config.getConfigurationSection("completion");

            if (completionSection == null) {
                return getGUIText("template-completion-edit.not-set");
            }

            ConfigurationSection killSpecificSection = completionSection.getConfigurationSection("killSpecific");
            if (killSpecificSection != null) {
                String mobType = killSpecificSection.getString("mobType", getGUIText("template-completion-edit.not-set"));
                int count = killSpecificSection.getInt("count", 1);
                java.util.Map<String, String> placeholders = new java.util.HashMap<>();
                placeholders.put("mob", mobType);
                placeholders.put("count", String.valueOf(count));
                return getGUIText("template-completion-edit.mob-count-format", placeholders);
            }

            return getGUIText("template-completion-edit.not-set");
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的特定怪物目标时发生错误: " + e.getMessage());
            return getGUIText("template-completion-edit.read-failed");
        }
    }

    /**
     * 获取到达区域位置
     */
    private String getReachAreaLocation() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return getGUIText("template-completion-edit.not-set");
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection completionSection = config.getConfigurationSection("completion");

            if (completionSection == null) {
                return getGUIText("template-completion-edit.not-set");
            }

            ConfigurationSection reachAreaSection = completionSection.getConfigurationSection("reachArea");
            if (reachAreaSection != null) {
                if (reachAreaSection.contains("x") && reachAreaSection.contains("y") && reachAreaSection.contains("z")) {
                    return getGUIText("template-completion-edit.location-set");
                }
            }

            return getGUIText("template-completion-edit.not-set");
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的到达区域位置时发生错误: " + e.getMessage());
            return getGUIText("template-completion-edit.read-failed");
        }
    }

    /**
     * 获取到达区域半径
     */
    private double getReachAreaRadius() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return 3.0;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection completionSection = config.getConfigurationSection("completion");

            if (completionSection == null) {
                return 3.0;
            }

            ConfigurationSection reachAreaSection = completionSection.getConfigurationSection("reachArea");
            if (reachAreaSection != null) {
                return reachAreaSection.getDouble("range", 3.0);
            }

            return 3.0;
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的到达区域半径时发生错误: " + e.getMessage());
            return 3.0;
        }
    }

    // 组合条件数量获取方法已移除

    /**
     * 添加功能按钮
     */
    private void addFunctionButtons() {
        // 保存按钮
        ItemStack saveButton = new ItemBuilder(Material.EMERALD)
                .setName(getGUIText("template-completion-edit.save-button"))
                .setLore(
                        getGUIText("template-completion-edit.save-button-desc"),
                        "",
                        getGUIText("template-completion-edit.save-button-click")
                )
                .build();
        inventory.setItem(49, saveButton);

        // 返回按钮
        ItemStack backButton = new ItemBuilder(Material.ARROW)
                .setName(getGUIText("template-completion-edit.back-button"))
                .setLore(getGUIText("template-completion-edit.back-button-desc"))
                .build();
        inventory.setItem(45, backButton);

        // 刷新按钮
        ItemStack refreshButton = new ItemBuilder(Material.LIME_DYE)
                .setName(getGUIText("template-completion-edit.refresh-button"))
                .setLore(getGUIText("template-completion-edit.refresh-button-desc"))
                .build();
        inventory.setItem(53, refreshButton);

        // 帮助按钮
        ItemStack helpButton = new ItemBuilder(Material.BOOK)
                .setName(getGUIText("template-completion-edit.help-button"))
                .setLore(
                        getGUIText("template-completion-edit.help-desc"),
                        getGUIText("template-completion-edit.help-kill-all"),
                        getGUIText("template-completion-edit.help-kill-count"),
                        getGUIText("template-completion-edit.help-kill-specific"),
                        getGUIText("template-completion-edit.help-reach-area")
                )
                .build();
        inventory.setItem(47, helpButton);
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
            case 47: // 帮助
                // 帮助按钮不需要处理
                return;
            case 10: // 全部击杀条件
                handleKillAllCondition(event);
                return;
            case 12: // 击杀数量条件
                handleKillCountCondition(event);
                return;
            case 14: // 击杀特定怪物条件
                handleKillSpecificCondition(event);
                return;
            case 21: // 到达区域条件
                handleReachAreaCondition(event);
                return;
            // 组合条件处理已移除
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
        plugin.getGUIManager().openTemplateBasicEditGUI(player, templateName);
    }

    /**
     * 处理全部击杀条件
     */
    private void handleKillAllCondition(InventoryClickEvent event) {
        if (event.isLeftClick()) {
            // 切换启用/禁用状态
            MessageUtil.sendMessage(player, "command.admin.edit.completion.kill-all-toggle");
            init(); // 刷新界面
        } else if (event.isRightClick()) {
            // 删除条件
            MessageUtil.sendMessage(player, "command.admin.edit.completion.kill-all-removed");
            init(); // 刷新界面
        }
    }

    /**
     * 处理击杀数量条件
     */
    private void handleKillCountCondition(InventoryClickEvent event) {
        if (event.isLeftClick()) {
            close();
            plugin.getChatInputListener().requestNumberInput(player, "command.admin.edit.completion.input-kill-count", input -> {
                // 使用BukkitScheduler确保在主线程中执行
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    // 设置击杀数量目标
                    int killCount = Integer.parseInt(input);
                    // TODO: 设置击杀数量条件到模板配置
                    MessageUtil.sendMessage(player, "command.admin.edit.completion.kill-count-updated",
                            MessageUtil.createPlaceholders("count", input));
                    // 重新打开界面
                    plugin.getGUIManager().openTemplateCompletionEditGUI(player, templateName);
                });
            });
        } else if (event.isRightClick()) {
            // 删除条件
            // TODO: 删除击杀数量条件配置
            MessageUtil.sendMessage(player, "command.admin.edit.completion.kill-count-removed");
            init(); // 刷新界面
        }
    }

    /**
     * 处理击杀特定怪物条件
     */
    private void handleKillSpecificCondition(InventoryClickEvent event) {
        if (event.isLeftClick()) {
            close();
            plugin.getChatInputListener().requestTextInput(player, "command.admin.edit.completion.input-specific-mob", input -> {
                // 使用BukkitScheduler确保在主线程中执行
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    // 设置特定怪物类型
                    String mobType = input;
                    // TODO: 设置击杀特定怪物条件到模板配置
                    MessageUtil.sendMessage(player, "command.admin.edit.completion.kill-specific-updated",
                            MessageUtil.createPlaceholders("mob", mobType));
                    // 重新打开界面
                    plugin.getGUIManager().openTemplateCompletionEditGUI(player, templateName);
                });
            });
        } else if (event.isRightClick()) {
            // 删除条件
            // TODO: 删除击杀特定怪物条件配置
            MessageUtil.sendMessage(player, "command.admin.edit.completion.kill-specific-removed");
            init(); // 刷新界面
        }
    }

    /**
     * 处理到达区域条件
     */
    private void handleReachAreaCondition(InventoryClickEvent event) {
        if (event.isLeftClick() && !event.isShiftClick()) {
            // 设置位置
            handleSetReachAreaLocation();
        } else if (event.isLeftClick() && event.isShiftClick()) {
            close();
            plugin.getChatInputListener().requestDecimalInput(player, "command.admin.edit.completion.input-reach-area-radius", input -> {
                // 使用BukkitScheduler确保在主线程中执行
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    // 设置检测半径
                    double radius = Double.parseDouble(input);
                    setReachAreaRadius(radius);
                    MessageUtil.sendMessage(player, "command.admin.edit.completion.reach-area-radius-updated",
                            MessageUtil.createPlaceholders("radius", input));
                    // 重新打开界面
                    plugin.getGUIManager().openTemplateCompletionEditGUI(player, templateName);
                });
            });
        } else if (event.isRightClick()) {
            // 删除条件
            MessageUtil.sendMessage(player, "command.admin.edit.completion.reach-area-removed");
            init(); // 刷新界面
        }
    }

    // 组合条件处理方法已移除

    /**
     * 处理设置到达区域位置
     */
    private void handleSetReachAreaLocation() {
        try {
            // 获取玩家当前位置
            Location playerLocation = player.getLocation();

            // 保存坐标到配置文件
            setReachAreaLocation(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());

            // 发送成功消息
            MessageUtil.sendMessage(player, "command.admin.edit.completion.reach-area-location-updated",
                    MessageUtil.createPlaceholders(
                            "x", String.valueOf(playerLocation.getBlockX()),
                            "y", String.valueOf(playerLocation.getBlockY()),
                            "z", String.valueOf(playerLocation.getBlockZ())
                    ));

            // 重新打开界面
            plugin.getGUIManager().openTemplateCompletionEditGUI(player, templateName);

        } catch (Exception e) {
            plugin.getLogger().warning("设置到达区域位置时发生错误: " + e.getMessage());
            MessageUtil.sendMessage(player, "command.admin.edit.completion.location-save-failed");
        }
    }

    /**
     * 设置到达区域位置到配置文件
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     */
    private void setReachAreaLocation(double x, double y, double z) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            // 确保配置文件存在
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

            // 获取或创建completion节点
            ConfigurationSection completionSection = config.getConfigurationSection("completion");
            if (completionSection == null) {
                completionSection = config.createSection("completion");
            }

            // 获取或创建reachArea节点
            ConfigurationSection reachAreaSection = completionSection.getConfigurationSection("reachArea");
            if (reachAreaSection == null) {
                reachAreaSection = completionSection.createSection("reachArea");
            }

            // 设置坐标
            reachAreaSection.set("x", x);
            reachAreaSection.set("y", y);
            reachAreaSection.set("z", z);

            // 如果range不存在，设置默认值
            if (!reachAreaSection.contains("range")) {
                reachAreaSection.set("range", 3.0);
            }

            // 保存配置文件
            config.save(configFile);

        } catch (Exception e) {
            plugin.getLogger().warning("保存到达区域位置配置时发生错误: " + e.getMessage());
            throw new RuntimeException("保存配置失败", e);
        }
    }

    /**
     * 设置到达区域范围到配置文件
     * @param range 检测范围
     */
    private void setReachAreaRadius(double range) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            // 确保配置文件存在
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

            // 获取或创建completion节点
            ConfigurationSection completionSection = config.getConfigurationSection("completion");
            if (completionSection == null) {
                completionSection = config.createSection("completion");
            }

            // 获取或创建reachArea节点
            ConfigurationSection reachAreaSection = completionSection.getConfigurationSection("reachArea");
            if (reachAreaSection == null) {
                reachAreaSection = completionSection.createSection("reachArea");
            }

            // 设置范围
            reachAreaSection.set("range", range);

            // 保存配置文件
            config.save(configFile);

        } catch (Exception e) {
            plugin.getLogger().warning("保存到达区域范围配置时发生错误: " + e.getMessage());
            throw new RuntimeException("保存配置失败", e);
        }
    }
}
