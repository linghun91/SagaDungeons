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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 物品奖励管理界面
 * 管理副本模板的物品奖励
 */
public class ItemRewardManageGUI extends AbstractGUI {

    private final String templateName;
    private List<ItemRewardEntry> itemRewards;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     * @param templateName 模板名称
     */
    public ItemRewardManageGUI(SagaDungeons plugin, Player player, String templateName) {
        super(plugin, player, "&6物品奖励管理 - " + templateName, 54);
        this.templateName = templateName;
        this.itemRewards = new ArrayList<>();
    }

    @Override
    public void init() {
        // 清空界面
        inventory.clear();

        // 加载物品奖励数据
        loadItemRewards();

        // 添加边框
        addBorder();

        // 添加功能按钮
        addFunctionButtons();

        // 显示物品奖励
        displayItemRewards();
    }

    /**
     * 添加边框
     */
    private void addBorder() {
        ItemStack borderItem = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setName(" ")
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
     * 添加功能按钮
     */
    private void addFunctionButtons() {
        // 添加物品奖励按钮
        ItemStack addItem = new ItemBuilder(Material.EMERALD)
                .setName("&a添加物品奖励")
                .setLore(
                        "&7点击添加新的物品奖励",
                        "&7将手中的物品添加为奖励",
                        "",
                        "&a左键点击添加手中物品",
                        "&7需要手中持有物品"
                )
                .build();
        inventory.setItem(46, addItem);

        // 返回按钮
        ItemStack backItem = new ItemBuilder(Material.ARROW)
                .setName("&c返回")
                .setLore("&7返回奖励编辑界面")
                .build();
        inventory.setItem(48, backItem);

        // 保存按钮
        ItemStack saveItem = new ItemBuilder(Material.WRITABLE_BOOK)
                .setName("&a保存配置")
                .setLore(
                        "&7保存当前物品奖励配置",
                        "&7到模板配置文件中"
                )
                .build();
        inventory.setItem(49, saveItem);

        // 刷新按钮
        ItemStack refreshItem = new ItemBuilder(Material.CLOCK)
                .setName("&e刷新界面")
                .setLore("&7重新加载物品奖励数据")
                .build();
        inventory.setItem(50, refreshItem);

        // 清空所有奖励按钮
        ItemStack clearAllItem = new ItemBuilder(Material.BARRIER)
                .setName("&c清空所有奖励")
                .setLore(
                        "&7清空所有物品奖励",
                        "&c警告：此操作不可撤销！",
                        "",
                        "&c右键点击确认清空"
                )
                .build();
        inventory.setItem(52, clearAllItem);
    }

    /**
     * 显示物品奖励
     */
    private void displayItemRewards() {
        int slot = 10;
        for (int i = 0; i < itemRewards.size() && slot < 35; i++) {
            ItemRewardEntry entry = itemRewards.get(i);
            ItemStack displayItem = createRewardDisplayItem(entry, i);
            
            inventory.setItem(slot, displayItem);
            
            // 更新槽位
            slot++;
            if (slot % 9 == 8) {
                slot += 2;
            }
        }
    }

    /**
     * 创建奖励显示物品
     */
    private ItemStack createRewardDisplayItem(ItemRewardEntry entry, int index) {
        ItemStack originalItem = entry.getItem();
        if (originalItem == null) {
            return new ItemBuilder(Material.BARRIER)
                    .setName("&c无效物品")
                    .setLore("&7物品数据损坏")
                    .build();
        }

        // 克隆原物品并修改显示信息
        ItemStack displayItem = originalItem.clone();
        ItemBuilder builder = new ItemBuilder(displayItem);

        List<String> lore = new ArrayList<>();
        lore.add("&7物品奖励 #" + (index + 1));
        lore.add("&7数量: &f" + entry.getAmount());
        lore.add("");
        
        // 添加原有lore
        if (originalItem.hasItemMeta() && originalItem.getItemMeta().hasLore()) {
            lore.add("&7原始描述:");
            for (String line : originalItem.getItemMeta().getLore()) {
                lore.add("&8" + line);
            }
            lore.add("");
        }
        
        lore.add("&a左键点击编辑数量");
        lore.add("&c右键点击删除奖励");
        lore.add("&eShift+左键复制到手中");

        return builder.setLore(lore).build();
    }

    /**
     * 加载物品奖励数据
     */
    private void loadItemRewards() {
        itemRewards.clear();
        
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection rewardsSection = config.getConfigurationSection("rewards");

            if (rewardsSection == null) {
                return;
            }

            ConfigurationSection itemsSection = rewardsSection.getConfigurationSection("items");
            if (itemsSection != null) {
                Set<String> keys = itemsSection.getKeys(false);
                for (String key : keys) {
                    ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                    if (itemSection != null) {
                        String serializedItem = itemSection.getString("serialized-item");
                        int amount = itemSection.getInt("amount", 1);
                        
                        if (serializedItem != null && !serializedItem.isEmpty()) {
                            ItemStack item = ItemStackUtil.deserializeItemStack(serializedItem);
                            if (item != null) {
                                itemRewards.add(new ItemRewardEntry(key, item, amount, serializedItem));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            MessageUtil.sendMessage(player, "command.admin.edit.load-error");
            plugin.getLogger().warning("加载物品奖励时出错: " + e.getMessage());
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
            case 46: // 添加物品奖励
                handleAddItemReward();
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
            case 52: // 清空所有奖励
                if (event.isRightClick()) {
                    handleClearAll();
                }
                return;
        }

        // 处理物品奖励点击
        if (slot >= 10 && slot <= 34 && slot % 9 != 0 && slot % 9 != 8) {
            handleItemRewardClick(event, slot);
        }
    }

    /**
     * 处理添加物品奖励
     */
    private void handleAddItemReward() {
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
        itemRewards.add(new ItemRewardEntry(key, handItem.clone(), handItem.getAmount(), serializedItem));
        
        // 刷新界面
        init();
        
        MessageUtil.sendMessage(player, "command.admin.edit.item-reward-added");
    }

    /**
     * 处理返回
     */
    private void handleBack() {
        close();
        plugin.getGUIManager().openTemplateRewardsEditGUI(player, templateName);
    }

    /**
     * 处理保存
     */
    private void handleSave() {
        saveItemRewards();
        MessageUtil.sendMessage(player, "command.admin.edit.item-rewards-saved");
    }

    /**
     * 处理清空所有奖励
     */
    private void handleClearAll() {
        itemRewards.clear();
        init();
        MessageUtil.sendMessage(player, "command.admin.edit.all-item-rewards-cleared");
    }

    /**
     * 处理物品奖励点击
     */
    private void handleItemRewardClick(InventoryClickEvent event, int slot) {
        // 计算物品索引
        int row = slot / 9 - 1;
        int col = slot % 9 - 1;
        int index = row * 7 + col;

        if (index >= itemRewards.size()) {
            return;
        }

        ItemRewardEntry entry = itemRewards.get(index);

        if (event.isShiftClick() && event.isLeftClick()) {
            // Shift+左键复制到手中
            handleCopyToHand(entry);
        } else if (event.isLeftClick()) {
            // 左键编辑数量
            handleEditAmount(entry, index);
        } else if (event.isRightClick()) {
            // 右键删除
            handleDeleteReward(index);
        }
    }

    /**
     * 处理复制到手中
     */
    private void handleCopyToHand(ItemRewardEntry entry) {
        ItemStack copyItem = entry.getItem().clone();
        copyItem.setAmount(entry.getAmount());

        // 检查手中是否有物品
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem != null && handItem.getType() != Material.AIR) {
            MessageUtil.sendMessage(player, "command.admin.edit.hand-not-empty");
            return;
        }

        player.getInventory().setItemInMainHand(copyItem);
        MessageUtil.sendMessage(player, "command.admin.edit.item-copied-to-hand");
    }

    /**
     * 处理编辑数量
     */
    private void handleEditAmount(ItemRewardEntry entry, int index) {
        close();
        plugin.getChatInputListener().requestNumberInput(player, "command.admin.edit.input-item-amount", input -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    int newAmount = Integer.parseInt(input);
                    if (newAmount <= 0 || newAmount > 64) {
                        MessageUtil.sendMessage(player, "command.admin.edit.invalid-amount");
                        plugin.getGUIManager().openItemRewardManageGUI(player, templateName);
                        return;
                    }

                    entry.setAmount(newAmount);
                    MessageUtil.sendMessage(player, "command.admin.edit.item-amount-updated",
                            MessageUtil.createPlaceholders("amount", String.valueOf(newAmount)));
                    plugin.getGUIManager().openItemRewardManageGUI(player, templateName);
                } catch (NumberFormatException e) {
                    MessageUtil.sendMessage(player, "command.admin.edit.invalid-number");
                    plugin.getGUIManager().openItemRewardManageGUI(player, templateName);
                }
            });
        });
    }

