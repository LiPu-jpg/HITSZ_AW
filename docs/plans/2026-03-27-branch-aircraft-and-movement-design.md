# 首 Boss 转职机型、后续成长与非瞬移移动 设计文档

## 背景

当前项目已经具备：

- 房间制联机与权威服务器
- 章节推进、Boss 战、白光过渡
- Boss 后个人升级选择

但还有三个结构问题：

1. 玩家在大厅阶段就要选技能，这和“初始统一机型、首 Boss 后再分支”的目标不一致。
2. 当前移动是客户端发送绝对坐标，服务器直接 `setPosition(x, y)`，飞机会瞬移到鼠标位置。
3. 后续希望接入的三种主武器差异较大：
   - 速度机激光
   - 防御机散射
   - 重轰机空爆
   继续沿用当前“通用技能 + 通用升级项”的模型会越来越混乱。

用户已经整理好最终素材，目录位于 [最终素材](/Users/jiaoziang/AircraftWar-base1.0/最终素材)。其中本轮直接相关的素材包括：

- [初始飞机.png](/Users/jiaoziang/AircraftWar-base1.0/最终素材/初始飞机.png)
- [速度分支.png](/Users/jiaoziang/AircraftWar-base1.0/最终素材/速度分支.png)
- [防御分支.png](/Users/jiaoziang/AircraftWar-base1.0/最终素材/防御分支.png)
- [重轰分支.png](/Users/jiaoziang/AircraftWar-base1.0/最终素材/重轰分支.png)
- [背景1-大草原.jpg](/Users/jiaoziang/AircraftWar-base1.0/最终素材/背景1-大草原.jpg)
- [背景2-砂石之地.jpg](/Users/jiaoziang/AircraftWar-base1.0/最终素材/背景2-砂石之地.jpg)
- [背景3-河源工厂.jpg](/Users/jiaoziang/AircraftWar-base1.0/最终素材/背景3-河源工厂.jpg)
- [背景4-熔岩火山.jpg](/Users/jiaoziang/AircraftWar-base1.0/最终素材/背景4-熔岩火山.jpg)
- [背景5-天域.jpg](/Users/jiaoziang/AircraftWar-base1.0/最终素材/背景5-天域.jpg)
- [普通子弹-友.png](/Users/jiaoziang/AircraftWar-base1.0/最终素材/普通子弹-友.png)
- [爆炸子弹-友.png](/Users/jiaoziang/AircraftWar-base1.0/最终素材/爆炸子弹-友.png)

## 目标

- 所有玩家以统一蓝色初始机开局，不在大厅提前选技能。
- 击败首个 Boss 后，玩家各自从三条分支中选择一种机型与主武器路线。
- 后续 Boss 后只做分支内成长，不再换主机型。
- 玩家移动从“瞬移到目标点”改为“服务器按速度上限追踪目标点”。
- 保持权威服务器结算，不把武器逻辑或移动判定下放到客户端。

## 非目标

- 本轮不一次性做完整成长树的所有数值。
- 本轮不把所有武器都做成完全可配置脚本。
- 本轮不重做房间协议的基础结构，只在现有协议上扩展分支与移动语义。

## 方案对比

### 方案 A：最小改动

- 保留大厅技能选择
- 首 Boss 后只是把技能替换成机型
- 继续使用当前瞬移移动

优点：

- 改动最小。

缺点：

- 机型和技能边界继续混乱。
- 移动手感问题完全没解决。
- 后续激光、空爆和分支升级会越来越难接。

### 方案 B：分层建模

- 开局统一 `STARTER_BLUE`
- 首 Boss 后进入 `BRANCH_SELECTION`
- 分支固定机型、主武器、主技能
- 后续 Boss 后进入分支内成长
- 移动改成目标点追踪

优点：

- 和当前需求完全一致。
- 改动可控，且后续可持续扩展。

缺点：

- 需要扩展快照、阶段状态和玩家运行时状态。

### 方案 C：全面策略化

- 机体、武器、技能、移动全部拆成策略对象并动态装配

优点：

- 扩展性最好。

缺点：

- 对当前项目阶段过重。

## 选择

采用方案 B。

## 核心设计

