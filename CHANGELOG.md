# SagaDungeons 更新日志

## 版本 1.2.0.91 (2025-06-05)

### 性能优化

- 优化了副本世界创建的异步处理机制
  - 将世界创建操作(`WorldCreator.createWorld()`)从主线程移至异步线程执行
  - 实现了完整的异步流程：文件复制 → 世界创建 → 切回主线程设置属性
  - 显著减少了主线程阻塞时间，特别是在Bukkit/Spigot服务端上的性能提升明显
  - 保持了线程安全，世界属性设置仍在主线程中进行

### 代码优化

- 重构了`WorldManager.createDungeonWorld()`方法的执行流程
  - 确保项目中只有一处处理世界创建且使用异步
  - 优化了异常处理机制，为世界创建添加了专门的错误处理逻辑
  - 保持了创建锁机制，防止并发创建副本导致服务器过载

## 版本 1.2.0.71 (2025-06-05)

### 新增功能

- 实现了GUI多语言系统
  - 创建了GUILanguageManager类，专门管理GUI界面的多语言支持
  - 添加了lang_gui.yml（中文）和lang_gui_en.yml（英文）配置文件
  - 修改了AbstractGUI基类，添加多语言支持方法和辅助工具
  - 成功改造了9个主要GUI类，支持根据config.yml的lang配置自动加载对应语言文件
  - 包含200+条GUI文本配置，覆盖所有界面元素

### 功能改进

- 优化了GUI文本管理系统
  - 所有GUI硬编码中文消息改为从配置文件读取
  - 支持动态占位符替换，如模板名称、玩家名称等
  - 实现了语言文件回退机制，确保系统稳定性
  - 添加了便捷的辅助方法，简化GUI类的多语言实现


## 版本 1.2.0.66 (2025-06-05)

### 问题修复

- 修复了GUI编辑功能与模板配置文件不同步的问题
  - 解决了GUI中修改创建条件开关后配置文件未更新的问题
  - 在所有GUI保存操作后添加了模板重新加载机制，确保内存与配置文件同步
  - 修复了创建副本时仍使用旧配置的问题，现在会使用最新的配置文件内容
  - 涉及13个文件的修改：9个GUI类和4个命令类

### 技术改进

- 在所有`saveTemplate()`调用后添加`reloadTemplate()`确保数据一致性
- 优化了模板配置的实时同步机制，提高了配置修改的可靠性
- 遵循统一方法原则，在所有相关位置应用相同的重新加载逻辑

## 版本 1.2.0.30 (2025-06-05)

### 新增功能

- 实现了玩家离线重连副本状态恢复系统
  - 解决了玩家在副本内离线后再次上线导致的状态不一致问题
  - 添加了`handlePlayerReconnection()`方法处理玩家重新连接时的副本状态检查
  - 实现了智能状态恢复机制：自动检测玩家是否在副本世界并恢复相应状态
  - 添加了孤立玩家处理：当玩家在已失效的副本世界时自动传送到安全位置

### 功能改进

- 增强了`PlayerListener.onPlayerJoin()`事件处理
  - 延迟1tick检查玩家状态，确保玩家完全加载后再进行状态检查
  - 添加了多种重连场景的处理逻辑：状态恢复、权限验证、孤立清理
- 扩展了`DungeonManager`类功能
  - 添加了`findDungeonByWorldName()`方法根据世界名称查找对应副本实例
  - 将`setPlayerGameMode()`方法改为公有，支持状态恢复时的游戏模式设置
- 完善了副本安全系统
  - 重连时自动验证玩家权限，无权限玩家将被安全移除
  - 支持公开副本、私有副本、创建者等多种权限验证

### 消息系统更新

- 在`messages.yml`中添加了重连相关消息配置
  - `dungeon.reconnect.restored`: 玩家状态恢复成功消息
  - `dungeon.reconnect.rejoined`: 玩家重新加入副本消息
  - `dungeon.reconnect.orphaned`: 玩家被从失效副本中移除消息

### 问题修复

