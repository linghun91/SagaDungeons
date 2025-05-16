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
        super(plugin, "create", "sagadungeons.command.create", true);
        addAlias("c");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        
        // 检查参数
        if (args.length < 1) {
            sendMessage(sender, "command.create.usage");
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
        
        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());
        
        // 检查玩家是否已经在副本中
        if (playerData.isInDungeon()) {
            sendMessage(sender, "command.create.already-in-dungeon");
            return;
        }
        
        // 检查冷却时间
        int cooldownSeconds = plugin.getConfigManager().getCreationCooldown();
        if (!plugin.getDungeonManager().getCooldownManager().canCreateDungeon(player.getUniqueId(), cooldownSeconds)) {
            int remainingSeconds = plugin.getDungeonManager().getCooldownManager().getRemainingCooldown(player.getUniqueId(), cooldownSeconds);
            sendMessage(sender, "command.create.cooldown", 
                    MessageUtil.createPlaceholders("time", String.valueOf(remainingSeconds)));
            return;
        }
        
        // 创建副本
        boolean success = plugin.getDungeonManager().createDungeon(player, templateName);
        
        // 发送消息
        if (success) {
            sendMessage(sender, "command.create.success", 
                    MessageUtil.createPlaceholders("template", templateName));
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
        
        return completions;
    }
}
