package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Bukkit;
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
 * 命令奖励管理界面
 * 管理副本模板的命令奖励
 */
public class CommandRewardManageGUI extends AbstractGUI {

    private final String templateName;
    private List<CommandRewardEntry> commandRewards;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     * @param templateName 模板名称
     */
    public CommandRewardManageGUI(SagaDungeons plugin, Player player, String templateName) {
        super(plugin, player, "&6命令奖励管理 - " + templateName, 54);
        this.templateName = templateName;
        this.commandRewards = new ArrayList<>();
    }

    @Override
    public void init() {
        // 清空界面
        inventory.clear();

        // 加载命令奖励数据
        loadCommandRewards();

        // 添加边框
        addBorder();

        // 添加功能按钮
        addFunctionButtons();

        // 显示命令奖励
        displayCommandRewards();
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
        // 添加命令奖励按钮
        ItemStack addItem = new ItemBuilder(Material.COMMAND_BLOCK)
                .setName("&a添加命令奖励")
                .setLore(
                        "&7点击添加新的命令奖励",
                        "&7输入要执行的命令",
                        "",
                        "&a左键点击添加命令",
                        "&7支持变量: {player}, {uuid}"
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
                        "&7保存当前命令奖励配置",
                        "&7到模板配置文件中"
                )
                .build();
        inventory.setItem(49, saveItem);

        // 刷新按钮
        ItemStack refreshItem = new ItemBuilder(Material.CLOCK)
                .setName("&e刷新界面")
                .setLore("&7重新加载命令奖励数据")
                .build();
        inventory.setItem(50, refreshItem);

        // 测试命令按钮
        ItemStack testItem = new ItemBuilder(Material.REDSTONE)
                .setName("&e测试命令")
                .setLore(
                        "&7测试所有命令奖励",
                        "&7在控制台执行命令",
                        "",
                        "&e左键点击测试"
                )
                .build();
        inventory.setItem(51, testItem);

        // 清空所有奖励按钮
        ItemStack clearAllItem = new ItemBuilder(Material.BARRIER)
                .setName("&c清空所有奖励")
                .setLore(
                        "&7清空所有命令奖励",
                        "&c警告：此操作不可撤销！",
                        "",
                        "&c右键点击确认清空"
                )
                .build();
        inventory.setItem(52, clearAllItem);
    }

