package cn.i7mc.sagadungeons.command.admin;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 强制关闭副本命令
 * 用于管理员强制关闭指定副本
 */
public class ForceCloseCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public ForceCloseCommand(SagaDungeons plugin) {
        super(plugin, "forceclose", "sagadungeons.admin", false);
        addAlias("close");
        addAlias("fc");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 检查参数
        if (args.length < 1) {
            sendMessage(sender, "command.admin.forceclose.usage");
            return;
        }

        // 获取副本ID
        String dungeonId = args[0];

        // 检查是否为"all"
        if (dungeonId.equalsIgnoreCase("all")) {
            // 获取所有活动副本
            Map<String, DungeonInstance> activeDungeons = plugin.getDungeonManager().getActiveDungeons();

            // 检查是否有活动副本
            if (activeDungeons.isEmpty()) {
                sendMessage(sender, "command.admin.forceclose.no-dungeons");
                return;
            }

            // 记录成功关闭的副本数量
            int closedCount = 0;

            // 复制副本ID列表，避免并发修改异常
            List<String> dungeonIds = new ArrayList<>(activeDungeons.keySet());

            // 关闭所有副本
            for (String id : dungeonIds) {
                if (plugin.getDungeonManager().deleteDungeon(id)) {
                    closedCount++;
                }
            }

            // 发送消息
            sendMessage(sender, "command.admin.forceclose.all-success",
                    MessageUtil.createPlaceholders("count", String.valueOf(closedCount)));
            return;
        }

        // 删除指定副本
        boolean success = plugin.getDungeonManager().deleteDungeon(dungeonId);

        // 发送消息
        if (success) {
            sendMessage(sender, "command.admin.forceclose.success",
                    MessageUtil.createPlaceholders("id", dungeonId));
        } else {
            sendMessage(sender, "command.admin.forceclose.fail",
                    MessageUtil.createPlaceholders("id", dungeonId));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        // 只补全第一个参数
        if (args.length == 1) {
            String arg = args[0].toLowerCase();

            // 添加"all"选项
            if ("all".startsWith(arg)) {
                completions.add("all");
            }

            // 添加所有活动副本ID
            for (String dungeonId : plugin.getDungeonManager().getActiveDungeons().keySet()) {
                if (dungeonId.toLowerCase().startsWith(arg)) {
                    completions.add(dungeonId);
                }
            }
        }

        return completions;
    }
}
