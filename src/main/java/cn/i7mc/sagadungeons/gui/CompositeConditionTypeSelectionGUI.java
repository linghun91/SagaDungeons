package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.UUID;

/**
 * 组合条件类型选择界面
 * 用于选择要添加到组合条件中的子条件类型
 */
public class CompositeConditionTypeSelectionGUI extends AbstractGUI {

    private final String templateName;
    private final String compositeType;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     * @param templateName 模板名称
     * @param compositeType 组合类型
     */
    public CompositeConditionTypeSelectionGUI(SagaDungeons plugin, Player player, String templateName, String compositeType) {
        super(plugin, player, "选择条件类型 - " + compositeType, 27);
        this.templateName = templateName;
        this.compositeType = compositeType;
    }

    @Override
    public void init() {
        // 清空界面
        inventory.clear();

        // 添加条件类型选项
        addConditionTypes();

        // 添加返回按钮
        addBackButton();
    }

    /**
     * 添加条件类型选项
     */
    private void addConditionTypes() {
        // 全部击杀条件
        ItemStack killAllItem = new ItemBuilder(Material.DIAMOND_SWORD)
                .setName("&e全部击杀条件")
                .setLore(
                        "&7要求击杀副本中的所有怪物",
                        "&7这是最常用的通关条件",
                        "",
                        "&a点击添加此条件"
                )
                .build();
        inventory.setItem(10, killAllItem);

        // 击杀数量条件
        ItemStack killCountItem = new ItemBuilder(Material.IRON_SWORD)
                .setName("&e击杀数量条件")
                .setLore(
                        "&7要求击杀指定数量的怪物",
                        "&7可以设置具体的击杀目标数量",
                        "",
                        "&a点击添加此条件"
                )
                .build();
        inventory.setItem(12, killCountItem);

        // 击杀特定怪物条件
        ItemStack killSpecificItem = new ItemBuilder(Material.GOLDEN_SWORD)
                .setName("&e击杀特定怪物条件")
                .setLore(
                        "&7要求击杀特定类型的怪物",
                        "&7可以指定MythicMobs怪物类型",
                        "",
                        "&a点击添加此条件"
                )
                .build();
        inventory.setItem(14, killSpecificItem);

        // 到达区域条件
        ItemStack reachAreaItem = new ItemBuilder(Material.COMPASS)
                .setName("&e到达区域条件")
                .setLore(
                        "&7要求玩家到达指定区域",
                        "&7可以设置目标位置和检测半径",
                        "",
                        "&a点击添加此条件"
                )
                .build();
        inventory.setItem(16, reachAreaItem);
    }

    /**
     * 添加返回按钮
     */
    private void addBackButton() {
        ItemStack backButton = new ItemBuilder(Material.BARRIER)
                .setName("&c返回")
                .setLore("&7返回组合条件管理界面")
                .build();
        inventory.setItem(22, backButton);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        switch (slot) {
            case 10: // 全部击杀条件
                addKillAllCondition();
                break;
            case 12: // 击杀数量条件
                addKillCountCondition();
                break;
            case 14: // 击杀特定怪物条件
                addKillSpecificCondition();
                break;
            case 16: // 到达区域条件
                addReachAreaCondition();
                break;
            case 22: // 返回
                close();
                plugin.getGUIManager().openCompositeConditionManageGUI(player, templateName, compositeType);
                break;
        }
    }

