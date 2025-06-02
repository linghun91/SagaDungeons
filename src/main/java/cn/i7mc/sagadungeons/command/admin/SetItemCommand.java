package cn.i7mc.sagadungeons.command.admin;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.util.ItemStackUtil;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 设置物品命令
 * 用于将手持物品设置为副本的条件或奖励
 */
public class SetItemCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public SetItemCommand(SagaDungeons plugin) {
        super(plugin, "setitem", "sagadungeons.admin", true);
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
        if (args.length < 2) {
            sendMessage(sender, "command.admin.setitem.usage");
            return;
        }

        // 获取模板名称
        String templateName = args[0];

        // 检查模板是否存在
        if (!plugin.getConfigManager().getTemplateManager().hasTemplate(templateName)) {
            sendMessage(sender, "command.admin.setitem.template-not-found",
                    MessageUtil.createPlaceholders("template", templateName));
            return;
        }

        // 获取类型
        String type = args[1].toLowerCase();

        // 检查类型是否合法
        if (!type.equals("condition") && !type.equals("reward") && !type.equals("revive")) {
            sendMessage(sender, "command.admin.setitem.invalid-type");
            return;
        }

        // 获取手持物品
        ItemStack item = player.getInventory().getItemInMainHand();

        // 检查物品是否为空
        if (item == null || item.getType().isAir()) {
            sendMessage(sender, "command.admin.setitem.no-item");
            return;
        }

        // 获取数量
        int amount = 1;
        if (args.length > 2) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount <= 0) {
                    sendMessage(sender, "command.admin.setitem.invalid-amount");
                    return;
                }
            } catch (NumberFormatException e) {
                sendMessage(sender, "command.admin.setitem.invalid-amount");
                return;
            }
        }

        // 序列化物品
        String serializedItem = ItemStackUtil.serializeItemStack(item);

        // 获取模板
        DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(templateName);

        // 获取模板配置文件
        File configFile = new File(plugin.getConfigManager().getTemplateManager().getTemplateDirectory(templateName), "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // 根据类型设置物品
        switch (type) {
            case "condition":
                // 设置条件物品
                config.set("creationConditions.items.serialized-item.serialized-item", serializedItem);
                config.set("creationConditions.items.serialized-item.amount", amount);
                break;
            case "reward":
                // 设置奖励物品
                config.set("rewards.items.serialized-item.serialized-item", serializedItem);
                config.set("rewards.items.serialized-item.amount", amount);
                break;
            case "revive":
                // 设置复活道具
                config.set("reviveItem.serialized-item", serializedItem);
                break;
        }

        // 保存配置
        try {
            config.save(configFile);

            // 重新加载模板
            plugin.getConfigManager().getTemplateManager().loadTemplates();

            // 发送成功消息
            sendMessage(sender, "command.admin.setitem.success",
                    MessageUtil.createPlaceholders("template", templateName, "type", type));
        } catch (IOException e) {
            // 发送失败消息
            sendMessage(sender, "command.admin.setitem.fail",
                    MessageUtil.createPlaceholders("template", templateName, "type", type));
            e.printStackTrace();
        }
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
        // 补全类型
        else if (args.length == 2) {
            String arg = args[1].toLowerCase();
            if ("condition".startsWith(arg)) {
                completions.add("condition");
            }
            if ("reward".startsWith(arg)) {
                completions.add("reward");
            }
            if ("revive".startsWith(arg)) {
                completions.add("revive");
            }
        }
        // 补全数量
        else if (args.length == 3) {
            completions.add("1");
            completions.add("5");
            completions.add("10");
        }

        return completions;
    }
}
