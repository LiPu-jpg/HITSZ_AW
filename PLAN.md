# AircraftWar Authority Server And Skill System Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将当前单机版 AircraftWar 重构为“客户端本地渲染 + 权威服务器统一结算”的多人架构，并补齐冻结、炸弹、护盾三类技能，以及它们随玩家等级增长的数值体系。

**Architecture:** 客户端只负责输入采集、快照消费和 Swing 渲染；服务器维护 `PlayerSession`、权威世界状态、敌机生成、碰撞、掉落、技能和升级。协议层先抽象消息包络、快照 DTO、编解码器和传输接口，通信格式统一 JSON，后续再落到 Java Socket。

**Tech Stack:** Java 8+/Swing，现有 `Timer` 驱动游戏循环，未来使用 Java Socket，消息体为 JSON；当前项目没有 Maven/Gradle 和 `.git`，验证方式以 `javac` + `java -ea` 为主。

---

## 1. Current Status

- 当前项目仍是单进程、本地权威的游戏循环，核心计算集中在 `src/edu/hitsz/application/Game.java`。
- `HeroAircraft` 已完成 DCL 单例化，但它目前仍服务于单机逻辑，还没有和“其他玩家”区分职责。
- 敌机体系已经扩展为：
  - `MobEnemy`
  - `EliteEnemy`
  - `ElitePlusEnemy`
  - `AceEnemy`
  - `BossEnemy`
- 道具体系已经扩展为：
  - `AbstractItem`
  - `BloodSupply`
  - `FireSupply`
  - `FirePlusSupply`
  - `BombSupply`
  - `FreezeSupply`
- 目前只有 `BloodSupply` 真正改变数值；`FireSupply`/`FirePlusSupply` 只打印日志；`BombSupply`/`FreezeSupply` 仍是占位类；护盾能力还不存在。
- 当前没有等级系统，没有技能持续时间/伤害随等级增长的统一配置。
- 当前 `Game` 只维护本地 `heroAircraft`、`enemyAircrafts`、`heroBullets`、`enemyBullets`、`items`，还没有 `List<AbstractAircraft> playerAircrafts`，也没有 `OtherPlayer`。
- 当前没有协议抽象、没有 `SessionID`、没有 `PlayerSession`、没有服务端世界状态、没有客户端快照应用层。
- 当前逻辑碰撞尺寸仍间接依赖图片资源；真正进入权威服务器前，最好把逻辑碰撞盒与 `ImageManager` 解耦。
- 当前没有构建工具和外部依赖管理；如果后续确实要稳定接入 JSON 库，最好补 Maven/Gradle，否则需要保持接口抽象并先用最小实现推进。

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
- 第一阶段不做客户端预测、回滚、插值和复杂反作弊。
- 先抽象协议和传输接口，再接 Java Socket。
- 消息格式统一为 JSON。

## 3. Suggested Package Layout

为了尽量遵循现有顶层包结构，新增内容建议放到 `application` 下的子包，而不是引入过多新的顶层目录。

- Keep:
  - `src/edu/hitsz/application`
  - `src/edu/hitsz/aircraft`
  - `src/edu/hitsz/bullet`
  - `src/edu/hitsz/basic`
- Add:
  - `src/edu/hitsz/application/client`
  - `src/edu/hitsz/application/protocol`
  - `src/edu/hitsz/application/protocol/dto`
  - `src/edu/hitsz/application/server`
  - `src/edu/hitsz/application/server/command`
  - `src/edu/hitsz/application/server/skill`
  - `src/edu/hitsz/application/protocol/json`
  - `src/edu/hitsz/application/protocol/socket`

推荐职责划分：

- `aircraft`: 客户端可渲染飞机对象，包括 `HeroAircraft` 与 `OtherPlayer`
- `application/client`: 客户端快照状态、快照应用器、命令发布器
- `application/protocol`: 消息包络、消息类型、编解码接口、传输接口
- `application/protocol/dto`: 快照 DTO 和命令载荷 DTO
- `application/server`: 会话、权威世界、命令路由、世界循环
- `application/server/skill`: 技能类型、缩放配置、技能状态、技能结算器

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
    void apply(WorldSnapshot snapshot, ClientWorldState state);
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

