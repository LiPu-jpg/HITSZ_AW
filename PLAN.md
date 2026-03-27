# AircraftWar Authority Server And Skill System Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

> **Current Layout Note:** 协议层现已迁移到 `modules/common/src`，非协议测试已拆到 `modules/server/test` 与 `modules/client-desktop/test`。当前仓库的统一编译命令应使用：
>
> `javac -encoding UTF-8 $(find src modules/common/src modules/server/src modules/client-desktop/src modules/server/test modules/client-desktop/test test -name '*.java') -d /tmp/aircraftwar-build`

**Goal:** 将当前单机版 AircraftWar 重构为“客户端本地渲染 + 权威服务器统一结算”的多人架构，并补齐冻结、炸弹、护盾三类技能，以及它们随玩家等级增长的数值体系。

**Architecture:** 客户端只负责输入采集、快照消费和 Swing 渲染；服务器维护 `PlayerSession`、权威世界状态、敌机生成、碰撞、掉落、技能和升级。协议层先抽象消息包络、快照 DTO、编解码器和传输接口，通信格式统一 JSON，后续再落到 Java Socket。

**Tech Stack:** Java 8+/Swing，运行时使用 Java Socket，消息体为 JSON；当前代码已托管到 GitHub，仍未引入 Maven/Gradle，验证方式以 `javac` + `java -ea` 为主。

---

## 0. Progress Update

- Plan last refreshed on `2026-03-27`.
- Repository: `https://github.com/LiPu-jpg/HITSZ_AW`
- Completed:
  - Hero/OtherPlayer 模型拆分
  - 协议包络、JSON codec、Socket 传输层
  - Session 注册与 `SessionID` 校验
  - 房间制多人模型：创建房间 / 加入房间 / 6 位房号 / 房主开局
  - 每个房间独立 `ServerWorldState`、独立快照、独立进度与 Boss 状态
  - 服务端技能系统：冻结、炸弹、护盾、等级缩放
  - 本地 authority server 启动链路
  - 单人模式走本地 authority server
  - 玩家、敌机、子弹、道具、分数进入 `WorldSnapshot`
  - 客户端按快照渲染完整战斗对象
  - `common/server/client-desktop` 多模块目录已建立并可编译
  - 协议层已迁入 `modules/common`
  - 服务端运行时已迁入 `modules/server`
  - 桌面客户端运行时已迁入 `modules/client-desktop`
  - `src/edu/hitsz/application` 已收敛为兼容入口壳
  - 客户端渲染实体树已迁入 `modules/client-desktop/src/edu/hitsz/client/{aircraft,bullet,basic}`
  - 服务端权威实体树已迁入 `modules/server/src/edu/hitsz/server/{aircraft,bullet,basic}`
  - 旧共享实体树 `src/edu/hitsz/{aircraft,bullet,basic}` 已删除
  - 章节状态已接入权威服务器：`chapterId / gamePhase / chapterTransitionFlash`
  - 快照协议已同步章节、升级候选、已选升级和玩家最大生命值
  - 客户端场景已切为章节驱动：`CH1 / CH2 / CH3 / boss`
  - Boss 击败后已进入 `UPGRADE_SELECTION`，并由每个玩家单独提交升级方向
  - 升级输入已经是服务器权威时钟判定，不再信任客户端时间戳
  - 死亡玩家已从客户端可渲染飞机列表中移除
  - 本地运行目录已刷新：`/tmp/aircraftwar-scene-runtime`
- Verified:
  - 本地 socket authority smoke test 通过
  - 本地 runtime session test 通过
  - 延迟入场 fresh-start smoke test 通过
  - 多客户端广播 smoke test 通过
  - 断线隐藏 / 同会话重连恢复 smoke test 通过
  - 大厅准备后开局 smoke test 通过
  - 大厅难度/技能选择 smoke test 通过
  - 房间隔离 smoke test 通过
  - 房主独占开局 smoke test 通过
  - 分数升级测试通过
  - 难度缩放测试通过
  - Boss 触发测试通过
  - 启动页默认选择测试通过
  - 难度背景切换测试通过
  - 敌机弹幕梯度测试通过
  - 敌机分层解锁测试通过
  - `game over` / 死亡后同 `sessionId` 重连复位 smoke test 通过
  - 服务端快照内容测试通过
  - 技能与回归测试通过
  - 章节快照协议 round-trip 测试通过
  - 章节状态到客户端快照流水线测试通过
  - 升级覆盖层显示与白光阻塞测试通过
  - 升级选择阻塞移动/技能测试通过
  - 已提交后断线不阻塞切章测试通过
  - 忽略客户端伪造未来时间戳的升级提交 smoke test 通过
