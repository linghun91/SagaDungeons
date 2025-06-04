package cn.i7mc.sagadungeons.dungeon.reward;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.entity.Player;

/**
 * 经验奖励
 * 给予玩家经验
 */
public class ExperienceReward implements DungeonReward {

    private final SagaDungeons plugin;
    private final int amount;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param amount 经验数量
     */
    public ExperienceReward(SagaDungeons plugin, int amount) {
        this.plugin = plugin;
        this.amount = amount;
    }

    @Override
    public boolean give(Player player) {
        // 给予经验
        player.giveExp(amount);
        return true;
    }

    @Override
    public String getDescription() {
        return plugin.getConfigManager().getMessageManager().getMessage("dungeon.reward.experience.description", 
                plugin.getConfigManager().getMessageManager().createPlaceholders("amount", String.valueOf(amount)));
    }

    @Override
    public RewardType getType() {
        return RewardType.EXPERIENCE;
    }
}
