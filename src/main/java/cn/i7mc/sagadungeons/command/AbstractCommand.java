package cn.i7mc.sagadungeons.command;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 命令抽象基类
 * 所有命令的基类
 */
public abstract class AbstractCommand {

    protected final SagaDungeons plugin;
    private final String name;
    private final String permission;
    private final boolean playerOnly;
    private final List<String> aliases;

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param name 命令名称
     * @param permission 权限节点
     * @param playerOnly 是否仅限玩家使用
     */
    public AbstractCommand(SagaDungeons plugin, String name, String permission, boolean playerOnly) {
        this.plugin = plugin;
        this.name = name;
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.aliases = new ArrayList<>();
    }

    /**
     * 执行命令
     * @param sender 命令发送者
     * @param args 参数
     */
    public abstract void execute(CommandSender sender, String[] args);

    /**
     * Tab补全
     * @param sender 命令发送者
     * @param args 参数
     * @return 补全列表
     */
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }

    /**
     * 获取命令名称
     * @return 命令名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取权限节点
     * @return 权限节点
     */
    public String getPermission() {
        return permission;
    }

    /**
     * 检查是否仅限玩家使用
     * @return 是否仅限玩家使用
     */
    public boolean isPlayerOnly() {
        return playerOnly;
    }

    /**
     * 获取别名列表
     * @return 别名列表
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * 添加别名
     * @param alias 别名
     */
    public void addAlias(String alias) {
        aliases.add(alias);
    }

    /**
     * 检查发送者是否有权限
     * @param sender 命令发送者
     * @return 是否有权限
     */
    public boolean hasPermission(CommandSender sender) {
        return permission == null || permission.isEmpty() || sender.hasPermission(permission);
    }

    /**
     * 发送消息
     * @param sender 命令发送者
     * @param path 消息路径
     */
    protected void sendMessage(CommandSender sender, String path) {
        MessageUtil.sendMessage(sender, path);
    }

    /**
     * 发送消息（带占位符）
     * @param sender 命令发送者
     * @param path 消息路径
     * @param placeholders 占位符
     */
    protected void sendMessage(CommandSender sender, String path, java.util.Map<String, String> placeholders) {
        MessageUtil.sendMessage(sender, path, placeholders);
    }

    /**
     * 获取玩家
     * @param sender 命令发送者
     * @return 玩家
     */
    protected Player getPlayer(CommandSender sender) {
        return (Player) sender;
    }

    /**
     * 检查发送者是否为玩家
     * @param sender 命令发送者
     * @return 是否为玩家
     */
    protected boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }
}
