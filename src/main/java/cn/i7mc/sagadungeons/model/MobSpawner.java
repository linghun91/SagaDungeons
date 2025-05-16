package cn.i7mc.sagadungeons.model;

/**
 * 怪物刷怪点数据模型
 * 存储怪物刷怪点的配置信息
 */
public class MobSpawner {

    private final String id;
    private final String mobType;
    private final String location;
    private int cooldown;
    private int amount;

    /**
     * 构造函数
     * @param id 刷怪点ID
     * @param mobType 怪物类型
     * @param location 位置字符串
     */
    public MobSpawner(String id, String mobType, String location) {
        this.id = id;
        this.mobType = mobType;
        this.location = location;
        this.cooldown = 30; // 默认30秒
        this.amount = 1; // 默认1个
    }

    /**
     * 获取刷怪点ID
     * @return 刷怪点ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取怪物类型
     * @return 怪物类型
     */
    public String getMobType() {
        return mobType;
    }

    /**
     * 获取位置字符串
     * @return 位置字符串
     */
    public String getLocation() {
        return location;
    }

    /**
     * 获取冷却时间
     * @return 冷却时间
     */
    public int getCooldown() {
        return cooldown;
    }

    /**
     * 设置冷却时间
     * @param cooldown 冷却时间
     */
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    /**
     * 获取生成数量
     * @return 生成数量
     */
    public int getAmount() {
        return amount;
    }

    /**
     * 设置生成数量
     * @param amount 生成数量
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }
}
