# 尺寸解耦、章节场景与 Boss 后升级 设计文档

## 背景

当前项目已经完成了房间制联机、权威服务器、章节前置的 Boss 与基础技能系统，但还有两个结构性问题：

1. 飞机、子弹、道具的碰撞尺寸和渲染尺寸大量依赖图片原始宽高，导致后续替换素材时，逻辑体积也会被动变化。
2. 场景推进仍然主要基于 `difficulty + bossActive`，还没有形成“章节 -> Boss -> 升级 -> 下一章”的正式关卡流。

这会直接限制后续的地图更换、敌机皮肤替换、Boss 套装扩展和升级分支设计。

## 目标

- 把英雄机、全部敌机、子弹、道具的尺寸从图片素材中解耦。
- 让服务器只负责权威逻辑尺寸，客户端按配置尺寸缩放绘制。
- 引入章节驱动的场景系统，让地图、小怪素材、Boss 素材和 Boss 风格按章节推进。
- 让 Boss 击败后进入统一的升级选择阶段，并允许每个玩家各自单独选择升级方向。
- 保持“难度只决定刷怪率、升级速度、血量上限”的约束不变。

## 非目标

- 本轮不直接实现全部章节素材和全部升级分支，只先把架构和第一批能力搭起来。
- 本轮不改网络模型，仍然保持权威服务器 + 快照同步。
- 本轮不把所有内容外部化为 JSON 脚本，先以 Java 配置类实现。

## 总体方案

采用“章节驱动 + 尺寸配置 + 分阶段状态机”的方案：

- `GameplayBalance` 继续负责战斗数值，如刷怪率、伤害、CD、得分、升级阈值。
- 新增独立的尺寸配置，统一维护逻辑碰撞尺寸和客户端显示尺寸。
- 新增章节配置，统一维护每章背景、敌机素材主题、Boss 素材主题、Boss 触发阈值和下一章关系。
- 服务端维护 `GamePhase`、`chapterId`、`bossStage`、`upgradeSelectionOpen` 等权威状态，并通过快照同步给客户端。
- 客户端不再根据图片原始分辨率绘制实体，而是按实体配置尺寸缩放绘制。

## 方案对比

### 方案 A：只抽尺寸，不引入章节状态

优点：
- 改动小，见效快。

缺点：
- 场景推进和 Boss 后升级仍然要再拆一次。
- 后续换地图和敌机皮肤时，流程层面还是不够稳定。

### 方案 B：尺寸 + 章节 + 升级阶段一起建模

优点：
- 一次把素材、逻辑尺寸、章节推进、升级阶段的边界划清。
- 后续增加地图和 Boss 成本最低。

缺点：
- DTO、快照和客户端 HUD 改动面会更大。

### 方案 C：直接做外部脚本化章节系统

优点：
- 灵活度最高。

缺点：
- 对当前课设过重，维护和调试成本高。

## 选择

采用方案 B。

原因：

- 这是当前项目从“可运行联机框架”升级到“可持续扩展关卡系统”的最合适节点。
- 用户已明确后续要继续更换地图、Boss、小怪和攻击方式，仅靠局部打补丁会很快失控。

## 核心设计

### 1. 尺寸系统

每个实体都拆成两套尺寸：

- `collisionWidth / collisionHeight`
  - 服务器权威判碰撞使用
  - 不依赖图片原始分辨率
- `renderWidth / renderHeight`
  - 客户端绘制时使用
  - 图片会被缩放到这个尺寸

这样可以保证：

- 更换高分辨率或低分辨率素材时，碰撞范围不被动变化
- 逻辑体型和视觉体型可以独立控制
- 同一个敌机类型在不同章节可以维持统一逻辑体积，但替换不同素材风格

### 2. 配置拆分

保留 [GameplayBalance.java](/Users/jiaoziang/AircraftWar-base1.0/modules/server/src/edu/hitsz/server/GameplayBalance.java) 作为战斗数值配置。

新增两个配置层：

- `EntitySizing`
  - 英雄机、小怪、精英、Boss、子弹、道具的逻辑碰撞尺寸和默认渲染尺寸