- 修复了玩家在副本内离线后再次上线时的状态不一致问题
- 修复了重连后玩家游戏模式不正确的问题
- 修复了重连后玩家缺少合法副本进入权限的问题
- 修复了孤立玩家可能滞留在已删除副本世界的问题


## 版本 1.2.0.13 (2025-06-04)

### 新增功能

- 实现了自定义遣返点功能
  - 添加了`/sd admin setback`命令，管理员可设置非法玩家遣返位置
  - 创建了SetBackCommand类处理遣返点设置
  - 增强了DungeonSecurityManager，优先使用自定义遣返点，包含安全位置检查
  - 在config.yml中添加了security.custom-kickback-location配置项

## 版本 1.1.0.99 (2025-06-04)

### 新增功能

- 实现了副本安全进入控制系统
  - 添加了指令黑名单功能，阻止玩家在副本中使用非法传送指令
  - 实现了合法身份检测机制，只允许通过项目合法方式进入副本
  - 创建了DungeonSecurityManager类统一管理副本安全功能

## 版本 1.1.0.98 (2025-06-04)

### 新增功能

- 完成了GUI管理系统开发
  - 实现了EditCommand和TemplateEditMainGUI主界面
  - 完成了基础信息编辑GUI(TemplateBasicEditGUI)
  - 完成了创建条件编辑GUI(TemplateConditionsEditGUI)
  - 完成了奖励系统编辑GUI(TemplateRewardsEditGUI)
  - 完成了通关条件和刷怪点编辑GUI(TemplateCompletionEditGUI/TemplateSpawnersEditGUI)

### 功能改进

- 实现了完整的聊天输入监听系统
  - 支持数值、文本、小数输入验证
  - 集成AsyncPlayerChatEvent确保Bukkit兼容性
  - 使用BukkitScheduler确保线程安全
- 完成了配置数据读取系统
  - 所有GUI界面现在显示配置文件中的实际数据
  - 支持实时配置读取和界面更新
- 实现了高级管理界面系统
  - 物品奖励管理界面(ItemRewardManageGUI)
  - 命令奖励管理界面(CommandRewardManageGUI)
  - 时间奖励管理界面(TimeRewardManageGUI)
  - 物品条件管理界面(ItemConditionManageGUI)
  - 组合条件管理界面(CompositeConditionManageGUI)
- 完成了刷怪点管理系统
  - 刷怪点编辑功能(SpawnerEditGUI)
  - 刷怪点传送功能
  - 刷怪点添加功能(SpawnerAddGUI)
- 实现了位置设置系统
  - 到达区域位置设置功能

## 版本 1.0.9.7 (2025-06-03)

### 新增功能

- 集成了bStats统计系统
  - 添加了插件使用数据收集功能，帮助开发者了解插件使用情况
  - 实现了自定义统计图表：活跃副本数量、可用模板数量、集成的外部插件、使用的语言
  - 使用官方bStats-bukkit 3.0.2版本，确保数据安全和隐私保护
  - 插件ID：26069，可在 https://bstats.org/plugin/bukkit/SagaDungeons 查看统计数据

### 技术改进

- 在build.gradle中添加了bStats依赖和shadow插件重定位配置
  - 使用`implementation 'org.bstats:bstats-bukkit:3.0.2'`添加依赖
  - 配置shadow插件将bStats重定位到`cn.i7mc.sagadungeons.libs.bstats`包下，避免与其他插件冲突
- 在SagaDungeons主类中添加了bStats初始化方法
  - 在onEnable方法中调用initializeMetrics()方法
  - 添加了四个自定义统计图表收集插件使用数据
- 为DungeonManager和TemplateManager类添加了统计方法
  - 添加了`getActiveDungeonCount()`方法统计活跃副本数量
  - 添加了`getTemplateCount()`方法统计可用模板数量

### 数据收集说明

- **活跃副本数量**：实时统计当前服务器上运行的副本实例数量
- **可用模板数量**：统计服务器配置的副本模板总数
- **集成的外部插件**：显示已成功集成的外部插件（Vault、PlayerPoints、MythicMobs、PlaceholderAPI）
- **使用的语言**：统计插件配置的语言设置（中文/英文等）

### 隐私保护

