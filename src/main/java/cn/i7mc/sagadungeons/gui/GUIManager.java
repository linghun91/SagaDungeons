package cn.i7mc.sagadungeons.gui;

import cn.i7mc.sagadungeons.SagaDungeons;
import org.bukkit.entity.Player;

/**
 * GUI管理器
 * 负责管理插件的所有GUI界面
 */
public class GUIManager {

    private final SagaDungeons plugin;

    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public GUIManager(SagaDungeons plugin) {
        this.plugin = plugin;
    }

    /**
     * 打开模板选择界面
     * @param player 玩家
     */
    public void openTemplateSelectGUI(Player player) {
        TemplateSelectGUI gui = new TemplateSelectGUI(plugin, player);
        gui.open();
    }

    /**
     * 打开副本管理界面
     * @param player 玩家
     * @param dungeonId 副本ID
     */
    public void openDungeonManageGUI(Player player, String dungeonId) {
        DungeonManageGUI gui = new DungeonManageGUI(plugin, player, dungeonId);
        gui.open();
    }

    /**
     * 打开玩家邀请界面
     * @param player 玩家
     * @param dungeonId 副本ID
     */
    public void openPlayerInviteGUI(Player player, String dungeonId) {
        PlayerInviteGUI gui = new PlayerInviteGUI(plugin, player, dungeonId);
        gui.open();
    }

    /**
     * 打开怪物刷怪点编辑界面
     * @param player 玩家
     */
    public void openMobSpawnerGUI(Player player) {
        MobSpawnerGUI gui = new MobSpawnerGUI(plugin, player);
        gui.open();
    }
}
