package cn.i7mc.sagadungeons.event;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.model.PlayerData;
import cn.i7mc.sagadungeons.util.DebugUtil;
import cn.i7mc.sagadungeons.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * 指令阻止监听器
 * 处理副本中的指令阻止功能
 */
public class CommandBlockListener extends AbstractListener {

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public CommandBlockListener(SagaDungeons plugin) {
        super(plugin);
    }

    /**
     * 处理玩家指令预处理事件
     * @param event 玩家指令预处理事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();

        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());

        // 检查玩家是否在副本中
        if (!playerData.isInDungeon()) {
            return;
        }

        // 获取副本ID
        String dungeonId = playerData.getCurrentDungeonId();
        if (dungeonId == null) {
            return;
        }

        // 获取副本实例
        DungeonInstance instance = plugin.getDungeonManager().getDungeon(dungeonId);
        if (instance == null) {
            return;
        }

        // 获取副本模板
        DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(instance.getTemplateName());
        if (template == null) {
            return;
        }

        // 检查指令是否被禁止
        if (template.isCommandBlocked(command)) {
            // 取消指令执行
            event.setCancelled(true);

            // 发送提示消息
            plugin.getConfigManager().getMessageManager().sendMessage(player, "dungeon.command.blocked",
                    MessageUtil.createPlaceholders("command", command));

            // 如果启用调试模式，输出调试信息
            if (plugin.getConfigManager().isDebugEnabled()) {
                DebugUtil.debug("dungeon.command.blocked-debug",
                        MessageUtil.createPlaceholders(
                                "player", player.getName(),
                                "command", command,
                                "dungeon", dungeonId,
                                "template", template.getName()
                        ));
            }
        }
    }
}