    /**
     * 添加全部击杀条件
     */
    private void addKillAllCondition() {
        String conditionKey = "killAll_" + UUID.randomUUID().toString().substring(0, 8);
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                MessageUtil.sendMessage(player, "command.admin.edit.completion.composite-save-failed");
                return;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection completionSection = config.getConfigurationSection("completion");
            if (completionSection == null) {
                completionSection = config.createSection("completion");
            }

            ConfigurationSection compositeSection = completionSection.getConfigurationSection("composite");
            if (compositeSection == null) {
                compositeSection = completionSection.createSection("composite");
                compositeSection.set("type", compositeType);
                compositeSection.set("priority", 0);
            }

            ConfigurationSection conditionsSection = compositeSection.getConfigurationSection("conditions");
            if (conditionsSection == null) {
                conditionsSection = compositeSection.createSection("conditions");
            }

            ConfigurationSection conditionSection = conditionsSection.createSection(conditionKey);
            conditionSection.set("killAll", true);

            config.save(configFile);

            MessageUtil.sendMessage(player, "command.admin.edit.completion.kill-all-toggle");
            close();
            plugin.getGUIManager().openCompositeConditionManageGUI(player, templateName, compositeType);

        } catch (Exception e) {
            plugin.getLogger().warning("添加全部击杀条件时发生错误: " + e.getMessage());
            MessageUtil.sendMessage(player, "command.admin.edit.completion.composite-save-failed");
        }
    }

    /**
     * 添加击杀数量条件
     */
    private void addKillCountCondition() {
        close();
        MessageUtil.sendMessage(player, "command.admin.edit.completion.input-kill-count");
        plugin.getChatInputListener().requestNumberInput(player, "command.admin.edit.completion.input-kill-count", countStr -> {
            try {
                int count = Integer.parseInt(countStr);
                String conditionKey = "killCount_" + UUID.randomUUID().toString().substring(0, 8);

                File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
                File configFile = new File(templateDir, "config.yml");

                if (!configFile.exists()) {
                    MessageUtil.sendMessage(player, "command.admin.edit.completion.composite-save-failed");
                    return;
                }

                YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                ConfigurationSection completionSection = config.getConfigurationSection("completion");
                if (completionSection == null) {
                    completionSection = config.createSection("completion");
                }

                ConfigurationSection compositeSection = completionSection.getConfigurationSection("composite");
                if (compositeSection == null) {
                    compositeSection = completionSection.createSection("composite");
                    compositeSection.set("type", compositeType);
                    compositeSection.set("priority", 0);
                }

                ConfigurationSection conditionsSection = compositeSection.getConfigurationSection("conditions");
                if (conditionsSection == null) {
                    conditionsSection = compositeSection.createSection("conditions");
                }

                ConfigurationSection conditionSection = conditionsSection.createSection(conditionKey);
                conditionSection.set("killCount", count);

                config.save(configFile);

                MessageUtil.sendMessage(player, "command.admin.edit.completion.kill-count-updated",
                        MessageUtil.createPlaceholders("count", String.valueOf(count)));
                plugin.getGUIManager().openCompositeConditionManageGUI(player, templateName, compositeType);

            } catch (Exception e) {
                plugin.getLogger().warning("添加击杀数量条件时发生错误: " + e.getMessage());
                MessageUtil.sendMessage(player, "command.admin.edit.completion.composite-save-failed");
            }
        });
    }

    /**
     * 添加击杀特定怪物条件
     */
    private void addKillSpecificCondition() {
        close();
        MessageUtil.sendMessage(player, "command.admin.edit.completion.input-specific-mob");
        plugin.getChatInputListener().requestTextInput(player, "command.admin.edit.completion.input-specific-mob", mobType -> {
            String conditionKey = "killSpecific_" + UUID.randomUUID().toString().substring(0, 8);
            try {
                File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
                File configFile = new File(templateDir, "config.yml");

                if (!configFile.exists()) {
                    MessageUtil.sendMessage(player, "command.admin.edit.completion.composite-save-failed");
                    return;
                }

                YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                ConfigurationSection completionSection = config.getConfigurationSection("completion");
                if (completionSection == null) {
                    completionSection = config.createSection("completion");
                }

                ConfigurationSection compositeSection = completionSection.getConfigurationSection("composite");
                if (compositeSection == null) {
                    compositeSection = completionSection.createSection("composite");
                    compositeSection.set("type", compositeType);
                    compositeSection.set("priority", 0);
                }

                ConfigurationSection conditionsSection = compositeSection.getConfigurationSection("conditions");
                if (conditionsSection == null) {
                    conditionsSection = compositeSection.createSection("conditions");
                }

                ConfigurationSection conditionSection = conditionsSection.createSection(conditionKey);
                conditionSection.set("killSpecific", mobType);

                config.save(configFile);

                MessageUtil.sendMessage(player, "command.admin.edit.completion.kill-specific-updated",
                        MessageUtil.createPlaceholders("mob", mobType));
                plugin.getGUIManager().openCompositeConditionManageGUI(player, templateName, compositeType);

            } catch (Exception e) {
                plugin.getLogger().warning("添加击杀特定怪物条件时发生错误: " + e.getMessage());
                MessageUtil.sendMessage(player, "command.admin.edit.completion.composite-save-failed");
            }
        });
    }

    /**
     * 添加到达区域条件
     */
    private void addReachAreaCondition() {
        close();
        MessageUtil.sendMessage(player, "command.admin.edit.completion.set-reach-area-location");
        // 使用玩家当前位置作为目标位置
        String locationString = player.getLocation().getWorld().getName() + "," +
                player.getLocation().getX() + "," +
                player.getLocation().getY() + "," +
                player.getLocation().getZ();

        MessageUtil.sendMessage(player, "command.admin.edit.completion.input-reach-area-radius");
        plugin.getChatInputListener().requestDecimalInput(player, "command.admin.edit.completion.input-reach-area-radius", radiusStr -> {
            try {
                double radius = Double.parseDouble(radiusStr);
                String conditionKey = "reachArea_" + UUID.randomUUID().toString().substring(0, 8);
                File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
                File configFile = new File(templateDir, "config.yml");

                if (!configFile.exists()) {
                    MessageUtil.sendMessage(player, "command.admin.edit.completion.composite-save-failed");
                    return;
                }

                YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                ConfigurationSection completionSection = config.getConfigurationSection("completion");
                if (completionSection == null) {
                    completionSection = config.createSection("completion");
                }

                ConfigurationSection compositeSection = completionSection.getConfigurationSection("composite");
                if (compositeSection == null) {
                    compositeSection = completionSection.createSection("composite");
                    compositeSection.set("type", compositeType);
                    compositeSection.set("priority", 0);
                }

                ConfigurationSection conditionsSection = compositeSection.getConfigurationSection("conditions");
                if (conditionsSection == null) {
                    conditionsSection = compositeSection.createSection("conditions");
                }

                ConfigurationSection conditionSection = conditionsSection.createSection(conditionKey);
                ConfigurationSection reachAreaSection = conditionSection.createSection("reachArea");
                reachAreaSection.set("location", locationString);
                reachAreaSection.set("radius", radius);

                config.save(configFile);

                MessageUtil.sendMessage(player, "command.admin.edit.completion.reach-area-radius-updated",
                        MessageUtil.createPlaceholders("radius", String.valueOf(radius)));
                plugin.getGUIManager().openCompositeConditionManageGUI(player, templateName, compositeType);

            } catch (Exception e) {
                plugin.getLogger().warning("添加到达区域条件时发生错误: " + e.getMessage());
                MessageUtil.sendMessage(player, "command.admin.edit.completion.composite-save-failed");
            }
        });
    }
}
