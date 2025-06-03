package cn.i7mc.sagadungeons.command.admin;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建模板命令
 * 用于创建新的副本模板
 */
public class CreateTemplateCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public CreateTemplateCommand(SagaDungeons plugin) {
        super(plugin, "createtemplate", "sagadungeons.admin", false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 检查参数
        if (args.length < 1) {
            sendMessage(sender, "command.admin.createtemplate.usage");
            return;
        }

        // 获取模板名称
        String templateName = args[0];

        // 检查模板名称是否合法（只允许字母、数字和下划线）
        if (!templateName.matches("^[a-zA-Z0-9_]+$")) {
            sendMessage(sender, "command.admin.createtemplate.invalid-name");
            return;
        }

        // 检查模板是否已存在
        if (plugin.getConfigManager().getTemplateManager().hasTemplate(templateName)) {
            sendMessage(sender, "command.admin.createtemplate.already-exists",
                    MessageUtil.createPlaceholders("template", templateName));
            return;
        }

        // 获取世界路径（可选）
        String worldPath = null;
        if (args.length > 1) {
            worldPath = args[1];
        }

        // 创建模板
        DungeonTemplate template = new DungeonTemplate(templateName);

        // 设置显示名称
        template.setDisplayName(templateName);

        // 设置世界显示名称
        template.setWorldDisplay(templateName);

        // 设置默认超时时间
        template.setDefaultTimeout(plugin.getConfigManager().getDefaultTimeout());

        // 设置世界路径
        if (worldPath != null) {
            template.setWorldPath(worldPath);
        }

        // 创建模板目录
        File templateDir = plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName);
        if (!templateDir.exists()) {
            templateDir.mkdirs();
        }

        // 创建世界目录（如果没有指定世界路径）
        if (worldPath == null) {
            File worldDir = new File(templateDir, "world");
            if (!worldDir.exists()) {
                worldDir.mkdirs();
            }

            // 创建region目录
            File regionDir = new File(worldDir, "region");
            if (!regionDir.exists()) {
                regionDir.mkdirs();
            }
        } else {
            // 检查指定的世界路径是否存在
            File worldDir = new File(plugin.getDataFolder(), worldPath);
            if (!worldDir.exists()) {
                worldDir.mkdirs();

                // 创建region目录
                File regionDir = new File(worldDir, "region");
                if (!regionDir.exists()) {
                    regionDir.mkdirs();
                }
            }
        }

        // 保存模板
        plugin.getConfigManager().getTemplateManager().saveTemplate(template);

        // 重新加载模板
        plugin.getConfigManager().getTemplateManager().loadTemplates();

        // 发送消息
        sendMessage(sender, "command.admin.createtemplate.success",
                MessageUtil.createPlaceholders("template", templateName));

        // 如果是玩家，提示设置世界
        if (sender instanceof Player) {
            Player player = (Player) sender;
            sendMessage(player, "command.admin.createtemplate.set-world-hint",
                    MessageUtil.createPlaceholders("template", templateName));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        // 不提供模板名称的补全

        return completions;
    }
}