    /**
     * 处理删除奖励
     */
    private void handleDeleteReward(int index) {
        if (index >= 0 && index < itemRewards.size()) {
            itemRewards.remove(index);
            init();
            MessageUtil.sendMessage(player, "command.admin.edit.item-reward-deleted");
        }
    }

    /**
     * 保存物品奖励到配置文件
     */
    private void saveItemRewards() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            YamlConfiguration config;
            if (configFile.exists()) {
                config = YamlConfiguration.loadConfiguration(configFile);
            } else {
                config = new YamlConfiguration();
            }

            // 清除现有物品奖励配置
            config.set("rewards.items", null);

            // 保存新的物品奖励配置
            if (!itemRewards.isEmpty()) {
                ConfigurationSection itemsSection = config.createSection("rewards.items");

                for (ItemRewardEntry entry : itemRewards) {
                    ConfigurationSection itemSection = itemsSection.createSection(entry.getKey());
                    itemSection.set("serialized-item", entry.getSerializedItem());
                    itemSection.set("amount", entry.getAmount());
                }
            }

            // 保存配置文件
            config.save(configFile);

        } catch (IOException e) {
            MessageUtil.sendMessage(player, "command.admin.edit.save-error");
            plugin.getLogger().warning("保存物品奖励配置时出错: " + e.getMessage());
        }
    }

    /**
     * 物品奖励条目类
     */
    private static class ItemRewardEntry {
        private final String key;
        private final ItemStack item;
        private int amount;
        private final String serializedItem;

        public ItemRewardEntry(String key, ItemStack item, int amount, String serializedItem) {
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
    }
}
