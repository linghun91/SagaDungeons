package cn.i7mc.sagadungeons.command.player;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import cn.i7mc.sagadungeons.model.PlayerData;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 统计命令
 * 用于查看玩家的副本统计数据
 */
public class StatsCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public StatsCommand(SagaDungeons plugin) {
        super(plugin, "stats", "sagadungeons.command.stats", true);
        addAlias("s");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        
        // 获取目标玩家
        Player target = player;
        if (args.length > 0 && sender.hasPermission("sagadungeons.command.stats.others")) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sendMessage(sender, "command.stats.player-not-found", 
                        MessageUtil.createPlaceholders("player", args[0]));
                return;
            }
        }
        
        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(target.getUniqueId());
        
        // 发送统计标题
        sendMessage(sender, "command.stats.header", 
                MessageUtil.createPlaceholders("player", target.getName()));
        
        // 发送总体统计
        sendMessage(sender, "command.stats.total", 
                MessageUtil.createPlaceholders("created", String.valueOf(playerData.getTotalCreated()), 
                        "joined", String.valueOf(playerData.getTotalJoined()), 
                        "completed", String.valueOf(playerData.getTotalCompleted())));
        
        // 发送副本完成统计
        Map<String, Integer> completedDungeons = playerData.getCompletedDungeons();
        if (!completedDungeons.isEmpty()) {
            sendMessage(sender, "command.stats.completed.header");
            
            for (Map.Entry<String, Integer> entry : completedDungeons.entrySet()) {
                String templateName = entry.getKey();
                int count = entry.getValue();
                
                // 获取副本模板
                String displayName = templateName;
                if (plugin.getConfigManager().getTemplateManager().hasTemplate(templateName)) {
                    displayName = plugin.getConfigManager().getTemplateManager().getTemplate(templateName).getDisplayName();
                }
                
                sendMessage(sender, "command.stats.completed.entry", 
                        MessageUtil.createPlaceholders("dungeon", displayName, 
                                "count", String.valueOf(count)));
            }
        } else {
            sendMessage(sender, "command.stats.completed.none");
        }
        
        // 发送统计页脚
        sendMessage(sender, "command.stats.footer");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // 补全玩家名称
        if (args.length == 1 && sender.hasPermission("sagadungeons.command.stats.others")) {
            String arg = args[0].toLowerCase();
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(arg)) {
                    completions.add(player.getName());
                }
            }
        }
        
        return completions;
    }
}
