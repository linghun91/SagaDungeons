package cn.i7mc.sagadungeons.dungeon.reward;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 命令奖励
 * 执行命令
 */
public class CommandReward implements DungeonReward {

    private final SagaDungeons plugin;
    private final String command;
    private final String description;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param command 命令
     * @param description 描述
     */
    public CommandReward(SagaDungeons plugin, String command, String description) {
        this.plugin = plugin;
        this.command = command;
        this.description = description;
    }

    @Override
    public boolean give(Player player) {
        // 替换变量
        String processedCommand = command.replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueId().toString());
        
        // 执行命令
        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
    }

    @Override
    public String getDescription() {
        if (description != null && !description.isEmpty()) {
            return description;
        }
        
        return plugin.getConfigManager().getMessageManager().getMessage("dungeon.reward.command.description");
    }

    @Override
    public RewardType getType() {
        return RewardType.COMMAND;
    }
}