- Remaining:
  - 服务端碰撞尺寸仍通过素材尺寸读取，未来可继续抽成无图片依赖的纯数值配置
  - 非法房号 / 入房失败的客户端提示仍可继续补强
  - 当前章节视觉仍基于现有素材做映射和染色，后续可继续替换为每章独立敌机/Boss 美术资源
  - 升级分支已接入权威状态与基础战斗参数，后续可继续加强独立 HUD 表现和更丰富的弹道差异

## 1. Current Status

- 当前项目已经是“本地客户端 + 本地 authority server”的房间制联机骨架，而不再是纯本地单机。
- 客户端已新增独立启动页：选择 `Create Room` 或 `Join Room`。
- 创建房间时：
  - 房主选择房间难度 `EASY / NORMAL / HARD`
  - 房主选择自己的技能 `FREEZE / BOMB / SHIELD`
- 加入房间时：
  - 玩家手动输入房号
  - 玩家只选择自己的技能
- 当前开局语义已经调整为“房间大厅 -> 玩家 ready -> 房主 start -> 全灭回房间大厅”。
- 战斗背景已经切为章节分层：
  - 启动页 `bg.jpg`
  - `CH1 -> bg2.jpg`
  - `CH2 -> bg3.jpg`
  - `CH3 -> bg4.jpg`
  - `Boss 阶段 -> bg5.jpg`
- `HeroAircraft` 已完成 DCL 单例化，只代表当前本地操作者；`OtherPlayer` 已存在并可由快照生成。
- `Main` 默认会启动 `LocalAuthorityServer`，然后让 Swing 客户端通过 `SocketClientSession` 连接本地 server。
- 单人模式也已经走服务器：本地鼠标移动与技能按键会先发命令，再由服务器推进世界并返回快照。
- `LocalAuthorityServer` 已不再维护单一全局大厅，而是协调多个 `RoomRuntime`。
- 每个 `RoomRuntime` 拥有：
  - 独立 `ServerWorldState`
  - 独立 `ServerGameLoop`
  - 独立房间难度、房主、ready 状态、总分和 Boss 进度
- `WorldSnapshot` 当前已包含：
  - 玩家
  - 敌机
  - 我方子弹
  - 敌方子弹
  - 道具
  - 本地玩家分数
  - 当前章节
  - 当前阶段
  - 章节白光标记
  - 玩家升级候选项与已选升级
- `DefaultSnapshotApplier` 已能把完整快照恢复为客户端渲染态，`Game` 在联机模式下按快照更新玩家、敌机、子弹、道具和分数。
- `Game` 当前已经收敛为“输入转发 + 快照应用 + 渲染”的客户端视图层，不再承担敌机生成、碰撞、掉落和伤害结算。
- 服务端 `ServerWorldState` 当前已接管：
  - 敌机生成
  - 玩家自动射击
  - 敌机自动射击
  - 子弹移动
  - 敌机移动
  - 子弹/飞机/道具碰撞
  - 分数结算
  - 精英敌机掉落
- 技能体系已在服务端生效：
  - 冻结：停全体敌机移动与敌机射击推进
  - 炸弹：群体伤害
  - 护盾：免伤
  - 数值随等级增长
- 进度系统已在服务端生效：
  - 玩家分数驱动升级
  - 升级会增强技能数值，并提高玩家机火力
  - 刷怪节奏会随难度和房间总分提高而加快
  - 房间总分达到阈值后触发 Boss 战
  - 敌机梯度已扩展为 `Mob -> Elite -> ElitePlus -> Ace -> Boss`
  - 不同敌机层级已有不同弹幕密度与散射方式
  - 章节流转已生效：`CH1 -> boss -> white flash -> per-player upgrade -> next chapter`
