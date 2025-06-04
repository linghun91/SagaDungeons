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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        super(plugin, player, "&6通关条件编辑 - " + templateName, 54);
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
     * 添加通关条件选项
     */
    private void addCompletionConditions() {
        // 基础条件区域 (第二行)
        addBasicConditions();

        // 高级条件区域 (第三行)
        addAdvancedConditions();

        // 组合条件区域 (第四行)
        addCompositeConditions();
    }

    /**
     * 添加基础条件选项
     */
    private void addBasicConditions() {
        // 全部击杀条件
        ItemStack killAllItem = new ItemBuilder(Material.IRON_SWORD)
                .setName("&e全部击杀")
                .setLore(
                        "&7要求击杀副本中的所有怪物",
                        "&7适用于清理型副本",
                        "&7状态: " + getConditionStatus("killAll"),
                        "",
                        "&a左键点击启用/禁用",
                        "&c右键点击删除条件"
                )
                .build();
        inventory.setItem(10, killAllItem);

        // 击杀数量条件
        ItemStack killCountItem = new ItemBuilder(Material.DIAMOND_SWORD)
                .setName("&e击杀数量")
                .setLore(
                        "&7要求击杀指定数量的怪物",
                        "&7当前目标数量: &f" + getKillCountTarget(),
                        "&7状态: " + getConditionStatus("killCount"),
                        "",
                        "&a左键点击编辑数量",
                        "&c右键点击删除条件"
                )
                .build();
        inventory.setItem(12, killCountItem);

        // 击杀特定怪物条件
        ItemStack killSpecificItem = new ItemBuilder(Material.GOLDEN_SWORD)
                .setName("&e击杀特定怪物")
                .setLore(
                        "&7要求击杀指定类型的怪物",
                        "&7目标怪物: &f" + getSpecificMobTarget(),
                        "&7状态: " + getConditionStatus("killSpecific"),
                        "",
                        "&a左键点击编辑怪物类型",
                        "&c右键点击删除条件"
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
                .setName("&e到达区域")
                .setLore(
                        "&7要求玩家到达指定区域",
                        "&7目标位置: &f" + getReachAreaLocation(),
                        "&7检测半径: &f" + getReachAreaRadius() + " 格",
                        "&7状态: " + getConditionStatus("reachArea"),
                        "",
                        "&a左键点击设置位置",
                        "&7Shift+左键编辑半径",
                        "&c右键点击删除条件"
                )
                .build();
        inventory.setItem(21, reachAreaItem);
    }

    /**
     * 添加组合条件选项
     */
    private void addCompositeConditions() {
        // AND组合条件
        ItemStack andConditionItem = new ItemBuilder(Material.REDSTONE_BLOCK)
                .setName("&eAND组合条件")
                .setLore(
                        "&7要求满足所有子条件",
                        "&7子条件数量: &f" + getCompositeConditionCount("AND"),
                        "&7状态: " + getConditionStatus("compositeAND"),
                        "",
                        "&a左键点击管理子条件",
                        "&c右键点击删除条件"
                )
                .build();
        inventory.setItem(30, andConditionItem);

        // OR组合条件
        ItemStack orConditionItem = new ItemBuilder(Material.EMERALD_BLOCK)
                .setName("&eOR组合条件")
                .setLore(
                        "&7要求满足任意一个子条件",
                        "&7子条件数量: &f" + getCompositeConditionCount("OR"),
                        "&7状态: " + getConditionStatus("compositeOR"),
                        "",
                        "&a左键点击管理子条件",
                        "&c右键点击删除条件"
                )
                .build();
        inventory.setItem(32, orConditionItem);
    }

    /**
     * 获取条件状态
     */
    private String getConditionStatus(String conditionType) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return "&c未配置";
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection completionSection = config.getConfigurationSection("completion");

            if (completionSection == null) {
                return "&c未配置";
            }

            switch (conditionType) {
                case "killAll":
                    if (completionSection.contains("killAll")) {
                        boolean enabled = completionSection.getBoolean("killAll", false);
                        return enabled ? "&a已启用" : "&c已禁用";
                    }
                    return "&c未配置";

                case "killCount":
                    if (completionSection.contains("killCount")) {
                        return "&a已配置";
                    }
                    return "&c未配置";

                case "killSpecific":
                    if (completionSection.contains("killSpecific")) {
                        return "&a已配置";
                    }
                    return "&c未配置";

                case "reachArea":
                    if (completionSection.contains("reachArea")) {
                        return "&a已配置";
                    }
                    return "&c未配置";

                case "compositeAND":
                    ConfigurationSection compositeSection = completionSection.getConfigurationSection("composite");
                    if (compositeSection != null && "AND".equals(compositeSection.getString("type"))) {
                        return "&a已配置";
                    }
                    return "&c未配置";

                case "compositeOR":
                    ConfigurationSection compositeSection2 = completionSection.getConfigurationSection("composite");
                    if (compositeSection2 != null && "OR".equals(compositeSection2.getString("type"))) {
                        return "&a已配置";
                    }
                    return "&c未配置";

                default:
                    return "&c未知";
            }
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的通关条件状态时发生错误: " + e.getMessage());
            return "&c读取失败";
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
                return "未设置";
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection completionSection = config.getConfigurationSection("completion");

            if (completionSection == null) {
                return "未设置";
            }

            ConfigurationSection killSpecificSection = completionSection.getConfigurationSection("killSpecific");
            if (killSpecificSection != null) {
                String mobType = killSpecificSection.getString("mobType", "未设置");
                int count = killSpecificSection.getInt("count", 1);
                return mobType + " (数量: " + count + ")";
            }

            return "未设置";
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的特定怪物目标时发生错误: " + e.getMessage());
            return "读取失败";
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
                return "未设置";
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection completionSection = config.getConfigurationSection("completion");

            if (completionSection == null) {
                return "未设置";
            }

            ConfigurationSection reachAreaSection = completionSection.getConfigurationSection("reachArea");
            if (reachAreaSection != null) {
                String location = reachAreaSection.getString("location", "未设置");
                return location.equals("未设置") ? "未设置" : "已设置位置";
            }

            return "未设置";
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的到达区域位置时发生错误: " + e.getMessage());
            return "读取失败";
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
                return reachAreaSection.getDouble("radius", 3.0);
            }

            return 3.0;
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的到达区域半径时发生错误: " + e.getMessage());
            return 3.0;
        }
    }

    /**
     * 获取组合条件数量
     */
    private int getCompositeConditionCount(String type) {
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

            ConfigurationSection compositeSection = completionSection.getConfigurationSection("composite");
            if (compositeSection != null && type.equals(compositeSection.getString("type"))) {
                ConfigurationSection conditionsSection = compositeSection.getConfigurationSection("conditions");
                if (conditionsSection != null) {
                    Set<String> keys = conditionsSection.getKeys(false);
                    return keys.size();
                }
            }

            return 0;
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的组合条件数量时发生错误: " + e.getMessage());
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

        // 帮助按钮
        ItemStack helpButton = new ItemBuilder(Material.BOOK)
                .setName("&e帮助")
                .setLore(
                        "&7通关条件说明:",
                        "&8- &f全部击杀: 击杀副本中所有怪物",
                        "&8- &f击杀数量: 击杀指定数量的怪物",
                        "&8- &f击杀特定: 击杀特定类型的怪物",
                        "&8- &f到达区域: 玩家到达指定位置",
                        "&8- &fAND组合: 满足所有子条件",
                        "&8- &fOR组合: 满足任意子条件"
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
            case 30: // AND组合条件
                handleCompositeCondition(event, "AND");
                return;
            case 32: // OR组合条件
                handleCompositeCondition(event, "OR");
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

    /**
     * 处理组合条件
     */
    private void handleCompositeCondition(InventoryClickEvent event, String type) {
        if (event.isLeftClick()) {
            close();
            plugin.getGUIManager().openCompositeConditionManageGUI(player, templateName, type);
        } else if (event.isRightClick()) {
            // 删除条件
            MessageUtil.sendMessage(player, "command.admin.edit.completion.composite-removed",
                    MessageUtil.createPlaceholders("type", type));
            init(); // 刷新界面
        }
    }

    /**
     * 处理设置到达区域位置
     */
    private void handleSetReachAreaLocation() {
        try {
            // 获取玩家当前位置
            Location playerLocation = player.getLocation();

            // 将位置序列化为字符串
            String locationString = LocationUtil.locationToString(playerLocation);

            if (locationString == null) {
                MessageUtil.sendMessage(player, "command.admin.edit.completion.location-save-failed");
                return;
            }

            // 保存位置到配置文件
            setReachAreaLocation(locationString);

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
     * @param locationString 位置字符串
     */
    private void setReachAreaLocation(String locationString) {
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

            // 设置位置
            reachAreaSection.set("location", locationString);

            // 如果半径不存在，设置默认值
            if (!reachAreaSection.contains("radius")) {
                reachAreaSection.set("radius", 3.0);
            }

            // 保存配置文件
            config.save(configFile);

        } catch (Exception e) {
            plugin.getLogger().warning("保存到达区域位置配置时发生错误: " + e.getMessage());
            throw new RuntimeException("保存配置失败", e);
        }
    }

    /**
     * 设置到达区域半径到配置文件
     * @param radius 检测半径
     */
    private void setReachAreaRadius(double radius) {
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

            // 设置半径
            reachAreaSection.set("radius", radius);

            // 保存配置文件
            config.save(configFile);

        } catch (Exception e) {
            plugin.getLogger().warning("保存到达区域半径配置时发生错误: " + e.getMessage());
            throw new RuntimeException("保存配置失败", e);
        }
    }
}
