package cn.i7mc.sagadungeons.command.admin;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import cn.i7mc.sagadungeons.command.CommandManager;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理员命令
 * 用于管理副本系统
 */
public class AdminCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public AdminCommand(SagaDungeons plugin) {
        super(plugin, "admin", "sagadungeons.admin", false);
        addAlias("a");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 检查参数
        if (args.length < 1) {
            showAdminHelp(sender);
            return;
        }
        
        // 获取子命令
        String subCommand = args[0].toLowerCase();
        
        // 处理子命令
        switch (subCommand) {
            case "reload":
                // 重新加载配置
                plugin.getConfigManager().reload();
                sendMessage(sender, "command.admin.reload.success");
                break;
            case "delete":
                // 删除副本
                if (args.length < 2) {
                    sendMessage(sender, "command.admin.delete.usage");
                    return;
                }
                
                // 获取副本ID
                String dungeonId = args[1];
                
                // 删除副本
                boolean success = plugin.getDungeonManager().deleteDungeon(dungeonId);
                
                // 发送消息
                if (success) {
                    sendMessage(sender, "command.admin.delete.success", 
                            MessageUtil.createPlaceholders("id", dungeonId));
                } else {
                    sendMessage(sender, "command.admin.delete.fail", 
                            MessageUtil.createPlaceholders("id", dungeonId));
                }
                break;
            case "tp":
                // 传送到副本
                if (args.length < 2) {
                    sendMessage(sender, "command.admin.tp.usage");
                    return;
                }
                
                // 检查是否为玩家
                if (!isPlayer(sender)) {
                    sendMessage(sender, "general.player-only");
                    return;
                }
                
                // 获取副本ID
                String tpDungeonId = args[1];
                
                // 传送到副本
                boolean tpSuccess = plugin.getDungeonManager().joinDungeon(getPlayer(sender), tpDungeonId);
                
                // 发送消息
                if (tpSuccess) {
                    sendMessage(sender, "command.admin.tp.success", 
                            MessageUtil.createPlaceholders("id", tpDungeonId));
                } else {
                    sendMessage(sender, "command.admin.tp.fail", 
                            MessageUtil.createPlaceholders("id", tpDungeonId));
                }
                break;
            case "list":
                // 列出所有副本
                plugin.getServer().dispatchCommand(sender, "sd list");
                break;
            case "help":
                // 显示帮助
                showAdminHelp(sender);
                break;
            default:
                // 未知命令
                sendMessage(sender, "command.admin.unknown", 
                        MessageUtil.createPlaceholders("command", subCommand));
                break;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // 补全子命令
        if (args.length == 1) {
            String arg = args[0].toLowerCase();
            
            List<String> subCommands = new ArrayList<>();
            subCommands.add("reload");
            subCommands.add("delete");
            subCommands.add("tp");
            subCommands.add("list");
            subCommands.add("help");
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(arg)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            // 补全副本ID
            if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("tp")) {
                String arg = args[1].toLowerCase();
                
                for (String dungeonId : plugin.getDungeonManager().getActiveDungeons().keySet()) {
                    if (dungeonId.toLowerCase().startsWith(arg)) {
                        completions.add(dungeonId);
                    }
                }
            }
        }
        
        return completions;
    }
    
    /**
     * 显示管理员帮助
     * @param sender 命令发送者
     */
    private void showAdminHelp(CommandSender sender) {
        // 发送帮助标题
        MessageUtil.sendMessage(sender, "command.admin.help.header");
        
        // 发送管理员命令帮助
        MessageUtil.sendMessage(sender, "command.admin.help.reload");
        MessageUtil.sendMessage(sender, "command.admin.help.delete");
        MessageUtil.sendMessage(sender, "command.admin.help.tp");
        MessageUtil.sendMessage(sender, "command.admin.help.list");
        
        // 发送帮助页脚
        MessageUtil.sendMessage(sender, "command.admin.help.footer");
    }
}