- 升级选择系统已在服务端和客户端闭环：
  - `FIRE_RATE / BULLET_POWER / SPREAD_SHOT / LIGHT_TRACKING`
  - 只有白光结束后服务器才接受升级提交
  - 所有存活玩家提交后才推进下一章
  - 选择结果会回写到玩家快照
- 道具体系已在服务端闭环：
  - `BloodSupply` 回血
  - `FireSupply` / `FirePlusSupply` 提升火力
  - `BombSupply` / `FreezeSupply` 已进入掉落池并在拾取时触发对应技能
- 实体树已完成双向拆分：
  - 客户端只保留渲染实体 `modules/client-desktop/src/edu/hitsz/client/{aircraft,bullet,basic}`
  - 服务端只保留权威实体 `modules/server/src/edu/hitsz/server/{aircraft,bullet,basic}`
- 当前仍存在的限制：
  - 房间内快照已改为按房间成员定向发送，而不是全局广播
  - 服务端已改为“至少有一名在线玩家后才推进世界”，更贴近课设的入场开局语义
  - 服务端当前采用房间大厅模型：客户端按 `Enter` 切换 ready，房主按 `S` / `START_GAME` 开局；全员阵亡后自动回到本房间大厅并重置轮次状态
  - 难度已经是严格房间级配置；只有房主可以修改
  - 本地 authority server 当前采用“创建/加入房间、断线隐藏、同 `sessionId` + `HELLO` 重连恢复、超时清理”的轻量会话生命周期
  - 服务端碰撞尺寸仍依赖图片资源尺寸
  - 当前没有构建工具；JSON 编解码仍是轻量手写实现
  - 章节视觉当前仍复用现有底图和敌机素材，尚未切到每章独立资源包

## 2. Confirmed Design Decisions

### 2.1 Client Side

- `HeroAircraft` 继续保持单例，但只代表“当前本地操作者自己”。
- 新增 `OtherPlayer extends AbstractAircraft`，不使用单例模式。
- `Game` 逐步从“本地战斗逻辑中心”转型为“本地渲染 + 快照应用中心”。
- 客户端维护 `List<AbstractAircraft> playerAircrafts`，其中固定包含：
  - 一个 `HeroAircraft`
  - 多个 `OtherPlayer`
- `HeroController` 负责采集本地鼠标位置和技能输入，并将其作为命令发送到服务器。
- 客户端最终以服务器快照为准，不再把本地 `Game` 视为最终权威。

### 2.2 Server Side

- 服务器端不使用 `HeroAircraft`。
- 服务器维护 `PlayerSession` 列表和权威 `ServerWorldState`。
- 每个连接客户端拥有唯一 `SessionID`。
- 每条客户端指令必须携带 `SessionID`、`sequence`、`timestamp`。
- 服务器只允许某个 `SessionID` 更新自己绑定的玩家状态，不能跨会话控制其他玩家。
- 敌机生成、移动、射击、碰撞、掉落、技能、升级统一在服务器执行。

### 2.3 Skill And Level Model

- 冻结：令全体敌人停止移动 `n` 秒，并暂停其主动射击推进。
- 炸弹：对全体敌人立即造成 `n` 点伤害，由服务器统一处理坠毁、得分和掉落。
- 护盾：令玩家在 `n` 秒内免伤，由服务器忽略该玩家期间受到的伤害。
- `n` 不直接硬编码在技能类里，统一从等级配置对象读取。
- 第一阶段推荐最小抽象：
  - `SkillType`
  - `SkillScalingConfig`
  - `PlayerSkillState`
  - `WorldEffectState`
  - `ServerSkillResolver`

### 2.4 Networking Model

- 采用“权威服务器快照同步”。
- 当前快照策略为“服务端广播统一世界状态，客户端按本地 `sessionId` 识别 `HeroAircraft`”。
- 第一阶段不做客户端预测、回滚、插值和复杂反作弊。
- 先抽象协议和传输接口，再接 Java Socket。
- 消息格式统一为 JSON。

## 3. Suggested Package Layout

当前仓库正处于“旧 `src` 运行时 + 新多模块目录”并存的迁移期，实际推荐布局已经调整为：