### 1. 玩家机型与阶段流转

新增机型分支枚举：

- `STARTER_BLUE`
- `RED_SPEED`
- `GREEN_DEFENSE`
- `BLACK_HEAVY`

游戏主流程改为：

1. `LOBBY`
2. `BATTLE`
3. 首个 Boss 击败后进入 `BRANCH_SELECTION`
4. 所有玩家各自完成分支选择
5. 进入下一章节 `BATTLE`
6. 后续 Boss 击败后进入 `UPGRADE_SELECTION`

约束：

- 所有玩家开局统一为 `STARTER_BLUE`
- `STARTER_BLUE` 无主动技能
- `BRANCH_SELECTION` 只出现一次
- 一旦分支确定，本局不再更换主机型

### 2. 三条分支定义

#### STARTER_BLUE

- 素材：[初始飞机.png](/Users/jiaoziang/AircraftWar-base1.0/最终素材/初始飞机.png)
- 武器：基础普通弹
- 主动技能：无
- 定位：首章过渡机

#### RED_SPEED

- 素材：[速度分支.png](/Users/jiaoziang/AircraftWar-base1.0/最终素材/速度分支.png)
- 主武器：前向激光
- 机体特性：最高移速、较低血量
- 主技能：激光强化
- 后续成长方向：
  - 激光持续时间
  - 激光宽度
  - 扫射角速度
  - 冷却缩短
  - 机动性增强

#### GREEN_DEFENSE

- 素材：[防御分支.png](/Users/jiaoziang/AircraftWar-base1.0/最终素材/防御分支.png)
- 主武器：扇形散射
- 机体特性：高生存、中等速度
- 主技能：护盾或控场强化
- 后续成长方向：
  - 散射弹数
  - 散射角度
  - 护盾强度/持续
  - 防御
  - 最大生命

#### BLACK_HEAVY

- 素材：[重轰分支.png](/Users/jiaoziang/AircraftWar-base1.0/最终素材/重轰分支.png)
- 主武器：空爆弹
- 机体特性：最慢移动、最高范围爆发
- 主技能：重轰爆破
- 后续成长方向：
  - 空爆半径
  - 空爆伤害
  - 最大射程
  - 装填速度
  - 二次爆炸或子母弹

### 3. 移动模型

当前移动问题：

- 客户端拖动鼠标时发送绝对坐标
- 服务器直接把飞机坐标设置为目标值
- 表现为瞬移

改造方案：

- 客户端仍然发送鼠标目标点
- 服务端不再直接改当前位置，而是保存 `targetX / targetY`
- 每个 tick 服务端按机型移动参数推进飞机：
  - `moveSpeed`
  - `acceleration`，可选
  - `stopRadius`
- 飞机到达目标点附近后停止

这样可以同时满足：

- 不瞬移
- 联机下仍由服务器权威同步
- 不同分支可以有不同机动手感

### 4. 武器模型

#### 激光

激光不继续伪装成普通子弹，而是独立建模：

- 服务端维护激光状态对象
- 激光有持续时间、角度、宽度、冷却
- 碰撞按线段或带宽线段判定
- 客户端单独绘制激光束

#### 散射

散射仍然使用子弹体系：

- 多发扇形子弹
- 随升级增加弹数、角度或分层

#### 空爆

空爆弹为独立的投射物类型：

- 发射后朝目标点飞行
- 到达目标点或达到最大射程后自动空爆
- 爆炸对半径内敌人造成范围伤害
- 客户端绘制爆炸特效

### 5. 后续成长模型

首 Boss 后的选择从“通用升级项”改为“转职分支选择”。

后续 Boss 后继续保留个人成长，但升级池改为按分支发放：

- 红机只看到红机升级项
- 绿机只看到绿机升级项
- 黑机只看到黑机升级项

当前已有的通用升级项：

- `FIRE_RATE`
- `BULLET_POWER`
- `SPREAD_SHOT`
- `LIGHT_TRACKING`

将被拆分或重命名为分支内升级项，而不再作为所有机型共享的固定集合。

### 6. 协议与状态变更

#### GamePhase

新增：

- `BRANCH_SELECTION`

#### PlayerRuntimeState

新增或调整：

