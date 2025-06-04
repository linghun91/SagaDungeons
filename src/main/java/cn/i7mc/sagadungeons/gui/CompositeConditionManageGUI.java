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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 组合条件管理界面
 * 用于管理副本模板的组合条件(AND/OR)及其子条件
 */
public class CompositeConditionManageGUI extends AbstractGUI {

    private final String templateName;
    private final String compositeType; // "AND" 或 "OR"
    private int currentPage = 0;
    private static final int CONDITIONS_PER_PAGE = 21; // 每页显示的条件数量

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     * @param templateName 模板名称
     * @param compositeType 组合类型 ("AND" 或 "OR")
     */
    public CompositeConditionManageGUI(SagaDungeons plugin, Player player, String templateName, String compositeType) {
        super(plugin, player, "组合条件管理 - " + compositeType + " - " + templateName, 54);
        this.templateName = templateName;
        this.compositeType = compositeType;
    }

    @Override
    public void init() {
        // 清空界面
        inventory.clear();

        // 添加组合条件信息显示
        addCompositeInfo();

        // 添加子条件列表
        addConditionsList();

        // 添加操作按钮
        addActionButtons();

        // 添加分页按钮
        addPaginationButtons();

        // 添加返回按钮
        addBackButton();
    }

    /**
     * 添加组合条件信息显示
     */
    private void addCompositeInfo() {
        // 组合条件类型显示
        ItemStack typeItem = new ItemBuilder(Material.REDSTONE_BLOCK)
                .setName("&e组合条件类型: &f" + compositeType)
                .setLore(
                        "&7当前组合类型: &f" + compositeType,
                        "&7优先级: &f" + getCompositePriority(),
                        "&7子条件数量: &f" + getSubConditionsCount(),
                        "",
                        "&a左键点击切换类型",
                        "&c右键点击设置优先级"
                )
                .build();
        inventory.setItem(4, typeItem);
    }

    /**
     * 添加子条件列表
     */
    private void addConditionsList() {
        List<String> subConditions = getSubConditionsList();
        int startIndex = currentPage * CONDITIONS_PER_PAGE;
        int endIndex = Math.min(startIndex + CONDITIONS_PER_PAGE, subConditions.size());

        // 显示子条件
        for (int i = startIndex; i < endIndex; i++) {
            String conditionKey = subConditions.get(i);
            int slot = 9 + (i - startIndex); // 从第二行开始显示

            // 跳过分页和操作按钮位置
            if (slot >= 45) break;

            ItemStack conditionItem = createConditionItem(conditionKey, i);
            inventory.setItem(slot, conditionItem);
        }
    }

    /**
     * 创建子条件显示物品
     */
    private ItemStack createConditionItem(String conditionKey, int index) {
        ConfigurationSection conditionSection = getConditionSection(conditionKey);
        if (conditionSection == null) {
            return new ItemBuilder(Material.BARRIER)
                    .setName("&c无效条件")
                    .setLore("&7条件键: &f" + conditionKey)
                    .build();
        }

        // 根据条件类型创建不同的显示
        Material material = Material.PAPER;
        List<String> lore = new ArrayList<>();
        lore.add("&7条件键: &f" + conditionKey);
        lore.add("&7索引: &f" + (index + 1));

        // 检查条件类型并添加相应信息
        if (conditionSection.getBoolean("killAll", false)) {
            material = Material.DIAMOND_SWORD;
            lore.add("&7类型: &f全部击杀");
        } else if (conditionSection.contains("killCount")) {
            material = Material.IRON_SWORD;
            lore.add("&7类型: &f击杀数量");
            lore.add("&7目标: &f" + conditionSection.getInt("killCount"));
        } else if (conditionSection.contains("killSpecific")) {
            material = Material.GOLDEN_SWORD;
            lore.add("&7类型: &f击杀特定怪物");
            lore.add("&7目标: &f" + conditionSection.getString("killSpecific"));
        } else if (conditionSection.contains("reachArea")) {
            material = Material.COMPASS;
            lore.add("&7类型: &f到达区域");
            ConfigurationSection reachArea = conditionSection.getConfigurationSection("reachArea");
            if (reachArea != null) {
                lore.add("&7位置: &f" + reachArea.getString("location", "未设置"));
                lore.add("&7半径: &f" + reachArea.getDouble("radius", 3.0));
            }
        }

        lore.add("");
        lore.add("&a左键点击编辑条件");
        lore.add("&c右键点击删除条件");

        return new ItemBuilder(material)
                .setName("&e子条件 #" + (index + 1))
                .setLore(lore)
                .build();
    }

