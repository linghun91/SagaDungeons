package cn.i7mc.sagadungeons.command.player;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import cn.i7mc.sagadungeons.model.PlayerData;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 踢出命令
 * 用于踢出副本中的玩家
 */
public class KickCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public KickCommand(SagaDungeons plugin) {
        super(plugin, "kick", "sagadungeons.command.kick", true);
        addAlias("k");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        
        // 检查参数
        if (args.length < 1) {
            sendMessage(sender, "command.kick.usage");
            return;
        }
        
        // 获取目标玩家
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sendMessage(sender, "command.kick.player-not-found", 
                    MessageUtil.createPlaceholders("player", args[0]));
            return;
        }
        
        // 检查是否踢出自己
        if (target.equals(player)) {
            sendMessage(sender, "command.kick.cannot-kick-self");
            return;
        }
        
        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());
        
        // 检查玩家是否在副本中
        if (!playerData.isInDungeon()) {
            sendMessage(sender, "command.kick.not-in-dungeon");
            return;
        }
        
        // 获取副本ID
        String dungeonId = playerData.getCurrentDungeonId();
        
        // 获取副本实例
        DungeonInstance dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) {
            sendMessage(sender, "command.kick.dungeon-not-found");
            return;
        }
        
        // 检查是否为副本创建者
        if (!dungeon.getOwnerUUID().equals(player.getUniqueId()) && !player.hasPermission("sagadungeons.admin")) {
            sendMessage(sender, "command.kick.not-owner");
            return;
        }
        
        // 获取目标玩家数据
        PlayerData targetData = plugin.getDungeonManager().getPlayerData(target.getUniqueId());
        
        // 检查目标玩家是否在同一个副本中
        if (!targetData.isInDungeon() || !targetData.getCurrentDungeonId().equals(dungeonId)) {
            sendMessage(sender, "command.kick.target-not-in-dungeon", 
                    MessageUtil.createPlaceholders("player", target.getName()));
            return;
        }
        
        // 踢出玩家
        plugin.getDungeonManager().leaveDungeon(target);
        
        // 从允许列表中移除
        dungeon.removeAllowedPlayer(target.getUniqueId());
        
        // 发送踢出消息
        sendMessage(sender, "command.kick.success", 
                MessageUtil.createPlaceholders("player", target.getName()));
        
        // 发送踢出消息给目标玩家
        MessageUtil.sendMessage(target, "command.kick.kicked", 
                MessageUtil.createPlaceholders("player", player.getName()));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // 补全玩家名称
        if (args.length == 1) {
            String arg = args[0].toLowerCase();
            
            // 获取玩家数据
            Player player = getPlayer(sender);
            if (player == null) {
                return completions;
            }
            
            PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());
            
            // 检查玩家是否在副本中
            if (!playerData.isInDungeon()) {
                return completions;
            }
            
            // 获取副本ID
            String dungeonId = playerData.getCurrentDungeonId();
            
            // 获取副本实例
            DungeonInstance dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
            if (dungeon == null) {
                return completions;
            }
            
            // 获取副本中的玩家
            for (Player p : dungeon.getWorld().getPlayers()) {
                if (p.getName().toLowerCase().startsWith(arg) && !p.equals(player)) {
                    completions.add(p.getName());
                }
            }
        }
        
        return completions;
    }
}
