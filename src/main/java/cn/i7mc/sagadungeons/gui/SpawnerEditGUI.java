package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.model.MobSpawner;
import cn.i7mc.sagadungeons.util.DebugUtil;
import cn.i7mc.sagadungeons.util.LocationUtil;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 刷怪点编辑界面
 * 用于编辑单个刷怪点的详细属性
 */
public class SpawnerEditGUI extends AbstractGUI {

    private final String templateName;
    private final String spawnerName;
    private final DungeonTemplate template;
    private MobSpawner spawner;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     * @param templateName 模板名称
     * @param spawnerName 刷怪点名称
     */
    public SpawnerEditGUI(SagaDungeons plugin, Player player, String templateName, String spawnerName) {
        super(plugin, player, getGUITitle(plugin, spawnerName), 45);
        this.templateName = templateName;
        this.spawnerName = spawnerName;
        this.template = plugin.getConfigManager().getTemplateManager().getTemplates().get(templateName);
        this.spawner = template != null ? template.getMobSpawners().get(spawnerName) : null;
    }

    /**
     * 获取GUI标题
     */
    private static String getGUITitle(SagaDungeons plugin, String spawnerName) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", spawnerName);
        return plugin.getConfigManager().getGUILanguageManager().getGUIText("spawner-edit.title-with-name", placeholders);
    }

    @Override
    public void init() {
        // 检查模板和刷怪点是否存在
        if (template == null || spawner == null) {
            MessageUtil.sendMessage(player, "command.admin.edit.spawner.not-found",
                    MessageUtil.createPlaceholders("spawner", spawnerName));
            close();
            return;
        }

        // 清空界面
        inventory.clear();

        // 添加装饰边框
        addBorder();

        // 添加刷怪点信息显示
        addSpawnerInfo();

        // 添加编辑按钮
        addEditButtons();

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
            inventory.setItem(36 + i, borderItem);
        }

        // 左右边框
        for (int i = 1; i < 4; i++) {
            inventory.setItem(i * 9, borderItem);
            inventory.setItem(i * 9 + 8, borderItem);
        }
    }

    /**
     * 添加刷怪点信息显示
     */
    private void addSpawnerInfo() {
        // 从配置文件读取实际数据
        String actualMobType = getSpawnerMobTypeFromConfig();
        String actualLocation = getSpawnerLocationFromConfig();
        int actualAmount = getSpawnerAmountFromConfig();
        int actualCooldown = getSpawnerCooldownFromConfig();

        // 刷怪点基本信息
        ItemStack infoItem = new ItemBuilder(Material.SPAWNER)
                .setName(getGUIText("spawner-edit.spawner-info"))
                .setLore(
                        getGUIText("spawner-edit.spawner-id", createPlaceholder("id", spawnerName)),
                        getGUIText("spawner-edit.mob-type", createPlaceholder("type", actualMobType != null ? actualMobType : spawner.getMobType())),
                        getGUIText("spawner-edit.spawn-amount", createPlaceholder("amount", String.valueOf(actualAmount))),
                        getGUIText("spawner-edit.cooldown-time", createPlaceholder("time", String.valueOf(actualCooldown))),
                        getGUIText("spawner-edit.location", createPlaceholder("location", formatLocation(actualLocation))),
                        "",
                        getGUIText("spawner-edit.spawner-info-desc")
                )
                .build();
        inventory.setItem(13, infoItem);
    }

    /**
     * 添加编辑按钮
     */
    private void addEditButtons() {
        // 编辑怪物类型
        ItemStack mobTypeItem = new ItemBuilder(Material.ZOMBIE_SPAWN_EGG)
                .setName(getGUIText("spawner-edit.mob-type-edit"))
                .setLore(
                        getGUIText("spawner-edit.mob-type-current", createPlaceholder("type", getSpawnerMobTypeFromConfig())),
                        "",
                        getGUIText("spawner-edit.mob-type-click")
                )
                .build();
        inventory.setItem(19, mobTypeItem);

        // 编辑生成数量
        ItemStack amountItem = new ItemBuilder(Material.REDSTONE)
                .setName(getGUIText("spawner-edit.spawn-amount-edit"))
                .setLore(
                        getGUIText("spawner-edit.spawn-amount-current", createPlaceholder("amount", String.valueOf(getSpawnerAmountFromConfig()))),
                        "",
                        getGUIText("spawner-edit.spawn-amount-click")
                )
                .build();
        inventory.setItem(21, amountItem);

        // 编辑冷却时间
        ItemStack cooldownItem = new ItemBuilder(Material.CLOCK)
                .setName(getGUIText("spawner-edit.cooldown-edit"))
                .setLore(
                        getGUIText("spawner-edit.cooldown-current", createPlaceholder("time", String.valueOf(getSpawnerCooldownFromConfig()))),
                        "",
                        getGUIText("spawner-edit.cooldown-click")
                )
                .build();
        inventory.setItem(23, cooldownItem);

        // 编辑位置
        ItemStack locationItem = new ItemBuilder(Material.COMPASS)
                .setName(getGUIText("spawner-edit.location-edit"))
                .setLore(
                        getGUIText("spawner-edit.location-current", createPlaceholder("location", formatLocation(getSpawnerLocationFromConfig()))),
                        "",
                        getGUIText("spawner-edit.location-set-click"),
                        getGUIText("spawner-edit.location-teleport-click")
                )
                .build();
        inventory.setItem(25, locationItem);
    }

    /**
     * 添加功能按钮
     */
    private void addFunctionButtons() {
        // 保存按钮
        ItemStack saveItem = new ItemBuilder(Material.EMERALD)
                .setName(getGUIText("spawner-edit.save-changes"))
                .setLore(
                        getGUIText("spawner-edit.save-changes-desc"),
                        "",
                        getGUIText("spawner-edit.save-click")
                )
                .build();
        inventory.setItem(40, saveItem);

        // 返回按钮
        ItemStack backItem = new ItemBuilder(Material.BARRIER)
                .setName(getGUIText("common.back"))
                .setLore(getGUIText("spawner-edit.back-to-spawners"))
                .build();
        inventory.setItem(36, backItem);

        // 删除刷怪点按钮
        ItemStack deleteItem = new ItemBuilder(Material.TNT)
                .setName(getGUIText("spawner-edit.delete-spawner"))
                .setLore(
                        getGUIText("spawner-edit.delete-spawner-desc"),
                        "",
                        getGUIText("spawner-edit.delete-click")
                )
                .build();
        inventory.setItem(44, deleteItem);

        // 测试刷怪点按钮
        ItemStack testItem = new ItemBuilder(Material.ENDER_EYE)
                .setName(getGUIText("spawner-edit.test-spawner"))
                .setLore(
                        getGUIText("spawner-edit.test-spawner-desc"),
                        "",
                        getGUIText("spawner-edit.test-click")
                )
                .build();
        inventory.setItem(42, testItem);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        int slot = event.getSlot();

        switch (slot) {
            case 19: // 编辑怪物类型
                handleEditMobType();
                break;
            case 21: // 编辑生成数量
                handleEditAmount();
                break;
            case 23: // 编辑冷却时间
                handleEditCooldown();
                break;
            case 25: // 编辑位置
                if (event.isLeftClick()) {
                    handleSetCurrentLocation();
                } else if (event.getClick() == org.bukkit.event.inventory.ClickType.MIDDLE) {
                    handleTeleportToLocation();
                }
                break;
            case 36: // 返回
                handleBack();
                break;
            case 40: // 保存
                handleSave();
                break;
            case 42: // 测试
                handleTest();
                break;
            case 44: // 删除
                if (event.isRightClick()) {
                    handleDelete();
                }
                break;
        }
    }

    /**
     * 格式化位置字符串
     */
    private String formatLocation(String location) {
        if (location == null || location.isEmpty()) {
            return getGUIText("spawner-edit.location-not-set");
        }

        String[] parts = location.split(",");
        if (parts.length >= 3) {
            try {
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                double z = Double.parseDouble(parts[2]);
                return String.format("%.1f, %.1f, %.1f", x, y, z);
            } catch (NumberFormatException e) {
                return getGUIText("spawner-edit.location-format-error");
            }
        }

        return location;
    }

    // ==================== 配置读取方法 ====================

    /**
     * 从配置文件读取刷怪点怪物类型
     */
    private String getSpawnerMobTypeFromConfig() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return spawner.getMobType();
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection spawnersSection = config.getConfigurationSection("mythicMobsSpawners");

            if (spawnersSection != null) {
                ConfigurationSection spawnerSection = spawnersSection.getConfigurationSection(spawnerName);
                if (spawnerSection != null) {
                    return spawnerSection.getString("mobType", spawner.getMobType());
                }
            }
        } catch (Exception e) {
            DebugUtil.debug("dungeon.spawner.read-mobtype-fail", DebugUtil.createPlaceholders("message", e.getMessage()));
        }
        return spawner.getMobType();
    }

    /**
     * 从配置文件读取刷怪点位置
     */
    private String getSpawnerLocationFromConfig() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return spawner.getLocation();
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection spawnersSection = config.getConfigurationSection("mythicMobsSpawners");

            if (spawnersSection != null) {
                ConfigurationSection spawnerSection = spawnersSection.getConfigurationSection(spawnerName);
                if (spawnerSection != null) {
                    return spawnerSection.getString("location", spawner.getLocation());
                }
            }
        } catch (Exception e) {
            DebugUtil.debug("dungeon.spawner.read-location-fail", DebugUtil.createPlaceholders("message", e.getMessage()));
        }
        return spawner.getLocation();
    }

    /**
     * 从配置文件读取刷怪点生成数量
     */
    private int getSpawnerAmountFromConfig() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return spawner.getAmount();
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection spawnersSection = config.getConfigurationSection("mythicMobsSpawners");

            if (spawnersSection != null) {
                ConfigurationSection spawnerSection = spawnersSection.getConfigurationSection(spawnerName);
                if (spawnerSection != null) {
                    return spawnerSection.getInt("amount", spawner.getAmount());
                }
            }
        } catch (Exception e) {
            DebugUtil.debug("dungeon.spawner.read-amount-fail", DebugUtil.createPlaceholders("message", e.getMessage()));
        }
        return spawner.getAmount();
    }

    /**
     * 从配置文件读取刷怪点冷却时间
     */
    private int getSpawnerCooldownFromConfig() {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            if (!configFile.exists()) {
                return spawner.getCooldown();
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection spawnersSection = config.getConfigurationSection("mythicMobsSpawners");

            if (spawnersSection != null) {
                ConfigurationSection spawnerSection = spawnersSection.getConfigurationSection(spawnerName);
                if (spawnerSection != null) {
                    return spawnerSection.getInt("cooldown", spawner.getCooldown());
                }
            }
        } catch (Exception e) {
            DebugUtil.debug("dungeon.spawner.read-cooldown-fail", DebugUtil.createPlaceholders("message", e.getMessage()));
        }
        return spawner.getCooldown();
    }

    // ==================== 事件处理方法 ====================

    /**
     * 处理编辑怪物类型
     */
    private void handleEditMobType() {
        close();
        plugin.getChatInputListener().requestTextInput(player, "command.admin.edit.spawner.input-mobtype",
            (input) -> {
                // 在主线程中执行回调
                Bukkit.getScheduler().runTask(plugin, () -> {
                    updateSpawnerMobType(input);
                    plugin.getGUIManager().openSpawnerEditGUI(player, templateName, spawnerName);
                });
            });
    }

    /**
     * 处理编辑生成数量
     */
    private void handleEditAmount() {
        close();
        plugin.getChatInputListener().requestNumberInput(player, "command.admin.edit.spawner.input-amount",
            (input) -> {
                // 在主线程中执行回调
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        int amount = Integer.parseInt(input);
                        if (amount > 0 && amount <= 100) {
                            updateSpawnerAmount(amount);
                            plugin.getGUIManager().openSpawnerEditGUI(player, templateName, spawnerName);
                        } else {
                            MessageUtil.sendMessage(player, "command.admin.edit.spawner.invalid-amount");
                            plugin.getGUIManager().openSpawnerEditGUI(player, templateName, spawnerName);
                        }
                    } catch (NumberFormatException e) {
                        MessageUtil.sendMessage(player, "command.admin.edit.invalid-number");
                        plugin.getGUIManager().openSpawnerEditGUI(player, templateName, spawnerName);
                    }
                });
            });
    }

    /**
     * 处理编辑冷却时间
     */
    private void handleEditCooldown() {
        close();
        plugin.getChatInputListener().requestNumberInput(player, "command.admin.edit.spawner.input-cooldown",
            (input) -> {
                // 在主线程中执行回调
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        int cooldown = Integer.parseInt(input);
                        if (cooldown > 0 && cooldown <= 3600) {
                            updateSpawnerCooldown(cooldown);
                            plugin.getGUIManager().openSpawnerEditGUI(player, templateName, spawnerName);
                        } else {
                            MessageUtil.sendMessage(player, "command.admin.edit.spawner.invalid-cooldown");
                            plugin.getGUIManager().openSpawnerEditGUI(player, templateName, spawnerName);
                        }
                    } catch (NumberFormatException e) {
                        MessageUtil.sendMessage(player, "command.admin.edit.invalid-number");
                        plugin.getGUIManager().openSpawnerEditGUI(player, templateName, spawnerName);
                    }
                });
            });
    }

    /**
     * 处理设置当前位置
     */
    private void handleSetCurrentLocation() {
        Location location = player.getLocation();
        String locationStr = LocationUtil.locationToStringWithoutWorld(location);

        updateSpawnerLocation(locationStr);
        MessageUtil.sendMessage(player, "command.admin.edit.spawner.location-updated");
        init(); // 刷新界面
    }

    /**
     * 处理传送到位置
     */
    private void handleTeleportToLocation() {
        String locationStr = getSpawnerLocationFromConfig();
        if (locationStr == null || locationStr.isEmpty()) {
            MessageUtil.sendMessage(player, "command.admin.edit.spawner.no-location");
            return;
        }

        Location location = LocationUtil.stringToLocationWithoutWorld(locationStr, player.getWorld());
        if (location == null) {
            MessageUtil.sendMessage(player, "command.admin.edit.spawner.invalid-location");
            return;
        }

        Location safeLocation = LocationUtil.findSafeLocation(location);
        player.teleport(safeLocation);
        MessageUtil.sendMessage(player, "command.admin.edit.spawner.teleported");
        close();
    }

    /**
     * 处理返回
     */
    private void handleBack() {
        close();
        plugin.getGUIManager().openTemplateSpawnersEditGUI(player, templateName);
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
     * 处理测试
     */
    private void handleTest() {
        handleTeleportToLocation();
    }

    /**
     * 处理删除
     */
    private void handleDelete() {
        template.removeMobSpawner(spawnerName);
        plugin.getConfigManager().getTemplateManager().saveTemplate(template);

        // 保存后重新加载模板，确保内存中的模板与配置文件同步
        plugin.getConfigManager().getTemplateManager().reloadTemplate(templateName);

        MessageUtil.sendMessage(player, "command.admin.edit.spawner.deleted",
                MessageUtil.createPlaceholders("spawner", spawnerName));
        close();
        plugin.getGUIManager().openTemplateSpawnersEditGUI(player, templateName);
    }

    // ==================== 配置更新方法 ====================

    /**
     * 更新刷怪点怪物类型
     */
    private void updateSpawnerMobType(String mobType) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection spawnersSection = config.getConfigurationSection("mythicMobsSpawners");

            if (spawnersSection == null) {
                spawnersSection = config.createSection("mythicMobsSpawners");
            }

            ConfigurationSection spawnerSection = spawnersSection.getConfigurationSection(spawnerName);
            if (spawnerSection == null) {
                spawnerSection = spawnersSection.createSection(spawnerName);
            }

            spawnerSection.set("mobType", mobType);
            config.save(configFile);

            // 更新内存中的数据
            spawner = new MobSpawner(spawnerName, mobType, spawner.getLocation());
            spawner.setAmount(getSpawnerAmountFromConfig());
            spawner.setCooldown(getSpawnerCooldownFromConfig());
            template.getMobSpawners().put(spawnerName, spawner);

            MessageUtil.sendMessage(player, "command.admin.edit.spawner.mobtype-updated",
                    MessageUtil.createPlaceholders("mobtype", mobType));
        } catch (Exception e) {
            DebugUtil.debug("dungeon.spawner.update-mobtype-fail", DebugUtil.createPlaceholders("message", e.getMessage()));
            MessageUtil.sendMessage(player, "command.admin.edit.spawner.update-failed");
        }
    }

    /**
     * 更新刷怪点生成数量
     */
    private void updateSpawnerAmount(int amount) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection spawnersSection = config.getConfigurationSection("mythicMobsSpawners");

            if (spawnersSection == null) {
                spawnersSection = config.createSection("mythicMobsSpawners");
            }

            ConfigurationSection spawnerSection = spawnersSection.getConfigurationSection(spawnerName);
            if (spawnerSection == null) {
                spawnerSection = spawnersSection.createSection(spawnerName);
            }

            spawnerSection.set("amount", amount);
            config.save(configFile);

            // 更新内存中的数据
            spawner.setAmount(amount);

            MessageUtil.sendMessage(player, "command.admin.edit.spawner.amount-updated",
                    MessageUtil.createPlaceholders("amount", String.valueOf(amount)));
        } catch (Exception e) {
            DebugUtil.debug("dungeon.spawner.update-amount-fail", DebugUtil.createPlaceholders("message", e.getMessage()));
            MessageUtil.sendMessage(player, "command.admin.edit.spawner.update-failed");
        }
    }

    /**
     * 更新刷怪点冷却时间
     */
    private void updateSpawnerCooldown(int cooldown) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection spawnersSection = config.getConfigurationSection("mythicMobsSpawners");

            if (spawnersSection == null) {
                spawnersSection = config.createSection("mythicMobsSpawners");
            }

            ConfigurationSection spawnerSection = spawnersSection.getConfigurationSection(spawnerName);
            if (spawnerSection == null) {
                spawnerSection = spawnersSection.createSection(spawnerName);
            }

            spawnerSection.set("cooldown", cooldown);
            config.save(configFile);

            // 更新内存中的数据
            spawner.setCooldown(cooldown);

            MessageUtil.sendMessage(player, "command.admin.edit.spawner.cooldown-updated",
                    MessageUtil.createPlaceholders("cooldown", String.valueOf(cooldown)));
        } catch (Exception e) {
            DebugUtil.debug("dungeon.spawner.update-cooldown-fail", DebugUtil.createPlaceholders("message", e.getMessage()));
            MessageUtil.sendMessage(player, "command.admin.edit.spawner.update-failed");
        }
    }

    /**
     * 更新刷怪点位置
     */
    private void updateSpawnerLocation(String location) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection spawnersSection = config.getConfigurationSection("mythicMobsSpawners");

            if (spawnersSection == null) {
                spawnersSection = config.createSection("mythicMobsSpawners");
            }

            ConfigurationSection spawnerSection = spawnersSection.getConfigurationSection(spawnerName);
            if (spawnerSection == null) {
                spawnerSection = spawnersSection.createSection(spawnerName);
            }

            spawnerSection.set("location", location);
            config.save(configFile);

            // 更新内存中的数据
            spawner = new MobSpawner(spawnerName, spawner.getMobType(), location);
            spawner.setAmount(spawner.getAmount());
            spawner.setCooldown(spawner.getCooldown());
            template.getMobSpawners().put(spawnerName, spawner);

        } catch (Exception e) {
            DebugUtil.debug("dungeon.spawner.update-location-fail", DebugUtil.createPlaceholders("message", e.getMessage()));
            MessageUtil.sendMessage(player, "command.admin.edit.spawner.update-failed");
        }
    }
}
