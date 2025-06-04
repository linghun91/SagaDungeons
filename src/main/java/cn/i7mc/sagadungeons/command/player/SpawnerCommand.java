package cn.i7mc.sagadungeons.command.player;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.model.PlayerData;
import cn.i7mc.sagadungeons.util.LocationUtil;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 刷怪点命令
 * 用于设置和删除刷怪点
 */
public class SpawnerCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public SpawnerCommand(SagaDungeons plugin) {
        super(plugin, "spawner", "sagadungeons.command.spawner", true);
        addAlias("sp");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        
        // 检查参数
        if (args.length < 1) {
            sendMessage(sender, "command.spawner.usage");
            return;
        }
        
        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());
        
        // 检查玩家是否在副本中
        if (!playerData.isInDungeon()) {
            sendMessage(sender, "command.spawner.not-in-dungeon");
            return;
        }
        
        // 获取副本ID
        String dungeonId = playerData.getCurrentDungeonId();
        
        // 获取副本实例
        DungeonInstance dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        if (dungeon == null) {
            sendMessage(sender, "command.spawner.dungeon-not-found");
            return;
        }
        
        // 检查是否为副本创建者
        if (!dungeon.getOwnerUUID().equals(player.getUniqueId()) && !player.hasPermission("sagadungeons.admin")) {
            sendMessage(sender, "command.spawner.not-owner");
            return;
        }
        
        // 获取副本模板
        DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(dungeon.getTemplateName());
        if (template == null) {
            sendMessage(sender, "command.spawner.template-not-found");
            return;
        }
        
        // 获取子命令
        String subCommand = args[0].toLowerCase();
        
        // 处理子命令
        switch (subCommand) {
            case "set":
                // 设置刷怪点
                if (args.length < 3) {
                    sendMessage(sender, "command.spawner.set.usage");
                    return;
                }
                
                // 获取刷怪点ID
                String spawnerId = args[1];
                
                // 获取怪物类型
                String mobType = args[2];
                
                // 获取刷怪数量
                int amount = 1;
                if (args.length > 3) {
                    try {
                        amount = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        sendMessage(sender, "command.spawner.set.invalid-amount");
                        return;
                    }
                }
                
                // 获取刷怪冷却
                int cooldown = 30;
                if (args.length > 4) {
                    try {
                        cooldown = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                        sendMessage(sender, "command.spawner.set.invalid-cooldown");
                        return;
                    }
                }
                
                // 获取玩家位置
                Location location = player.getLocation();
                
                // 转换为字符串
                String locationString = LocationUtil.locationToString(location);
                
                // 添加刷怪点
                template.addMobSpawner(spawnerId, mobType, locationString, cooldown, amount);
                
                // 保存模板
                plugin.getConfigManager().getTemplateManager().saveTemplate(template);
                
                // 发送消息
                sendMessage(sender, "command.spawner.set.success", 
                        MessageUtil.createPlaceholders("id", spawnerId, 
                                "mob", mobType, 
                                "amount", String.valueOf(amount), 
                                "cooldown", String.valueOf(cooldown)));
                break;
            case "remove":
                // 删除刷怪点
                if (args.length < 2) {
                    sendMessage(sender, "command.spawner.remove.usage");
                    return;
                }
                
                // 获取刷怪点ID
                String removeId = args[1];
                
                // 删除刷怪点
                if (template.removeMobSpawner(removeId)) {
                    // 保存模板
                    plugin.getConfigManager().getTemplateManager().saveTemplate(template);
                    
                    // 发送消息
                    sendMessage(sender, "command.spawner.remove.success", 
                            MessageUtil.createPlaceholders("id", removeId));
                } else {
                    // 发送消息
                    sendMessage(sender, "command.spawner.remove.not-found", 
                            MessageUtil.createPlaceholders("id", removeId));
                }
                break;
            case "list":
                // 列出刷怪点
                if (template.getMobSpawners().isEmpty()) {
                    sendMessage(sender, "command.spawner.list.empty");
                    return;
                }
                
                // 发送标题
                sendMessage(sender, "command.spawner.list.header");
                
                // 发送刷怪点列表
                for (String id : template.getMobSpawners().keySet()) {
                    sendMessage(sender, "command.spawner.list.entry", 
                            MessageUtil.createPlaceholders("id", id, 
                                    "mob", template.getMobSpawners().get(id).getMobType(), 
                                    "amount", String.valueOf(template.getMobSpawners().get(id).getAmount()), 
                                    "cooldown", String.valueOf(template.getMobSpawners().get(id).getCooldown())));
                }
                
                // 发送页脚
                sendMessage(sender, "command.spawner.list.footer");
                break;
            default:
                // 未知命令
                sendMessage(sender, "command.spawner.unknown", 
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
            subCommands.add("set");
            subCommands.add("remove");
            subCommands.add("list");
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(arg)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            // 补全刷怪点ID
            if (args[0].equalsIgnoreCase("remove")) {
                String arg = args[1].toLowerCase();
                
                // 获取玩家
                if (!isPlayer(sender)) {
                    return completions;
                }
                
                Player player = getPlayer(sender);
                
                // 获取玩家数据
                PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());
                
                // 检查玩家是否在副本中
                if (!playerData.isInDungeon()) {
                    return completions;
                }
                
                // 获取副本ID
                String dungeonId = playerData.getCurrentDungeonId();
                
                // 获取副本实例
                DungeonInstance dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
                if (dungeon == null) {
                    return completions;
                }
                
                // 获取副本模板
                DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(dungeon.getTemplateName());
                if (template == null) {
                    return completions;
                }
                
                // 补全刷怪点ID
                for (String id : template.getMobSpawners().keySet()) {
                    if (id.toLowerCase().startsWith(arg)) {
                        completions.add(id);
                    }
                }
            }
        } else if (args.length == 3) {
            // 补全怪物类型
            if (args[0].equalsIgnoreCase("set")) {
                String arg = args[2].toLowerCase();
                
                // 检查MythicMobs是否可用
                if (plugin.getHookManager().isMythicMobsAvailable()) {
                    // 获取所有怪物类型
                    List<String> mobTypes = plugin.getHookManager().getMythicMobsHook().getMobTypes();
                    
                    // 补全怪物类型
                    for (String mobType : mobTypes) {
                        if (mobType.toLowerCase().startsWith(arg)) {
                            completions.add(mobType);
                        }
                    }
                }
            }
        }
        
        return completions;
    }
}