- Shared:
  - `modules/common/src/edu/hitsz/common`
  - `modules/common/src/edu/hitsz/common/protocol`
  - `modules/common/src/edu/hitsz/common/protocol/dto`
  - `modules/common/src/edu/hitsz/common/protocol/json`
  - `modules/common/src/edu/hitsz/common/protocol/socket`
- Server:
  - `modules/server/src/edu/hitsz/server`
  - `modules/server/src/edu/hitsz/server/aircraft`
  - `modules/server/src/edu/hitsz/server/bullet`
  - `modules/server/src/edu/hitsz/server/basic`
  - `modules/server/src/edu/hitsz/server/command`
  - `modules/server/src/edu/hitsz/server/skill`
  - `modules/server/test/edu/hitsz/server`
- Client:
  - `modules/client-desktop/src/edu/hitsz/client`
  - `modules/client-desktop/src/edu/hitsz/client/aircraft`
  - `modules/client-desktop/src/edu/hitsz/client/bullet`
  - `modules/client-desktop/src/edu/hitsz/client/basic`
  - `modules/client-desktop/test/edu/hitsz/client`
  - `modules/client-desktop/test/edu/hitsz/e2e`
- Compatibility:
  - `src/edu/hitsz/application/Main.java`
- Root shared tests:
  - `test/edu/hitsz/protocol`

推荐职责划分：

- `modules/client-desktop/.../{aircraft,bullet,basic}`: 客户端渲染对象
- `modules/server/.../{aircraft,bullet,basic}`: 服务端权威模拟对象
- `modules/common/.../protocol`: 消息包络、消息类型、编解码接口、传输接口、DTO
- `modules/client-desktop`: 桌面客户端逻辑
- `modules/server`: 服务端逻辑
- `modules/server/test` / `modules/client-desktop/test`: 模块级测试入口

## 4. Interface Draft

### 4.1 Transport Boundary

```java
public interface Transport {
    void start();
    void stop();
    void send(ProtocolMessage message);
    void setListener(ProtocolMessageListener listener);
}
```

### 4.2 Codec Boundary

```java
public interface MessageCodec {
    String encode(ProtocolMessage message);
    ProtocolMessage decode(String raw);
}
```

### 4.3 Session Boundary

```java
public final class PlayerSession {
    private final String sessionId;
    private final String playerId;
    private final PlayerRuntimeState playerState;
}
```

### 4.4 Snapshot Boundary

```java
public interface SnapshotApplier {
    void apply(WorldSnapshot snapshot, ClientWorldState state, String localSessionId);
}
```

### 4.5 Skill Boundary

```java
public interface ServerSkillResolver {
    void applySkill(
        SkillType skillType,
        PlayerSession session,
        ServerWorldState worldState,
        long nowMillis
    );
}
```

## 5. JSON Message Draft

### 5.1 Client Move Command

```json
{
  "messageType": "INPUT_MOVE",
  "sessionId": "session-123",
  "sequence": 15,
  "timestamp": 1760000000000,
  "payload": {
    "x": 240,
    "y": 650
  }
}
```

### 5.2 Client Skill Command

```json
{
  "messageType": "INPUT_SKILL",
  "sessionId": "session-123",
  "sequence": 16,
  "timestamp": 1760000000100,
  "payload": {
    "skillType": "FREEZE"
  }
}
```

### 5.3 Server Snapshot Event

```json
{
  "messageType": "WORLD_SNAPSHOT",
  "sessionId": "session-123",
  "sequence": 500,
  "timestamp": 1760000000200,
  "payload": {
    "tick": 1024,
    "players": [],
    "enemies": [],
    "bullets": [],
    "items": [],
    "worldEffects": {
      "enemyFrozenUntil": 1760000002200
    }
  }
}
```

## 6. Non-Goals For The First Milestone

- 不做客户端预测。
- 不做回滚和插值补偿。
- 不做数据库持久化。
- 不做房间大厅和匹配系统。
- 不做复杂反作弊。
- 不做增量同步和二进制协议。

## 7. Implementation Plan

### Task 1: Split Local Hero And Remote Players `(Completed)`

