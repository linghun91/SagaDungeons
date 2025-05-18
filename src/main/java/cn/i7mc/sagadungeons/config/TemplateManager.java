package cn.i7mc.sagadungeons.config;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.completion.CompletionManager;
import cn.i7mc.sagadungeons.dungeon.condition.RequirementManager;
import cn.i7mc.sagadungeons.dungeon.reward.RewardManager;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.model.MobSpawner;

import java.io.IOException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 模板管理器
 * 负责管理副本模板配置
 */
public class TemplateManager {

    private final SagaDungeons plugin;
    private final Map<String, DungeonTemplate> templates = new HashMap<>();
    private final File templatesDir;
    private final RequirementManager requirementManager;
    private final CompletionManager completionManager;
    private final RewardManager rewardManager;

    public TemplateManager(SagaDungeons plugin) {
        this.plugin = plugin;
        this.templatesDir = new File(plugin.getDataFolder(), "templates");
        this.requirementManager = new RequirementManager(plugin);
        this.completionManager = new CompletionManager(plugin);
        this.rewardManager = new RewardManager(plugin);

        // 确保模板目录存在
        if (!templatesDir.exists()) {
            templatesDir.mkdirs();

            // 保存示例模板
            plugin.saveResource("templates/example/config.yml", false);
        }
    }

    /**
     * 加载所有模板
     */
    public void loadTemplates() {
        templates.clear();

        // 确保模板目录存在
        if (!templatesDir.exists() || !templatesDir.isDirectory()) {
            templatesDir.mkdirs();
            return;
        }

        // 遍历模板目录
        File[] templateDirs = templatesDir.listFiles(File::isDirectory);
        if (templateDirs == null) {
            return;
        }

        // 加载每个模板
        for (File templateDir : templateDirs) {
            String templateName = templateDir.getName();
            File configFile = new File(templateDir, "config.yml");

            // 检查配置文件是否存在
            if (!configFile.exists()) {
                continue;
            }

            // 加载配置文件
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

            // 创建模板对象
            DungeonTemplate template = new DungeonTemplate(templateName);
            template.setDisplayName(config.getString("displayName", templateName));
            template.setDefaultTimeout(config.getInt("defaultTimeout", plugin.getConfigManager().getDefaultTimeout()));

            // 加载创建条件
            ConfigurationSection conditionsSection = config.getConfigurationSection("creationConditions");
            if (conditionsSection != null) {
                // 加载条件启用状态
                if (conditionsSection.contains("moneyEnabled")) {
                    template.setMoneyEnabled(conditionsSection.getBoolean("moneyEnabled", true));
                }

                if (conditionsSection.contains("pointsEnabled")) {
                    template.setPointsEnabled(conditionsSection.getBoolean("pointsEnabled", true));
                }

                if (conditionsSection.contains("levelEnabled")) {
                    template.setLevelEnabled(conditionsSection.getBoolean("levelEnabled", true));
                }

                if (conditionsSection.contains("itemsEnabled")) {
                    template.setItemsEnabled(conditionsSection.getBoolean("itemsEnabled", true));
                }

                // 金币条件
                if (conditionsSection.contains("money")) {
                    template.setMoneyCost(conditionsSection.getDouble("money"));
                }

                // 点券条件
                if (conditionsSection.contains("points")) {
                    template.setPointsCost(conditionsSection.getInt("points"));
                }

                // 等级条件
                if (conditionsSection.contains("level")) {
                    template.setLevelRequirement(conditionsSection.getInt("level"));
                }

                // 使用条件管理器加载条件
                requirementManager.loadRequirements(template, conditionsSection);
            }

            // 加载死亡限制
            if (config.contains("deathLimit")) {
                template.setDeathLimit(config.getInt("deathLimit"));
            }

            // 加载世界路径
            if (config.contains("worldPath")) {
                template.setWorldPath(config.getString("worldPath"));
            }

            // 加载重生点
            if (config.contains("spawnLocation")) {
                template.setSpawnLocation(config.getString("spawnLocation"));
            }

            // 加载复活道具
            ConfigurationSection reviveItemSection = config.getConfigurationSection("reviveItem");
            if (reviveItemSection != null) {
                // 检查是否是序列化物品
                if (reviveItemSection.contains("serialized-item")) {
                    String serializedItem = reviveItemSection.getString("serialized-item");
                    if (serializedItem != null && !serializedItem.isEmpty()) {
                        template.setSerializedReviveItem(serializedItem);
                    }
                } else {
                    String material = reviveItemSection.getString("material");
                    String name = reviveItemSection.getString("name");

                    if (material != null && !material.isEmpty()) {
                        template.setReviveItemMaterial(material);
                        template.setReviveItemName(name);
                    }
                }
            }

            // 加载通关条件
            ConfigurationSection completionSection = config.getConfigurationSection("completion");
            if (completionSection != null) {
                completionManager.loadCompletionConditions(template, completionSection);
            }

            // 加载奖励
            ConfigurationSection rewardsSection = config.getConfigurationSection("rewards");
            if (rewardsSection != null) {
                rewardManager.loadRewards(template, rewardsSection);
            }

            // 加载MythicMobs刷怪点
            ConfigurationSection spawnersSection = config.getConfigurationSection("mythicMobsSpawners");
            if (spawnersSection != null) {
                for (String key : spawnersSection.getKeys(false)) {
                    ConfigurationSection spawnerSection = spawnersSection.getConfigurationSection(key);
                    if (spawnerSection != null) {
                        String mobType = spawnerSection.getString("mobType");
                        String location = spawnerSection.getString("location");
                        int cooldown = spawnerSection.getInt("cooldown", 30);
                        int amount = spawnerSection.getInt("amount", 1);

                        template.addMobSpawner(key, mobType, location, cooldown, amount);
                    }
                }
            }

            // 将模板添加到映射
            templates.put(templateName, template);
        }
    }