- bStats收集的数据完全匿名，不包含任何服务器或玩家的个人信息
- 所有数据仅用于统计分析，帮助改进插件功能和性能
- 服务器管理员可以通过bStats配置文件禁用数据收集

## 版本 1.0.9.6 (2025-06-03)

### 新增功能

- 实现了副本通关后的视觉效果系统
  - 添加了通关后烟花效果：玩家每3秒放3个随机样式的烟花，持续到传送离开副本
  - 实现了Title大标题显示：显示"恭喜你通关副本"和倒计时小标题
  - 添加了倒计时提示系统：最后3秒前每5秒提示，最后3秒每1秒提示
  - 使用Bukkit原生API处理Title显示，确保bukkit服务端兼容性

### 功能改进

- 优化了通关后的用户体验
  - 将硬编码的时间值移至config.yml配置文件，支持自定义配置
  - 实现了可配置的倒计时系统，管理员可自定义提示间隔和传送延迟
  - 所有提示消息在消息配置文件中可自定义，避免硬编码

### 问题修复

- 修复了通关消息配置结构问题
  - 解决了消息key路径不一致的问题：统一使用'dungeon.completion.success'路径
  - 修正了英文消息文件messages_en.yml中completion节点位置错误的问题
  - 确保中英文消息文件结构一致，避免"消息未找到"错误

### 配置更新

- 在config.yml中添加了通关相关的时间配置
  ```yaml
  completion:
    teleport-delay: 10  # 通关后传送延迟（秒）
    countdown-interval: 5  # 倒计时提示间隔（秒）
    final-countdown: 3  # 最后倒计时开始时间（秒）
    firework-interval: 3  # 烟花发射间隔（秒）
  ```

- 在消息配置文件中添加了通关相关消息
  - 倒计时提示消息：`dungeon.completion.countdown`
  - 最后倒计时消息：`dungeon.completion.final-countdown`
  - Title显示消息：`dungeon.completion.title`和`dungeon.completion.subtitle`


## 版本 1.0.9.4 (2025-05-24)

### 新增功能

- 实现了基于时间的提前通关奖励系统
  - 添加了 `TimeReward` 奖励类，支持根据完成时间给予不同奖励
  - 在 `DungeonTemplate` 中添加了 `timeRewards` 配置支持，使用 `TreeMap` 智能匹配最佳时间奖励
  - 扩展了 `TemplateManager` 的 `parseTimeString()` 方法，支持多种时间格式：`3600`（秒）、`"1h"`（小时）、`"90m"`（分钟）、`"30s"`（秒）、`"1d"`（天）
  - 在 `RewardManager` 中添加了 `giveTimeRewards()` 方法，处理时间奖励的给予和消息显示
  - 修改了 `DungeonInstance.handleCompletion()` 方法，自动计算完成时间并给予相应的时间奖励

### 功能特点

- **智能时间匹配**：使用 `TreeMap.floorEntry()` 方法自动选择符合条件的最佳时间奖励
- **灵活的时间格式**：支持纯数字秒数和带单位的时间格式，自动解析转换
- **完整的消息系统**：在完成消息中显示用时，专门的时间奖励消息提示
- **向后兼容**：不影响现有的基础奖励系统，可选功能，完全遵循项目的模块化架构

### 配置示例

```yaml
# 时间奖励配置 - 根据完成时间给予不同奖励
timeRewards:
  # 1小时内完成 - 最高奖励
  "1h":
    commands:
      - "give %player% diamond 5"
      - "eco give %player% 1000"
      - "say %player% 在1小时内完成了副本，获得钻石奖励！"

  # 1.5小时内完成 - 中等奖励
  "90m":
    commands:
      - "give %player% gold_ingot 10"
      - "eco give %player% 500"
      - "say %player% 在1.5小时内完成了副本，获得金锭奖励！"

  # 2小时内完成 - 基础奖励
  "2h":
    commands:
      - "give %player% iron_ingot 20"
      - "eco give %player% 200"
      - "say %player% 在2小时内完成了副本，获得铁锭奖励！"
```

### 消息配置更新