**Files:**
- Create: `src/edu/hitsz/aircraft/OtherPlayer.java`
- Modify: `src/edu/hitsz/application/Game.java`
- Modify: `src/edu/hitsz/application/HeroController.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/PlayerAircraftModelTest.java`

**Step 1: Write the failing test**
- 验证 `HeroAircraft` 仍为单例。
- 验证 `OtherPlayer` 不是单例。
- 验证 `Game` 可以维护玩家集合概念。

**Step 2: Run test to verify it fails**
- `javac -encoding UTF-8 $(find src modules/common/src test modules/client-desktop/test modules/server/test -name '*.java') -d /tmp/aircraftwar-build`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.client.PlayerAircraftModelTest`

**Step 3: Write minimal implementation**
- 新增 `OtherPlayer`，继承 `AbstractAircraft`。
- 给 `Game` 引入 `playerAircrafts` 数据结构。
- 保持 `HeroAircraft` 单例语义不变。

**Step 4: Run test to verify it passes**
- 重新运行同一组编译与测试命令。

### Task 2: Introduce Protocol Envelope And Snapshot DTOs `(Completed)`

**Files:**
- Create: `modules/common/src/edu/hitsz/common/protocol/ProtocolMessage.java`
- Create: `modules/common/src/edu/hitsz/common/protocol/MessageType.java`
- Create: `modules/common/src/edu/hitsz/common/protocol/MessageCodec.java`
- Create: `modules/common/src/edu/hitsz/common/protocol/Transport.java`
- Create: `modules/common/src/edu/hitsz/common/protocol/ProtocolMessageListener.java`
- Create: `modules/common/src/edu/hitsz/common/protocol/dto/InputMovePayload.java`
- Create: `modules/common/src/edu/hitsz/common/protocol/dto/InputSkillPayload.java`
- Create: `modules/common/src/edu/hitsz/common/protocol/dto/WorldSnapshot.java`
- Test: `test/edu/hitsz/protocol/ProtocolEnvelopeTest.java`

**Step 1: Write the failing test**
- 验证消息包络包含 `messageType/sessionId/sequence/timestamp/payload`。
- 验证 `WorldSnapshot` DTO 可被构造与消费。

**Step 2: Run test to verify it fails**
- `javac -encoding UTF-8 $(find src modules/common/src test modules/client-desktop/test modules/server/test -name '*.java') -d /tmp/aircraftwar-build`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.protocol.ProtocolEnvelopeTest`

**Step 3: Write minimal implementation**
- 先定义协议对象和接口，不把 Java Socket 或 JSON 库写死到领域层。

**Step 4: Run test to verify it passes**
- 重新运行同一组编译与测试命令。

### Task 3: Build Server Session Registry And Identity Validation `(Completed)`

**Files:**
- Create: `src/edu/hitsz/application/server/PlayerSession.java`
- Create: `src/edu/hitsz/application/server/SessionRegistry.java`
- Create: `src/edu/hitsz/application/server/ServerCommandRouter.java`
- Create: `src/edu/hitsz/application/server/command/MoveCommand.java`
- Create: `src/edu/hitsz/application/server/command/SkillCommand.java`
- Test: `modules/server/test/edu/hitsz/server/SessionValidationTest.java`

**Step 1: Write the failing test**
- 验证 `SessionRegistry` 可以创建、查找、注销 `PlayerSession`。
- 验证错误 `SessionID` 的命令不会被路由到其他玩家。

**Step 2: Run test to verify it fails**
- `javac -encoding UTF-8 $(find src modules/common/src test modules/client-desktop/test modules/server/test -name '*.java') -d /tmp/aircraftwar-build`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.server.SessionValidationTest`

**Step 3: Write minimal implementation**
- 实现会话注册表。
- 在命令执行前统一做会话合法性校验。

**Step 4: Run test to verify it passes**
- 重新运行同一组编译与测试命令。

### Task 4: Extract Authoritative Server World `(Completed)`

**Files:**
- Create: `src/edu/hitsz/application/server/ServerWorldState.java`
- Create: `src/edu/hitsz/application/server/ServerGameLoop.java`
- Create: `src/edu/hitsz/application/server/WorldSnapshotFactory.java`
- Modify: `src/edu/hitsz/application/Game.java`
- Test: `modules/server/test/edu/hitsz/server/ServerWorldLoopTest.java`

**Step 1: Write the failing test**
- 验证服务端世界 tick 可以推进。
- 验证权威世界能够输出 `WorldSnapshot`。

**Step 2: Run test to verify it fails**
- `javac -encoding UTF-8 $(find src modules/common/src test modules/client-desktop/test modules/server/test -name '*.java') -d /tmp/aircraftwar-build`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.server.ServerWorldLoopTest`

