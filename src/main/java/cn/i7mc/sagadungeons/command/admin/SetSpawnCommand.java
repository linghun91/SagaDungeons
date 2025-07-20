package cn.i7mc.sagadungeons.command.admin;

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
 * 设置副本重生点命令
 * 用于管理员设置副本模板的重生点
 */
public class SetSpawnCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public SetSpawnCommand(SagaDungeons plugin) {
        super(plugin, "setspawn", "sagadungeons.admin", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);

        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());

        // 检查玩家是否在副本中
        if (!playerData.isInDungeon()) {
            sendMessage(sender, "command.admin.setspawn.not-in-dungeon");
            return;
        }

        // 获取玩家当前副本
        String dungeonId = playerData.getCurrentDungeonId();
        DungeonInstance instance = plugin.getDungeonManager().getDungeon(dungeonId);

        if (instance == null) {
            sendMessage(sender, "command.admin.setspawn.invalid-dungeon");
            return;
        }

        // 获取模板名称
        String templateName = instance.getTemplateName();

        // 获取模板
        DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(templateName);

        if (template == null) {
            sendMessage(sender, "command.admin.setspawn.template-not-found");
            return;
        }

        // 获取玩家当前位置
        Location location = player.getLocation();

        // 转换为字符串（不包含世界名）
        String locationString = LocationUtil.locationToStringWithoutWorld(location);

        // 设置重生点
        template.setSpawnLocation(locationString);

        // 保存模板
        plugin.getConfigManager().getTemplateManager().saveTemplate(template);

        // 保存后重新加载模板，确保内存中的模板与配置文件同步
        plugin.getConfigManager().getTemplateManager().reloadTemplate(templateName);

        // 发送成功消息
        sendMessage(sender, "command.admin.setspawn.success",
                MessageUtil.createPlaceholders("template", templateName));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // 不需要参数补全
        return new ArrayList<>();
    }
}