- 更新了 `messages.yml` 中的完成消息，添加了用时显示
- 添加了时间奖励相关的消息配置：`dungeon.reward.time.header`、`dungeon.reward.time.description`、`dungeon.reward.time.footer`
- 更新了副本模板示例 `templates/example/config.yml`，添加了时间奖励配置示例

## 版本 1.0.9.3 (2025-05-18)

### 问题修复

- 修复了使用`/sd admin delete`命令删除副本时世界卸载失败的问题
  - 实现了完整的世界卸载和删除流程：1.将玩家传送出世界 2.保存世界 3.延迟20tick 4.卸载世界 5.再延迟20tick 6.删除世界文件
  - 添加了适当的延迟机制，确保每个操作都有足够的时间完成，避免NoSuchFileException错误
  - 保留了玩家传送和实体清理步骤，确保安全删除
- 修复了复制世界文件时`session.lock`和`uid.dat`文件导致的问题
  - 在文件复制过程中添加了对`session.lock`文件的过滤，避免复制被锁定的文件
  - 在文件复制过程中添加了对`uid.dat`文件的过滤，避免创建的副本世界被识别为重复世界
  - 在文件计数和大小统计过程中也忽略这些特殊文件，确保进度计算准确

### 性能优化

- 优化了副本世界生成过程，显著减少主线程卡顿
  - 添加了创建锁机制，防止多个副本同时创建导致服务器过载
  - 使用Java 17的新特性优化文件操作，提高文件复制和删除效率
  - 改进了异步文件复制机制，使用Files.walk和Stream API替代传统的递归方法
  - 优化了世界加载参数，减少资源消耗
  - 添加了详细的性能日志，记录文件操作和世界加载耗时
  - 使用最新的Paper 1.20.1 API，如WorldCreator的keepSpawnLoaded方法和GameRule枚举

### 代码优化

- 重构了WorldManager类中的世界卸载和删除相关方法
  - 改进了deleteDungeonWorld方法，实现分步骤的世界卸载和删除流程
  - 优化了deleteWorldFolder方法，添加了世界保存、延迟卸载和延迟删除机制
  - 统一了世界删除逻辑，确保代码简洁高效且操作安全可靠
  - 添加了更详细的调试日志，记录世界卸载和删除的每个步骤
- 优化了BukkitFileUtil类中的文件操作方法
  - 改进了文件删除逻辑，使用倒序排序确保先删除文件和子目录，再删除父目录
  - 优化了文件复制过程，分两步进行：先创建所有目录结构，再复制所有文件
  - 添加了对特殊文件的过滤机制，避免复制被锁定的文件
  - 将所有硬编码的日志信息移至debugmessage.yml文件中，使其可自定义

### 功能改进

- 改进了服务器关闭时的副本世界清理机制，确保不会留下残留世界
- 优化了世界创建过程，添加了更多游戏规则设置，如关闭火焰蔓延、随机刻等
- 改进了世界加载参数配置，减少资源消耗和服务器负载
- 添加了创建锁状态检查和提示消息，当有副本正在创建时，其他玩家会收到友好提示
- 增强了世界卸载和删除机制，添加了延迟处理和状态检查，确保操作安全可靠
- 添加了对英文版消息文件的支持，修复了"admin-close"消息键缺失的问题

## 版本 1.0.9 (2025-05-17)

### 代码优化

- 优化了指令结构，将刷怪点管理指令从玩家指令移至管理员指令
  - 创建了新的`SpawnerAdminCommand`类，用于管理副本刷怪点
  - 修改了`AdminCommand`类，添加了对`SpawnerAdminCommand`的调用
  - 更新了指令帮助信息，反映了新的指令结构
  - 添加了`getCurrentDungeonId`方法到`DungeonManager`类，用于获取玩家当前所在副本ID

### 功能改进

- 改进了指令权限管理，确保只有管理员可以管理刷怪点
- 保持了与现有功能的兼容性，刷怪点管理功能不变，只是移动了指令位置

## 版本 1.0.8 (2025-05-17)

### 功能改进