**Step 3: Write minimal implementation**
- 将敌机生成、移动、射击、碰撞、掉落从本地 `Game` 迁移到服务端世界循环。
- 客户端 `Game` 只保留渲染和快照消费相关职责。

**Step 4: Run test to verify it passes**
- 重新运行同一组编译与测试命令。

### Task 5: Add Client Snapshot State And Snapshot Applier `(Completed)`

**Files:**
- Create: `src/edu/hitsz/application/client/ClientWorldState.java`
- Create: `src/edu/hitsz/application/client/SnapshotApplier.java`
- Create: `src/edu/hitsz/application/client/DefaultSnapshotApplier.java`
- Modify: `src/edu/hitsz/application/Game.java`
- Modify: `src/edu/hitsz/application/HeroController.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/SnapshotApplyTest.java`

**Step 1: Write the failing test**
- 验证客户端能把 `WorldSnapshot` 应用到本地渲染态。
- 验证 `HeroAircraft` 和 `OtherPlayer` 可以根据快照更新。

**Step 2: Run test to verify it fails**
- `javac -encoding UTF-8 $(find src modules/common/src test modules/client-desktop/test modules/server/test -name '*.java') -d /tmp/aircraftwar-build`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.client.SnapshotApplyTest`

**Step 3: Write minimal implementation**
- 引入客户端世界状态对象。
- 引入默认快照应用器。
- 让 `HeroController` 转为命令发布者，而不是最终世界状态决定者。

**Step 4: Run test to verify it passes**
- 重新运行同一组编译与测试命令。

### Task 6: Introduce Freeze, Bomb, Shield And Level Scaling `(Completed)`

**Files:**
- Create: `src/edu/hitsz/application/server/skill/SkillType.java`
- Create: `src/edu/hitsz/application/server/skill/SkillScalingConfig.java`
- Create: `src/edu/hitsz/application/server/skill/PlayerSkillState.java`
- Create: `src/edu/hitsz/application/server/skill/WorldEffectState.java`
- Create: `src/edu/hitsz/application/server/skill/ServerSkillResolver.java`
- Modify: `src/edu/hitsz/basic/BombSupply.java`
- Modify: `src/edu/hitsz/basic/FreezeSupply.java`
- Test: `modules/server/test/edu/hitsz/server/SkillScalingTest.java`
- Test: `modules/server/test/edu/hitsz/server/FreezeBombShieldTest.java`

**Step 1: Write the failing tests**
- 验证等级越高，冻结时长、炸弹伤害、护盾时长越大。
- 验证三种技能都能在服务端被正确识别和结算。

**Step 2: Run tests to verify they fail**
- `javac -encoding UTF-8 $(find src modules/common/src test modules/client-desktop/test modules/server/test -name '*.java') -d /tmp/aircraftwar-build`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.server.SkillScalingTest`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.server.FreezeBombShieldTest`

**Step 3: Write minimal implementation**
- 用 `SkillScalingConfig` 统一读取技能数值。
- 用 `WorldEffectState` 管理全局冻结截止时间。
- 用 `PlayerSkillState` 管理玩家护盾和技能状态。
- 让 `BombSupply` / `FreezeSupply` 从“本地占位效果”升级为“服务端技能触发入口”。

**Step 4: Run tests to verify they pass**
- 重新运行同一组编译与测试命令。

### Task 7: Bridge Client And Server With An In-Memory Transport `(Completed In Spirit, Implemented With Socket-Based Smoke Flow)`

**Files:**
- Create: `src/edu/hitsz/application/client/ClientCommandPublisher.java`
- Modify: `src/edu/hitsz/application/client/DefaultSnapshotApplier.java`
- Modify: `src/edu/hitsz/application/server/ServerCommandRouter.java`
- Test: `modules/client-desktop/test/edu/hitsz/e2e/AuthorityFlowSmokeTest.java`

**Step 1: Write the failing test**
- 验证客户端发出的移动/技能指令能通过内存传输到服务端。
- 验证服务端返回快照后客户端能正确消费。

**Step 2: Run test to verify it fails**
- `javac -encoding UTF-8 $(find src modules/common/src test modules/client-desktop/test modules/server/test -name '*.java') -d /tmp/aircraftwar-build`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.e2e.AuthorityFlowSmokeTest`

