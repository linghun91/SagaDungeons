# SagaDungeons 开发者指南

## 简介

SagaDungeons 是一个简洁高效的 Minecraft 副本管理系统插件，专注于副本的创建、管理和基础通关功能。插件采用模块化设计，提供清晰的代码结构和易于扩展的架构。

**v1.2.1.51 重大更新**: 项目已完成大幅简化优化，移除了复杂的经济系统集成（Vault、PlayerPoints），专注于核心副本管理功能，提供更纯净、高效的副本体验。系统现在仅依赖权限检查和等级门槛，极大简化了部署和维护成本。

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
│   │   ├── EditCommand.java (编辑命令)
│   │   ├── ForceCloseCommand.java (强制关闭副本命令)
│   │   ├── GUICommand.java (GUI管理命令)
│   │   ├── SetBackCommand.java (设置遣返点命令)
│   │   ├── SetItemCommand.java (设置物品命令)
│   │   ├── SetSpawnCommand.java (设置重生点命令)
│   │   ├── SetWorldCommand.java (设置世界命令)
│   │   └── SpawnerAdminCommand.java (刷怪点管理命令)
│   └── player (玩家命令)
│       ├── CreateCommand.java (创建副本命令)
│       ├── InviteCommand.java (邀请玩家命令)
│       ├── JoinCommand.java (加入副本命令)
│       ├── KickCommand.java (踢出玩家命令)
│       ├── LeaveCommand.java (离开副本命令)
│       ├── ListCommand.java (列出副本命令)
│       ├── PublicCommand.java (公开副本命令)
│       └── StatsCommand.java (统计信息命令)
├── config (配置管理)
│   ├── ConfigManager.java (配置管理器)
│   ├── DebugMessageManager.java (调试消息管理器)
│   ├── GUILanguageManager.java (GUI语言管理器)
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
│   ├── condition (创建条件 - 已简化)
│   │   ├── DungeonRequirement.java (副本要求接口)
│   │   ├── LevelRequirement.java (等级要求)
│   │   ├── RequirementManager.java (要求管理器)
│   │   └── RequirementType.java (要求类型枚举 - 仅LEVEL和PERMISSION)
│   ├── cooldown (冷却管理)
│   │   └── CooldownManager.java (冷却管理器)
│   ├── death (死亡管理)
│   │   └── DeathManager.java (死亡管理器)
│   # 奖励系统和经济条件检测已移除，专注于核心副本管理功能
│   └── trigger (触发器系统)
│       ├── DungeonTrigger.java (触发器接口)
│       ├── LevelJumpTrigger.java (关卡跳转触发器)
│       └── TriggerManager.java (触发器管理器)
├── event (事件监听)
│   ├── AbstractListener.java (监听器抽象类)
│   ├── ChatInputListener.java (聊天输入监听器)
│   ├── CommandBlockListener.java (命令方块监听器)
│   ├── CompletionListener.java (通关条件监听器)
│   ├── InventoryListener.java (物品栏监听器)
│   ├── PlayerListener.java (玩家监听器)
│   ├── TeleportSecurityListener.java (传送安全监听器)
│   └── WorldListener.java (世界监听器)
├── gui (图形界面)
│   ├── AbstractGUI.java (GUI抽象类)
│   ├── DungeonManageGUI.java (副本管理GUI)
│   ├── GUIManager.java (GUI管理器)
│   ├── ItemBuilder.java (物品构建器)
│   ├── ItemConditionManageGUI.java (物品条件管理GUI - 已废弃)
│   ├── MobSpawnerGUI.java (刷怪点GUI)
│   ├── PlayerInviteGUI.java (玩家邀请GUI)
│   ├── SpawnerAddGUI.java (刷怪点添加GUI)
│   ├── SpawnerEditGUI.java (刷怪点编辑GUI)
│   ├── TemplateBasicEditGUI.java (模板基础编辑GUI)
│   ├── TemplateCompletionEditGUI.java (模板通关条件编辑GUI)
│   ├── TemplateConditionsEditGUI.java (模板创建条件编辑GUI - 仅等级条件)
│   ├── TemplateEditMainGUI.java (模板编辑主GUI)
│   ├── TemplateSelectGUI.java (模板选择GUI)
│   └── TemplateSpawnersEditGUI.java (模板刷怪点编辑GUI)
├── hook (插件集成 - 已精简)
│   ├── HookManager.java (集成管理器)
│   ├── MultiverseHook.java (Multiverse集成)
│   ├── MultiverseWorldListener.java (Multiverse世界监听器)
│   ├── MythicMobsHook.java (MythicMobs集成)
│   └── PlaceholderAPIHook.java (PlaceholderAPI集成)
│   # VaultHook.java 和 PlayerPointsHook.java 已移除
├── manager (管理器)
│   ├── DungeonSecurityManager.java (副本安全管理器)
│   ├── MobSpawnerManager.java (怪物生成管理器)
│   └── WorldManager.java (世界管理器)
├── metrics (统计系统)
│   └── Metrics.java (bStats统计)
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
├── config.yml (主配置文件 - 已精简，移除经济配置)
├── debugmessage.yml (调试消息配置)
├── lang_gui.yml (GUI界面语言配置)
├── lang_gui_en.yml (GUI界面英文语言配置)
├── messages.yml (消息配置)
├── messages_en.yml (英文消息配置)
├── plugin.yml (插件信息 - 移除Vault和PlayerPoints软依赖)
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