- 优化了MythicMobs怪物生成机制
  - 添加了`MobSpawnerManager`类，负责管理副本中的怪物生成
  - 在`MythicMobsHook`类中添加了`spawnMob`方法，用于直接生成怪物
  - 修改了`SpawnerCommand`类，使用新的方法生成怪物
  - 在`DungeonManager`类中添加了代码，在创建副本世界成功后初始化刷怪点，并在删除副本时清理刷怪点

### 问题修复

- 修复了命令设置的副本MythicMobs怪物出生点会在MythicMobs插件目录生成持久化保存的出生点配置文件的问题
- 修复了玩家进入副本后怪物不刷新的问题，通过改进位置解析和延迟初始化刷怪点解决
- 修复了刷怪点位置错误保存临时副本世界名称（如"sd_001-fba"）的问题，现在只保存坐标信息
- 修复了位置字符串包含模板名称（如"fba,x,y,z,yaw,pitch"）导致无法正确解析的问题
- 改进了怪物生成逻辑，不再依赖持久化的刷怪点配置文件
- 优化了副本关闭时的怪物清理机制，确保不会留下残留实体

### 代码优化

- 实现了更高效的怪物生成方式，提高了性能
- 简化了位置解析逻辑，移除了冗余代码，保持高可读性
- 完全移除了调试日志输出，使代码更加简洁
- 修改了`SpawnerCommand`类，使用`LocationUtil.locationToStringWithoutWorld`方法保存位置，避免保存临时副本世界名
- 增强了`MobSpawnerManager`类中的位置解析逻辑，能够处理多种格式的位置字符串
- 支持一次性刷怪点和周期性刷怪点，满足不同的需求
- 遵循模块化、可视化、统一化和简约化原则，简化了怪物生成流程

## 版本 1.0.7 (2025-05-17)

### 新增功能

- 实现了副本触发系统
  - 添加了 `DungeonTrigger` 接口，支持自定义触发器实现
  - 实现了 `LevelJumpTrigger` 类，用于处理副本关卡跳转
  - 添加了 `TriggerManager` 类，用于管理和执行触发器
  - 支持触发器优先级和条件组合
  - 优化了触发器执行逻辑，确保线程安全

### 功能改进

- 优化了副本关卡切换机制，使用触发器系统替代原有的直接跳转
- 改进了触发器条件检查逻辑，支持更复杂的条件组合
- 增强了触发器执行的可配置性，支持自定义执行顺序和优先级

### 配置示例

```yaml
triggers:
  level_jump:
    type: LEVEL_JUMP
    priority: 1
    conditions:
      - type: MOB_KILL
        count: 10
      - type: REACH_AREA
        location: "world,100,64,100"
        radius: 3.0
    actions:
      - type: JUMP_TO_LEVEL
        target_level: 2
```

## 版本 1.0.6 (2025-05-17)

### 新增功能

- 增强了副本通关条件系统
  - 添加了条件组合模式（AND/OR）支持
  - 实现了条件优先级系统
  - 支持多级条件组合
  - 优化了条件进度显示

### 功能改进

- 优化了通关条件的配置结构，支持更灵活的条件组合
- 改进了条件进度计算逻辑，为AND/OR模式提供不同的进度计算方式
- 增强了条件描述系统，提供更详细的进度信息

### 配置示例

```yaml
completion:
  # 组合条件示例
  composite:
    type: AND  # 或 OR
    priority: 1
    conditions:
      condition1:
        killCount:
          count: 10
      condition2:
        reachArea:
          location: "world,100,64,100"
          radius: 3.0
```

## 版本 1.0.5 (2025-05-17)

### 修复问题

- 修复了GUI创建副本失败的问题，现在GUI创建副本和指令创建副本使用相同的消息路径和参数
- 修复了"消息未找到: admin-close"的错误，通过正确识别YAML文件中的消息层次结构并更新代码中的路径引用为"command.death.admin-close"

## 版本 1.0.4 (2025-05-17)

### 新增功能

- 添加了 `/sd admin gui` 命令，用于打开GUI管理界面
- 在 `messages.yml` 中添加了相关的命令帮助和提示信息

### 修复问题