    /**
     * 显示命令奖励
     */
    private void displayCommandRewards() {
        int slot = 10;
        for (int i = 0; i < commandRewards.size() && slot < 35; i++) {
            CommandRewardEntry entry = commandRewards.get(i);
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
    private ItemStack createRewardDisplayItem(CommandRewardEntry entry, int index) {
        ItemBuilder builder = new ItemBuilder(Material.PAPER);

        List<String> lore = new ArrayList<>();
        lore.add("&7命令奖励 #" + (index + 1));
        lore.add("&7命令: &f" + entry.getCommand());
        
        if (entry.getDescription() != null && !entry.getDescription().isEmpty()) {
            lore.add("&7描述: &f" + entry.getDescription());
        }
        
        lore.add("");
        lore.add("&7支持的变量:");
        lore.add("&8- {player}: 玩家名称");
        lore.add("&8- {uuid}: 玩家UUID");
        lore.add("");
        lore.add("&a左键点击编辑命令");
        lore.add("&e中键点击编辑描述");
        lore.add("&c右键点击删除奖励");
        lore.add("&bShift+左键测试命令");

        return builder.setName("&6命令奖励 #" + (index + 1))
                .setLore(lore)
                .build();
    }

    /**
     * 加载命令奖励数据
     */
    private void loadCommandRewards() {
        commandRewards.clear();
        
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

            ConfigurationSection commandsSection = rewardsSection.getConfigurationSection("commands");
            if (commandsSection != null) {
                Set<String> keys = commandsSection.getKeys(false);
                for (String key : keys) {
                    ConfigurationSection commandSection = commandsSection.getConfigurationSection(key);
                    if (commandSection != null) {
                        String command = commandSection.getString("command");
                        String description = commandSection.getString("description", "");
                        
                        if (command != null && !command.isEmpty()) {
                            commandRewards.add(new CommandRewardEntry(key, command, description));
                        }
                    }
                }
            }
        } catch (Exception e) {
            MessageUtil.sendMessage(player, "command.admin.edit.load-error");
            plugin.getLogger().warning("加载命令奖励时出错: " + e.getMessage());
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
            case 46: // 添加命令奖励
                handleAddCommandReward();
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
            case 51: // 测试命令
                handleTestCommands();
                return;
            case 52: // 清空所有奖励
                if (event.isRightClick()) {
                    handleClearAll();
                }
                return;
        }

        // 处理命令奖励点击
        if (slot >= 10 && slot <= 34 && slot % 9 != 0 && slot % 9 != 8) {
            handleCommandRewardClick(event, slot);
        }
    }

    /**
     * 处理添加命令奖励
     */
    private void handleAddCommandReward() {
        close();
        plugin.getChatInputListener().requestTextInput(player, "command.admin.edit.input-command", input -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // 验证命令格式
                if (input.trim().isEmpty()) {
                    MessageUtil.sendMessage(player, "command.admin.edit.invalid-command");
                    plugin.getGUIManager().openCommandRewardManageGUI(player, templateName);
                    return;
                }

                // 生成唯一键名
                String key = "command_" + System.currentTimeMillis();
                
                // 添加到列表
                commandRewards.add(new CommandRewardEntry(key, input.trim(), ""));
                
                // 刷新界面
                init();
                
                MessageUtil.sendMessage(player, "command.admin.edit.command-reward-added");
            });
        });
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
        saveCommandRewards();
        MessageUtil.sendMessage(player, "command.admin.edit.command-rewards-saved");
    }

    /**
     * 处理测试命令
     */
    private void handleTestCommands() {
        if (commandRewards.isEmpty()) {
            MessageUtil.sendMessage(player, "command.admin.edit.no-commands-to-test");
            return;
        }

        MessageUtil.sendMessage(player, "command.admin.edit.testing-commands");

        for (CommandRewardEntry entry : commandRewards) {
            String processedCommand = entry.getCommand()
                    .replace("{player}", player.getName())
                    .replace("{uuid}", player.getUniqueId().toString());

            plugin.getLogger().info("测试命令: " + processedCommand);
            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);

            if (success) {
                MessageUtil.sendMessage(player, "command.admin.edit.command-test-success",
                        MessageUtil.createPlaceholders("command", processedCommand));
            } else {
                MessageUtil.sendMessage(player, "command.admin.edit.command-test-failed",
                        MessageUtil.createPlaceholders("command", processedCommand));
            }
        }
    }

    /**
     * 处理清空所有奖励
     */
    private void handleClearAll() {
        commandRewards.clear();
        init();
        MessageUtil.sendMessage(player, "command.admin.edit.all-command-rewards-cleared");
    }

    /**
     * 处理命令奖励点击
     */
    private void handleCommandRewardClick(InventoryClickEvent event, int slot) {
        // 计算命令索引
        int row = slot / 9 - 1;
        int col = slot % 9 - 1;
        int index = row * 7 + col;

        if (index >= commandRewards.size()) {
            return;
        }

        CommandRewardEntry entry = commandRewards.get(index);

        if (event.isShiftClick() && event.isLeftClick()) {
            // Shift+左键测试命令
            handleTestSingleCommand(entry);
        } else if (event.isLeftClick()) {
            // 左键编辑命令
            handleEditCommand(entry, index);
        } else if (event.getClick().name().equals("MIDDLE")) {
            // 中键编辑描述
            handleEditDescription(entry, index);
        } else if (event.isRightClick()) {
            // 右键删除
            handleDeleteReward(index);
        }
    }

    /**
     * 处理测试单个命令
     */
    private void handleTestSingleCommand(CommandRewardEntry entry) {
        String processedCommand = entry.getCommand()
                .replace("{player}", player.getName())
                .replace("{uuid}", player.getUniqueId().toString());

        plugin.getLogger().info("测试单个命令: " + processedCommand);
        boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);

        if (success) {
            MessageUtil.sendMessage(player, "command.admin.edit.command-test-success",
                    MessageUtil.createPlaceholders("command", processedCommand));
        } else {
            MessageUtil.sendMessage(player, "command.admin.edit.command-test-failed",
                    MessageUtil.createPlaceholders("command", processedCommand));
        }
    }

    /**
     * 处理编辑命令
     */
    private void handleEditCommand(CommandRewardEntry entry, int index) {
        close();
        plugin.getChatInputListener().requestTextInput(player, "command.admin.edit.input-new-command", input -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (input.trim().isEmpty()) {
                    MessageUtil.sendMessage(player, "command.admin.edit.invalid-command");
                    plugin.getGUIManager().openCommandRewardManageGUI(player, templateName);
                    return;
                }

                entry.setCommand(input.trim());
                MessageUtil.sendMessage(player, "command.admin.edit.command-updated");
                plugin.getGUIManager().openCommandRewardManageGUI(player, templateName);
            });
        });
    }

    /**
     * 处理编辑描述
     */
    private void handleEditDescription(CommandRewardEntry entry, int index) {
        close();
        plugin.getChatInputListener().requestTextInput(player, "command.admin.edit.input-command-description", input -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                entry.setDescription(input.trim());
                MessageUtil.sendMessage(player, "command.admin.edit.description-updated");
                plugin.getGUIManager().openCommandRewardManageGUI(player, templateName);
            });
        });
    }

    /**
     * 处理删除奖励
     */
    private void handleDeleteReward(int index) {
        if (index >= 0 && index < commandRewards.size()) {
            commandRewards.remove(index);
            init();
            MessageUtil.sendMessage(player, "command.admin.edit.command-reward-deleted");
        }
    }

    /**
     * 保存命令奖励到配置文件
     */
    private void saveCommandRewards() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            YamlConfiguration config;
            if (configFile.exists()) {
                config = YamlConfiguration.loadConfiguration(configFile);
            } else {
                config = new YamlConfiguration();
            }

            // 清除现有命令奖励配置
            config.set("rewards.commands", null);

            // 保存新的命令奖励配置
            if (!commandRewards.isEmpty()) {
                ConfigurationSection commandsSection = config.createSection("rewards.commands");

                for (CommandRewardEntry entry : commandRewards) {
                    ConfigurationSection commandSection = commandsSection.createSection(entry.getKey());
                    commandSection.set("command", entry.getCommand());
                    commandSection.set("description", entry.getDescription());
                }
            }

            // 保存配置文件
            config.save(configFile);

        } catch (IOException e) {
            MessageUtil.sendMessage(player, "command.admin.edit.save-error");
            plugin.getLogger().warning("保存命令奖励配置时出错: " + e.getMessage());
        }
    }

    /**
     * 命令奖励条目类
     */
    private static class CommandRewardEntry {
        private final String key;
        private String command;
        private String description;

        public CommandRewardEntry(String key, String command, String description) {
            this.key = key;
            this.command = command;
            this.description = description;
        }

        public String getKey() { return key; }
        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
