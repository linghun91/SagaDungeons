package cn.i7mc.sagadungeons.command;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.admin.AdminCommand;
import cn.i7mc.sagadungeons.command.player.CreateCommand;
import cn.i7mc.sagadungeons.command.player.InviteCommand;
import cn.i7mc.sagadungeons.command.player.JoinCommand;
import cn.i7mc.sagadungeons.command.player.KickCommand;
import cn.i7mc.sagadungeons.command.player.ListCommand;
import cn.i7mc.sagadungeons.command.player.PublicCommand;
import cn.i7mc.sagadungeons.command.player.SpawnerCommand;
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
        registerSubCommand(new KickCommand(plugin));
        registerSubCommand(new PublicCommand(plugin));
        registerSubCommand(new SpawnerCommand(plugin));

        // 注册管理员子命令
        registerSubCommand(new AdminCommand(plugin));
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

            // 获取管理员子命令
            String adminSubCommand = args[1].toLowerCase();

            // 处理管理员子命令
            // TODO: 处理管理员子命令

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

            // 添加重载命令
            if (sender.hasPermission("sagadungeons.admin") && "reload".startsWith(arg)) {
                completions.add("reload");
            }

            // 添加管理员命令
            if (sender.hasPermission("sagadungeons.admin") && "admin".startsWith(arg)) {
                completions.add("admin");
            }

            // 添加子命令
            for (AbstractCommand subCommand : commands.values()) {
                if (subCommand.getName().toLowerCase().startsWith(arg) && subCommand.hasPermission(sender)) {
                    completions.add(subCommand.getName());
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            // 补全管理员子命令
            String arg = args[1].toLowerCase();

            // 检查权限
            if (sender.hasPermission("sagadungeons.admin")) {
                // 添加管理员子命令
                // TODO: 添加管理员子命令
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
        MessageUtil.sendMessage(sender, "command.help.kick");
        MessageUtil.sendMessage(sender, "command.help.public");
        MessageUtil.sendMessage(sender, "command.help.spawner");

        // 如果有管理员权限，发送管理员命令帮助
        if (sender.hasPermission("sagadungeons.admin")) {
            MessageUtil.sendMessage(sender, "command.help.admin");
            MessageUtil.sendMessage(sender, "command.help.reload");
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

        // 发送管理员帮助页脚
        MessageUtil.sendMessage(sender, "command.admin.help.footer");
    }
}
