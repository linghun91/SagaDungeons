package cn.i7mc.sagadungeons.command;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.admin.AdminCommand;
import cn.i7mc.sagadungeons.command.admin.CopyWorldCommand;
import cn.i7mc.sagadungeons.command.admin.CreateTemplateCommand;
import cn.i7mc.sagadungeons.command.admin.ForceCloseCommand;
import cn.i7mc.sagadungeons.command.admin.GUICommand;
import cn.i7mc.sagadungeons.command.admin.SetItemCommand;
import cn.i7mc.sagadungeons.command.admin.SetSpawnCommand;
import cn.i7mc.sagadungeons.command.admin.SetWorldCommand;
import cn.i7mc.sagadungeons.command.admin.SpawnerAdminCommand;
import cn.i7mc.sagadungeons.command.player.CreateCommand;
import cn.i7mc.sagadungeons.command.player.InviteCommand;
import cn.i7mc.sagadungeons.command.player.JoinCommand;
import cn.i7mc.sagadungeons.command.player.KickCommand;
import cn.i7mc.sagadungeons.command.player.LeaveCommand;
import cn.i7mc.sagadungeons.command.player.ListCommand;
import cn.i7mc.sagadungeons.command.player.PublicCommand;
import cn.i7mc.sagadungeons.command.player.StatsCommand;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 命令管理器
 * 负责管理插件的所有命令
 */
public class CommandManager implements CommandExecutor, TabCompleter {

    private final SagaDungeons plugin;
    private final Map<String, AbstractCommand> commands = new HashMap<>();

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public CommandManager(SagaDungeons plugin) {
        this.plugin = plugin;
    }

    /**
     * 注册命令
     */
    public void registerCommands() {
        // 注册主命令
        plugin.getCommand("sagadungeons").setExecutor(this);
        plugin.getCommand("sagadungeons").setTabCompleter(this);

        // 注册玩家子命令
        registerSubCommand(new CreateCommand(plugin));
        registerSubCommand(new ListCommand(plugin));
        registerSubCommand(new StatsCommand(plugin));
        registerSubCommand(new InviteCommand(plugin));
        registerSubCommand(new JoinCommand(plugin));
        registerSubCommand(new LeaveCommand(plugin));
        registerSubCommand(new KickCommand(plugin));
        registerSubCommand(new PublicCommand(plugin));

        // 注册管理员子命令
        registerSubCommand(new AdminCommand(plugin));
        registerSubCommand(new CreateTemplateCommand(plugin));
        registerSubCommand(new SetWorldCommand(plugin));
        registerSubCommand(new CopyWorldCommand(plugin));
        registerSubCommand(new SetItemCommand(plugin));
        registerSubCommand(new ForceCloseCommand(plugin));
        registerSubCommand(new SetSpawnCommand(plugin));
        registerSubCommand(new GUICommand(plugin));
        registerSubCommand(new SpawnerAdminCommand(plugin));
    }

    /**
     * 注册子命令
     * @param command 子命令
     */
    public void registerSubCommand(AbstractCommand command) {
        commands.put(command.getName().toLowerCase(), command);

        // 注册别名
        for (String alias : command.getAliases()) {
            commands.put(alias.toLowerCase(), command);
        }
    }

