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
import java.util.*;

/**
 * 时间奖励管理界面
 * 管理副本模板的时间奖励配置
 */
public class TimeRewardManageGUI extends AbstractGUI {

    private final String templateName;
    private List<TimeRewardEntry> timeRewards;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     * @param templateName 模板名称
     */
    public TimeRewardManageGUI(SagaDungeons plugin, Player player, String templateName) {
        super(plugin, player, "&6时间奖励管理 - " + templateName, 54);
        this.templateName = templateName;
        this.timeRewards = new ArrayList<>();
    }

    @Override
    public void init() {
        // 清空界面
        inventory.clear();

        // 加载时间奖励数据
        loadTimeRewards();

        // 添加边框
        addBorder();

        // 添加功能按钮
        addFunctionButtons();

        // 显示时间奖励
        displayTimeRewards();
    }

    /**
     * 添加边框装饰
     */
    private void addBorder() {
        ItemStack borderItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
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
        // 添加时间奖励
        ItemStack addItem = new ItemBuilder(Material.EMERALD)
                .setName("&a添加时间奖励")
                .setLore(Arrays.asList(
                        "&7点击添加新的时间奖励配置",
                        "&7设置时间限制和对应的奖励命令",
                        "",
                        "&a左键点击添加"
                ))
                .build();
        inventory.setItem(46, addItem);

        // 返回按钮
        ItemStack backItem = new ItemBuilder(Material.ARROW)
                .setName("&c返回")
                .setLore(Arrays.asList(
                        "&7返回奖励系统编辑界面",
                        "",
                        "&c左键点击返回"
                ))
                .build();
        inventory.setItem(48, backItem);

        // 保存按钮
        ItemStack saveItem = new ItemBuilder(Material.WRITABLE_BOOK)
                .setName("&a保存配置")
                .setLore(Arrays.asList(
                        "&7保存当前时间奖励配置到文件",
                        "&7保存后配置将立即生效",
                        "",
                        "&a左键点击保存"
                ))
                .build();
        inventory.setItem(49, saveItem);

        // 刷新按钮
        ItemStack refreshItem = new ItemBuilder(Material.COMPASS)
                .setName("&e刷新界面")
                .setLore(Arrays.asList(
                        "&7重新加载时间奖励配置",
                        "&7从配置文件重新读取数据",
                        "",
                        "&e左键点击刷新"
                ))
                .build();
        inventory.setItem(50, refreshItem);

        // 清空所有奖励
        ItemStack clearItem = new ItemBuilder(Material.BARRIER)
                .setName("&c清空所有时间奖励")
                .setLore(Arrays.asList(
                        "&7删除所有时间奖励配置",
                        "&c此操作不可撤销！",
                        "",
                        "&c右键点击清空"
                ))
                .build();
        inventory.setItem(52, clearItem);
    }

    /**
     * 显示时间奖励
     */
    private void displayTimeRewards() {
        int startSlot = 10;
        int maxSlots = 7 * 4; // 4行，每行7个位置

        for (int i = 0; i < Math.min(timeRewards.size(), maxSlots); i++) {
            TimeRewardEntry entry = timeRewards.get(i);
            
            // 计算位置
            int row = i / 7;
            int col = i % 7;
            int slot = startSlot + row * 9 + col;

            // 创建时间奖励物品
            ItemStack rewardItem = createTimeRewardItem(entry, i);
            inventory.setItem(slot, rewardItem);
        }
    }

    /**
     * 创建时间奖励物品
     */
    private ItemStack createTimeRewardItem(TimeRewardEntry entry, int index) {
        List<String> lore = new ArrayList<>();
        lore.add("&7时间限制: &e" + formatTime(entry.getTimeSeconds()) + " &7(" + entry.getTimeSeconds() + "秒)");
        lore.add("&7命令数量: &a" + entry.getCommands().size());
        lore.add("");
        
        if (!entry.getCommands().isEmpty()) {
            lore.add("&7奖励命令:");
            for (int i = 0; i < Math.min(entry.getCommands().size(), 3); i++) {
                String command = entry.getCommands().get(i);
                if (command.length() > 30) {
                    command = command.substring(0, 27) + "...";
                }
                lore.add("&8- &f" + command);
            }
            if (entry.getCommands().size() > 3) {
                lore.add("&8... 还有 " + (entry.getCommands().size() - 3) + " 个命令");
            }
        } else {
            lore.add("&7暂无奖励命令");
        }
        
        lore.add("");
        lore.add("&a左键编辑时间限制");
        lore.add("&eShift+左键管理奖励命令");
        lore.add("&c右键删除此时间奖励");

        return new ItemBuilder(Material.CLOCK)
                .setName("&6时间奖励 #" + (index + 1))
                .setLore(lore)
                .build();
    }

