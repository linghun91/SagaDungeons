package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.util.ItemStackUtil;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 物品条件管理界面
 * 用于管理模板的物品条件
 */
public class ItemConditionManageGUI extends AbstractGUI {

    private final String templateName;
    private final List<ItemConditionEntry> itemConditions = new ArrayList<>();

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     * @param templateName 模板名称
     */
    public ItemConditionManageGUI(SagaDungeons plugin, Player player, String templateName) {
        super(plugin, player, createGUITitle(plugin, templateName), 54);
        this.templateName = templateName;
    }

    /**
     * 创建GUI标题
     */
    protected static String createGUITitle(SagaDungeons plugin, String templateName) {
        return plugin.getConfigManager().getGUILanguageManager().getGUIText("item-condition-manage.title",
                createPlaceholderMap("template", templateName));
    }

    /**
     * 创建占位符映射的静态方法
     */
    private static java.util.Map<String, String> createPlaceholderMap(String key, String value) {
        java.util.Map<String, String> placeholders = new java.util.HashMap<>();
        placeholders.put(key, value);
        return placeholders;
    }

    @Override
    public void init() {
        // 清空界面
        inventory.clear();

        // 添加装饰性玻璃
        addDecorationGlass();

        // 加载物品条件数据
        loadItemConditions();

        // 显示物品条件
        displayItemConditions();

        // 添加功能按钮
        addFunctionButtons();
    }