### Task 1: Split Local Hero And Remote Players

**Files:**
- Create: `src/edu/hitsz/aircraft/OtherPlayer.java`
- Modify: `src/edu/hitsz/application/Game.java`
- Modify: `src/edu/hitsz/application/HeroController.java`
- Test: `test/edu/hitsz/client/PlayerAircraftModelTest.java`

**Step 1: Write the failing test**
- 验证 `HeroAircraft` 仍为单例。
- 验证 `OtherPlayer` 不是单例。
- 验证 `Game` 可以维护玩家集合概念。

**Step 2: Run test to verify it fails**
- `javac -encoding UTF-8 $(find src test -name '*.java') -d /tmp/aircraftwar-build`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.client.PlayerAircraftModelTest`

**Step 3: Write minimal implementation**
- 新增 `OtherPlayer`，继承 `AbstractAircraft`。
- 给 `Game` 引入 `playerAircrafts` 数据结构。
- 保持 `HeroAircraft` 单例语义不变。

**Step 4: Run test to verify it passes**
- 重新运行同一组编译与测试命令。

### Task 2: Introduce Protocol Envelope And Snapshot DTOs

**Files:**
- Create: `src/edu/hitsz/application/protocol/ProtocolMessage.java`
- Create: `src/edu/hitsz/application/protocol/MessageType.java`
- Create: `src/edu/hitsz/application/protocol/MessageCodec.java`
- Create: `src/edu/hitsz/application/protocol/Transport.java`
- Create: `src/edu/hitsz/application/protocol/ProtocolMessageListener.java`
- Create: `src/edu/hitsz/application/protocol/dto/InputMovePayload.java`
- Create: `src/edu/hitsz/application/protocol/dto/InputSkillPayload.java`
- Create: `src/edu/hitsz/application/protocol/dto/WorldSnapshot.java`
- Test: `test/edu/hitsz/protocol/ProtocolEnvelopeTest.java`

**Step 1: Write the failing test**
- 验证消息包络包含 `messageType/sessionId/sequence/timestamp/payload`。
- 验证 `WorldSnapshot` DTO 可被构造与消费。

**Step 2: Run test to verify it fails**
- `javac -encoding UTF-8 $(find src test -name '*.java') -d /tmp/aircraftwar-build`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.protocol.ProtocolEnvelopeTest`

**Step 3: Write minimal implementation**
- 先定义协议对象和接口，不把 Java Socket 或 JSON 库写死到领域层。

**Step 4: Run test to verify it passes**
- 重新运行同一组编译与测试命令。

### Task 3: Build Server Session Registry And Identity Validation

**Files:**
- Create: `src/edu/hitsz/application/server/PlayerSession.java`
- Create: `src/edu/hitsz/application/server/SessionRegistry.java`
- Create: `src/edu/hitsz/application/server/ServerCommandRouter.java`
- Create: `src/edu/hitsz/application/server/command/MoveCommand.java`
- Create: `src/edu/hitsz/application/server/command/SkillCommand.java`
- Test: `test/edu/hitsz/server/SessionValidationTest.java`

**Step 1: Write the failing test**
- 验证 `SessionRegistry` 可以创建、查找、注销 `PlayerSession`。
- 验证错误 `SessionID` 的命令不会被路由到其他玩家。

