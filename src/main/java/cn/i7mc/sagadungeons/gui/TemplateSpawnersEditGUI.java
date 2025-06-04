package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.model.MobSpawner;
import cn.i7mc.sagadungeons.util.MessageUtil;
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

/**
 * 模板刷怪点管理界面
 * 用于管理模板的刷怪点
 */
public class TemplateSpawnersEditGUI extends AbstractGUI {

    private final String templateName;
    private final DungeonTemplate template;
    private int currentPage = 0;
    private final int itemsPerPage = 28; // 7x4 区域

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     * @param templateName 模板名称
     */
    public TemplateSpawnersEditGUI(SagaDungeons plugin, Player player, String templateName) {
        super(plugin, player, "&6刷怪点管理 - " + templateName, 54);
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

        // 添加刷怪点列表
        addSpawnersList();

        // 添加功能按钮
        addFunctionButtons();
    }

    /**
     * 添加装饰边框
     */
    private void addBorder() {
        ItemStack borderItem = new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE)
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
     * 添加刷怪点列表
     */
    private void addSpawnersList() {
        Map<String, MobSpawner> spawners = template.getMobSpawners();
        List<MobSpawner> spawnerList = new ArrayList<>(spawners.values());

        // 计算分页
        int totalPages = (int) Math.ceil((double) spawnerList.size() / itemsPerPage);
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, spawnerList.size());

        // 显示当前页的刷怪点
        int slot = 10; // 从第二行第二列开始
        for (int i = startIndex; i < endIndex; i++) {
            MobSpawner spawner = spawnerList.get(i);
            
            ItemStack spawnerItem = new ItemBuilder(Material.SPAWNER)
                    .setName("&e刷怪点: &f" + spawner.getId())
                    .setLore(createSpawnerLore(spawner))
                    .build();
            
            inventory.setItem(slot, spawnerItem);
            
            // 计算下一个位置
            slot++;
            if (slot % 9 == 8) { // 到达右边界
                slot += 2; // 跳到下一行的第二列
            }
            if (slot >= 45) { // 超出显示区域
                break;
            }
        }

