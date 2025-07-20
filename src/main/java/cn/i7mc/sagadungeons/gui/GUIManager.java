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
     * 打开模板编辑主界面
     * @param player 玩家
     */
    public void openTemplateEditMainGUI(Player player) {
        TemplateEditMainGUI gui = new TemplateEditMainGUI(plugin, player);
        gui.open();
    }

    /**
     * 打开模板基础信息编辑界面
     * @param player 玩家
     * @param templateName 模板名称
     */
    public void openTemplateBasicEditGUI(Player player, String templateName) {
        TemplateBasicEditGUI gui = new TemplateBasicEditGUI(plugin, player, templateName);
        gui.open();
    }

    /**
     * 打开模板创建条件编辑界面
     * @param player 玩家
     * @param templateName 模板名称
     */
    public void openTemplateConditionsEditGUI(Player player, String templateName) {
        TemplateConditionsEditGUI gui = new TemplateConditionsEditGUI(plugin, player, templateName);
        gui.open();
    }

    // 奖励系统已移除

    /**
     * 打开模板通关条件编辑界面
     * @param player 玩家
     * @param templateName 模板名称
     */
    public void openTemplateCompletionEditGUI(Player player, String templateName) {
        TemplateCompletionEditGUI gui = new TemplateCompletionEditGUI(plugin, player, templateName);
        gui.open();
    }

    /**
     * 打开模板刷怪点管理界面
     * @param player 玩家
     * @param templateName 模板名称
     */
    public void openTemplateSpawnersEditGUI(Player player, String templateName) {
        TemplateSpawnersEditGUI gui = new TemplateSpawnersEditGUI(plugin, player, templateName);
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

    // 物品奖励管理已移除

    // 命令奖励管理已移除

    // 时间奖励管理已移除

    /**
     * 打开怪物刷怪点编辑界面
     * @param player 玩家
     */
    public void openMobSpawnerGUI(Player player) {
        MobSpawnerGUI gui = new MobSpawnerGUI(plugin, player);
        gui.open();
    }

    /**
     * 打开物品条件管理界面
     * @param player 玩家
     * @param templateName 模板名称
     */
    public void openItemConditionManageGUI(Player player, String templateName) {
        ItemConditionManageGUI gui = new ItemConditionManageGUI(plugin, player, templateName);
        gui.open();
    }

    // 组合条件管理界面已移除

    /**
     * 打开刷怪点编辑界面
     * @param player 玩家
     * @param templateName 模板名称
     * @param spawnerName 刷怪点名称
     */
    public void openSpawnerEditGUI(Player player, String templateName, String spawnerName) {
        SpawnerEditGUI gui = new SpawnerEditGUI(plugin, player, templateName, spawnerName);
        gui.open();
    }

    /**
     * 打开刷怪点添加界面
     * @param player 玩家
     * @param templateName 模板名称
     */
    public void openSpawnerAddGUI(Player player, String templateName) {
        SpawnerAddGUI gui = new SpawnerAddGUI(plugin, player, templateName);
        gui.open();
    }
}
