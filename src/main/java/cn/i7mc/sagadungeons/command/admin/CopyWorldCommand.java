package cn.i7mc.sagadungeons.command.admin;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.util.BukkitFileUtil;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 复制世界命令
 * 用于将指定世界复制到副本模板的世界目录
 */
public class CopyWorldCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public CopyWorldCommand(SagaDungeons plugin) {
        super(plugin, "copyworld", "sagadungeons.admin", false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 检查参数
        if (args.length < 2) {
            sendMessage(sender, "command.admin.copyworld.usage");
            return;
        }

        // 获取模板名称
        String templateName = args[0];

        // 检查模板是否存在
        if (!plugin.getConfigManager().getTemplateManager().hasTemplate(templateName)) {
            sendMessage(sender, "command.admin.copyworld.template-not-found",
                    MessageUtil.createPlaceholders("template", templateName));
            return;
        }

        // 获取模板
        DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(templateName);

        // 获取世界名称
        String worldName = args[1];

        // 检查世界是否存在
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sendMessage(sender, "command.admin.copyworld.world-not-found",
                    MessageUtil.createPlaceholders("world", worldName));
            return;
        }

        // 获取世界目录
        File worldDir = world.getWorldFolder();

        // 创建世界路径
        String worldPath = "templates/" + templateName + "/world";

        // 设置世界路径
        template.setWorldPath(worldPath);

        // 创建目标目录
        File targetDir = new File(plugin.getDataFolder(), worldPath);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        // 发送开始复制消息
        sendMessage(sender, "command.admin.copyworld.start",
                MessageUtil.createPlaceholders("template", templateName, "world", worldName));

        // 异步复制世界文件
        BukkitFileUtil.copyDirectoryAsync(worldDir, targetDir, progress -> {
            // 发送进度消息
            if (progress % 0.1 == 0) {
                sendMessage(sender, "command.admin.copyworld.progress",
                        MessageUtil.createPlaceholders("progress", String.format("%.1f", progress * 100)));
            }
        }, success -> {
            if (success) {
                // 保存模板
                plugin.getConfigManager().getTemplateManager().saveTemplate(template);

                // 发送完成消息
                sendMessage(sender, "command.admin.copyworld.success",
                        MessageUtil.createPlaceholders("template", templateName, "world", worldName));
            } else {
                // 发送失败消息
                sendMessage(sender, "command.admin.copyworld.fail",
                        MessageUtil.createPlaceholders("template", templateName, "world", worldName));
            }
        });
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
        // 补全世界名称
        else if (args.length == 2) {
            String arg = args[1].toLowerCase();
            for (World world : Bukkit.getWorlds()) {
                if (world.getName().toLowerCase().startsWith(arg)) {
                    completions.add(world.getName());
                }
            }
        }

        return completions;
    }
}