        // 添加分页信息
        if (totalPages > 1) {
            addPaginationInfo(currentPage + 1, totalPages);
        }
    }

    /**
     * 创建刷怪点说明
     */
    private List<String> createSpawnerLore(MobSpawner spawner) {
        List<String> lore = new ArrayList<>();

        // 从配置文件读取实际数据
        String actualMobType = getSpawnerMobTypeFromConfig(spawner.getId());
        String actualLocation = getSpawnerLocationFromConfig(spawner.getId());
        int actualAmount = getSpawnerAmountFromConfig(spawner.getId());
        int actualCooldown = getSpawnerCooldownFromConfig(spawner.getId());

        lore.add("&7怪物类型: &f" + (actualMobType != null ? actualMobType : spawner.getMobType()));
        lore.add("&7位置: &f" + formatLocation(actualLocation != null ? actualLocation : spawner.getLocation()));
        lore.add("&7生成数量: &f" + actualAmount);
        lore.add("&7冷却时间: &f" + actualCooldown + "秒");
        lore.add("");
        lore.add("&a左键点击编辑刷怪点");
        lore.add("&e中键点击传送到位置");
        lore.add("&c右键点击删除刷怪点");
        return lore;
    }

    /**
     * 格式化位置字符串
     */
    private String formatLocation(String location) {
        if (location == null || location.isEmpty()) {
            return "未设置";
        }
        
        // 简化位置显示
        String[] parts = location.split(",");
        if (parts.length >= 3) {
            try {
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);
                return String.format("%.1f, %.1f, %.1f", x, y, z);
            } catch (NumberFormatException e) {
                return "格式错误";
            }
        }
        
        return location;
    }

    /**
     * 添加分页信息
     */
    private void addPaginationInfo(int currentPage, int totalPages) {
        // 上一页按钮
        if (currentPage > 1) {
            ItemStack prevButton = new ItemBuilder(Material.ARROW)
                    .setName("&a上一页")
                    .setLore("&7第 " + (currentPage - 1) + " 页")
                    .build();
            inventory.setItem(48, prevButton);
        }

        // 页面信息
        ItemStack pageInfo = new ItemBuilder(Material.PAPER)
                .setName("&e页面信息")
                .setLore(
                        "&7当前页: &f" + currentPage + " / " + totalPages,
                        "&7刷怪点总数: &f" + template.getMobSpawners().size()
                )
                .build();
        inventory.setItem(49, pageInfo);

        // 下一页按钮
        if (currentPage < totalPages) {
            ItemStack nextButton = new ItemBuilder(Material.ARROW)
                    .setName("&a下一页")
                    .setLore("&7第 " + (currentPage + 1) + " 页")
                    .build();
            inventory.setItem(50, nextButton);
        }
    }

    /**
     * 添加功能按钮
     */
    private void addFunctionButtons() {
        // 添加刷怪点按钮
        ItemStack addButton = new ItemBuilder(Material.LIME_DYE)
                .setName("&a添加刷怪点")
                .setLore(
                        "&7在当前位置创建新的刷怪点",
                        "",
                        "&a左键点击添加"
                )
                .build();
        inventory.setItem(46, addButton);

        // 返回按钮
        ItemStack backButton = new ItemBuilder(Material.BARRIER)
                .setName("&c返回")
                .setLore("&7返回基础信息编辑界面")
                .build();
        inventory.setItem(45, backButton);

        // 保存按钮
        ItemStack saveButton = new ItemBuilder(Material.EMERALD)
                .setName("&a保存更改")
                .setLore(
                        "&7保存当前的所有更改",
                        "",
                        "&a左键点击保存"
                )
                .build();
        inventory.setItem(53, saveButton);

        // 刷新按钮
        ItemStack refreshButton = new ItemBuilder(Material.CYAN_DYE)
                .setName("&a刷新")
                .setLore("&7刷新界面显示")
                .build();
        inventory.setItem(52, refreshButton);

        // 帮助按钮
        ItemStack helpButton = new ItemBuilder(Material.BOOK)
                .setName("&e帮助")
                .setLore(
                        "&7刷怪点管理说明:",
                        "&8- &f左键编辑: 修改刷怪点属性",
                        "&8- &f中键传送: 传送到刷怪点位置",
                        "&8- &f右键删除: 删除刷怪点",
                        "&8- &f添加刷怪点: 在当前位置创建",
                        "&8- &f支持MythicMobs怪物类型"
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
            case 45: // 返回
                handleBack();
                return;
            case 46: // 添加刷怪点
                handleAddSpawner();
                return;
            case 47: // 帮助
                // 帮助按钮不需要处理
                return;
            case 48: // 上一页
                handlePreviousPage();
                return;
            case 49: // 页面信息
                // 页面信息不需要处理
                return;
            case 50: // 下一页
                handleNextPage();
                return;
            case 52: // 刷新
                init();
                return;
            case 53: // 保存
                handleSave();
                return;
        }

        // 处理刷怪点点击
        if (item.getType() == Material.SPAWNER) {
            handleSpawnerClick(event, slot);
        }
    }

    /**
     * 处理刷怪点点击
     */
    private void handleSpawnerClick(InventoryClickEvent event, int slot) {
        String spawnerName = getSpawnerNameFromSlot(slot);
        if (spawnerName == null) {
            return;
        }

        MobSpawner spawner = template.getMobSpawners().get(spawnerName);
        if (spawner == null) {
            return;
        }

        if (event.isLeftClick()) {
            // 编辑刷怪点
            close();
            plugin.getGUIManager().openSpawnerEditGUI(player, templateName, spawnerName);
        } else if (event.getClick() == org.bukkit.event.inventory.ClickType.MIDDLE) {
            // 传送到刷怪点位置
            handleTeleportToSpawner(spawnerName);
        } else if (event.isRightClick()) {
            // 删除刷怪点
            template.removeMobSpawner(spawnerName);
            MessageUtil.sendMessage(player, "command.admin.edit.spawner.removed",
                    MessageUtil.createPlaceholders("spawner", spawnerName));
            init(); // 刷新界面
        }
    }

    /**
     * 从槽位获取刷怪点名称
     */
    private String getSpawnerNameFromSlot(int slot) {
        // 计算在当前页面中的索引
        int relativeSlot = getRelativeSlotIndex(slot);
        if (relativeSlot == -1) {
            return null;
        }

        List<MobSpawner> spawnerList = new ArrayList<>(template.getMobSpawners().values());
        int actualIndex = currentPage * itemsPerPage + relativeSlot;
        
        if (actualIndex >= 0 && actualIndex < spawnerList.size()) {
            return spawnerList.get(actualIndex).getId();
        }
        
        return null;
    }

    /**
     * 获取相对槽位索引
     */
    private int getRelativeSlotIndex(int slot) {
        // 将槽位转换为相对索引
        if (slot < 10 || slot >= 45) {
            return -1;
        }
        
        int row = slot / 9 - 1; // 减去第一行
        int col = slot % 9 - 1; // 减去第一列
        
        if (col < 0 || col >= 7) { // 超出有效列范围
            return -1;
        }
        
        return row * 7 + col;
    }

    /**
     * 处理返回
     */
    private void handleBack() {
        close();
        plugin.getGUIManager().openTemplateBasicEditGUI(player, templateName);
    }

    /**
     * 处理添加刷怪点
     */
    private void handleAddSpawner() {
        close();
        plugin.getGUIManager().openSpawnerAddGUI(player, templateName);
    }

    /**
     * 处理传送到刷怪点
     */
    private void handleTeleportToSpawner(String spawnerName) {
        MobSpawner spawner = template.getMobSpawners().get(spawnerName);
        if (spawner == null) {
            MessageUtil.sendMessage(player, "command.admin.edit.spawner.not-found",
                    MessageUtil.createPlaceholders("spawner", spawnerName));
            return;
        }

        // 获取刷怪点位置
        String locationStr = getSpawnerLocationFromConfig(spawnerName);
        if (locationStr == null || locationStr.isEmpty()) {
            MessageUtil.sendMessage(player, "command.admin.edit.spawner.no-location",
                    MessageUtil.createPlaceholders("spawner", spawnerName));
            return;
        }

        // 解析位置
        org.bukkit.Location location = cn.i7mc.sagadungeons.util.LocationUtil.stringToLocationWithoutWorld(
                locationStr, player.getWorld());

        if (location == null) {
            MessageUtil.sendMessage(player, "command.admin.edit.spawner.invalid-location",
                    MessageUtil.createPlaceholders("spawner", spawnerName));
            return;
        }

        // 查找安全位置并传送
        org.bukkit.Location safeLocation = cn.i7mc.sagadungeons.util.LocationUtil.findSafeLocation(location);
        player.teleport(safeLocation);

        MessageUtil.sendMessage(player, "command.admin.edit.spawner.teleported",
                MessageUtil.createPlaceholders("spawner", spawnerName));

        close();
    }

    /**
     * 处理上一页
     */
    private void handlePreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            init();
        }
    }

    /**
     * 处理下一页
     */
    private void handleNextPage() {
        Map<String, MobSpawner> spawners = template.getMobSpawners();
        int totalPages = (int) Math.ceil((double) spawners.size() / itemsPerPage);
        
        if (currentPage < totalPages - 1) {
            currentPage++;
            init();
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

    // ==================== 配置读取方法 ====================

    /**
     * 从配置文件读取刷怪点怪物类型
     * @param spawnerId 刷怪点ID
     * @return 怪物类型，如果读取失败返回null
     */
    private String getSpawnerMobTypeFromConfig(String spawnerId) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return null;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection spawnersSection = config.getConfigurationSection("mythicMobsSpawners");

            if (spawnersSection != null) {
                ConfigurationSection spawnerSection = spawnersSection.getConfigurationSection(spawnerId);
                if (spawnerSection != null) {
                    return spawnerSection.getString("mobType");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("读取刷怪点怪物类型失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 从配置文件读取刷怪点位置
     * @param spawnerId 刷怪点ID
     * @return 位置字符串，如果读取失败返回null
     */
    private String getSpawnerLocationFromConfig(String spawnerId) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return null;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection spawnersSection = config.getConfigurationSection("mythicMobsSpawners");

            if (spawnersSection != null) {
                ConfigurationSection spawnerSection = spawnersSection.getConfigurationSection(spawnerId);
                if (spawnerSection != null) {
                    return spawnerSection.getString("location");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("读取刷怪点位置失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 从配置文件读取刷怪点生成数量
     * @param spawnerId 刷怪点ID
     * @return 生成数量，如果读取失败返回默认值1
     */
    private int getSpawnerAmountFromConfig(String spawnerId) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return 1; // 默认值
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection spawnersSection = config.getConfigurationSection("mythicMobsSpawners");

            if (spawnersSection != null) {
                ConfigurationSection spawnerSection = spawnersSection.getConfigurationSection(spawnerId);
                if (spawnerSection != null) {
                    return spawnerSection.getInt("amount", 1);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("读取刷怪点生成数量失败: " + e.getMessage());
        }
        return 1; // 默认值
    }

    /**
     * 从配置文件读取刷怪点冷却时间
     * @param spawnerId 刷怪点ID
     * @return 冷却时间，如果读取失败返回默认值30
     */
    private int getSpawnerCooldownFromConfig(String spawnerId) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return 30; // 默认值
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection spawnersSection = config.getConfigurationSection("mythicMobsSpawners");

            if (spawnersSection != null) {
                ConfigurationSection spawnerSection = spawnersSection.getConfigurationSection(spawnerId);
                if (spawnerSection != null) {
                    return spawnerSection.getInt("cooldown", 30);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("读取刷怪点冷却时间失败: " + e.getMessage());
        }
        return 30; // 默认值
    }
}
