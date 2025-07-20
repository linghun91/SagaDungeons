package cn.i7mc.sagadungeons.command.admin;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 设置世界命令
 * 用于将当前世界设置为副本模板的世界
 */
public class SetWorldCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public SetWorldCommand(SagaDungeons plugin) {
        super(plugin, "setworld", "sagadungeons.admin", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 检查是否为玩家
        Player player = getPlayer(sender);
        if (player == null) {
            sendMessage(sender, "general.player-only");
            return;
        }

        // 检查参数
        if (args.length < 1) {
            sendMessage(sender, "command.admin.setworld.usage");
            return;
        }

        // 获取模板名称
        String templateName = args[0];

        // 检查模板是否存在
        if (!plugin.getConfigManager().getTemplateManager().hasTemplate(templateName)) {
            sendMessage(sender, "command.admin.setworld.template-not-found",
                    MessageUtil.createPlaceholders("template", templateName));
            return;
        }

        // 获取模板
        DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(templateName);

        // 获取当前世界
        World world = player.getWorld();

        // 创建世界路径
        String worldPath = "templates/" + templateName + "/world";

        // 设置世界路径
        template.setWorldPath(worldPath);

        // 创建世界目录
        File worldDir = new File(plugin.getDataFolder(), worldPath);
        if (!worldDir.exists()) {
            worldDir.mkdirs();
        }

        // 保存模板
        plugin.getConfigManager().getTemplateManager().saveTemplate(template);

        // 保存后重新加载模板，确保内存中的模板与配置文件同步
        plugin.getConfigManager().getTemplateManager().reloadTemplate(templateName);

        // 发送消息
        sendMessage(sender, "command.admin.setworld.success",
                MessageUtil.createPlaceholders("template", templateName, "world", world.getName()));

        // 提示复制世界文件
        sendMessage(sender, "command.admin.setworld.copy-hint",
                MessageUtil.createPlaceholders("template", templateName));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        // 补全模板名称
        if (args.length == 1) {
            String arg = args[0].toLowerCase();
            for (String templateName : plugin.getConfigManager().getTemplateManager().getTemplates().keySet()) {
                if (templateName.toLowerCase().startsWith(arg)) {
                    completions.add(templateName);
                }
            }
        }

        return completions;
    }
}