- `aircraftBranch`
- `branchUnlocked`
- `branchSelectionPending`
- `branchUpgradeLevels`
- `moveTargetX`
- `moveTargetY`

#### PlayerSnapshot

新增：

- `aircraftBranch`
- `branchUnlocked`
- `availableBranchChoices`
- `availableUpgradeChoices`

#### WorldSnapshot

新增或调整：

- `gamePhase = BRANCH_SELECTION`
- 当前章节是否已完成首次转职

### 7. 启动页与大厅调整

当前启动页仍有技能选择区，这和新设计冲突。

调整后：

- 启动页只保留：
  - `Create / Join`
  - 房间号输入
  - 房主难度选择
- 删除大厅阶段的技能选择
- 机型与技能在战中由分支选择决定

### 8. 素材映射

本轮素材映射建议如下：

- 初始机： [初始飞机.png](/Users/jiaoziang/AircraftWar-base1.0/最终素材/初始飞机.png)
- 红色速度机： [速度分支.png](/Users/jiaoziang/AircraftWar-base1.0/最终素材/速度分支.png)
- 绿色防御机： [防御分支.png](/Users/jiaoziang/AircraftWar-base1.0/最终素材/防御分支.png)
- 黑色重轰机： [重轰分支.png](/Users/jiaoziang/AircraftWar-base1.0/最终素材/重轰分支.png)
- 基础友方弹： [普通子弹-友.png](/Users/jiaoziang/AircraftWar-base1.0/最终素材/普通子弹-友.png)
- 重轰空爆弹： [爆炸子弹-友.png](/Users/jiaoziang/AircraftWar-base1.0/最终素材/爆炸子弹-友.png)
- 背景按章节使用：
  - `CH1` -> [背景1-大草原.jpg](/Users/jiaoziang/AircraftWar-base1.0/最终素材/背景1-大草原.jpg)
  - `CH2` -> [背景2-砂石之地.jpg](/Users/jiaoziang/AircraftWar-base1.0/最终素材/背景2-砂石之地.jpg)
  - `CH3` -> [背景3-河源工厂.jpg](/Users/jiaoziang/AircraftWar-base1.0/最终素材/背景3-河源工厂.jpg)
  - `CH4` -> [背景4-熔岩火山.jpg](/Users/jiaoziang/AircraftWar-base1.0/最终素材/背景4-熔岩火山.jpg)
  - `CH5` -> [背景5-天域.jpg](/Users/jiaoziang/AircraftWar-base1.0/最终素材/背景5-天域.jpg)

## 测试策略

### 服务端

- 首 Boss 前所有玩家统一为初始机测试
- 首 Boss 后进入 `BRANCH_SELECTION` 测试
- 每个玩家可独立选择分支测试
- 目标点移动不再瞬移测试
- 激光/散射/空爆的权威结算测试

### 客户端

- 分支选择覆盖层显示测试
- 机型素材切换测试
- 空爆与激光渲染测试
- 鼠标移动下本地显示平滑跟随测试

### 联调

- 双客户端同房间：
  - 统一初始机开局
  - 打过首 Boss
  - 各自选择不同分支
  - 继续战斗且状态不串线

## 风险与对策

### 风险 1：首个 Boss 后既要切章又要转职，阶段判断容易混乱

对策：

- 明确把“首 Boss 后三选一”定义为 `BRANCH_SELECTION`
- 后续普通成长才走 `UPGRADE_SELECTION`

### 风险 2：激光和空爆都不适合硬塞进现有普通子弹模型

对策：

- 单独引入激光状态对象和空爆投射物类型
- 不强行复用普通子弹的全部行为

### 风险 3：移动从瞬移改为追踪后，客户端会感觉“跟手性变差”

对策：

- 服务器移动速度、停止半径做成可调参数
- 客户端继续发送目标点，保留鼠标操作习惯

## 结论

后续实现应按以下顺序推进：

1. 删除大厅技能选择，恢复统一蓝色初始机
2. 改造目标点追踪移动，移除瞬移行为
3. 新增 `BRANCH_SELECTION` 和分支状态
4. 接入三条分支的基础机型、武器和素材
5. 把后续 Boss 升级池改成按分支下发