- `ChapterCatalog`
  - 每章背景、敌机素材主题、Boss 素材主题、Boss 阈值、下一章关系、升级面板显示文案

这样后续调整“数值”和“视觉/尺寸”不会再挤在同一个类里。

### 3. 场景/章节模型

新增权威状态：

- `chapterId`
- `gamePhase`
- `bossStage`
- `upgradeSelectionOpen`
- `flashUntilMillis`

推荐的阶段流转：

1. `LOBBY`
2. `BATTLE`
3. `BOSS_WARNING`
4. `BATTLE` 中的 Boss 战
5. `UPGRADE_SELECTION`
6. 下一章 `BATTLE`

章节切换规则采用混合式：

- 总分达到当前章节阈值时触发 Boss
- 击败 Boss 后进入升级选择
- 全员完成选择后正式切下一章

### 4. 升级选择模型

Boss 击败后，所有在线玩家进入同一个暂停阶段，但每个玩家单独选择升级方向。

首批支持的升级方向：

- `FIRE_RATE`
- `BULLET_POWER`
- `SPREAD_SHOT`
- `LIGHT_TRACKING`

服务器权威保存每个玩家的升级树或升级计数，客户端只负责展示候选项和发送选择结果。

### 5. 客户端渲染策略

客户端不再直接：

- 用 `image.getWidth()/getHeight()` 作为实体显示尺寸
- 用图片原始高度决定英雄机初始 Y 坐标

改为：

- 从尺寸配置读取 `renderWidth/renderHeight`
- `Graphics.drawImage(...)` 时显式传缩放宽高
- 背景、敌机、Boss 皮肤根据 `chapterId + enemyType + bossActive` 选图

### 6. 服务端职责

服务端继续负责：

- 逻辑碰撞尺寸
- 刷怪
- Boss 触发
- 升级阶段开启与关闭
- 升级结果应用
- 快照同步

客户端不负责：

- 章节推进
- Boss 触发
- 升级结算
- 碰撞判定

## 协议变更

### WorldSnapshot 需要新增

- `gamePhase`
- `chapterId`
- `chapterTransitionFlash`
- `upgradeSelectionOpen`
- `availableUpgradeChoices` 或按玩家拆到 `PlayerSnapshot`

### PlayerSnapshot 需要新增

- `maxHp`
- `selectedUpgradeChoice`
- `availableUpgradeChoices`
- 可能的强化等级摘要，如 `fireRateLevel`、`bulletPowerLevel`

### 新消息类型

- `INPUT_UPGRADE_CHOICE`

用于玩家在升级阶段提交自己的强化方向。

## 测试策略

### 服务端

- 尺寸配置生效测试
- 碰撞不再依赖图片大小测试
- Boss 击破后进入升级阶段测试
- 所有玩家选完后推进下一章测试

### 协议层

- `chapterId / gamePhase / upgrade choices` 的 JSON round-trip 测试

### 客户端

- 缩放绘制尺寸测试
- 章节背景切换测试
- Boss 击破白光状态展示测试
- 升级面板状态展示测试

### 联调

- 双客户端同房间打到 Boss -> 升级 -> 切下一章 smoke test

## 风险与对策

### 风险 1：尺寸和素材切换同时改，回归面大

对策：
- 先引入尺寸配置，再切换绘制逻辑，最后再接章节素材。

### 风险 2：升级阶段会打断原有联机快照流程

对策：
- 先把 `gamePhase` 接入快照，让客户端进入纯展示态，再补选择输入。

### 风险 3：章节状态可能和 Boss 触发逻辑互相耦合

对策：
- 用独立 `ChapterProgressionState` 管理章节与阶段，避免散落在 `ServerWorldState` 中。

## 结论

后续实现应分三层推进：

1. 尺寸解耦
2. 章节与快照状态接入
3. Boss 后升级选择和章节切换

这样可以先解决“素材尺寸绑逻辑”的根问题，再平滑扩到地图切换和升级分支，而不会把现有联机架构重新打散。
