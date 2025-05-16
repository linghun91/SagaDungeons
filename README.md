# SagaDungeons 开发者指南

## 简介

SagaDungeons 是一个功能强大的 Minecraft 副本系统插件，允许服务器管理员创建自定义副本，玩家可以进入这些副本挑战各种任务并获得奖励。本指南将帮助开发者理解 SagaDungeons 的代码结构，并提供扩展开发的指导。

## 代码结构

SagaDungeons 采用模块化设计，主要包含以下包：

```
src/main/java/cn/i7mc/sagadungeons
├── SagaDungeons.java (主类)
├── command (命令处理)
│   ├── AbstractCommand.java (命令抽象类)
│   ├── CommandManager.java (命令管理器)
│   ├── admin (管理员命令)
│   │   ├── AdminCommand.java (管理员命令主类)
│   │   ├── CopyWorldCommand.java (复制世界命令)
│   │   ├── CreateTemplateCommand.java (创建模板命令)
│   │   ├── SetItemCommand.java (设置物品命令)
│   │   └── SetWorldCommand.java (设置世界命令)
│   └── player (玩家命令)
│       ├── CreateCommand.java (创建副本命令)
│       ├── InviteCommand.java (邀请玩家命令)
│       ├── JoinCommand.java (加入副本命令)
│       ├── KickCommand.java (踢出玩家命令)
│       ├── ListCommand.java (列出副本命令)
│       ├── PublicCommand.java (公开副本命令)
│       ├── SpawnerCommand.java (刷怪点命令)
│       └── StatsCommand.java (统计信息命令)
├── config (配置管理)
│   ├── ConfigManager.java (配置管理器)
│   ├── DebugMessageManager.java (调试消息管理器)
│   ├── MessageManager.java (消息管理器)
│   └── TemplateManager.java (模板管理器)
├── dungeon (副本核心)
│   ├── DungeonInstance.java (副本实例)
│   ├── DungeonManager.java (副本管理器)
│   ├── DungeonState.java (副本状态枚举)
│   ├── completion (通关条件)
│   │   ├── CompletionCondition.java (通关条件接口)
│   │   ├── CompletionManager.java (通关条件管理器)
│   │   ├── CompletionType.java (通关条件类型枚举)
│   │   ├── KillAllCondition.java (击杀全部条件)
│   │   ├── KillCountCondition.java (击杀数量条件)
│   │   ├── KillSpecificCondition.java (击杀特定怪物条件)
│   │   └── ReachAreaCondition.java (到达区域条件)
│   ├── condition (创建条件)
│   │   ├── DungeonRequirement.java (副本要求接口)
│   │   ├── ItemRequirement.java (物品要求)
│   │   ├── LevelRequirement.java (等级要求)
│   │   ├── MoneyRequirement.java (金钱要求)
│   │   ├── PointsRequirement.java (点数要求)
│   │   ├── RequirementManager.java (要求管理器)
│   │   ├── RequirementType.java (要求类型枚举)
│   │   └── SerializedItemRequirement.java (序列化物品要求)
│   ├── cooldown (冷却管理)
│   │   └── CooldownManager.java (冷却管理器)
│   ├── death (死亡管理)
│   │   └── DeathManager.java (死亡管理器)
│   └── reward (奖励系统)
│       ├── CommandReward.java (命令奖励)
│       ├── DungeonReward.java (副本奖励接口)
│       ├── ExperienceReward.java (经验奖励)
│       ├── ItemReward.java (物品奖励)
│       ├── MoneyReward.java (金钱奖励)
│       ├── PointsReward.java (点数奖励)
│       ├── RewardManager.java (奖励管理器)
│       ├── RewardType.java (奖励类型枚举)
│       └── SerializedItemReward.java (序列化物品奖励)
├── event (事件监听)
│   ├── AbstractListener.java (监听器抽象类)
│   ├── CompletionListener.java (通关条件监听器)
│   ├── InventoryListener.java (物品栏监听器)
│   ├── PlayerListener.java (玩家监听器)
│   └── WorldListener.java (世界监听器)
├── gui (图形界面)
│   ├── AbstractGUI.java (GUI抽象类)
│   ├── DungeonManageGUI.java (副本管理GUI)
│   ├── GUIManager.java (GUI管理器)
│   ├── ItemBuilder.java (物品构建器)
│   ├── MobSpawnerGUI.java (刷怪点GUI)
│   ├── PlayerInviteGUI.java (玩家邀请GUI)
│   └── TemplateSelectGUI.java (模板选择GUI)
├── hook (插件集成)
│   ├── HookManager.java (集成管理器)
│   ├── MythicMobsHook.java (MythicMobs集成)
│   ├── PlaceholderAPIHook.java (PlaceholderAPI集成)
│   ├── PlayerPointsHook.java (PlayerPoints集成)
│   └── VaultHook.java (Vault集成)
├── manager (管理器)
│   └── WorldManager.java (世界管理器)
├── model (数据模型)
│   ├── DungeonInvitation.java (副本邀请)
│   ├── DungeonTemplate.java (副本模板)
│   ├── MobSpawner.java (刷怪点)
│   └── PlayerData.java (玩家数据)
└── util (工具类)
    ├── BukkitFileUtil.java (Bukkit文件工具)
    ├── DebugUtil.java (调试工具)
    ├── ItemStackUtil.java (物品堆工具)
    ├── LocationUtil.java (位置工具)
    ├── MessageUtil.java (消息工具)
    └── TimeUtil.java (时间工具)

src/main/resources
├── config.yml (主配置文件)
├── debugmessage.yml (调试消息配置)
├── messages.yml (消息配置)
├── plugin.yml (插件信息)
└── templates (模板目录)
    └── example (示例模板)
        └── config.yml (模板配置)
```