    /**
     * 添加操作按钮
     */
    private void addActionButtons() {
        // 添加新条件按钮
        ItemStack addButton = new ItemBuilder(Material.GREEN_WOOL)
                .setName("&a添加新条件")
                .setLore(
                        "&7点击添加新的子条件",
                        "&7可选类型:",
                        "&8- &f全部击杀",
                        "&8- &f击杀数量",
                        "&8- &f击杀特定怪物",
                        "&8- &f到达区域"
                )
                .build();
        inventory.setItem(45, addButton);

        // 保存配置按钮
        ItemStack saveButton = new ItemBuilder(Material.EMERALD)
                .setName("&a保存配置")
                .setLore(
                        "&7保存当前组合条件配置",
                        "&7到模板配置文件"
                )
                .build();
        inventory.setItem(53, saveButton);
    }

    /**
     * 添加分页按钮
     */
    private void addPaginationButtons() {
        int totalConditions = getSubConditionsCount();
        int totalPages = (totalConditions + CONDITIONS_PER_PAGE - 1) / CONDITIONS_PER_PAGE;

        if (totalPages > 1) {
            // 上一页按钮
            if (currentPage > 0) {
                ItemStack prevButton = new ItemBuilder(Material.ARROW)
                        .setName("&e上一页")
                        .setLore("&7当前页: &f" + (currentPage + 1) + "/" + totalPages)
                        .build();
                inventory.setItem(46, prevButton);
            }

            // 下一页按钮
            if (currentPage < totalPages - 1) {
                ItemStack nextButton = new ItemBuilder(Material.ARROW)
                        .setName("&e下一页")
                        .setLore("&7当前页: &f" + (currentPage + 1) + "/" + totalPages)
                        .build();
                inventory.setItem(52, nextButton);
            }
        }
    }

    /**
     * 添加返回按钮
     */
    private void addBackButton() {
        ItemStack backButton = new ItemBuilder(Material.BARRIER)
                .setName("&c返回")
                .setLore("&7返回通关条件编辑界面")
                .build();
        inventory.setItem(49, backButton);
    }

    /**
     * 获取组合条件优先级
     */
    private int getCompositePriority() {
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
            if (compositeSection != null && compositeType.equals(compositeSection.getString("type"))) {
                return compositeSection.getInt("priority", 0);
            }

            return 0;
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的组合条件优先级时发生错误: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 获取子条件数量
     */
    private int getSubConditionsCount() {
        return getSubConditionsList().size();
    }

    /**
     * 获取子条件列表
     */
    private List<String> getSubConditionsList() {
        List<String> conditions = new ArrayList<>();
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return conditions;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection completionSection = config.getConfigurationSection("completion");
            if (completionSection == null) {
                return conditions;
            }

            ConfigurationSection compositeSection = completionSection.getConfigurationSection("composite");
            if (compositeSection != null && compositeType.equals(compositeSection.getString("type"))) {
                ConfigurationSection conditionsSection = compositeSection.getConfigurationSection("conditions");
                if (conditionsSection != null) {
                    Set<String> keys = conditionsSection.getKeys(false);
                    conditions.addAll(keys);
                }
            }

            return conditions;
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的子条件列表时发生错误: " + e.getMessage());
            return conditions;
        }
    }

    /**
     * 获取指定条件的配置节点
     */
    private ConfigurationSection getConditionSection(String conditionKey) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return null;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection completionSection = config.getConfigurationSection("completion");
            if (completionSection == null) {
                return null;
            }

            ConfigurationSection compositeSection = completionSection.getConfigurationSection("composite");
            if (compositeSection != null && compositeType.equals(compositeSection.getString("type"))) {
                ConfigurationSection conditionsSection = compositeSection.getConfigurationSection("conditions");
                if (conditionsSection != null) {
                    return conditionsSection.getConfigurationSection(conditionKey);
                }
            }

