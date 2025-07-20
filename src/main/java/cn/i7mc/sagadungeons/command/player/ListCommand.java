package cn.i7mc.sagadungeons.command.player;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import cn.i7mc.sagadungeons.util.MessageUtil;
import cn.i7mc.sagadungeons.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 列表命令
 * 用于列出所有副本
 */
public class ListCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public ListCommand(SagaDungeons plugin) {
        super(plugin, "list", "sagadungeons.command.list", true);
        addAlias("l");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 获取所有副本
        Map<String, DungeonInstance> dungeons = plugin.getDungeonManager().getActiveDungeons();
        
        // 检查是否有副本
        if (dungeons.isEmpty()) {
            sendMessage(sender, "command.list.no-dungeons");
            return;
        }
        
        // 发送标题
        sendMessage(sender, "command.list.header");
        
        // 发送副本列表
        for (DungeonInstance dungeon : dungeons.values()) {
            // 获取创建者名称
            String ownerName = Bukkit.getOfflinePlayer(dungeon.getOwnerUUID()).getName();
            if (ownerName == null) {
                ownerName = dungeon.getOwnerUUID().toString();
            }
            
            // 获取玩家数量
            int playerCount = dungeon.getPlayerCount();
            
            // 获取剩余时间
            String remainingTime = TimeUtil.formatTimeShort(dungeon.getRemainingTime());
            
            // 获取公开状态
            String publicStatus = dungeon.isPublic() ? 
                    plugin.getConfigManager().getMessageManager().getMessage("general.public") : 
                    plugin.getConfigManager().getMessageManager().getMessage("general.private");
            
            // 发送副本信息
            sendMessage(sender, "command.list.entry", 
                    MessageUtil.createPlaceholders("id", dungeon.getId(), 
                            "template", dungeon.getTemplateName(), 
                            "owner", ownerName, 
                            "players", String.valueOf(playerCount), 
                            "time", remainingTime, 
                            "status", publicStatus));
        }
        
        // 发送页脚
        sendMessage(sender, "command.list.footer");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