### 触发器接口

```java
public interface DungeonTrigger {
    boolean checkCondition(DungeonInstance instance, Player player);
    void execute(DungeonInstance instance, Player player);
    ConfigurationSection getConfig();
}
```

### 创建条件接口（已大幅简化）

```java
public interface DungeonRequirement {
    boolean check(Player player);
    String getFailMessage(Player player);
    boolean take(Player player);
    RequirementType getType();
}
```

**重要变更**: 创建条件系统已大幅简化，现在仅支持：
- **等级条件**: 检查玩家等级
- **权限条件**: 检查玩家权限

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

    // 奖励管理器已移除

    // 获取触发器管理器
    public TriggerManager getTriggerManager();

    // 获取活跃副本数量（用于统计）
    public int getActiveDungeonCount();
}
```

**重大变更**: DungeonManager 现在专注于权限检查和等级门槛：
- ✅ **权限检查**：`sagadungeons.command.create` 或模板特定权限
- ✅ **等级检查**：可选的等级要求检查
- ✅ **冷却检查**：副本创建冷却时间检查
- ✅ **状态检查**：玩家是否已在副本中检查

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

### 创建自定义创建条件（仅支持等级和权限）

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
        // 检查条件（现在只推荐等级和权限相关的检查）
        return player.getLevel() >= 20; // 示例：检查等级
    }

    @Override
    public String getFailMessage(Player player) {
        return "需要等级达到20级";
    }

    @Override
    public boolean take(Player player) {
        // 对于等级检查，通常不需要"扣除"
        return check(player);
    }

    @Override
    public RequirementType getType() {
        return RequirementType.LEVEL; // 只支持 LEVEL 和 PERMISSION
    }
}
```

**注意**: 由于系统简化，现在只支持 `RequirementType.LEVEL` 和 `RequirementType.PERMISSION` 两种类型。

### 创建自定义触发器

1. 创建一个实现 `DungeonTrigger` 接口的类
2. 实现所有必要的方法
3. 在 `TriggerManager` 中注册你的触发器

示例：

```java
public class CustomTrigger implements DungeonTrigger {
    private final SagaDungeons plugin;
    private final ConfigurationSection config;

    public CustomTrigger(SagaDungeons plugin, ConfigurationSection config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public boolean checkCondition(DungeonInstance instance, Player player) {
        // 检查触发条件
        return instance.getState() == DungeonState.COMPLETED;
    }

    @Override
    public void execute(DungeonInstance instance, Player player) {
        // 执行触发器动作
        player.sendMessage("自定义触发器被执行！");
    }

    @Override
    public ConfigurationSection getConfig() {
        return config;
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

### 获取触发器管理器

```java
TriggerManager triggerManager = dungeonManager.getTriggerManager();
```

### 检查并执行触发器

```java
triggerManager.checkAndExecuteTriggers(dungeonInstance, player);
```

### 获取统计数据

```java
// 获取活跃副本数量
int activeDungeonCount = dungeonManager.getActiveDungeonCount();

// 获取可用模板数量
int templateCount = plugin.getConfigManager().getTemplateManager().getTemplateCount();
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

## 系统特性

### bStats 统计系统

SagaDungeons 集成了 bStats 统计系统，用于收集插件使用数据，帮助开发者了解插件使用情况：

