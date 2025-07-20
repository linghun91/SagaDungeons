package cn.i7mc.sagadungeons.command.player;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import cn.i7mc.sagadungeons.model.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 离开副本命令
 * 用于玩家离开当前副本
 */
public class LeaveCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public LeaveCommand(SagaDungeons plugin) {
        super(plugin, "leave", "sagadungeons.command.leave", true);
        addAlias("quit");
        addAlias("exit");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);

        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());

        // 检查玩家是否在副本中
        if (!playerData.isInDungeon()) {
            sendMessage(sender, "command.leave.not-in-dungeon");
            return;
        }

        // 离开副本
        boolean success = plugin.getDungeonManager().leaveDungeon(player);

        // 发送消息
        if (success) {
            sendMessage(sender, "command.leave.success");
        } else {
            sendMessage(sender, "command.leave.fail");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // 离开命令不需要参数补全
        return new ArrayList<>();
    }
}