    /**
     * 处理命令
     * @param sender 命令发送者
     * @param command 命令
     * @param label 标签
     * @param args 参数
     * @return 是否成功
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查是否有参数
        if (args.length == 0) {
            // 显示帮助信息
            showHelp(sender);
            return true;
        }

        // 获取子命令
        String subCommand = args[0].toLowerCase();

        // 检查是否为帮助命令
        if (subCommand.equals("help")) {
            showHelp(sender);
            return true;
        }

        // 检查是否为重载命令
        if (subCommand.equals("reload")) {
            // 检查权限
            if (!sender.hasPermission("sagadungeons.admin")) {
                MessageUtil.sendMessage(sender, "general.no-permission");
                return true;
            }

            // 重载配置
            plugin.getConfigManager().reloadConfigs();

            // 发送消息
            MessageUtil.sendMessage(sender, "general.reload-success");
            return true;
        }

        // 检查是否为管理员命令
        if (subCommand.equals("admin")) {
            // 检查权限
            if (!sender.hasPermission("sagadungeons.admin")) {
                MessageUtil.sendMessage(sender, "general.no-permission");
                return true;
            }

            // 检查是否有参数
            if (args.length < 2) {
                showAdminHelp(sender);
                return true;
            }

            // 处理管理员子命令
            String[] adminArgs = new String[args.length - 1];
            System.arraycopy(args, 1, adminArgs, 0, args.length - 1);

            // 执行管理员命令
            AbstractCommand adminCommand = commands.get("admin");
            if (adminCommand != null) {
                adminCommand.execute(sender, adminArgs);
            }

            return true;
        }

        // 获取子命令处理器
        AbstractCommand subCommandHandler = commands.get(subCommand);

        // 检查子命令是否存在
        if (subCommandHandler == null) {
            MessageUtil.sendMessage(sender, "general.unknown-command");
            return true;
        }

        // 检查是否为玩家命令
        if (subCommandHandler.isPlayerOnly() && !(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "general.player-only");
            return true;
        }

        // 检查权限
        if (!subCommandHandler.hasPermission(sender)) {
            MessageUtil.sendMessage(sender, "general.no-permission");
            return true;
        }

        // 创建参数数组
        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, args.length - 1);

        // 执行子命令
        subCommandHandler.execute(sender, subArgs);

        return true;
    }

    /**
     * 处理Tab补全
     * @param sender 命令发送者
     * @param command 命令
     * @param alias 别名
     * @param args 参数
     * @return 补全列表
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // 检查参数长度
        if (args.length == 1) {
            // 补全子命令
            String arg = args[0].toLowerCase();

            // 添加帮助命令
            if ("help".startsWith(arg)) {
                completions.add("help");
            }

            // 添加重载命令 - 移至管理员命令
            if (sender.hasPermission("sagadungeons.admin") && "admin".startsWith(arg)) {
                completions.add("admin");
            }

            // 添加子命令
            for (AbstractCommand subCommand : commands.values()) {
                // 排除createtemplate等管理员直接命令，保留在admin子命令中
                if (!isAdminOnlyCommand(subCommand.getName()) &&
                    subCommand.getName().toLowerCase().startsWith(arg) &&
                    subCommand.hasPermission(sender)) {
                    completions.add(subCommand.getName());
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            // 补全管理员子命令
            String arg = args[1].toLowerCase();

            // 检查权限
            if (sender.hasPermission("sagadungeons.admin")) {
                // 添加管理员子命令
                List<String> adminSubCommands = new ArrayList<>();
                adminSubCommands.add("reload");
                adminSubCommands.add("delete");
                adminSubCommands.add("tp");
                adminSubCommands.add("list");
                adminSubCommands.add("createtemplate");
                adminSubCommands.add("setworld");
                adminSubCommands.add("copyworld");
                adminSubCommands.add("setitem");
                adminSubCommands.add("forceclose");
                adminSubCommands.add("setspawn");
                adminSubCommands.add("gui");
                adminSubCommands.add("help");

                for (String subCommand : adminSubCommands) {
                    if (subCommand.startsWith(arg)) {
                        completions.add(subCommand);
                    }
                }
            }
        } else if (args.length >= 2) {
            // 获取子命令
            String subCommand = args[0].toLowerCase();

            // 获取子命令处理器
            AbstractCommand subCommandHandler = commands.get(subCommand);

            // 检查子命令是否存在
            if (subCommandHandler != null && subCommandHandler.hasPermission(sender)) {
                // 创建参数数组
                String[] subArgs = new String[args.length - 1];
                System.arraycopy(args, 1, subArgs, 0, args.length - 1);

                // 获取子命令的Tab补全
                List<String> subCompletions = subCommandHandler.tabComplete(sender, subArgs);
                if (subCompletions != null) {
                    completions.addAll(subCompletions);
                }
            }
        }

        return completions;
    }

    /**
     * 判断是否为仅管理员使用的命令
     * 这些命令应该通过/sd admin来访问
     * @param commandName 命令名称
     * @return 是否为仅管理员使用的命令
     */
    private boolean isAdminOnlyCommand(String commandName) {
        // 这些命令仅应通过/sd admin子命令访问
        return commandName.equalsIgnoreCase("createtemplate") ||
               commandName.equalsIgnoreCase("setworld") ||
               commandName.equalsIgnoreCase("copyworld") ||
               commandName.equalsIgnoreCase("setitem") ||
               commandName.equalsIgnoreCase("forceclose") ||
               commandName.equalsIgnoreCase("setspawn") ||
               commandName.equalsIgnoreCase("gui") ||
               commandName.equalsIgnoreCase("spawner");
    }

    /**
     * 显示帮助信息
     * @param sender 命令发送者
     */
    private void showHelp(CommandSender sender) {
        // 发送帮助标题
        MessageUtil.sendMessage(sender, "command.help.header");

        // 发送玩家命令帮助
        MessageUtil.sendMessage(sender, "command.help.create");
        MessageUtil.sendMessage(sender, "command.help.list");
        MessageUtil.sendMessage(sender, "command.help.stats");
        MessageUtil.sendMessage(sender, "command.help.invite");
        MessageUtil.sendMessage(sender, "command.help.join");
        MessageUtil.sendMessage(sender, "command.help.leave");
        MessageUtil.sendMessage(sender, "command.help.kick");
        MessageUtil.sendMessage(sender, "command.help.public");
        MessageUtil.sendMessage(sender, "command.help.help");

        // 如果有管理员权限，提示使用管理员命令
        if (sender.hasPermission("sagadungeons.admin")) {
            MessageUtil.sendMessage(sender, "command.help.admin");
        }

        // 发送帮助页脚
        MessageUtil.sendMessage(sender, "command.help.footer");
    }

    /**
     * 显示管理员帮助信息
     * @param sender 命令发送者
     */
    private void showAdminHelp(CommandSender sender) {
        // 发送管理员帮助标题
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
        MessageUtil.sendMessage(sender, "command.admin.help.help");

        // 发送帮助页脚
        MessageUtil.sendMessage(sender, "command.admin.help.footer");
    }
}