**Step 2: Run test to verify it fails**
- `javac -encoding UTF-8 $(find src test -name '*.java') -d /tmp/aircraftwar-build`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.server.SessionValidationTest`

**Step 3: Write minimal implementation**
- 实现会话注册表。
- 在命令执行前统一做会话合法性校验。

**Step 4: Run test to verify it passes**
- 重新运行同一组编译与测试命令。

### Task 4: Extract Authoritative Server World

**Files:**
- Create: `src/edu/hitsz/application/server/ServerWorldState.java`
- Create: `src/edu/hitsz/application/server/ServerGameLoop.java`
- Create: `src/edu/hitsz/application/server/WorldSnapshotFactory.java`
- Modify: `src/edu/hitsz/application/Game.java`
- Test: `test/edu/hitsz/server/ServerWorldLoopTest.java`

**Step 1: Write the failing test**
- 验证服务端世界 tick 可以推进。
- 验证权威世界能够输出 `WorldSnapshot`。

**Step 2: Run test to verify it fails**
- `javac -encoding UTF-8 $(find src test -name '*.java') -d /tmp/aircraftwar-build`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.server.ServerWorldLoopTest`

**Step 3: Write minimal implementation**
- 将敌机生成、移动、射击、碰撞、掉落从本地 `Game` 迁移到服务端世界循环。
- 客户端 `Game` 只保留渲染和快照消费相关职责。

**Step 4: Run test to verify it passes**
- 重新运行同一组编译与测试命令。

### Task 5: Add Client Snapshot State And Snapshot Applier

**Files:**
- Create: `src/edu/hitsz/application/client/ClientWorldState.java`
- Create: `src/edu/hitsz/application/client/SnapshotApplier.java`
- Create: `src/edu/hitsz/application/client/DefaultSnapshotApplier.java`
- Modify: `src/edu/hitsz/application/Game.java`
- Modify: `src/edu/hitsz/application/HeroController.java`
- Test: `test/edu/hitsz/client/SnapshotApplyTest.java`

**Step 1: Write the failing test**
- 验证客户端能把 `WorldSnapshot` 应用到本地渲染态。
- 验证 `HeroAircraft` 和 `OtherPlayer` 可以根据快照更新。

**Step 2: Run test to verify it fails**
- `javac -encoding UTF-8 $(find src test -name '*.java') -d /tmp/aircraftwar-build`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.client.SnapshotApplyTest`

**Step 3: Write minimal implementation**
- 引入客户端世界状态对象。
- 引入默认快照应用器。
- 让 `HeroController` 转为命令发布者，而不是最终世界状态决定者。

**Step 4: Run test to verify it passes**
- 重新运行同一组编译与测试命令。

### Task 6: Introduce Freeze, Bomb, Shield And Level Scaling

**Files:**
- Create: `src/edu/hitsz/application/server/skill/SkillType.java`
- Create: `src/edu/hitsz/application/server/skill/SkillScalingConfig.java`
- Create: `src/edu/hitsz/application/server/skill/PlayerSkillState.java`
- Create: `src/edu/hitsz/application/server/skill/WorldEffectState.java`
- Create: `src/edu/hitsz/application/server/skill/ServerSkillResolver.java`
- Modify: `src/edu/hitsz/basic/BombSupply.java`
- Modify: `src/edu/hitsz/basic/FreezeSupply.java`
- Test: `test/edu/hitsz/server/SkillScalingTest.java`
- Test: `test/edu/hitsz/server/FreezeBombShieldTest.java`

**Step 1: Write the failing tests**
- 验证等级越高，冻结时长、炸弹伤害、护盾时长越大。
- 验证三种技能都能在服务端被正确识别和结算。

**Step 2: Run tests to verify they fail**
- `javac -encoding UTF-8 $(find src test -name '*.java') -d /tmp/aircraftwar-build`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.server.SkillScalingTest`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.server.FreezeBombShieldTest`

**Step 3: Write minimal implementation**
- 用 `SkillScalingConfig` 统一读取技能数值。
- 用 `WorldEffectState` 管理全局冻结截止时间。
- 用 `PlayerSkillState` 管理玩家护盾和技能状态。
- 让 `BombSupply` / `FreezeSupply` 从“本地占位效果”升级为“服务端技能触发入口”。

**Step 4: Run tests to verify they pass**
- 重新运行同一组编译与测试命令。

### Task 7: Bridge Client And Server With An In-Memory Transport

**Files:**
- Create: `src/edu/hitsz/application/client/ClientCommandPublisher.java`
- Create: `src/edu/hitsz/application/protocol/InMemoryTransport.java`
- Modify: `src/edu/hitsz/application/client/DefaultSnapshotApplier.java`
- Modify: `src/edu/hitsz/application/server/ServerCommandRouter.java`
- Test: `test/edu/hitsz/e2e/AuthorityFlowSmokeTest.java`

**Step 1: Write the failing test**
- 验证客户端发出的移动/技能指令能通过内存传输到服务端。
- 验证服务端返回快照后客户端能正确消费。

**Step 2: Run test to verify it fails**
- `javac -encoding UTF-8 $(find src test -name '*.java') -d /tmp/aircraftwar-build`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.e2e.AuthorityFlowSmokeTest`

