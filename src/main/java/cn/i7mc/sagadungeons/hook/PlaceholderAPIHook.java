package cn.i7mc.sagadungeons.hook;

import cn.i7mc.sagadungeons.SagaDungeons;
import cn.i7mc.sagadungeons.dungeon.DungeonInstance;
import cn.i7mc.sagadungeons.model.DungeonTemplate;
import cn.i7mc.sagadungeons.model.PlayerData;
import cn.i7mc.sagadungeons.util.TimeUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

/**
 * PlaceholderAPI集成
 * 负责注册和处理自定义占位符
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final SagaDungeons plugin;

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public PlaceholderAPIHook(SagaDungeons plugin) {
        this.plugin = plugin;
    }

    /**
     * 获取插件名称
     * @return 插件名称
     */
    @Override
    public String getPlugin() {
        return plugin.getName();
    }

    /**
     * 获取作者
     * @return 作者
     */
    @Override
    public String getAuthor() {
        return "linghun91";
    }

    /**
     * 获取版本
     * @return 版本
     */
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    /**
     * 获取标识符
     * @return 标识符
     */
    @Override
    public String getIdentifier() {
        return "sd";
    }

    /**
     * 是否持久化
     * @return 是否持久化
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * 处理占位符请求
     * @param player 玩家
     * @param identifier 标识符
     * @return 替换后的值
     */
    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (player == null) {
            return "";
        }
        
        // 获取玩家数据
        PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());
        
        // 检查玩家是否在副本中
        if (!playerData.isInDungeon()) {
            // 不在副本中的占位符
            if (identifier.equals("in_dungeon")) {
                return "false";
            }
            return "";
        }
        
        // 获取玩家当前副本
        String dungeonId = playerData.getCurrentDungeonId();
        DungeonInstance dungeon = plugin.getDungeonManager().getDungeon(dungeonId);
        
        if (dungeon == null) {
            return "";
        }
        
        // 处理各种占位符
        switch (identifier) {
            case "in_dungeon":
                return "true";
            case "dungeon_id":
                return dungeon.getId();
            case "dungeon_name":
                return dungeon.getTemplateName();
            case "dungeon_display_name":
                return dungeon.getDisplayName();
            case "dungeon_owner":
                OfflinePlayer owner = Bukkit.getOfflinePlayer(dungeon.getOwnerUUID());
                return owner.getName() != null ? owner.getName() : "Unknown";
            case "dungeon_time_left":
                return String.valueOf(dungeon.getRemainingTime());
            case "dungeon_time_left_formatted":
                return TimeUtil.formatTime(dungeon.getRemainingTime());
            case "dungeon_time_left_short":
                return TimeUtil.formatTimeShort(dungeon.getRemainingTime());
            case "dungeon_player_count":
                return String.valueOf(dungeon.getPlayerCount());
            case "dungeon_is_public":
                return dungeon.isPublic() ? "true" : "false";
            case "dungeon_state":
                return dungeon.getState().name();
            case "display":
                // 获取模板的世界显示名称
                DungeonTemplate template = plugin.getConfigManager().getTemplateManager().getTemplate(dungeon.getTemplateName());
                if (template != null && template.getWorldDisplay() != null) {
                    return ChatColor.translateAlternateColorCodes('&', template.getWorldDisplay());
                }
                return dungeon.getTemplateName();
            default:
                return null;
        }
    }
}