    /**
     * 添加装饰性玻璃
     */
    private void addDecorationGlass() {
        ItemStack glass = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setName(" ")
                .build();

        // 边框装饰
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, glass);
        }
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, glass);
        }
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, glass);
            inventory.setItem(i + 8, glass);
        }
    }

    /**
     * 添加功能按钮
     */
    private void addFunctionButtons() {
        // 添加物品条件按钮
        ItemStack addItem = new ItemBuilder(Material.EMERALD)
                .setName(getGUIText("item-condition-manage.add-hand-item"))
                .setLore(getGUITextList("item-condition-manage.add-hand-item-lore"))
                .build();
        inventory.setItem(46, addItem);

        // 返回按钮
        ItemStack backItem = new ItemBuilder(Material.ARROW)
                .setName(getGUIText("item-condition-manage.back-button"))
                .setLore(getGUIText("item-condition-manage.back-button-lore"))
                .build();
        inventory.setItem(48, backItem);

        // 保存按钮
        ItemStack saveItem = new ItemBuilder(Material.WRITABLE_BOOK)
                .setName(getGUIText("item-condition-manage.save-config"))
                .setLore(getGUITextList("item-condition-manage.save-config-lore"))
                .build();
        inventory.setItem(49, saveItem);

        // 刷新按钮
        ItemStack refreshItem = new ItemBuilder(Material.CLOCK)
                .setName(getGUIText("item-condition-manage.refresh-interface"))
                .setLore(getGUIText("item-condition-manage.refresh-interface-lore"))
                .build();
        inventory.setItem(50, refreshItem);

        // 清空所有条件按钮
        ItemStack clearItem = new ItemBuilder(Material.BARRIER)
                .setName(getGUIText("item-condition-manage.clear-all-conditions"))
                .setLore(getGUITextList("item-condition-manage.clear-all-conditions-lore"))
                .build();
        inventory.setItem(52, clearItem);
    }

    /**
     * 显示物品条件
     */
    private void displayItemConditions() {
        int slot = 10;
        for (int i = 0; i < itemConditions.size() && slot <= 34; i++) {
            // 跳过边框位置
            if (slot % 9 == 0 || slot % 9 == 8) {
                slot++;
                if (slot > 34) break;
                i--; // 重新处理当前条件
                continue;
            }

            ItemConditionEntry entry = itemConditions.get(i);
            ItemStack displayItem = createConditionDisplayItem(entry, i);
            inventory.setItem(slot, displayItem);
            slot++;
        }
    }

    /**
     * 创建条件显示物品
     */
    private ItemStack createConditionDisplayItem(ItemConditionEntry entry, int index) {
        ItemStack item = entry.getItem().clone();

        List<String> lore = new ArrayList<>();

        // 添加条件信息
        java.util.Map<String, String> placeholders = new java.util.HashMap<>();
        placeholders.put("index", String.valueOf(index + 1));
        placeholders.put("type", item.getType().name());
        placeholders.put("amount", String.valueOf(entry.getAmount()));

        lore.add(getGUIText("item-condition-manage.condition-info", placeholders));
        lore.add(getGUIText("item-condition-manage.item-type", placeholders));
        lore.add(getGUIText("item-condition-manage.need-amount", placeholders));

        // 如果物品有自定义名称，显示名称
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            placeholders.put("name", item.getItemMeta().getDisplayName());
            lore.add(getGUIText("item-condition-manage.item-name", placeholders));
        }

        lore.add("");
        lore.add(getGUIText("item-condition-manage.left-click-edit-amount"));
        lore.add(getGUIText("item-condition-manage.right-click-delete"));
        lore.add(getGUIText("item-condition-manage.shift-left-copy"));

        return new ItemBuilder(item)
                .setLore(lore)
                .build();
    }

    /**
     * 加载物品条件数据
     */
    private void loadItemConditions() {
        itemConditions.clear();
        
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection conditionsSection = config.getConfigurationSection("creationConditions");
            
            if (conditionsSection == null) {
                return;
            }

            ConfigurationSection itemsSection = conditionsSection.getConfigurationSection("items");
            if (itemsSection == null) {
                return;
            }

            // 加载序列化物品条件
            if (itemsSection.contains("serialized-item")) {
                ConfigurationSection serializedSection = itemsSection.getConfigurationSection("serialized-item");
                if (serializedSection != null) {
                    String serializedItem = serializedSection.getString("serialized-item");
                    int amount = serializedSection.getInt("amount", 1);
                    
                    if (serializedItem != null && !serializedItem.isEmpty()) {
                        ItemStack item = ItemStackUtil.deserializeItemStack(serializedItem);
                        if (item != null) {
                            itemConditions.add(new ItemConditionEntry("serialized-item", item, amount, serializedItem));
                        }
                    }
                }
            }

            // 加载普通物品条件
            for (String key : itemsSection.getKeys(false)) {
                if (key.equals("serialized-item")) {
                    continue; // 已处理
                }
                
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection != null) {
                    String materialName = itemSection.getString("material");
                    int amount = itemSection.getInt("amount", 1);
                    String displayName = itemSection.getString("name");

                    if (materialName != null) {
                        try {
                            Material material = Material.valueOf(materialName.toUpperCase());
                            ItemStack item = new ItemStack(material, amount);
                            
                            if (displayName != null && !displayName.isEmpty()) {
                                ItemMeta meta = item.getItemMeta();
                                if (meta != null) {
                                    meta.setDisplayName(displayName);
                                    item.setItemMeta(meta);
                                }
                            }
                            
                            itemConditions.add(new ItemConditionEntry(key, item, amount, null));
                        } catch (IllegalArgumentException e) {
                            java.util.Map<String, String> debugPlaceholders = new java.util.HashMap<>();
                            debugPlaceholders.put("material", materialName);
                            cn.i7mc.sagadungeons.util.DebugUtil.debug("item-condition.invalid-material", debugPlaceholders);
                        }
                    }
                }
            }
        } catch (Exception e) {
            MessageUtil.sendMessage(player, "command.admin.edit.load-error");
            java.util.Map<String, String> debugPlaceholders = new java.util.HashMap<>();
            debugPlaceholders.put("message", e.getMessage());
            cn.i7mc.sagadungeons.util.DebugUtil.debug("item-condition.load-error", debugPlaceholders);
        }
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
            case 46: // 添加物品条件
                handleAddItemCondition();
                return;
            case 48: // 返回
                handleBack();
                return;
            case 49: // 保存
                handleSave();
                return;
            case 50: // 刷新
                init();
                return;
            case 52: // 清空所有条件
                if (event.isRightClick()) {
                    handleClearAll();
                }
                return;
        }

        // 处理物品条件点击
        if (slot >= 10 && slot <= 34 && slot % 9 != 0 && slot % 9 != 8) {
            handleItemConditionClick(event, slot);
        }
    }

    /**
     * 处理添加物品条件
     */
    private void handleAddItemCondition() {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() == Material.AIR) {
            MessageUtil.sendMessage(player, "command.admin.edit.no-item-in-hand");
            return;
        }

        // 序列化物品
        String serializedItem = ItemStackUtil.serializeItemStack(handItem);
        if (serializedItem == null) {
            MessageUtil.sendMessage(player, "command.admin.edit.item-serialize-failed");
            return;
        }

        // 生成唯一键名
        String key = "item_" + System.currentTimeMillis();

        // 添加到列表
        itemConditions.add(new ItemConditionEntry(key, handItem.clone(), handItem.getAmount(), serializedItem));

        // 刷新界面
        init();

        MessageUtil.sendMessage(player, "command.admin.edit.item-condition-added");
    }

    /**
     * 处理物品条件点击
     */
    private void handleItemConditionClick(InventoryClickEvent event, int slot) {
        // 计算条件索引
        int index = getConditionIndex(slot);
        if (index < 0 || index >= itemConditions.size()) {
            return;
        }

        ItemConditionEntry entry = itemConditions.get(index);

        if (event.isShiftClick() && event.isLeftClick()) {
            // Shift+左键复制物品
            handleCopyItem(entry);
        } else if (event.isLeftClick()) {
            // 左键编辑数量
            handleEditAmount(entry, index);
        } else if (event.isRightClick()) {
            // 右键删除条件
            handleDeleteCondition(index);
        }
    }

    /**
     * 计算条件索引
     */
    private int getConditionIndex(int slot) {
        int index = 0;
        for (int s = 10; s < slot; s++) {
            if (s % 9 != 0 && s % 9 != 8) {
                index++;
            }
        }
        return index;
    }

    /**
     * 处理复制物品
     */
    private void handleCopyItem(ItemConditionEntry entry) {
        ItemStack copyItem = entry.getItem().clone();
        copyItem.setAmount(entry.getAmount());

        // 给玩家物品
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(copyItem);
            MessageUtil.sendMessage(player, "command.admin.edit.item-copied");
        } else {
            MessageUtil.sendMessage(player, "command.admin.edit.inventory-full");
        }
    }

    /**
     * 处理编辑数量
     */
    private void handleEditAmount(ItemConditionEntry entry, int index) {
        close();
        plugin.getChatInputListener().requestNumberInput(player, "command.admin.edit.input-item-amount", input -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    int amount = Integer.parseInt(input);
                    if (amount <= 0) {
                        MessageUtil.sendMessage(player, "command.admin.edit.invalid-amount");
                        plugin.getGUIManager().openItemConditionManageGUI(player, templateName);
                        return;
                    }

                    entry.setAmount(amount);
                    MessageUtil.sendMessage(player, "command.admin.edit.item-amount-updated",
                            MessageUtil.createPlaceholders("amount", input));
                    plugin.getGUIManager().openItemConditionManageGUI(player, templateName);
                } catch (NumberFormatException e) {
                    MessageUtil.sendMessage(player, "command.admin.edit.invalid-number");
                    plugin.getGUIManager().openItemConditionManageGUI(player, templateName);
                }
            });
        });
    }

    /**
     * 处理删除条件
     */
    private void handleDeleteCondition(int index) {
        itemConditions.remove(index);
        init();
        MessageUtil.sendMessage(player, "command.admin.edit.item-condition-deleted");
    }

    /**
     * 处理清空所有条件
     */
    private void handleClearAll() {
        itemConditions.clear();
        init();
        MessageUtil.sendMessage(player, "command.admin.edit.all-item-conditions-cleared");
    }

    /**
     * 处理保存
     */
    private void handleSave() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

            // 清空现有物品条件
            config.set("creationConditions.items", null);

            if (!itemConditions.isEmpty()) {
                ConfigurationSection itemsSection = config.createSection("creationConditions.items");

                for (ItemConditionEntry entry : itemConditions) {
                    ConfigurationSection itemSection = itemsSection.createSection(entry.getKey());

                    if (entry.isSerializedItem()) {
                        // 序列化物品
                        itemSection.set("serialized-item", entry.getSerializedItem());
                        itemSection.set("amount", entry.getAmount());
                    } else {
                        // 普通物品
                        itemSection.set("material", entry.getItem().getType().name());
                        itemSection.set("amount", entry.getAmount());

                        if (entry.getItem().hasItemMeta() && entry.getItem().getItemMeta().hasDisplayName()) {
                            itemSection.set("name", entry.getItem().getItemMeta().getDisplayName());
                        }
                    }
                }
            }

            config.save(configFile);
            MessageUtil.sendMessage(player, "command.admin.edit.save-success",
                    MessageUtil.createPlaceholders("template", templateName));
        } catch (Exception e) {
            MessageUtil.sendMessage(player, "command.admin.edit.save-error");
            java.util.Map<String, String> debugPlaceholders = new java.util.HashMap<>();
            debugPlaceholders.put("message", e.getMessage());
            cn.i7mc.sagadungeons.util.DebugUtil.debug("item-condition.save-error", debugPlaceholders);
        }
    }

    /**
     * 处理返回
     */
    private void handleBack() {
        close();
        plugin.getGUIManager().openTemplateConditionsEditGUI(player, templateName);
    }

    /**
     * 物品条件条目类
     */
    private static class ItemConditionEntry {
        private final String key;
        private final ItemStack item;
        private int amount;
        private final String serializedItem;

        public ItemConditionEntry(String key, ItemStack item, int amount, String serializedItem) {
            this.key = key;
            this.item = item;
            this.amount = amount;
            this.serializedItem = serializedItem;
        }

        public String getKey() { return key; }
        public ItemStack getItem() { return item; }
        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
        public String getSerializedItem() { return serializedItem; }
        public boolean isSerializedItem() { return serializedItem != null; }
    }
}
