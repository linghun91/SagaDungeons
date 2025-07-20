package cn.i7mc.sagadungeons.command.admin;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI命令
 * 用于打开GUI管理界面
 */
public class GUICommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public GUICommand(SagaDungeons plugin) {
        super(plugin, "gui", "sagadungeons.admin", true);
        addAlias("g");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 检查是否为玩家
        if (!isPlayer(sender)) {
            sendMessage(sender, "general.player-only");
            return;
        }

        Player player = getPlayer(sender);

        // 打开模板选择界面
        plugin.getGUIManager().openTemplateSelectGUI(player);
        
        // 发送消息
        sendMessage(sender, "command.admin.gui.success");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // 无需补全参数
        return new ArrayList<>();
    }
}
