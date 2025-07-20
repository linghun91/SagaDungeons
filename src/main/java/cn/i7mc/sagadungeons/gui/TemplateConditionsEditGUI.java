package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.util.DebugUtil;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        super(plugin, player, createGUITitle(plugin, templateName), 54);
        this.templateName = templateName;
        this.template = plugin.getConfigManager().getTemplateManager().getTemplates().get(templateName);
    }

    /**
     * 创建GUI标题
     */
    protected static String createGUITitle(SagaDungeons plugin, String templateName) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("template", templateName);
        return plugin.getConfigManager().getGUILanguageManager().getGUIText("template-conditions-edit.title", placeholders);
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
     * 添加条件编辑选项
     */
    private void addConditionOptions() {
        // 等级条件
        ItemStack levelItem = new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setName(getGUIText("template-conditions-edit.level-condition"))
                .setLore(createLevelLore())
                .build();
        inventory.setItem(13, levelItem);

        // 等级开关
        ItemStack levelToggleItem = new ItemBuilder(
                template.isLevelEnabled() ? Material.LIME_DYE : Material.GRAY_DYE)
                .setName(getGUIText("template-conditions-edit.level-toggle"))
                .setLore(createToggleLore("level", template.isLevelEnabled()))
                .build();
        inventory.setItem(31, levelToggleItem);
    }


    /**
     * 创建等级条件说明
     */
    private List<String> createLevelLore() {
        List<String> lore = new ArrayList<>();
        int levelRequirement = getLevelConditionFromConfig();
        boolean levelEnabled = getLevelEnabledFromConfig();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("level", String.valueOf(levelRequirement));
        placeholders.put("status", levelEnabled ? getGUIText("template-conditions-edit.status-enabled") : getGUIText("template-conditions-edit.status-disabled"));

        lore.add(getGUIText("template-conditions-edit.level-condition-current", placeholders));
        lore.add(getGUIText("template-conditions-edit.level-condition-status", placeholders));
        lore.add("");
        lore.add(getGUIText("template-conditions-edit.level-condition-edit-lore"));
        lore.add(getGUIText("template-conditions-edit.level-condition-input-hint"));
        return lore;
    }


    /**
     * 创建开关按钮说明
     */
    private List<String> createToggleLore(String type, boolean enabled) {
        List<String> lore = new ArrayList<>();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("status", enabled ? getGUIText("template-conditions-edit.status-enabled") : getGUIText("template-conditions-edit.status-disabled"));

        lore.add(getGUIText("template-conditions-edit." + type + "-toggle-status", placeholders));
        lore.add("");
        lore.add(getGUIText("template-conditions-edit." + type + "-toggle-click"));
        lore.add(getGUIText("template-conditions-edit." + type + "-toggle-desc"));
        return lore;
    }

    /**
     * 添加功能按钮
     */
    private void addFunctionButtons() {
        // 保存按钮
        ItemStack saveButton = new ItemBuilder(Material.EMERALD)
                .setName(getGUIText("template-conditions-edit.save-button"))
                .setLore(
                        getGUIText("template-conditions-edit.save-button-lore"),
                        "",
                        getGUIText("template-conditions-edit.save-button-click")
                )
                .build();
        inventory.setItem(49, saveButton);

        // 返回按钮
        ItemStack backButton = new ItemBuilder(Material.ARROW)
                .setName(getGUIText("template-conditions-edit.back-button"))
                .setLore(getGUIText("template-conditions-edit.back-button-lore"))
                .build();
        inventory.setItem(45, backButton);

        // 刷新按钮
        ItemStack refreshButton = new ItemBuilder(Material.LIME_DYE)
                .setName(getGUIText("template-conditions-edit.refresh-button"))
                .setLore(getGUIText("template-conditions-edit.refresh-button-lore"))
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
            case 13: // 等级条件编辑
                handleLevelEdit();
                return;
            case 31: // 等级开关
                handleLevelToggle();
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
        plugin.getGUIManager().openTemplateBasicEditGUI(player, templateName);
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
     * 处理等级开关切换
     */
    private void handleLevelToggle() {
        template.setLevelEnabled(!template.isLevelEnabled());
        init(); // 刷新界面
        String statusText = template.isLevelEnabled() ?
                getGUIText("template-conditions-edit.status-enabled") :
                getGUIText("template-conditions-edit.status-disabled");
        MessageUtil.sendMessage(player, "command.admin.edit.level-toggle",
                MessageUtil.createPlaceholders("status", statusText));
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
            DebugUtil.debug("template-conditions.read-level-error",
                    DebugUtil.createPlaceholders("template", templateName, "message", e.getMessage()));
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
            DebugUtil.debug("template-conditions.read-level-enabled-error",
                    DebugUtil.createPlaceholders("template", templateName, "message", e.getMessage()));
            return true;
        }
    }

}