## 核心接口与类

### 通关条件接口

```java
public interface CompletionCondition {
    boolean check(DungeonInstance instance);
    String getDescription();
    CompletionType getType();
    void reset();
    double getProgress();
    String getProgressDescription();
    void handleEvent(Player player, String event, Object data);
}
```

### 奖励接口

```java
public interface DungeonReward {
    boolean give(Player player);
    String getDescription();
    RewardType getType();
}
```

### 创建条件接口

```java
public interface DungeonRequirement {
    boolean check(Player player);
    String getDescription();
    RequirementType getType();
}
```

### 副本实例类

```java
public class DungeonInstance {
    // 获取副本ID
    public String getId();

    // 获取模板名称
    public String getTemplateName();

    // 获取显示名称
    public String getDisplayName();

    // 获取副本世界
    public World getWorld();

    // 获取创建者UUID
    public UUID getOwnerUUID();

    // 获取副本状态
    public DungeonState getState();

    // 设置副本状态
    public void setState(DungeonState state);

    // 获取玩家数量
    public int getPlayerCount();

    // 检查是否公开
    public boolean isPublic();

    // 检查玩家是否允许加入
    public boolean isAllowed(UUID playerUUID);

    // 添加允许的玩家
    public void addAllowedPlayer(UUID playerUUID);

    // 移除允许的玩家
    public void removeAllowedPlayer(UUID playerUUID);

    // 获取剩余时间
    public long getRemainingTime();
}
```

### 副本管理器类

```java
public class DungeonManager {
    // 创建副本
    public boolean createDungeon(Player player, String templateName);

    // 删除副本
    public boolean deleteDungeon(String dungeonId);

    // 加入副本
    public boolean joinDungeon(Player player, String dungeonId);

    // 离开副本
    public boolean leaveDungeon(Player player);

    // 获取玩家数据
    public PlayerData getPlayerData(UUID playerUUID);

    // 获取副本实例
    public DungeonInstance getDungeon(String dungeonId);

    // 获取所有活动副本
    public Map<String, DungeonInstance> getActiveDungeons();

    // 获取冷却管理器
    public CooldownManager getCooldownManager();

    // 获取死亡管理器
    public DeathManager getDeathManager();

    // 获取通关管理器
    public CompletionManager getCompletionManager();

    // 获取奖励管理器
    public RewardManager getRewardManager();
}
```

