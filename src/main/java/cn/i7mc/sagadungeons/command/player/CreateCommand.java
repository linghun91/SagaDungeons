package cn.i7mc.sagadungeons.command.player;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import cn.i7mc.sagadungeons.model.PlayerData;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建副本命令
 * 用于创建副本
 */
public class CreateCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public CreateCommand(SagaDungeons plugin) {
        super(plugin, "create", "sagadungeons.command.create", false);
        addAlias("c");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 检查参数
        if (args.length < 1) {
            if (sender instanceof Player) {
                sendMessage(sender, "command.create.usage");
            } else {
                sendMessage(sender, "command.create.console-usage");
            }
            return;
        }

        // 获取模板名称
        String templateName = args[0];

        // 检查模板是否存在
        if (!plugin.getConfigManager().getTemplateManager().hasTemplate(templateName)) {
            sendMessage(sender, "command.create.template-not-found",
                    MessageUtil.createPlaceholders("template", templateName));
            return;
        }

        Player targetPlayer;

        if (sender instanceof Player) {
            // 玩家执行命令
            targetPlayer = (Player) sender;

            // 检查权限
            if (!hasPermission(sender)) {
                sendMessage(sender, "general.no-permission");
                return;
            }
        } else {
            // 控制台执行命令
            if (args.length < 2) {
                sendMessage(sender, "command.create.console-usage");
                return;
            }

            String playerName = args[1];
            targetPlayer = plugin.getServer().getPlayer(playerName);

            if (targetPlayer == null) {
                sendMessage(sender, "command.create.player-not-found",
                        MessageUtil.createPlaceholders("player", playerName));
                return;
            }
        }

        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(targetPlayer.getUniqueId());

        // 检查玩家是否已经在副本中
        if (playerData.isInDungeon()) {
            sendMessage(sender, "command.create.already-in-dungeon");
            return;
        }

        // 检查冷却时间（仅对玩家执行时检查）
        if (sender instanceof Player) {
            int cooldownSeconds = plugin.getConfigManager().getCreationCooldown();
            if (!plugin.getDungeonManager().getCooldownManager().canCreateDungeon(targetPlayer.getUniqueId(), cooldownSeconds)) {
                int remainingSeconds = plugin.getDungeonManager().getCooldownManager().getRemainingCooldown(targetPlayer.getUniqueId(), cooldownSeconds);
                sendMessage(sender, "command.create.cooldown",
                        MessageUtil.createPlaceholders("time", String.valueOf(remainingSeconds)));
                return;
            }
        }

        // 创建副本
        boolean success = plugin.getDungeonManager().createDungeon(targetPlayer, templateName);

        // 发送消息
        if (success) {
            if (sender instanceof Player && sender.equals(targetPlayer)) {
                // 玩家为自己创建副本
                sendMessage(sender, "command.create.success",
                        MessageUtil.createPlaceholders("template", templateName));
            } else {
                // 控制台为玩家创建副本
                sendMessage(sender, "command.create.console-success",
                        MessageUtil.createPlaceholders("template", templateName, "player", targetPlayer.getName()));
                sendMessage(targetPlayer, "command.create.created-by-console",
                        MessageUtil.createPlaceholders("template", templateName));
            }
        } else {
            sendMessage(sender, "command.create.fail");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        // 补全模板名称
        if (args.length == 1) {
            String arg = args[0].toLowerCase();

            // 获取所有模板
            for (String templateName : plugin.getConfigManager().getTemplateManager().getTemplates().keySet()) {
                if (templateName.toLowerCase().startsWith(arg)) {
                    completions.add(templateName);
                }
            }
        }
        // 控制台执行时补全玩家名称
        else if (args.length == 2 && !(sender instanceof Player)) {
            String arg = args[1].toLowerCase();

            // 获取所有在线玩家
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(arg)) {
                    completions.add(player.getName());
                }
            }
        }

        return completions;
    }
}
