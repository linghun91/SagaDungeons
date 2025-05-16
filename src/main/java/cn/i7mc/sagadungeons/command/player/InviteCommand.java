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
 * 邀请命令
 * 用于邀请玩家加入副本
 */
public class InviteCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public InviteCommand(SagaDungeons plugin) {
        super(plugin, "invite", "sagadungeons.command.invite", true);
        addAlias("i");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        
        // 检查参数
        if (args.length < 1) {
            sendMessage(sender, "command.invite.usage");
            return;
        }
        
        // 获取目标玩家
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sendMessage(sender, "command.invite.player-not-found", 
                    MessageUtil.createPlaceholders("player", args[0]));
            return;
        }
        
        // 检查是否邀请自己
        if (target.equals(player)) {
            sendMessage(sender, "command.invite.cannot-invite-self");
            return;
        }
        
        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());
        
        // 检查玩家是否在副本中
        if (!playerData.isInDungeon()) {
            sendMessage(sender, "command.invite.not-in-dungeon");
            return;
        }
        
        // 获取副本ID
        String dungeonId = playerData.getCurrentDungeonId();
        
        // 获取副本实例
        DungeonInstance dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) {
            sendMessage(sender, "command.invite.dungeon-not-found");
            return;
        }
        
        // 检查是否为副本创建者
        if (!dungeon.getOwnerUUID().equals(player.getUniqueId())) {
            sendMessage(sender, "command.invite.not-owner");
            return;
        }
        
        // 检查目标玩家是否已经在副本中
        PlayerData targetData = plugin.getDungeonManager().getPlayerData(target.getUniqueId());
        if (targetData.isInDungeon()) {
            sendMessage(sender, "command.invite.target-in-dungeon", 
                    MessageUtil.createPlaceholders("player", target.getName()));
            return;
        }
        
        // 添加到允许列表
        dungeon.addAllowedPlayer(target.getUniqueId());
        
        // 发送邀请消息
        sendMessage(sender, "command.invite.success", 
                MessageUtil.createPlaceholders("player", target.getName()));
        
        // 发送邀请消息给目标玩家
        MessageUtil.sendMessage(target, "command.invite.received", 
                MessageUtil.createPlaceholders("player", player.getName(), 
                        "dungeon", dungeon.getDisplayName(), 
                        "id", dungeonId));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // 补全玩家名称
        if (args.length == 1) {
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