## 扩展开发

### 创建自定义通关条件

1. 创建一个实现 `CompletionCondition` 接口的类
2. 实现所有必要的方法
3. 在 `CompletionManager` 中注册你的条件

示例：

```java
public class CustomCondition implements CompletionCondition {
    private final SagaDungeons plugin;
    private boolean completed = false;

    public CustomCondition(SagaDungeons plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean check(DungeonInstance instance) {
        return completed;
    }

    @Override
    public String getDescription() {
        return "自定义条件描述";
    }

    @Override
    public CompletionType getType() {
        return CompletionType.CUSTOM;
    }

    @Override
    public void reset() {
        completed = false;
    }

    @Override
    public double getProgress() {
        return completed ? 1.0 : 0.0;
    }

    @Override
    public String getProgressDescription() {
        return completed ? "已完成" : "未完成";
    }

    @Override
    public void handleEvent(Player player, String event, Object data) {
        // 处理事件
        if ("custom_event".equals(event)) {
            completed = true;
        }
    }
}
```

### 创建自定义奖励

1. 创建一个实现 `DungeonReward` 接口的类
2. 实现所有必要的方法
3. 在 `RewardManager` 中注册你的奖励

示例：

```java
public class CustomReward implements DungeonReward {
    private final SagaDungeons plugin;

    public CustomReward(SagaDungeons plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean give(Player player) {
        // 给予奖励
        player.sendMessage("你获得了自定义奖励！");
        return true;
    }

    @Override
    public String getDescription() {
        return "自定义奖励描述";
    }

    @Override
    public RewardType getType() {
        return RewardType.CUSTOM;
    }
}
```

### 创建自定义创建条件

1. 创建一个实现 `DungeonRequirement` 接口的类
2. 实现所有必要的方法
3. 在 `RequirementManager` 中注册你的条件

示例：

```java
public class CustomRequirement implements DungeonRequirement {
    private final SagaDungeons plugin;

    public CustomRequirement(SagaDungeons plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean check(Player player) {
        // 检查条件
        return player.getHealth() > 10;
    }

    @Override
    public String getDescription() {
        return "需要生命值大于10";
    }

    @Override
    public RequirementType getType() {
        return RequirementType.CUSTOM;
    }
}
```

## API 使用

### 获取插件实例

```java
SagaDungeons plugin = (SagaDungeons) Bukkit.getPluginManager().getPlugin("SagaDungeons");
```

### 获取副本管理器

```java
DungeonManager dungeonManager = plugin.getDungeonManager();
```

### 检查玩家是否在副本中

```java
PlayerData playerData = dungeonManager.getPlayerData(player.getUniqueId());
boolean inDungeon = playerData.isInDungeon();
```

### 获取玩家当前副本

```java
String dungeonId = playerData.getCurrentDungeonId();
DungeonInstance dungeon = dungeonManager.getDungeon(dungeonId);
```

### 创建副本

```java
boolean success = dungeonManager.createDungeon(player, templateName);
```

### 加入副本

```java
boolean success = dungeonManager.joinDungeon(player, dungeonId);
```

### 离开副本

```java
boolean success = dungeonManager.leaveDungeon(player);
```

## 事件监听

SagaDungeons 没有提供自定义事件，但你可以监听 Bukkit 事件并检查玩家是否在副本中：

```java
@EventHandler
public void onPlayerMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();

    // 获取插件实例
    SagaDungeons plugin = (SagaDungeons) Bukkit.getPluginManager().getPlugin("SagaDungeons");
    if (plugin == null) {
        return;
    }

    // 获取玩家数据
    PlayerData playerData = plugin.getDungeonManager().getPlayerData(player.getUniqueId());

    // 检查玩家是否在副本中
    if (playerData.isInDungeon()) {
        // 玩家在副本中
        String dungeonId = playerData.getCurrentDungeonId();
        DungeonInstance dungeon = plugin.getDungeonManager().getDungeon(dungeonId);

        // 处理副本内的移动事件
    }
}
```