package cn.i7mc.sagadungeons.command.player;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import cn.i7mc.sagadungeons.model.PlayerData;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 加入命令
 * 用于加入副本
 */
public class JoinCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public JoinCommand(SagaDungeons plugin) {
        super(plugin, "join", "sagadungeons.command.join", true);
        addAlias("j");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        
        // 检查参数
        if (args.length < 1) {
            sendMessage(sender, "command.join.usage");
            return;
        }
        
        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());
        
        // 检查玩家是否已经在副本中
        if (playerData.isInDungeon()) {
            sendMessage(sender, "command.join.already-in-dungeon");
            return;
        }
        
        // 获取副本ID或创建者名称
        String idOrName = args[0];
        
        // 尝试直接通过ID查找副本
        DungeonInstance dungeon = plugin.getDungeonManager().getDungeon(idOrName);
        
        // 如果没有找到，尝试通过创建者名称查找
        if (dungeon == null) {
            // 查找玩家
            OfflinePlayer targetPlayer = null;
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(idOrName)) {
                    targetPlayer = offlinePlayer;
                    break;
                }
            }
            
            // 如果找到玩家，查找他创建的副本
            if (targetPlayer != null) {
                UUID targetUUID = targetPlayer.getUniqueId();
                
                // 遍历所有副本
                for (DungeonInstance instance : plugin.getDungeonManager().getActiveDungeons().values()) {
                    if (instance.getOwnerUUID().equals(targetUUID)) {
                        dungeon = instance;
                        break;
                    }
                }
            }
        }
        
        // 检查是否找到副本
        if (dungeon == null) {
            sendMessage(sender, "command.join.dungeon-not-found", 
                    MessageUtil.createPlaceholders("id", idOrName));
            return;
        }
        
        // 检查是否有权限加入
        if (!dungeon.isPublic() && !dungeon.isAllowed(player.getUniqueId()) && 
                !dungeon.getOwnerUUID().equals(player.getUniqueId()) && 
                !player.hasPermission("sagadungeons.admin")) {
            sendMessage(sender, "command.join.not-allowed");
            return;
        }
        
        // 加入副本
        boolean success = plugin.getDungeonManager().joinDungeon(player, dungeon.getId());
        
        // 发送消息
        if (success) {
            sendMessage(sender, "command.join.success", 
                    MessageUtil.createPlaceholders("dungeon", dungeon.getDisplayName()));
        } else {
            sendMessage(sender, "command.join.fail");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // 补全副本ID或创建者名称
        if (args.length == 1) {
            String arg = args[0].toLowerCase();
            
            // 添加副本ID
            for (String dungeonId : plugin.getDungeonManager().getActiveDungeons().keySet()) {
                if (dungeonId.toLowerCase().startsWith(arg)) {
                    completions.add(dungeonId);
                }
            }
            
            // 添加创建者名称
            for (Map.Entry<String, DungeonInstance> entry : plugin.getDungeonManager().getActiveDungeons().entrySet()) {
                UUID ownerUUID = entry.getValue().getOwnerUUID();
                String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
                
                if (ownerName != null && ownerName.toLowerCase().startsWith(arg) && 
                        !completions.contains(ownerName)) {
                    completions.add(ownerName);
                }
            }
        }
        
        return completions;
    }
}