    /**
     * 获取所有模板
     * @return 模板映射
     */
    public Map<String, DungeonTemplate> getTemplates() {
        return templates;
    }

    /**
     * 保存模板
     * @param template 模板
     */
    public void saveTemplate(DungeonTemplate template) {
        // 获取模板目录
        File templateDir = new File(templatesDir, template.getName());

        // 检查目录是否存在
        if (!templateDir.exists()) {
            templateDir.mkdirs();
        }

        // 获取配置文件
        File configFile = new File(templateDir, "config.yml");

        // 加载配置
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // 保存基本信息
        config.set("name", template.getName());
        config.set("displayName", template.getDisplayName());
        config.set("defaultTimeout", template.getDefaultTimeout());

        // 保存创建条件
        ConfigurationSection conditionsSection = config.createSection("creationConditions");

        // 保存条件启用状态
        conditionsSection.set("moneyEnabled", template.isMoneyEnabled());
        conditionsSection.set("pointsEnabled", template.isPointsEnabled());
        conditionsSection.set("levelEnabled", template.isLevelEnabled());
        conditionsSection.set("itemsEnabled", template.isItemsEnabled());

        // 保存条件值
        conditionsSection.set("money", template.getMoneyCost());
        conditionsSection.set("points", template.getPointsCost());
        conditionsSection.set("level", template.getLevelRequirement());

        // 保存死亡限制
        config.set("deathLimit", template.getDeathLimit());

        // 保存世界路径
        if (template.hasWorldPath()) {
            config.set("worldPath", template.getWorldPath());
        }

        // 保存重生点
        if (template.hasSpawnLocation()) {
            config.set("spawnLocation", template.getSpawnLocation());
        }

        // 保存复活道具
        if (template.hasReviveItem()) {
            ConfigurationSection reviveItemSection = config.createSection("reviveItem");

            // 优先保存序列化物品
            if (template.hasSerializedReviveItem()) {
                reviveItemSection.set("serialized-item", template.getSerializedReviveItem());
            } else {
                reviveItemSection.set("material", template.getReviveItemMaterial());
                reviveItemSection.set("name", template.getReviveItemName());
            }
        }

        // 保存刷怪点
        ConfigurationSection spawnersSection = config.createSection("mythicMobsSpawners");
        for (Map.Entry<String, MobSpawner> entry : template.getMobSpawners().entrySet()) {
            String id = entry.getKey();
            MobSpawner spawner = entry.getValue();

            ConfigurationSection spawnerSection = spawnersSection.createSection(id);
            spawnerSection.set("mobType", spawner.getMobType());
            spawnerSection.set("location", spawner.getLocation());
            spawnerSection.set("cooldown", spawner.getCooldown());
            spawnerSection.set("amount", spawner.getAmount());
        }

        // 保存配置
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取指定模板
     * @param templateName 模板名称
     * @return 模板对象，如果不存在则返回null
     */
    public DungeonTemplate getTemplate(String templateName) {
        return templates.get(templateName);
    }

    /**
     * 检查模板是否存在
     * @param templateName 模板名称
     * @return 是否存在
     */
    public boolean hasTemplate(String templateName) {
        return templates.containsKey(templateName);
    }

    /**
     * 获取模板目录
     * @param templateName 模板名称
     * @return 模板目录
     */
    public File getTemplateDirectory(String templateName) {
        return new File(templatesDir, templateName);
    }
}