    /**
     * 格式化时间显示
     */
    private String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return minutes + "分" + (remainingSeconds > 0 ? remainingSeconds + "秒" : "");
        } else {
            int hours = seconds / 3600;
            int remainingMinutes = (seconds % 3600) / 60;
            return hours + "小时" + (remainingMinutes > 0 ? remainingMinutes + "分" : "");
        }
    }

    /**
     * 加载时间奖励数据
     */
    private void loadTimeRewards() {
        timeRewards.clear();
        
        try {
            File templateFile = new File(plugin.getDataFolder(), "templates/" + templateName + ".yml");
            if (!templateFile.exists()) {
                return;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(templateFile);
            ConfigurationSection timeRewardsSection = config.getConfigurationSection("timeRewards");
            
            if (timeRewardsSection != null) {
                for (String timeKey : timeRewardsSection.getKeys(false)) {
                    try {
                        int timeSeconds = Integer.parseInt(timeKey);
                        ConfigurationSection timeRewardSection = timeRewardsSection.getConfigurationSection(timeKey);
                        
                        if (timeRewardSection != null) {
                            List<String> commands = timeRewardSection.getStringList("commands");
                            timeRewards.add(new TimeRewardEntry(timeSeconds, commands));
                        }
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("无效的时间格式: " + timeKey + " 在模板 " + templateName + " 中");
                    }
                }
            }
            
            // 按时间排序
            timeRewards.sort(Comparator.comparingInt(TimeRewardEntry::getTimeSeconds));
            
        } catch (Exception e) {
            MessageUtil.sendMessage(player, "command.admin.edit.load-error");
            plugin.getLogger().warning("加载时间奖励时出错: " + e.getMessage());
        }
    }

    /**
     * 保存时间奖励数据
     */
    private void saveTimeRewards() {
        try {
            File templateFile = new File(plugin.getDataFolder(), "templates/" + templateName + ".yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(templateFile);
            
            // 清除现有的时间奖励配置
            config.set("timeRewards", null);
            
            // 保存新的时间奖励配置
            if (!timeRewards.isEmpty()) {
                ConfigurationSection timeRewardsSection = config.createSection("timeRewards");
                
                for (TimeRewardEntry entry : timeRewards) {
                    ConfigurationSection timeSection = timeRewardsSection.createSection(String.valueOf(entry.getTimeSeconds()));
                    timeSection.set("commands", entry.getCommands());
                }
            }
            
            config.save(templateFile);
            
            // 重新加载模板配置
            plugin.getConfigManager().getTemplateManager().reloadTemplate(templateName);
            
        } catch (Exception e) {
            MessageUtil.sendMessage(player, "command.admin.edit.save-error");
            plugin.getLogger().warning("保存时间奖励时出错: " + e.getMessage());
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
            case 46: // 添加时间奖励
                handleAddTimeReward();
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

        // 处理时间奖励点击
        if (slot >= 10 && slot <= 34 && slot % 9 != 0 && slot % 9 != 8) {
            handleTimeRewardClick(event, slot);
        }
    }

    /**
     * 处理添加时间奖励
     */
    private void handleAddTimeReward() {
        close();
        plugin.getChatInputListener().requestNumberInput(player, "command.admin.edit.input-time-seconds", input -> {
            // 使用BukkitScheduler确保在主线程中执行
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    int timeSeconds = Integer.parseInt(input);
                    if (timeSeconds <= 0) {
                        MessageUtil.sendMessage(player, "command.admin.edit.invalid-time-value");
                        plugin.getGUIManager().openTimeRewardManageGUI(player, templateName);
                        return;
                    }

                    // 检查是否已存在相同时间的奖励
                    boolean exists = timeRewards.stream().anyMatch(entry -> entry.getTimeSeconds() == timeSeconds);
                    if (exists) {
                        MessageUtil.sendMessage(player, "command.admin.edit.time-reward-exists");
                        plugin.getGUIManager().openTimeRewardManageGUI(player, templateName);
                        return;
                    }

                    // 添加新的时间奖励
                    timeRewards.add(new TimeRewardEntry(timeSeconds, new ArrayList<>()));
                    timeRewards.sort(Comparator.comparingInt(TimeRewardEntry::getTimeSeconds));

                    MessageUtil.sendMessage(player, "command.admin.edit.time-reward-added",
                            MessageUtil.createPlaceholders("time", formatTime(timeSeconds)));

                    // 重新打开界面
                    plugin.getGUIManager().openTimeRewardManageGUI(player, templateName);
                } catch (NumberFormatException e) {
                    MessageUtil.sendMessage(player, "command.admin.edit.invalid-number");
                    plugin.getGUIManager().openTimeRewardManageGUI(player, templateName);
                }
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
        saveTimeRewards();
        MessageUtil.sendMessage(player, "command.admin.edit.time-rewards-saved");
    }

    /**
     * 处理清空所有奖励
     */
    private void handleClearAll() {
        timeRewards.clear();
        init();
        MessageUtil.sendMessage(player, "command.admin.edit.all-time-rewards-cleared");
    }

    /**
     * 处理时间奖励点击
     */
    private void handleTimeRewardClick(InventoryClickEvent event, int slot) {
        // 计算时间奖励索引
        int row = slot / 9 - 1;
        int col = slot % 9 - 1;
        int index = row * 7 + col;

        if (index >= timeRewards.size()) {
            return;
        }

        TimeRewardEntry entry = timeRewards.get(index);

        if (event.isLeftClick()) {
            // 左键编辑时间限制
            handleEditTimeLimit(entry, index);
        } else if (event.isShiftClick() && event.isLeftClick()) {
            // Shift+左键管理奖励命令
            handleManageCommands(entry, index);
        } else if (event.isRightClick()) {
            // 右键删除
            handleDeleteTimeReward(index);
        }
    }

    /**
     * 处理编辑时间限制
     */
    private void handleEditTimeLimit(TimeRewardEntry entry, int index) {
        close();
        plugin.getChatInputListener().requestNumberInput(player, "command.admin.edit.input-new-time-seconds", input -> {
            // 使用BukkitScheduler确保在主线程中执行
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    int newTimeSeconds = Integer.parseInt(input);
                    if (newTimeSeconds <= 0) {
                        MessageUtil.sendMessage(player, "command.admin.edit.invalid-time-value");
                        plugin.getGUIManager().openTimeRewardManageGUI(player, templateName);
                        return;
                    }

                    // 检查是否已存在相同时间的奖励（排除当前编辑的）
                    boolean exists = timeRewards.stream()
                            .anyMatch(e -> e != entry && e.getTimeSeconds() == newTimeSeconds);
                    if (exists) {
                        MessageUtil.sendMessage(player, "command.admin.edit.time-reward-exists");
                        plugin.getGUIManager().openTimeRewardManageGUI(player, templateName);
                        return;
                    }

                    // 更新时间限制
                    entry.setTimeSeconds(newTimeSeconds);
                    timeRewards.sort(Comparator.comparingInt(TimeRewardEntry::getTimeSeconds));

                    MessageUtil.sendMessage(player, "command.admin.edit.time-limit-updated",
                            MessageUtil.createPlaceholders("time", formatTime(newTimeSeconds)));

                    // 重新打开界面
                    plugin.getGUIManager().openTimeRewardManageGUI(player, templateName);
                } catch (NumberFormatException e) {
                    MessageUtil.sendMessage(player, "command.admin.edit.invalid-number");
                    plugin.getGUIManager().openTimeRewardManageGUI(player, templateName);
                }
            });
        });
    }

    /**
     * 处理管理命令
     */
    private void handleManageCommands(TimeRewardEntry entry, int index) {
        close();
        // 打开时间奖励命令管理界面
        plugin.getGUIManager().openTimeRewardCommandGUI(player, templateName, entry.getTimeSeconds());
    }

    /**
     * 处理删除时间奖励
     */
    private void handleDeleteTimeReward(int index) {
        if (index >= 0 && index < timeRewards.size()) {
            TimeRewardEntry entry = timeRewards.get(index);
            timeRewards.remove(index);
            init();
            MessageUtil.sendMessage(player, "command.admin.edit.time-reward-deleted",
                    MessageUtil.createPlaceholders("time", formatTime(entry.getTimeSeconds())));
        }
    }

    /**
     * 时间奖励条目类
     */
    private static class TimeRewardEntry {
        private int timeSeconds;
        private List<String> commands;

        public TimeRewardEntry(int timeSeconds, List<String> commands) {
            this.timeSeconds = timeSeconds;
            this.commands = new ArrayList<>(commands);
        }

        public int getTimeSeconds() {
            return timeSeconds;
        }

        public void setTimeSeconds(int timeSeconds) {
            this.timeSeconds = timeSeconds;
        }

        public List<String> getCommands() {
            return commands;
        }

        public void setCommands(List<String> commands) {
            this.commands = new ArrayList<>(commands);
        }
    }
}