- **插件ID**: 26069
- **统计页面**: https://bstats.org/plugin/bukkit/SagaDungeons
- **收集数据**: 活跃副本数量、可用模板数量、集成的外部插件（MythicMobs、PlaceholderAPI）、使用的语言
- **隐私保护**: 使用官方 bStats-bukkit 3.0.2 版本，确保数据安全和隐私保护

### 触发器系统

触发器系统允许在特定条件下自动执行动作：

- **关卡跳转触发器**: 副本完成后自动跳转到下一关卡
- **自定义触发器**: 支持开发者创建自定义触发器
- **条件检查**: 支持复杂的触发条件组合
- **延迟执行**: 支持延迟触发器执行

### 插件集成（已精简）

**MythicMobs 集成**:
- **怪物生成**: 支持 MythicMobs 自定义怪物的副本内生成
- **刷怪点管理**: 完整的 MythicMobs 刷怪点配置和管理
- **通关条件**: 支持击杀特定 MythicMobs 怪物的通关条件

**PlaceholderAPI 集成**:
- **占位符支持**: 提供丰富的副本相关占位符
- **动态显示**: 实时显示副本状态、进度等信息

**Multiverse 集成**:
- **世界管理**: 自动处理副本世界的创建和删除
- **冲突避免**: 防止 Multiverse 导入临时副本世界
- **兼容性**: 确保与 Multiverse 的完全兼容

### 通关条件系统

专注于简洁高效的单一通关条件：

- **击杀全部条件**: 击杀副本中的所有怪物
- **击杀数量条件**: 击杀指定数量的怪物
- **击杀特定条件**: 击杀特定类型的怪物
- **到达区域条件**: 玩家到达指定位置

### 增强的管理员命令

提供完整的管理员命令集，方便服务器管理：

- `/sd admin forceclose <副本ID/all>` - 强制关闭指定副本或所有副本
- `/sd admin gui` - 打开管理员GUI界面
- `/sd admin setspawn <模板名称>` - 设置副本模板的重生点
- `/sd admin spawner <set/remove/list>` - 管理 MythicMobs 刷怪点
- `/sd admin createtemplate <模板名称>` - 创建新的副本模板
- `/sd admin copyworld <源世界> <目标世界>` - 复制世界文件
- `/sd admin edit <模板名称>` - 编辑模板配置

## 技术特性

### 模块化设计

- **抽象类优先**: 使用抽象类实现模块化编程，提高代码可维护性
- **统一方法原则**: 避免重复造轮子，统一调用相同功能的方法
- **简洁性原则**: 代码结构简洁，专注核心功能，易于维护
- **扩展性**: 易于添加新功能和扩展现有功能

### 性能优化

- **异步处理**: 世界复制和文件操作使用异步处理
- **内存管理**: 及时清理不再使用的资源
- **缓存机制**: 合理使用缓存提高性能
- **线程安全**: 确保多线程环境下的数据安全
- **依赖精简**: 移除不必要的经济系统依赖，减少内存占用

### 兼容性

- **Paper API**: 优先使用 Paper API 原生方法
- **向后兼容**: 保持与旧版本的兼容性
- **精简集成**: 仅集成核心必要插件（MythicMobs, PlaceholderAPI）
- **多语言**: 支持中英文等多种语言
- **轻量部署**: 移除 Vault 和 PlayerPoints 依赖，简化服务器部署

## 版本变更说明

### v1.2.1.51 主要变更

**移除的功能**:
- ✅ 完全移除 Vault 经济系统集成
- ✅ 完全移除 PlayerPoints 点券系统集成
- ✅ 移除所有金币/点券/物品条件检测
- ✅ 清理相关配置文件和消息文本
- ✅ 移除 build.gradle 中的相关依赖

**保留的功能**:
- ✅ 基础单一通关条件功能完整
- ✅ 单一触发器系统（LevelJumpTrigger等）正常
- ✅ 副本核心功能（创建、管理、加入、离开）正常
- ✅ MythicMobs 和 PlaceholderAPI 集成保持完整

**技术改进**:
- ✅ 代码维护性大幅提升，移除复杂嵌套逻辑
- ✅ 项目复杂度显著降低，聚焦核心功能
- ✅ 构建性能优化，减少不必要的依赖和类文件
- ✅ 功能纯净度提高，高度专注于副本核心管理功能