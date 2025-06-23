package cn.i7mc.sagadungeons.command.player;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import cn.i7mc.sagadungeons.model.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 公开命令
 * 用于切换副本的公开/私有状态
 */
public class PublicCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public PublicCommand(SagaDungeons plugin) {
        super(plugin, "public", "sagadungeons.command.public", true);
        addAlias("p");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        
        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());
        
        // 检查玩家是否在副本中
        if (!playerData.isInDungeon()) {
            sendMessage(sender, "command.public.not-in-dungeon");
            return;
        }
        
        // 获取副本ID
        String dungeonId = playerData.getCurrentDungeonId();
        
        // 获取副本实例
        DungeonInstance dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) {
            sendMessage(sender, "command.public.dungeon-not-found");
            return;
        }
        
        // 检查是否为副本创建者
        if (!dungeon.getOwnerUUID().equals(player.getUniqueId()) && !player.hasPermission("sagadungeons.admin")) {
            sendMessage(sender, "command.public.not-owner");
            return;
        }
        
        // 切换公开/私有状态
        boolean isPublic = !dungeon.isPublic();
        dungeon.setPublic(isPublic);
        
        // 发送消息
        if (isPublic) {
            sendMessage(sender, "command.public.set-public");
        } else {
            sendMessage(sender, "command.public.set-private");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