- 修复了服务器启动时删除残留副本世界失败的问题
- 修复了管理员使用 `/sd admin delete <副本ID>` 命令删除副本时，无法成功删除副本世界文件夹的问题
- 修复了 `dungeon.admin-close` 消息未找到的问题，确保玩家在被管理员关闭副本时能收到正确的提示

### 代码优化

- 优化了 `WorldManager` 类中的世界删除逻辑，添加了文件夹存在性检查和延迟删除机制
- 改进了 `deleteDungeonWorld` 和 `deleteWorldFolder` 方法，确保在主线程中执行删除操作
- 增强了日志记录，添加了更详细的操作信息和错误提示
- 遵循模块化、可视化、统一化和简约化原则，简化了世界删除流程

## 版本 1.0.3 (2025-05-17)

### 修复问题

- 修复了使用 `/sd admin delete <副本ID>` 删除副本后，仍然可以通过 `/sd join <副本ID>` 加入该副本的问题
- 优化了 `WorldManager` 类中关于卸载和删除世界的代码，简化为三个清晰步骤：1.将玩家传送出世界 2.卸载世界 3.删除世界文件
- 改进了 `DungeonManager.deleteDungeon()` 方法，立即从活动副本列表中移除副本实例，并设置副本状态为 `DELETING`
- 增强了 `JoinCommand` 和 `DungeonManager.joinDungeon()` 方法，添加对副本状态的检查，防止玩家加入正在删除或已完成的副本
- 在 `messages.yml` 中添加了新的消息键 `command.join.dungeon-not-available`，用于提示玩家副本不可用

### 代码优化

- 重构了 `WorldManager` 类，将复杂的 `cleanupRemnantWorlds` 方法拆分为多个职责单一的方法
- 创建了 `teleportPlayersOutOfWorld` 方法专门处理玩家传送
- 创建了 `cleanupLoadedDungeonWorlds` 和 `cleanupUnloadedDungeonFolders` 方法分别处理已加载和未加载的副本世界
- 创建了 `tryLoadAndDeleteWorld` 和 `deleteWorldFolder` 方法处理世界加载和删除
- 减少了代码嵌套层级，提高了可读性和可维护性

## 版本 1.0.2 (2025-05-17)

### 修复问题

- 修复了副本模板中重生点坐标包含临时副本世界名的问题，现在只保存坐标信息（X, Y, Z, Yaw, Pitch）而不包含世界名
- 修复了命令帮助信息不完整的问题，补充了缺失的命令介绍，包括`/sd leave`、`/sd help`和`/sd admin help`等命令
- 修复了`SetSpawnCommand`类中的重生点保存逻辑，确保在不同副本实例中能正确使用模板中的重生点
- 修复了`DeathManager`类中的重生逻辑，现在玩家死亡后会正确传送到模板中设置的重生点

### 功能改进

- 优化了`LocationUtil`类，添加了`locationToStringWithoutWorld`和`stringToLocationWithoutWorld`方法，用于处理不包含世界名的坐标
- 改进了`DungeonManager`类中的玩家传送逻辑，使用新的坐标处理方法确保玩家能正确传送到副本重生点
- 在`messages.yml`中添加了刷怪点ID提示信息，提升用户体验
- 完善了命令帮助系统，确保所有命令都有对应的帮助信息

## 版本 1.0.1 (2025-05-16)

### 修复问题

- 修复了player_cooldowns.yml文件在服务器重启后不会重置的问题，现在每次插件启动时会自动清除所有玩家的副本创建冷却时间
- 修复了"/sd spawner set"命令的自动补全功能，现在只会显示简洁的刷怪点ID建议（如mob1、boss、spawn1），并添加了提示消息
- 修复了MythicMobsHook类中的API兼容性问题，解决了"setRemainingCooldownSeconds"方法不存在的错误
- 改进了一次性刷怪点的实现方式，现在会尝试多种方法来设置刷怪点只刷新一次
- 添加了更详细的日志输出，便于排查刷怪点相关问题

### 功能改进

