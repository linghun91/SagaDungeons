package cn.i7mc.sagadungeons.dungeon.reward;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * 时间奖励
 * 根据完成时间给予不同的奖励
 */
public class TimeReward implements DungeonReward {

    private final SagaDungeons plugin;
    private final List<String> commands;
    private final String description;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param commands 奖励命令列表
     * @param description 奖励描述
     */
    public TimeReward(SagaDungeons plugin, List<String> commands, String description) {
        this.plugin = plugin;
        this.commands = commands;
        this.description = description;
    }

    @Override
    public boolean give(Player player) {
        if (commands == null || commands.isEmpty()) {
            return false;
        }

        boolean success = true;
        for (String command : commands) {
            // 替换变量
            String processedCommand = command.replace("%player%", player.getName())
                    .replace("%uuid%", player.getUniqueId().toString());
            
            // 执行命令
            try {
                boolean result = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                if (!result) {
                    success = false;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("执行时间奖励命令失败: " + processedCommand + " - " + e.getMessage());
                success = false;
            }
        }
        
        return success;
    }

    @Override
    public String getDescription() {
        return description != null ? description : "时间奖励";
    }

    @Override
    public RewardType getType() {
        return RewardType.COMMAND;
    }

    /**
     * 获取奖励命令列表
     * @return 命令列表
     */
    public List<String> getCommands() {
        return commands;
    }
}
