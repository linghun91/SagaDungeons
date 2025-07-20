package cn.i7mc.sagadungeons.command.admin;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import cn.i7mc.sagadungeons.util.LocationUtil;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * 设置遣返点命令
 * 用于设置非法玩家的遣返位置
 */
public class SetBackCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public SetBackCommand(SagaDungeons plugin) {
        super(plugin, "setback", "sagadungeons.admin", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 检查是否为玩家
        if (!isPlayer(sender)) {
            sendMessage(sender, "general.player-only");
            return;
        }

        Player player = getPlayer(sender);
        Location location = player.getLocation();

        // 将位置序列化为字符串
        String locationString = LocationUtil.locationToString(location);
        
        if (locationString == null) {
            sendMessage(sender, "command.admin.setback.fail");
            return;
        }

        // 保存到配置文件
        plugin.getConfig().set("security.custom-kickback-location", locationString);
        plugin.saveConfig();

        // 发送成功消息
        sendMessage(sender, "command.admin.setback.success",
                MessageUtil.createPlaceholders(
                        "world", location.getWorld().getName(),
                        "x", String.format("%.1f", location.getX()),
                        "y", String.format("%.1f", location.getY()),
                        "z", String.format("%.1f", location.getZ())
                ));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return null; // 无需tab补全
    }
}
