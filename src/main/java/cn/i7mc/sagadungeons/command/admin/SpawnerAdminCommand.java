package cn.i7mc.sagadungeons.command.admin;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.util.LocationUtil;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 刷怪点管理命令
 * 用于管理副本中的刷怪点
 */
public class SpawnerAdminCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public SpawnerAdminCommand(SagaDungeons plugin) {
        super(plugin, "spawner", "sagadungeons.admin.spawner", true);
        addAlias("sp");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 检查参数
        if (args.length < 1) {
            sendMessage(sender, "command.admin.spawner.usage");
            return;
        }

        // 获取玩家
        Player player = getPlayer(sender);

        // 获取子命令
        String subCommand = args[0].toLowerCase();

        // 获取当前副本
        String dungeonId = plugin.getDungeonManager().getCurrentDungeonId(player);
        if (dungeonId == null) {
            sendMessage(sender, "command.admin.spawner.not-in-dungeon");
            return;
        }

        // 获取副本模板
        String templateName = plugin.getDungeonManager().getDungeon(dungeonId).getTemplateName();
        DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(templateName);
        if (template == null) {
            sendMessage(sender, "command.admin.spawner.template-not-found");
            return;
        }

        // 处理子命令
        switch (subCommand) {
            case "set":
                // 设置刷怪点
                if (args.length < 3) {
                    sendMessage(sender, "command.admin.spawner.set.usage");
                    return;
                }

                // 获取刷怪点ID
                String spawnerId = args[1];

                // 获取怪物类型
                String mobType = args[2];

                // 检查MythicMobs是否可用
                if (plugin.getHookManager().isMythicMobsAvailable()) {
                    // 检查怪物类型是否存在
                    if (!plugin.getHookManager().getMythicMobsHook().isMobTypeExists(mobType)) {
                        sendMessage(sender, "command.admin.spawner.set.invalid-mob-type",
                                MessageUtil.createPlaceholders("mob", mobType));
                        return;
                    }
                }

                // 获取刷怪数量
                int amount = 1;
                if (args.length > 3) {
                    try {
                        amount = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        sendMessage(sender, "command.admin.spawner.set.invalid-amount");
                        return;
                    }
                }

                // 检查数量是否有效
                if (amount <= 0) {
                    sendMessage(sender, "command.admin.spawner.set.invalid-amount");
                    return;
                }

                // 获取冷却时间
                int cooldown = 0;
                if (args.length > 4) {
                    try {
                        cooldown = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                        sendMessage(sender, "command.admin.spawner.set.invalid-cooldown");
                        return;
                    }
                }

                // 检查冷却时间是否有效
                if (cooldown < 0) {
                    sendMessage(sender, "command.admin.spawner.set.invalid-cooldown");
                    return;
                }

                // 获取玩家位置
                Location location = player.getLocation();

                // 转换为字符串（不包含世界名）
                String locationString = LocationUtil.locationToStringWithoutWorld(location);

                // 添加刷怪点到模板
                template.addMobSpawner(spawnerId, mobType, locationString, cooldown, amount);

                // 保存模板
                plugin.getConfigManager().getTemplateManager().saveTemplate(template);

                // 如果MythicMobs可用，直接生成怪物
                if (plugin.getHookManager().isMythicMobsAvailable() && cooldown == 0) {
                    // 直接生成怪物
                    plugin.getHookManager().getMythicMobsHook().spawnMob(mobType, location, amount);
                }

                // 发送消息
                sendMessage(sender, "command.admin.spawner.set.success",
                        MessageUtil.createPlaceholders("id", spawnerId,
                                "mob", mobType,
                                "amount", String.valueOf(amount),
                                "cooldown", String.valueOf(cooldown)));
                break;
            case "remove":
                // 删除刷怪点
                if (args.length < 2) {
                    sendMessage(sender, "command.admin.spawner.remove.usage");
                    return;
                }

                // 获取刷怪点ID
                String removeId = args[1];

                // 删除刷怪点
                if (template.removeMobSpawner(removeId)) {
                    // 保存模板
                    plugin.getConfigManager().getTemplateManager().saveTemplate(template);

                    // 发送消息
                    sendMessage(sender, "command.admin.spawner.remove.success",
                            MessageUtil.createPlaceholders("id", removeId));
                } else {
                    // 发送消息
                    sendMessage(sender, "command.admin.spawner.remove.not-found",
                            MessageUtil.createPlaceholders("id", removeId));
                }
                break;
            case "list":
                // 列出刷怪点
                if (template.getMobSpawners().isEmpty()) {
                    sendMessage(sender, "command.admin.spawner.list.empty");
                    return;
                }

                // 发送标题
                sendMessage(sender, "command.admin.spawner.list.header");

                // 发送刷怪点列表
                for (String id : template.getMobSpawners().keySet()) {
                    sendMessage(sender, "command.admin.spawner.list.entry",
                            MessageUtil.createPlaceholders("id", id,
                                    "mob", template.getMobSpawners().get(id).getMobType(),
                                    "amount", String.valueOf(template.getMobSpawners().get(id).getAmount()),
                                    "cooldown", String.valueOf(template.getMobSpawners().get(id).getCooldown())));
                }

                // 发送页脚
                sendMessage(sender, "command.admin.spawner.list.footer");
                break;
            default:
                // 未知命令
                sendMessage(sender, "command.admin.spawner.unknown",
                        MessageUtil.createPlaceholders("command", subCommand));
                break;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        // 检查是否为玩家
        if (!(sender instanceof Player)) {
            return completions;
        }

        Player player = (Player) sender;

        // 获取当前副本
        String dungeonId = plugin.getDungeonManager().getCurrentDungeonId(player);
        if (dungeonId == null) {
            return completions;
        }

        // 获取副本模板
        String templateName = plugin.getDungeonManager().getDungeon(dungeonId).getTemplateName();
        DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(templateName);
        if (template == null) {
            return completions;
        }

        // 补全子命令
        if (args.length == 1) {
            String arg = args[0].toLowerCase();

            if ("set".startsWith(arg)) {
                completions.add("set");
            }
            if ("remove".startsWith(arg)) {
                completions.add("remove");
            }
            if ("list".startsWith(arg)) {
                completions.add("list");
            }
        } else if (args.length == 2) {
            String arg = args[1].toLowerCase();
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("remove")) {
                // 补全刷怪点ID
                for (String id : template.getMobSpawners().keySet()) {
                    if (id.toLowerCase().startsWith(arg)) {
                        completions.add(id);
                    }
                }
            } else if (subCommand.equals("set")) {
                // 为set命令提供一些建议的刷怪点ID
                List<String> suggestedIds = new ArrayList<>();

                // 添加一些常用的ID前缀
                suggestedIds.add("mob1");
                suggestedIds.add("boss");
                suggestedIds.add("spawn1");

                // 过滤并添加到补全列表
                for (String id : suggestedIds) {
                    if (id.toLowerCase().startsWith(arg)) {
                        completions.add(id);
                    }
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            String arg = args[2].toLowerCase();

            // 补全怪物类型
            if (plugin.getHookManager().isMythicMobsAvailable()) {
                for (String mobType : plugin.getHookManager().getMythicMobsHook().getMobTypes()) {
                    if (mobType.toLowerCase().startsWith(arg)) {
                        completions.add(mobType);
                    }
                }
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("set")) {
            // 补全数量
            completions.add("1");
            completions.add("5");
            completions.add("10");
        } else if (args.length == 5 && args[0].equalsIgnoreCase("set")) {
            // 补全冷却时间
            completions.add("0");
            completions.add("60");
            completions.add("300");
            completions.add("600");
        }

        return completions;
    }
}