**Step 3: Write minimal implementation**
- 本实现没有单独落 `InMemoryTransport`，而是直接使用本地 Socket smoke flow 跑通权威链路。
- 当前目标已经达到：客户端发命令，服务端推进世界，客户端消费快照。

**Step 4: Run test to verify it passes**
- 重新运行同一组编译与测试命令。

### Task 8: Plug In Java Socket And JSON Codec `(Completed)`

**Files:**
- Create: `modules/common/src/edu/hitsz/common/protocol/json/JsonMessageCodec.java`
- Create: `modules/common/src/edu/hitsz/common/protocol/socket/SocketClientTransport.java`
- Create: `modules/common/src/edu/hitsz/common/protocol/socket/SocketServerTransport.java`
- Create: `modules/common/src/edu/hitsz/common/protocol/socket/LineMessageFramer.java`
- Test: `test/edu/hitsz/protocol/JsonCodecSmokeTest.java`
- Test: `modules/client-desktop/test/edu/hitsz/e2e/SocketAuthoritySmokeTest.java`

**Step 1: Write the failing tests**
- 验证消息对象和 JSON 之间可以往返编解码。
- 验证一个本地客户端能连上服务端并收到世界快照。

**Step 2: Run tests to verify they fail**
- `javac -encoding UTF-8 $(find src modules/common/src test modules/client-desktop/test modules/server/test -name '*.java') -d /tmp/aircraftwar-build`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.protocol.JsonCodecSmokeTest`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.e2e.SocketAuthoritySmokeTest`

**Step 3: Write minimal implementation**
- 保持 `Transport` 接口不变，只把底层从内存桥接换成 Java Socket。
- 保持协议对象不感知 Socket 细节。

**Step 4: Run tests to verify they pass**
- 重新运行同一组编译与测试命令。

### Task 9: Broadcast Snapshot + Client Session Identification `(Completed)`

**Goal:**
- 保持服务端广播统一世界快照。
- 客户端不再依赖服务端写入 `localPlayer` 标记，而是根据本地 `SessionID` 识别自己。

**Files:**
- Modify: `modules/common/src/edu/hitsz/common/protocol/dto/PlayerSnapshot.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/json/WorldSnapshotJsonMapper.java`
- Modify: `modules/server/src/edu/hitsz/server/WorldSnapshotFactory.java`
- Modify: `modules/server/src/edu/hitsz/server/LocalAuthorityServer.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/DefaultSnapshotApplier.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/SocketClientSession.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/SnapshotApplyTest.java`
- Test: `modules/client-desktop/test/edu/hitsz/e2e/AuthorityFlowSmokeTest.java`

### Task 10: Remove Legacy Offline Combat Fallback From Game `(Completed)`

**Goal:**
- 让 `Game` 在联机模式下成为纯渲染层，不再保留旧单机权威循环作为主要路径。
- 当前已完成：`Game` 只负责输入转发、快照应用和渲染，敌机生成、碰撞、掉落、伤害结算均已移至服务端。

**Files:**
- Modify: `modules/client-desktop/src/edu/hitsz/client/Game.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/HeroController.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/SocketClientSession.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/GameClientBoundaryTest.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/ClientEdtSnapshotTest.java`

### Task 11: Expand Server-Side Drop Pool And Item Effects `(Completed)`

**Goal:**
- 把 `BombSupply` / `FreezeSupply` 纳入掉落池。
- 当前已完成：所有道具都统一成“服务端结算、客户端只消费结果”的模式。

