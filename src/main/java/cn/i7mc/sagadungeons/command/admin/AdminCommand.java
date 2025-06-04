package cn.i7mc.sagadungeons.command.admin;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
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
            case "createtemplate":
                // 创建模板
                if (args.length < 2) {
                    sendMessage(sender, "command.admin.createtemplate.usage");
                    return;
                }

                // 创建参数数组
                String[] createTemplateArgs = new String[args.length - 1];
                System.arraycopy(args, 1, createTemplateArgs, 0, args.length - 1);

                // 执行命令
                new CreateTemplateCommand(plugin).execute(sender, createTemplateArgs);
                break;
            case "setworld":
                // 设置世界
                if (args.length < 2) {
                    sendMessage(sender, "command.admin.setworld.usage");
                    return;
                }

                // 创建参数数组
                String[] setWorldArgs = new String[args.length - 1];
                System.arraycopy(args, 1, setWorldArgs, 0, args.length - 1);

                // 执行命令
                new SetWorldCommand(plugin).execute(sender, setWorldArgs);
                break;
            case "copyworld":
                // 复制世界
                if (args.length < 3) {
                    sendMessage(sender, "command.admin.copyworld.usage");
                    return;
                }

                // 创建参数数组
                String[] copyWorldArgs = new String[args.length - 1];
                System.arraycopy(args, 1, copyWorldArgs, 0, args.length - 1);

                // 执行命令
                new CopyWorldCommand(plugin).execute(sender, copyWorldArgs);
                break;
            case "setitem":
                // 设置物品
                if (args.length < 3) {
                    sendMessage(sender, "command.admin.setitem.usage");
                    return;
                }

                // 创建参数数组
                String[] setItemArgs = new String[args.length - 1];
                System.arraycopy(args, 1, setItemArgs, 0, args.length - 1);

                // 执行命令
                new SetItemCommand(plugin).execute(sender, setItemArgs);
                break;
            case "forceclose":
                // 强制关闭副本
                if (args.length < 2) {
                    sendMessage(sender, "command.admin.forceclose.usage");
                    return;
                }

                // 创建参数数组
                String[] forceCloseArgs = new String[args.length - 1];
                System.arraycopy(args, 1, forceCloseArgs, 0, args.length - 1);

                // 执行命令
                new ForceCloseCommand(plugin).execute(sender, forceCloseArgs);
                break;
            case "setspawn":
                // 设置副本重生点
                // 执行命令
                new SetSpawnCommand(plugin).execute(sender, new String[0]);
                break;
            case "gui":
                // 打开GUI管理界面
                // 执行命令
                new GUICommand(plugin).execute(sender, new String[0]);
                break;
            case "spawner":
                // 管理刷怪点
                if (args.length < 2) {
                    sendMessage(sender, "command.admin.spawner.usage");
                    return;
                }

                // 创建参数数组
                String[] spawnerArgs = new String[args.length - 1];
                System.arraycopy(args, 1, spawnerArgs, 0, args.length - 1);

                // 执行命令
                new SpawnerAdminCommand(plugin).execute(sender, spawnerArgs);
                break;
            case "edit":
                // 打开模板编辑界面
                // 执行命令
                new EditCommand(plugin).execute(sender, new String[0]);
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
            subCommands.add("createtemplate");
            subCommands.add("setworld");
            subCommands.add("copyworld");
            subCommands.add("setitem");
            subCommands.add("forceclose");
            subCommands.add("setspawn");
            subCommands.add("gui");
            subCommands.add("spawner");
            subCommands.add("edit");
            subCommands.add("help");

            for (String subCommand : subCommands) {
                if (subCommand.startsWith(arg)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            // 补全副本ID或模板名称
            if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("forceclose")) {
                String arg = args[1].toLowerCase();

                // 如果是forceclose命令，添加"all"选项
                if (args[0].equalsIgnoreCase("forceclose") && "all".startsWith(arg)) {
                    completions.add("all");
                }

                for (String dungeonId : plugin.getDungeonManager().getActiveDungeons().keySet()) {
                    if (dungeonId.toLowerCase().startsWith(arg)) {
                        completions.add(dungeonId);
                    }
                }
            } else if (args[0].equalsIgnoreCase("setworld") || args[0].equalsIgnoreCase("copyworld") || args[0].equalsIgnoreCase("setitem")) {
                String arg = args[1].toLowerCase();

                for (String templateName : plugin.getConfigManager().getTemplateManager().getTemplates().keySet()) {
                    if (templateName.toLowerCase().startsWith(arg)) {
                        completions.add(templateName);
                    }
                }
            } else if (args[0].equalsIgnoreCase("spawner")) {
                String arg = args[1].toLowerCase();

                // 补全刷怪点子命令
                if ("set".startsWith(arg)) {
                    completions.add("set");
                }
                if ("remove".startsWith(arg)) {
                    completions.add("remove");
                }
                if ("list".startsWith(arg)) {
                    completions.add("list");
                }
            }
        } else if (args.length == 3) {
            // 补全世界名称或物品类型
            if (args[0].equalsIgnoreCase("copyworld")) {
                String arg = args[2].toLowerCase();

                for (org.bukkit.World world : plugin.getServer().getWorlds()) {
                    if (world.getName().toLowerCase().startsWith(arg)) {
                        completions.add(world.getName());
                    }
                }
            } else if (args[0].equalsIgnoreCase("setitem")) {
                String arg = args[2].toLowerCase();

                if ("condition".startsWith(arg)) {
                    completions.add("condition");
                }
                if ("reward".startsWith(arg)) {
                    completions.add("reward");
                }
                if ("revive".startsWith(arg)) {
                    completions.add("revive");
                }
            } else if (args[0].equalsIgnoreCase("spawner") && args[1].equalsIgnoreCase("set")) {
                String arg = args[2].toLowerCase();

                // 为set命令提供一些建议的刷怪点ID
                List<String> suggestedIds = new ArrayList<>();
                suggestedIds.add("mob1");
                suggestedIds.add("boss");
                suggestedIds.add("spawn1");

                // 过滤并添加到补全列表
                for (String id : suggestedIds) {
                    if (id.toLowerCase().startsWith(arg)) {
                        completions.add(id);
                    }
                }
            } else if (args[0].equalsIgnoreCase("spawner") && args[1].equalsIgnoreCase("remove")) {
                // 这里需要获取当前玩家所在副本的模板，然后列出所有刷怪点ID
                // 但由于这需要更复杂的逻辑，我们在SpawnerAdminCommand中处理
            }
        } else if (args.length == 4) {
            // 补全物品数量或怪物类型
            if (args[0].equalsIgnoreCase("setitem")) {
                completions.add("1");
                completions.add("5");
                completions.add("10");
            } else if (args[0].equalsIgnoreCase("spawner") && args[1].equalsIgnoreCase("set")) {
                String arg = args[3].toLowerCase();

                // 补全怪物类型
                if (plugin.getHookManager().isMythicMobsAvailable()) {
                    for (String mobType : plugin.getHookManager().getMythicMobsHook().getMobTypes()) {
                        if (mobType.toLowerCase().startsWith(arg)) {
                            completions.add(mobType);
                        }
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
        MessageUtil.sendMessage(sender, "command.admin.help.createtemplate");
        MessageUtil.sendMessage(sender, "command.admin.help.setworld");
        MessageUtil.sendMessage(sender, "command.admin.help.copyworld");
        MessageUtil.sendMessage(sender, "command.admin.help.setitem");
        MessageUtil.sendMessage(sender, "command.admin.help.forceclose");
        MessageUtil.sendMessage(sender, "command.admin.help.setspawn");
        MessageUtil.sendMessage(sender, "command.admin.help.gui");
        MessageUtil.sendMessage(sender, "command.admin.help.spawner");
        MessageUtil.sendMessage(sender, "command.admin.help.help");

        // 发送帮助页脚
        MessageUtil.sendMessage(sender, "command.admin.help.footer");
    }
}
