# SagaDungeons 更新日志

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
