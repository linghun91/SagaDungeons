package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.util.LocationUtil;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;

/**
 * 刷怪点添加界面
 * 用于添加新的刷怪点到副本模板
 */
public class SpawnerAddGUI extends AbstractGUI {

    private final SagaDungeons plugin;
    private final Player player;
    private final String templateName;
    private final DungeonTemplate template;

    // 默认值
    private String mobType = "Zombie";
    private int amount = 1;
    private int cooldown = 30;
    private String location;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param player 玩家
     * @param templateName 模板名称
     */
    public SpawnerAddGUI(SagaDungeons plugin, Player player, String templateName) {
        super(plugin, player, plugin.getConfigManager().getGUILanguageManager().getGUIText("spawner-add.title"), 54);
        this.plugin = plugin;
        this.player = player;
        this.templateName = templateName;
        this.template = plugin.getConfigManager().getTemplateManager().getTemplate(templateName);

        // 设置当前位置为默认位置
        this.location = LocationUtil.locationToStringWithoutWorld(player.getLocation());

        init();
    }

    @Override
    public void init() {
        // 清空界面
        inventory.clear();

        // 添加装饰边框
        addBorder();

        // 添加编辑按钮
        addEditButtons();

        // 添加功能按钮
        addFunctionButtons();
    }

    /**
     * 添加装饰边框
     */
    private void addBorder() {
        ItemStack borderItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setName(" ")
                .build();

        // 上边框
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, borderItem);
        }

        // 下边框
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, borderItem);
        }

        // 左右边框
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, borderItem);
            inventory.setItem(i + 8, borderItem);
        }
    }

    /**
     * 添加编辑按钮
     */
    private void addEditButtons() {
        // 编辑怪物类型
        ItemStack mobTypeItem = new ItemBuilder(Material.ZOMBIE_SPAWN_EGG)
                .setName(getGUIText("spawner-add.edit-mob-type"))
                .setLore(
                        getGUIText("spawner-add.edit-mob-type-current", createPlaceholder("type", mobType)),
                        "",
                        getGUIText("spawner-add.edit-mob-type-click")
                )
                .build();
        inventory.setItem(19, mobTypeItem);

        // 编辑生成数量
        ItemStack amountItem = new ItemBuilder(Material.REDSTONE)
                .setName(getGUIText("spawner-add.edit-spawn-amount"))
                .setLore(
                        getGUIText("spawner-add.edit-spawn-amount-current", createPlaceholder("amount", String.valueOf(amount))),
                        "",
                        getGUIText("spawner-add.edit-spawn-amount-click")
                )
                .build();
        inventory.setItem(21, amountItem);

        // 编辑冷却时间
        ItemStack cooldownItem = new ItemBuilder(Material.CLOCK)
                .setName(getGUIText("spawner-add.edit-cooldown"))
                .setLore(
                        getGUIText("spawner-add.edit-cooldown-current", createPlaceholder("time", String.valueOf(cooldown))),
                        "",
                        getGUIText("spawner-add.edit-cooldown-click")
                )
                .build();
        inventory.setItem(23, cooldownItem);

        // 编辑位置
        ItemStack locationItem = new ItemBuilder(Material.COMPASS)
                .setName(getGUIText("spawner-add.edit-location"))
                .setLore(
                        getGUIText("spawner-add.edit-location-current", createPlaceholder("location", location != null ? location : getGUIText("spawner-add.location-not-set"))),
                        "",
                        getGUIText("spawner-add.edit-location-set-click"),
                        getGUIText("spawner-add.edit-location-teleport-click")
                )
                .build();
        inventory.setItem(25, locationItem);
    }

    /**
     * 添加功能按钮
     */
    private void addFunctionButtons() {
        // 返回按钮
        ItemStack backButton = new ItemBuilder(Material.BARRIER)
                .setName(getGUIText("common.back"))
                .setLore(getGUIText("spawner-add.back-to-spawners"))
                .build();
        inventory.setItem(36, backButton);

        // 创建按钮
        ItemStack createButton = new ItemBuilder(Material.EMERALD)
                .setName(getGUIText("spawner-add.create-spawner"))
                .setLore(
                        getGUIText("spawner-add.create-spawner-desc"),
                        "",
                        getGUIText("spawner-add.create-spawner-mob-type", createPlaceholder("type", mobType)),
                        getGUIText("spawner-add.create-spawner-amount", createPlaceholder("amount", String.valueOf(amount))),
                        getGUIText("spawner-add.create-spawner-cooldown", createPlaceholder("time", String.valueOf(cooldown))),
                        getGUIText("spawner-add.create-spawner-location", createPlaceholder("location", location != null ? getGUIText("spawner-add.create-spawner-location-set") : getGUIText("spawner-add.location-not-set"))),
                        "",
                        getGUIText("spawner-add.create-spawner-click")
                )
                .build();
        inventory.setItem(40, createButton);

        // 预览按钮
        ItemStack previewButton = new ItemBuilder(Material.ENDER_EYE)
                .setName(getGUIText("spawner-add.preview-settings"))
                .setLore(
                        getGUIText("spawner-add.preview-settings-desc"),
                        "",
                        getGUIText("spawner-add.create-spawner-mob-type", createPlaceholder("type", mobType)),
                        getGUIText("spawner-add.create-spawner-amount", createPlaceholder("amount", String.valueOf(amount))),
                        getGUIText("spawner-add.create-spawner-cooldown", createPlaceholder("time", String.valueOf(cooldown))),
                        getGUIText("spawner-add.create-spawner-location", createPlaceholder("location", location != null ? location : getGUIText("spawner-add.location-not-set")))
                )
                .build();
        inventory.setItem(42, previewButton);

        // 重置按钮
        ItemStack resetButton = new ItemBuilder(Material.RED_DYE)
                .setName(getGUIText("spawner-add.reset-settings"))
                .setLore(
                        getGUIText("spawner-add.reset-settings-desc"),
                        "",
                        getGUIText("spawner-add.reset-settings-click")
                )
                .build();
        inventory.setItem(44, resetButton);
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
            case 40: // 创建
                handleCreate();
                break;
            case 42: // 预览
                handlePreview();
                break;
            case 44: // 重置
                if (event.isRightClick()) {
                    handleReset();
                }
                break;
        }
    }

    /**
     * 处理编辑怪物类型
     */
    private void handleEditMobType() {
        close();
        MessageUtil.sendMessage(player, "command.admin.edit.spawner.input-mobtype");
        
        plugin.getChatInputListener().requestTextInput(player, "command.admin.edit.spawner.input-mobtype", input -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (input != null && !input.trim().isEmpty()) {
                    mobType = input.trim();
                    MessageUtil.sendMessage(player, "command.admin.edit.spawner.mobtype-updated",
                            MessageUtil.createPlaceholders("mobtype", mobType));
                } else {
                    MessageUtil.sendMessage(player, "command.admin.edit.invalid-input");
                }
                plugin.getGUIManager().openSpawnerAddGUI(player, templateName);
            });
        });
    }

    /**
     * 处理编辑生成数量
     */
    private void handleEditAmount() {
        close();
        MessageUtil.sendMessage(player, "command.admin.edit.spawner.input-amount");
        
        plugin.getChatInputListener().requestNumberInput(player, "command.admin.edit.spawner.input-amount", input -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    int newAmount = Integer.parseInt(input);
                    if (newAmount > 0 && newAmount <= 100) {
                        amount = newAmount;
                        MessageUtil.sendMessage(player, "command.admin.edit.spawner.amount-updated",
                                MessageUtil.createPlaceholders("amount", String.valueOf(amount)));
                    } else {
                        MessageUtil.sendMessage(player, "command.admin.edit.spawner.invalid-amount");
                    }
                } catch (NumberFormatException e) {
                    MessageUtil.sendMessage(player, "command.admin.edit.invalid-number");
                }
                plugin.getGUIManager().openSpawnerAddGUI(player, templateName);
            });
        });
    }

    /**
     * 处理编辑冷却时间
     */
    private void handleEditCooldown() {
        close();
        MessageUtil.sendMessage(player, "command.admin.edit.spawner.input-cooldown");
        
        plugin.getChatInputListener().requestNumberInput(player, "command.admin.edit.spawner.input-cooldown", input -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    int newCooldown = Integer.parseInt(input);
                    if (newCooldown >= 0 && newCooldown <= 3600) {
                        cooldown = newCooldown;
                        MessageUtil.sendMessage(player, "command.admin.edit.spawner.cooldown-updated",
                                MessageUtil.createPlaceholders("cooldown", String.valueOf(cooldown)));
                    } else {
                        MessageUtil.sendMessage(player, "command.admin.edit.spawner.invalid-cooldown");
                    }
                } catch (NumberFormatException e) {
                    MessageUtil.sendMessage(player, "command.admin.edit.invalid-number");
                }
                plugin.getGUIManager().openSpawnerAddGUI(player, templateName);
            });
        });
    }

    /**
     * 处理设置当前位置
     */
    private void handleSetCurrentLocation() {
        Location currentLocation = player.getLocation();
        location = LocationUtil.locationToStringWithoutWorld(currentLocation);
        MessageUtil.sendMessage(player, "command.admin.edit.spawner.location-updated");
        init(); // 刷新界面
    }

    /**
     * 处理传送到位置
     */
    private void handleTeleportToLocation() {
        if (location == null || location.isEmpty()) {
            MessageUtil.sendMessage(player, "command.admin.edit.spawner.no-location");
            return;
        }

        Location targetLocation = LocationUtil.stringToLocationWithoutWorld(location, player.getWorld());
        if (targetLocation == null) {
            MessageUtil.sendMessage(player, "command.admin.edit.spawner.invalid-location");
            return;
        }

        Location safeLocation = LocationUtil.findSafeLocation(targetLocation);
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
     * 处理创建刷怪点
     */
    private void handleCreate() {
        // 验证必要信息
        if (location == null || location.isEmpty()) {
            MessageUtil.sendMessage(player, "command.admin.edit.spawner.no-location");
            return;
        }

        if (mobType == null || mobType.trim().isEmpty()) {
            MessageUtil.sendMessage(player, "command.admin.edit.spawner.invalid-mobtype");
            return;
        }

        // 生成唯一的刷怪点ID
        String spawnerId = generateUniqueSpawnerId();

        try {
            // 添加刷怪点到模板
            template.addMobSpawner(spawnerId, mobType, location, cooldown, amount);

            // 保存到配置文件
            saveSpawnerToConfig(spawnerId);

            // 保存模板
            plugin.getConfigManager().getTemplateManager().saveTemplate(template);

            // 保存后重新加载模板，确保内存中的模板与配置文件同步
            plugin.getConfigManager().getTemplateManager().reloadTemplate(templateName);

            MessageUtil.sendMessage(player, "command.admin.edit.spawner.created",
                    MessageUtil.createPlaceholders("spawner", spawnerId));

            // 返回刷怪点管理界面
            close();
            plugin.getGUIManager().openTemplateSpawnersEditGUI(player, templateName);

        } catch (Exception e) {
            plugin.getLogger().warning("创建刷怪点失败: " + e.getMessage());
            MessageUtil.sendMessage(player, "command.admin.edit.spawner.create-failed");
        }
    }

    /**
     * 处理预览设置
     */
    private void handlePreview() {
        MessageUtil.sendMessage(player, "command.admin.edit.spawner.preview-header");
        MessageUtil.sendMessage(player, "command.admin.edit.spawner.preview-mobtype",
                MessageUtil.createPlaceholders("mobtype", mobType));
        MessageUtil.sendMessage(player, "command.admin.edit.spawner.preview-amount",
                MessageUtil.createPlaceholders("amount", String.valueOf(amount)));
        MessageUtil.sendMessage(player, "command.admin.edit.spawner.preview-cooldown",
                MessageUtil.createPlaceholders("cooldown", String.valueOf(cooldown)));
        MessageUtil.sendMessage(player, "command.admin.edit.spawner.preview-location",
                MessageUtil.createPlaceholders("location", location != null ? location : getGUIText("spawner-add.location-not-set")));
    }

    /**
     * 处理重置设置
     */
    private void handleReset() {
        mobType = "Zombie";
        amount = 1;
        cooldown = 30;
        location = LocationUtil.locationToStringWithoutWorld(player.getLocation());

        MessageUtil.sendMessage(player, "command.admin.edit.spawner.reset-success");
        init(); // 刷新界面
    }

    /**
     * 生成唯一的刷怪点ID
     * @return 唯一的刷怪点ID
     */
    private String generateUniqueSpawnerId() {
        String baseId = "spawner_";
        int counter = 1;

        // 检查现有的刷怪点ID
        while (template.getMobSpawners().containsKey(baseId + counter)) {
            counter++;
        }

        return baseId + counter;
    }

    /**
     * 保存刷怪点到配置文件
     * @param spawnerId 刷怪点ID
     */
    private void saveSpawnerToConfig(String spawnerId) {
        try {
            File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
            File configFile = new File(templateDir, "config.yml");

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection spawnersSection = config.getConfigurationSection("mythicMobsSpawners");

            if (spawnersSection == null) {
                spawnersSection = config.createSection("mythicMobsSpawners");
            }

            ConfigurationSection spawnerSection = spawnersSection.createSection(spawnerId);
            spawnerSection.set("mobType", mobType);
            spawnerSection.set("location", location);
            spawnerSection.set("cooldown", cooldown);
            spawnerSection.set("amount", amount);

            config.save(configFile);

        } catch (Exception e) {
            plugin.getLogger().warning("保存刷怪点配置失败: " + e.getMessage());
            throw new RuntimeException("保存刷怪点配置失败", e);
        }
    }
}