**Files:**
- Modify: `modules/server/src/edu/hitsz/server/ServerWorldState.java`
- Modify: `modules/server/src/edu/hitsz/server/skill/DefaultServerSkillResolver.java`
- Test: `modules/server/test/edu/hitsz/server/ItemPickupEffectTest.java`
- Test: `modules/server/test/edu/hitsz/server/SupplyDropCoverageTest.java`

### Task 12: Add Disconnect/Reconnect Session Lifecycle `(Completed)`

**Goal:**
- 客户端断线后应从广播快照里隐藏。
- 同一个 `SessionID` 重连时，应恢复原有玩家状态而不是创建一份全新状态。
- 断线会话在保留窗口后可被服务端清理。

**Files:**
- Add: `modules/common/src/edu/hitsz/common/protocol/socket/ServerConnectionListener.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/socket/SocketServerTransport.java`
- Modify: `modules/server/src/edu/hitsz/server/PlayerSession.java`
- Modify: `modules/server/src/edu/hitsz/server/SessionRegistry.java`
- Modify: `modules/server/src/edu/hitsz/server/ServerWorldState.java`
- Modify: `modules/server/src/edu/hitsz/server/LocalAuthorityServer.java`
- Test: `modules/client-desktop/test/edu/hitsz/e2e/ReconnectLifecycleSmokeTest.java`

### Task 13: Gate World Start On First Player Join `(Completed)`

**Goal:**
- 服务端在没有在线玩家前不推进世界。
- 玩家延迟入场时，第一帧应该从“新开局”状态开始，而不是接到已经运行很久的战场快照。

**Files:**
- Modify: `modules/server/src/edu/hitsz/server/LocalAuthorityServer.java`
- Test: `modules/client-desktop/test/edu/hitsz/e2e/DelayedJoinStartsFreshTest.java`

## 8. Recommended Execution Order

已完成：

1. Hero/OtherPlayer 拆分
2. 协议对象与快照 DTO
3. Session 注册与校验
4. 服务端权威世界
5. 客户端快照应用
6. 技能与等级缩放
7. 权威链路 smoke flow
8. Java Socket + JSON 落地

接下来建议顺序：

10. 收敛服务端素材尺寸依赖
11. 如果需要，再补更严格的房间制/连接认证/重连令牌

## 9. Risks And Notes

- 如果碰撞尺寸仍依赖图片资源，服务端会继续和本地素材耦合。
- 如果远端玩家不是由快照驱动，而是由客户端自己推断，状态很容易漂移。
- 如果技能时长/伤害分散硬编码在多个类里，冻结和护盾行为会很快失控。
- 如果客户端没有稳定保存自己的 `sessionId`，广播快照下会把本地玩家识别错。

## 10. Acceptance Criteria

- 已达成：
  - 客户端 `HeroAircraft` 仍为单例，但只代表当前本地操作者。
  - 客户端存在 `OtherPlayer`，并能按服务器快照创建、更新。
  - `Game` 已维护玩家集合，并可按快照更新敌机、子弹、道具和分数。
  - 服务器维护 `PlayerSession` 列表，且每条输入都必须通过 `SessionID` 校验。
  - 冻结技能能让全体敌人在服务端停止移动 `n` 秒。
  - 炸弹技能能对全体敌机造成 `n` 点伤害。
  - 护盾技能能让玩家在 `n` 秒内免伤。
  - `n` 随等级增长，并通过统一配置对象读取。
  - 协议层与 Java Socket 实现解耦，消息统一使用 JSON。
  - 单人模式已不再把本地 `Game` 作为主要权威战斗逻辑源头。
  - `BombSupply` / `FreezeSupply` 已进入掉落池，并由服务端统一结算。
  - 客户端与服务端实体树已彻底模块化，旧共享实体树已删除。
  - `WorldSnapshot` 已切换为公共广播语义，客户端按本地 `sessionId` 自识别自己。
  - 两个客户端连接同一个本地 authority server 时，能够收到同一份广播快照并各自识别自己。
  - 客户端断线后会从广播快照中隐藏，并可通过同一 `sessionId` 重连恢复原状态。
  - 服务端在首个玩家 `HELLO` 前不会推进世界，延迟入场时会从 fresh-start 状态开始。
- 未完全达成：
  - 服务端碰撞尺寸对图片素材尺寸的依赖仍可继续收敛。
