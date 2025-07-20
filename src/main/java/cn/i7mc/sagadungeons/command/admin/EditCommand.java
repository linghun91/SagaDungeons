package cn.i7mc.sagadungeons.command.admin;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 模板编辑命令
 * 用于打开模板编辑GUI界面
 */
public class EditCommand extends AbstractCommand {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public EditCommand(SagaDungeons plugin) {
        super(plugin, "edit", "sagadungeons.admin", true);
        addAlias("e");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 检查是否为玩家
        if (!isPlayer(sender)) {
            sendMessage(sender, "general.player-only");
            return;
        }

        Player player = getPlayer(sender);

        // 打开模板编辑主界面
        plugin.getGUIManager().openTemplateEditMainGUI(player);
        
        // 发送消息
        sendMessage(sender, "command.admin.edit.success");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // 无需补全参数
        return new ArrayList<>();
    }
}