- 优化了刷怪点系统，当不指定冷却时间参数时（如"/sd spawner set fb1 野狼王 1"），刷怪点将设置为一次性刷怪点，默认冷却时间为99999999秒
- 当指定冷却时间参数时（如"/sd spawner set fb1 野狼王 1 30"），刷怪点将在怪物被击杀后按指定时间重新刷新
- 在messages.yml中添加了刷怪点命令相关的提示消息，支持自定义配置

## 版本 1.0.0 (2025-05-16)

### 新增功能

- 副本系统核心功能
  - 副本创建与管理
  - 副本世界复制与加载
  - 副本超时与自动清理
  - 玩家传送与位置记录
  - 副本公开/私有设置
  - 玩家权限管理

- 副本创建条件系统
  - 金币条件 (Vault)
  - 点券条件 (PlayerPoints)
  - 物品条件
  - 等级条件
  - 自定义条件接口

- 副本通关系统
  - 全部击杀条件
  - 到达区域条件
  - 击杀特定怪物条件
  - 击杀数量条件
  - 自定义条件接口

- 副本奖励系统
  - 金币奖励
  - 点券奖励
  - 物品奖励
  - 经验奖励
  - 命令奖励
  - 自定义奖励接口

- 副本死亡管理系统
  - 死亡次数限制
  - 达到限制后的踢出处理
  - 自定义复活道具系统
  - 复活点设置与传送

- 玩家数据持久化
  - 玩家数据模型设计
  - 数据保存与加载
  - 统计数据收集

- 指令系统
  - `/sd create <模板>` - 创建一个副本
  - `/sd list` - 列出所有副本
  - `/sd stats [玩家]` - 查看副本统计数据
  - `/sd invite <玩家>` - 邀请玩家加入副本
  - `/sd join <ID/玩家名>` - 加入一个副本
  - `/sd leave` - 离开当前副本
  - `/sd kick <玩家>` - 将玩家踢出副本
  - `/sd public` - 切换副本公开/私有状态
  - `/sd spawner <set/remove/list>` - 管理刷怪点
  - `/sd help` - 显示帮助信息
  - `/sd reload` - 重新加载配置
  - `/sd admin reload` - 重新加载配置
  - `/sd admin delete <副本ID>` - 删除指定副本
  - `/sd admin tp <副本ID>` - 传送到指定副本
  - `/sd admin list` - 列出所有副本
  - `/sd admin createtemplate <模板名称> [世界路径]` - 创建新的副本模板
  - `/sd admin setworld <模板名称>` - 将当前世界设置为副本模板的世界
  - `/sd admin copyworld <模板名称> <世界名称>` - 将指定世界复制到副本模板的世界目录
  - `/sd admin setitem <模板名称> <类型> [数量]` - 将手持物品设置为副本的条件或奖励
  - `/sd admin forceclose <副本ID/all>` - 强制关闭指定副本或所有副本
  - `/sd admin setspawn <模板名称>` - 将当前位置设置为副本模板的重生点
  - `/sd admin help` - 显示管理员帮助信息

- 插件集成
  - Vault 集成 (经济系统)
  - PlayerPoints 集成 (点券系统)
  - MythicMobs 集成 (怪物系统)
  - PlaceholderAPI 集成 (变量系统)

### 优化

- 异步世界复制，避免服务器卡顿
- 使用读写锁机制保护副本实例数据
- 实现玩家位置记录与恢复机制
- 所有耗时操作在异步线程执行
- 使用线程安全的集合类
- 使用反射机制兼容不同版本的MythicMobs API

### 文档

- 用户指南
  - 玩家命令指南
  - 管理员命令指南
  - 配置文件说明
  - 副本通关条件与奖励配置说明
  - 死亡限制与复活道具配置说明

- 开发者指南
  - API使用说明
  - 扩展开发指南
  - 代码结构说明
  - 自定义通关条件与奖励开发指南
  - 自定义死亡处理与复活机制开发指南

## 版本计划

### 版本 1.1.0

- 副本排行榜系统
- 多阶段副本支持
- 副本难度调整
- 自定义副本事件触发器

### 版本 1.2.0

- 副本统计系统增强
- 副本成就系统
- 副本限时挑战模式
- GUI界面功能扩展
