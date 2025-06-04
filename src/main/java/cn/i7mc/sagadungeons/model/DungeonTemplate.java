package cn.i7mc.sagadungeons.model;

import cn.i7mc.sagadungeons.dungeon.condition.DungeonRequirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 副本模板数据模型
 * 存储副本模板的配置信息
 */
public class DungeonTemplate {

    private final String name;
    private String displayName;
    private int defaultTimeout;
    private double moneyCost;
    private int pointsCost;
    private int levelRequirement;
    private final Map<String, MobSpawner> mobSpawners = new HashMap<>();
    private final List<DungeonRequirement> requirements = new ArrayList<>();
    private int deathLimit;
    private String reviveItemMaterial;
    private String reviveItemName;

    /**
     * 构造函数
     * @param name 模板名称
     */
    public DungeonTemplate(String name) {
        this.name = name;
        this.displayName = name;
        this.defaultTimeout = 3600; // 默认1小时
    }

    /**
     * 获取模板名称
     * @return 模板名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取显示名称
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 设置显示名称
     * @param displayName 显示名称
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取默认超时时间(秒)
     * @return 默认超时时间
     */
    public int getDefaultTimeout() {
        return defaultTimeout;
    }

    /**
     * 设置默认超时时间(秒)
     * @param defaultTimeout 默认超时时间
     */
    public void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * 获取金币花费
     * @return 金币花费
     */
    public double getMoneyCost() {
        return moneyCost;
    }

    /**
     * 设置金币花费
     * @param moneyCost 金币花费
     */
    public void setMoneyCost(double moneyCost) {
        this.moneyCost = moneyCost;
    }

    /**
     * 获取点券花费
     * @return 点券花费
     */
    public int getPointsCost() {
        return pointsCost;
    }

    /**
     * 设置点券花费
     * @param pointsCost 点券花费
     */
    public void setPointsCost(int pointsCost) {
        this.pointsCost = pointsCost;
    }

    /**
     * 获取等级要求
     * @return 等级要求
     */
    public int getLevelRequirement() {
        return levelRequirement;
    }

    /**
     * 设置等级要求
     * @param levelRequirement 等级要求
     */
    public void setLevelRequirement(int levelRequirement) {
        this.levelRequirement = levelRequirement;
    }

    /**
     * 获取所有怪物刷怪点
     * @return 怪物刷怪点映射
     */
    public Map<String, MobSpawner> getMobSpawners() {
        return mobSpawners;
    }

    /**
     * 添加怪物刷怪点
     * @param id 刷怪点ID
     * @param mobType 怪物类型
     * @param location 位置字符串
     * @param cooldown 冷却时间
     * @param amount 生成数量
     */
    public void addMobSpawner(String id, String mobType, String location, int cooldown, int amount) {
        MobSpawner spawner = new MobSpawner(id, mobType, location);
        spawner.setCooldown(cooldown);
        spawner.setAmount(amount);
        mobSpawners.put(id, spawner);
    }

    /**
     * 移除怪物刷怪点
     * @param id 刷怪点ID
     * @return 是否成功移除
     */
    public boolean removeMobSpawner(String id) {
        return mobSpawners.remove(id) != null;
    }

    /**
     * 检查是否有金币花费
     * @return 是否有金币花费
     */
    public boolean hasMoneyCost() {
        return moneyCost > 0;
    }

    /**
     * 检查是否有点券花费
     * @return 是否有点券花费
     */
    public boolean hasPointsCost() {
        return pointsCost > 0;
    }

    /**
     * 检查是否有等级要求
     * @return 是否有等级要求
     */
    public boolean hasLevelRequirement() {
        return levelRequirement > 0;
    }

    /**
     * 获取所有条件
     * @return 条件列表
     */
    public List<DungeonRequirement> getRequirements() {
        return requirements;
    }

    /**
     * 添加条件
     * @param requirement 条件
     */
    public void addRequirement(DungeonRequirement requirement) {
        requirements.add(requirement);
    }

    /**
     * 获取死亡次数限制
     * @return 死亡次数限制
     */
    public int getDeathLimit() {
        return deathLimit;
    }

    /**
     * 设置死亡次数限制
     * @param deathLimit 死亡次数限制
     */
    public void setDeathLimit(int deathLimit) {
        this.deathLimit = deathLimit;
    }

    /**
     * 检查是否有死亡次数限制
     * @return 是否有死亡次数限制
     */
    public boolean hasDeathLimit() {
        return deathLimit > 0;
    }

    /**
     * 获取复活道具材质
     * @return 复活道具材质
     */
    public String getReviveItemMaterial() {
        return reviveItemMaterial;
    }

    /**
     * 设置复活道具材质
     * @param reviveItemMaterial 复活道具材质
     */
    public void setReviveItemMaterial(String reviveItemMaterial) {
        this.reviveItemMaterial = reviveItemMaterial;
    }

    /**
     * 获取复活道具名称
     * @return 复活道具名称
     */
    public String getReviveItemName() {
        return reviveItemName;
    }

    /**
     * 设置复活道具名称
     * @param reviveItemName 复活道具名称
     */
    public void setReviveItemName(String reviveItemName) {
        this.reviveItemName = reviveItemName;
    }

    /**
     * 检查是否有复活道具
     * @return 是否有复活道具
     */
    public boolean hasReviveItem() {
        return reviveItemMaterial != null && !reviveItemMaterial.isEmpty();
    }
}