            return null;
        } catch (Exception e) {
            plugin.getLogger().warning("读取模板 " + templateName + " 的条件配置时发生错误: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        switch (slot) {
            case 4: // 组合条件类型
                handleCompositeTypeClick(event);
                return;
            case 45: // 添加新条件
                handleAddCondition();
                return;
            case 46: // 上一页
                if (currentPage > 0) {
                    currentPage--;
                    init();
                }
                return;
            case 49: // 返回
                close();
                plugin.getGUIManager().openTemplateCompletionEditGUI(player, templateName);
                return;
            case 52: // 下一页
                int totalConditions = getSubConditionsCount();
                int totalPages = (totalConditions + CONDITIONS_PER_PAGE - 1) / CONDITIONS_PER_PAGE;
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    init();
                }
                return;
            case 53: // 保存配置
                handleSaveConfiguration();
                return;
        }

        // 处理子条件点击
        if (slot >= 9 && slot < 45) {
            handleConditionClick(event, slot);
        }
    }

    /**
     * 处理组合条件类型点击
     */
    private void handleCompositeTypeClick(InventoryClickEvent event) {
        if (event.isLeftClick()) {
            // 切换组合类型
            String newType = compositeType.equals("AND") ? "OR" : "AND";
            switchCompositeType(newType);
            MessageUtil.sendMessage(player, "command.admin.edit.completion.composite-type-switched",
                    MessageUtil.createPlaceholders("old", compositeType, "new", newType));
        } else if (event.isRightClick()) {
            // 设置优先级
            close();
            MessageUtil.sendMessage(player, "command.admin.edit.completion.input-composite-priority");
            plugin.getChatInputListener().requestNumberInput(player, "command.admin.edit.completion.input-composite-priority", priorityStr -> {
                try {
                    int priority = Integer.parseInt(priorityStr);
                    setCompositePriority(priority);
                    MessageUtil.sendMessage(player, "command.admin.edit.completion.composite-priority-updated",
                            MessageUtil.createPlaceholders("priority", String.valueOf(priority)));
                    plugin.getGUIManager().openCompositeConditionManageGUI(player, templateName, compositeType);
                } catch (NumberFormatException e) {
                    MessageUtil.sendMessage(player, "command.admin.edit.completion.composite-save-failed");
                    plugin.getGUIManager().openCompositeConditionManageGUI(player, templateName, compositeType);
                }
            });
        }
    }

    /**
     * 处理添加条件
     */
    private void handleAddCondition() {
        close();
        // 打开条件类型选择界面
        openConditionTypeSelectionGUI();
    }

    /**
     * 处理条件点击
     */
    private void handleConditionClick(InventoryClickEvent event, int slot) {
        int conditionIndex = currentPage * CONDITIONS_PER_PAGE + (slot - 9);
        List<String> conditions = getSubConditionsList();

        if (conditionIndex >= 0 && conditionIndex < conditions.size()) {
            String conditionKey = conditions.get(conditionIndex);

            if (event.isLeftClick()) {
                // 编辑条件
                MessageUtil.sendMessage(player, "command.admin.edit.completion.composite-edit-condition-hint",
                        MessageUtil.createPlaceholders("condition", conditionKey));
                // TODO: 实现条件编辑逻辑 - 可以在后续版本中实现
            } else if (event.isRightClick()) {
                // 删除条件
                deleteSubCondition(conditionKey);
                MessageUtil.sendMessage(player, "command.admin.edit.completion.composite-condition-deleted",
                        MessageUtil.createPlaceholders("condition", conditionKey));
                init(); // 刷新界面
            }
        }
    }

    /**
     * 打开条件类型选择界面
     */
    private void openConditionTypeSelectionGUI() {
        // 创建一个简单的条件类型选择界面
        CompositeConditionTypeSelectionGUI selectionGUI = new CompositeConditionTypeSelectionGUI(
                plugin, player, templateName, compositeType);
        selectionGUI.open();
    }

    /**
     * 处理保存配置
     */
    private void handleSaveConfiguration() {
        try {
            saveCompositeConfiguration();
            MessageUtil.sendMessage(player, "command.admin.edit.completion.composite-saved");
            init(); // 刷新界面
        } catch (Exception e) {
            plugin.getLogger().warning("保存组合条件配置时发生错误: " + e.getMessage());
            MessageUtil.sendMessage(player, "command.admin.edit.completion.composite-save-failed");
        }
    }

    /**
     * 切换组合条件类型
     */
    private void switchCompositeType(String newType) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
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
            }

            compositeSection.set("type", newType);
            config.save(configFile);

        } catch (Exception e) {
            plugin.getLogger().warning("切换组合条件类型时发生错误: " + e.getMessage());
        }
    }

    /**
     * 设置组合条件优先级
     */
    private void setCompositePriority(int priority) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
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
            }

            compositeSection.set("priority", priority);
            config.save(configFile);

        } catch (Exception e) {
            plugin.getLogger().warning("设置组合条件优先级时发生错误: " + e.getMessage());
        }
    }

    /**
     * 保存组合条件配置
     */
    private void saveCompositeConfiguration() throws IOException {
        File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
        File configFile = new File(templateDir, "config.yml");

        if (!configFile.exists()) {
            throw new IOException("配置文件不存在: " + configFile.getPath());
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

        config.save(configFile);
    }

    /**
     * 删除子条件
     */
    private void deleteSubCondition(String conditionKey) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection completionSection = config.getConfigurationSection("completion");
            if (completionSection == null) {
                return;
            }

            ConfigurationSection compositeSection = completionSection.getConfigurationSection("composite");
            if (compositeSection != null && compositeType.equals(compositeSection.getString("type"))) {
                ConfigurationSection conditionsSection = compositeSection.getConfigurationSection("conditions");
                if (conditionsSection != null) {
                    conditionsSection.set(conditionKey, null);
                    config.save(configFile);
                }
            }

        } catch (Exception e) {
            plugin.getLogger().warning("删除子条件时发生错误: " + e.getMessage());
        }
    }
}
