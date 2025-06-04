package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 模板编辑主界面
 * 显示所有模板，允许选择进入编辑
 */
public class TemplateEditMainGUI extends AbstractGUI {

    private final Map<String, DungeonTemplate> templates;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     */
    public TemplateEditMainGUI(SagaDungeons plugin, Player player) {
        super(plugin, player, "&6模板编辑管理", 54);
        this.templates = plugin.getConfigManager().getTemplateManager().getTemplates();
    }

    @Override
    public void init() {
        // 清空界面
        inventory.clear();

        // 添加装饰边框
        addBorder();

        // 添加模板物品
        addTemplateItems();

        // 添加功能按钮
        addFunctionButtons();
    }

    /**
     * 添加装饰边框
     */
    private void addBorder() {
        ItemStack borderItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
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
     * 添加模板物品
     */
    private void addTemplateItems() {
        int slot = 10;
        int row = 1;

        for (DungeonTemplate template : templates.values()) {
            // 检查是否需要换行
            if (slot % 9 == 8) {
                row++;
                slot = row * 9 + 1;
            }

            // 检查是否超出界面范围
            if (slot >= 44) {
                break;
            }

            // 创建模板物品
            ItemStack templateItem = createTemplateItem(template);
            inventory.setItem(slot, templateItem);

            slot++;
        }
    }

    /**
     * 创建模板物品
     * @param template 模板
     * @return 物品
     */
    private ItemStack createTemplateItem(DungeonTemplate template) {
        List<String> lore = new ArrayList<>();
        lore.add("&7模板名称: &f" + template.getName());
        lore.add("&7显示名称: " + template.getDisplayName());
        lore.add("&7世界显示: " + template.getWorldDisplay());
        lore.add("&7超时时间: &f" + template.getDefaultTimeout() + "秒");
        lore.add("");
        lore.add("&e左键点击进入编辑");
        lore.add("&c右键点击删除模板");

        return new ItemBuilder(Material.BOOK)
                .setName(template.getDisplayName())
                .setLore(lore)
                .build();
    }

    /**
     * 添加功能按钮
     */
    private void addFunctionButtons() {
        // 创建新模板按钮
        ItemStack createButton = new ItemBuilder(Material.EMERALD)
                .setName("&a创建新模板")
                .setLore(
                        "&7点击创建一个新的副本模板",
                        "",
                        "&e左键点击创建"
                )
                .build();
        inventory.setItem(49, createButton);

        // 返回按钮
        ItemStack backButton = new ItemBuilder(Material.BARRIER)
                .setName("&c返回")
                .setLore("&7返回上一级菜单")
                .build();
        inventory.setItem(53, backButton);

        // 刷新按钮
        ItemStack refreshButton = new ItemBuilder(Material.LIME_DYE)
                .setName("&a刷新")
                .setLore("&7刷新模板列表")
                .build();
        inventory.setItem(45, refreshButton);
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
        if (slot == 49) {
            // 创建新模板
            handleCreateTemplate();
            return;
        }

        if (slot == 53) {
            // 返回
            close();
            return;
        }

        if (slot == 45) {
            // 刷新
            init();
            return;
        }

        // 处理模板选择
        if (slot >= 10 && slot < 45 && slot % 9 != 0 && slot % 9 != 8) {
            handleTemplateClick(event, item);
        }
    }

    /**
     * 处理模板点击
     * @param event 点击事件
     * @param item 物品
     */
    private void handleTemplateClick(InventoryClickEvent event, ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }

        // 获取模板名称
        String templateName = null;
        for (DungeonTemplate template : templates.values()) {
            if (MessageUtil.colorize(template.getDisplayName()).equals(item.getItemMeta().getDisplayName())) {
                templateName = template.getName();
                break;
            }
        }

        if (templateName == null) {
            return;
        }

        // 根据点击类型处理
        if (event.isLeftClick()) {
            // 左键编辑模板
            close();
            // 打开模板基础信息编辑界面
            plugin.getGUIManager().openTemplateBasicEditGUI(player, templateName);
        } else if (event.isRightClick()) {
            // 右键删除模板
            handleDeleteTemplate(templateName);
        }
    }

    /**
     * 处理创建模板
     */
    private void handleCreateTemplate() {
        close();
        // TODO: 打开创建模板界面或提示使用命令
        MessageUtil.sendMessage(player, "command.admin.edit.create-template-hint");
    }

    /**
     * 处理删除模板
     * @param templateName 模板名称
     */
    private void handleDeleteTemplate(String templateName) {
        // TODO: 添加确认删除界面
        MessageUtil.sendMessage(player, "command.admin.edit.delete-template-hint",
                MessageUtil.createPlaceholders("template", templateName));
    }
}
