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

    /**
     * 打开模板奖励系统编辑界面
     * @param player 玩家
     * @param templateName 模板名称
     */
    public void openTemplateRewardsEditGUI(Player player, String templateName) {
        TemplateRewardsEditGUI gui = new TemplateRewardsEditGUI(plugin, player, templateName);
        gui.open();
    }

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

    /**
     * 打开物品奖励管理界面
     * @param player 玩家
     * @param templateName 模板名称
     */
    public void openItemRewardManageGUI(Player player, String templateName) {
        ItemRewardManageGUI gui = new ItemRewardManageGUI(plugin, player, templateName);
        gui.open();
    }

    /**
     * 打开命令奖励管理界面
     * @param player 玩家
     * @param templateName 模板名称
     */
    public void openCommandRewardManageGUI(Player player, String templateName) {
        CommandRewardManageGUI gui = new CommandRewardManageGUI(plugin, player, templateName);
        gui.open();
    }

    /**
     * 打开时间奖励管理界面
     * @param player 玩家
     * @param templateName 模板名称
     */
    public void openTimeRewardManageGUI(Player player, String templateName) {
        TimeRewardManageGUI gui = new TimeRewardManageGUI(plugin, player, templateName);
        gui.open();
    }

    /**
     * 打开时间奖励命令管理界面（临时方法）
     * @param player 玩家
     * @param templateName 模板名称
     * @param timeSeconds 时间限制
     */
    public void openTimeRewardCommandGUI(Player player, String templateName, int timeSeconds) {
        // 临时实现：显示提示信息并返回时间奖励管理界面
        cn.i7mc.sagadungeons.util.MessageUtil.sendMessage(player, "command.admin.edit.time-command-management-hint");
        openTimeRewardManageGUI(player, templateName);
    }

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

    /**
     * 打开组合条件管理界面
     * @param player 玩家
     * @param templateName 模板名称
     * @param compositeType 组合类型 ("AND" 或 "OR")
     */
    public void openCompositeConditionManageGUI(Player player, String templateName, String compositeType) {
        CompositeConditionManageGUI gui = new CompositeConditionManageGUI(plugin, player, templateName, compositeType);
        gui.open();
    }

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
