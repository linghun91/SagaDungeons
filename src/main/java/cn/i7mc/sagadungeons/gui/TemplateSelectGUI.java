package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模板选择界面
 * 用于选择副本模板
 */
public class TemplateSelectGUI extends AbstractGUI {

    private final Map<String, DungeonTemplate> templates;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     */
    public TemplateSelectGUI(SagaDungeons plugin, Player player) {
        super(plugin, player, plugin.getConfigManager().getGUILanguageManager().getGUIText("template-select.title"), 54);
        this.templates = plugin.getConfigManager().getTemplateManager().getTemplates();
    }

    /**
     * 初始化界面
     */
    @Override
    public void init() {
        // 填充边框
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(getGUIText("common.border")).build());
            inventory.setItem(45 + i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(getGUIText("common.border")).build());
        }

        for (int i = 0; i < 5; i++) {
            inventory.setItem(i * 9, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(getGUIText("common.border")).build());
            inventory.setItem(i * 9 + 8, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(getGUIText("common.border")).build());
        }

        // 添加模板物品
        int slot = 10;
        for (DungeonTemplate template : templates.values()) {
            // 创建物品
            ItemStack item = createTemplateItem(template);

            // 添加到界面
            inventory.setItem(slot, item);

            // 更新槽位
            slot++;
            if (slot % 9 == 8) {
                slot += 2;
            }

            // 检查是否超出界面
            if (slot >= 45) {
                break;
            }
        }

        // 添加关闭按钮
        inventory.setItem(49, new ItemBuilder(Material.BARRIER).setName(getGUIText("common.close")).build());
    }

    /**
     * 处理点击事件
     * @param event 点击事件
     */
    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);

        // 获取点击的物品
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || item.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            return;
        }

        // 获取点击的槽位
        int slot = event.getRawSlot();

        // 处理关闭按钮
        if (slot == 49) {
            close();
            return;
        }

        // 处理模板选择
        if (slot >= 10 && slot < 45 && slot % 9 != 0 && slot % 9 != 8) {
            // 获取物品元数据
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

            // 关闭界面
            close();

            // 创建副本
            boolean success = plugin.getDungeonManager().createDungeon(player, templateName);

            // 发送消息
            if (success) {
                MessageUtil.sendMessage(player, "command.create.success",
                        MessageUtil.createPlaceholders("template", templateName));
            } else {
                MessageUtil.sendMessage(player, "command.create.fail");
            }
        }
    }

    /**
     * 创建模板物品
     * @param template 模板
     * @return 物品
     */
    private ItemStack createTemplateItem(DungeonTemplate template) {
        // 创建物品构建器
        ItemBuilder builder = new ItemBuilder(Material.BOOK);

        // 设置名称
        builder.setName(template.getDisplayName());

        // 创建描述
        List<String> lore = new ArrayList<>();
        lore.add(getGUIText("template-select.click-to-create"));
        lore.add("");

        // 添加条件信息
        if (template.hasMoneyCost()) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.valueOf(template.getMoneyCost()));
            lore.add(getGUIText("template-select.need-money", placeholders));
        }

        if (template.hasPointsCost()) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.valueOf(template.getPointsCost()));
            lore.add(getGUIText("template-select.need-points", placeholders));
        }

        if (template.hasLevelRequirement()) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("level", String.valueOf(template.getLevelRequirement()));
            lore.add(getGUIText("template-select.need-level", placeholders));
        }

        lore.add("");
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("time", String.valueOf(template.getDefaultTimeout() / 60));
        lore.add(getGUIText("template-select.timeout", placeholders));

        // 设置描述
        builder.setLore(lore);

        // 构建物品
        return builder.build();
    }
}