**Step 3: Write minimal implementation**
- 使用内存版 `Transport` 模拟客户端和服务器通信。
- 证明整个权威链路先能在单进程内闭环跑通。

**Step 4: Run test to verify it passes**
- 重新运行同一组编译与测试命令。

### Task 8: Plug In Java Socket And JSON Codec

**Files:**
- Create: `src/edu/hitsz/application/protocol/json/JsonMessageCodec.java`
- Create: `src/edu/hitsz/application/protocol/socket/SocketClientTransport.java`
- Create: `src/edu/hitsz/application/protocol/socket/SocketServerTransport.java`
- Create: `src/edu/hitsz/application/protocol/socket/LineMessageFramer.java`
- Test: `test/edu/hitsz/protocol/JsonCodecSmokeTest.java`
- Test: `test/edu/hitsz/e2e/SocketAuthoritySmokeTest.java`

**Step 1: Write the failing tests**
- 验证消息对象和 JSON 之间可以往返编解码。
- 验证一个本地客户端能连上服务端并收到世界快照。

**Step 2: Run tests to verify they fail**
- `javac -encoding UTF-8 $(find src test -name '*.java') -d /tmp/aircraftwar-build`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.protocol.JsonCodecSmokeTest`
- `java -ea -cp /tmp/aircraftwar-build edu.hitsz.e2e.SocketAuthoritySmokeTest`

**Step 3: Write minimal implementation**
- 保持 `Transport` 接口不变，只把底层从内存桥接换成 Java Socket。
- 保持协议对象不感知 Socket 细节。

**Step 4: Run tests to verify they pass**
- 重新运行同一组编译与测试命令。

## 8. Recommended Execution Order

按下面顺序实现，返工最少：

1. Hero/OtherPlayer 拆分
2. 协议对象与快照 DTO
3. Session 注册与校验
4. 服务端权威世界
5. 客户端快照应用
6. 技能与等级缩放
7. 内存传输闭环
8. Java Socket + JSON 落地

## 9. Risks And Notes

- 如果碰撞尺寸仍依赖图片资源，服务端会继续和本地素材耦合。
- 如果 `Game` 继续同时承担权威逻辑和快照渲染，后续多人同步会很难维护。
- 如果远端玩家不是由快照驱动，而是由客户端自己推断，状态很容易漂移。
- 如果技能时长/伤害分散硬编码在多个类里，冻结和护盾行为会很快失控。
- 如果先写 Socket 再稳定协议对象，会很容易重写两遍网络层。

## 10. Acceptance Criteria

- 客户端 `HeroAircraft` 仍为单例，但只代表当前本地操作者。
- 客户端存在 `OtherPlayer`，并能按服务器快照创建、更新和销毁。
- `Game` 至少在数据结构上维护玩家集合，而不是只默认一架英雄机。
- 服务器维护 `PlayerSession` 列表，且每条输入都必须通过 `SessionID` 校验。
- 冻结技能能让全体敌人在服务端停止移动 `n` 秒。
- 炸弹技能能对全体敌机造成 `n` 点伤害。
- 护盾技能能让玩家在 `n` 秒内免伤。
- `n` 随等级增长，并通过统一配置对象读取。
- 协议层与 Java Socket 实现解耦，消息统一使用 JSON。
- 客户端最终以服务器快照为准，不再把本地 `Game` 作为权威战斗逻辑源头。
