# Branch Aircraft And Movement Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace lobby-time skill selection with starter-blue opening, first-boss branch selection, branch-specific weapons, and authoritative non-teleport target-follow movement.

**Architecture:** Keep the authority-server model intact. The server owns branch state, movement targets, weapon simulation, and progression gates; the client only sends target/selection input and renders snapshots. Split the work into protocol/state changes first, then lobby cleanup, movement, branch flow, branch weapons, and branch-specific upgrades.

**Tech Stack:** Java, Swing, custom JSON protocol DTOs, authority server game loop, existing room-based multiplayer stack

---

### Task 1: Add branch and branch-selection protocol/state primitives

**Files:**
- Create: `modules/common/src/edu/hitsz/common/AircraftBranch.java`
- Create: `modules/common/src/edu/hitsz/common/protocol/dto/BranchChoicePayload.java`
- Create: `modules/common/src/edu/hitsz/common/protocol/json/BranchChoicePayloadJsonMapper.java`
- Modify: `modules/common/src/edu/hitsz/common/GamePhase.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/MessageType.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/dto/PlayerSnapshot.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/dto/WorldSnapshot.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/json/WorldSnapshotJsonMapper.java`
- Test: `test/edu/hitsz/protocol/BranchChoiceProtocolTest.java`
- Test: `test/edu/hitsz/protocol/BranchSnapshotProtocolTest.java`

**Step 1: Write the failing protocol tests**

```java
public final class BranchChoiceProtocolTest {
    public static void main(String[] args) {
        BranchChoicePayload payload = new BranchChoicePayload("RED_SPEED");
        String json = new BranchChoicePayloadJsonMapper().toJson(payload);
        BranchChoicePayload restored = new BranchChoicePayloadJsonMapper().fromJson(json);
        assert "RED_SPEED".equals(restored.getBranch());
    }
}
```

```java
public final class BranchSnapshotProtocolTest {
    public static void main(String[] args) {
        WorldSnapshot snapshot = new WorldSnapshot();
        snapshot.setGamePhase(GamePhase.BRANCH_SELECTION);
        snapshot.setFirstBossBranchSelection(true);
        // add one PlayerSnapshot with AircraftBranch.STARTER_BLUE and choices
        String json = new WorldSnapshotJsonMapper().toJson(snapshot);
        WorldSnapshot restored = new WorldSnapshotJsonMapper().fromJson(json);
        assert restored.getGamePhase() == GamePhase.BRANCH_SELECTION;
        assert restored.isFirstBossBranchSelection();
    }
}
```

**Step 2: Run tests to verify they fail**

Run:

```bash
javac -encoding UTF-8 $(find modules/common/src test -name '*.java') -d /tmp/branch-plan-task1
java -ea -cp /tmp/branch-plan-task1 edu.hitsz.protocol.BranchChoiceProtocolTest
java -ea -cp /tmp/branch-plan-task1 edu.hitsz.protocol.BranchSnapshotProtocolTest
```

Expected:
- compile or runtime failure because `AircraftBranch`, `BranchChoicePayload`, or snapshot fields do not exist yet

**Step 3: Write the minimal protocol implementation**

Add:
- `AircraftBranch` enum with `STARTER_BLUE`, `RED_SPEED`, `GREEN_DEFENSE`, `BLACK_HEAVY`
- `MessageType.INPUT_BRANCH_CHOICE`
- `GamePhase.BRANCH_SELECTION`
- `BranchChoicePayload` and mapper
- snapshot fields:
  - `PlayerSnapshot.aircraftBranch`
  - `PlayerSnapshot.availableBranchChoices`
  - `PlayerSnapshot.branchUnlocked`
  - `WorldSnapshot.firstBossBranchSelection`

**Step 4: Run tests to verify they pass**

Run the same commands as Step 2.

Expected:
- both tests PASS

**Step 5: Commit**

```bash
git add \
  modules/common/src/edu/hitsz/common/AircraftBranch.java \
  modules/common/src/edu/hitsz/common/GamePhase.java \
  modules/common/src/edu/hitsz/common/protocol/MessageType.java \
  modules/common/src/edu/hitsz/common/protocol/dto/BranchChoicePayload.java \
  modules/common/src/edu/hitsz/common/protocol/dto/PlayerSnapshot.java \
  modules/common/src/edu/hitsz/common/protocol/dto/WorldSnapshot.java \
  modules/common/src/edu/hitsz/common/protocol/json/BranchChoicePayloadJsonMapper.java \
  modules/common/src/edu/hitsz/common/protocol/json/WorldSnapshotJsonMapper.java \
  test/edu/hitsz/protocol/BranchChoiceProtocolTest.java \
  test/edu/hitsz/protocol/BranchSnapshotProtocolTest.java
git commit -m "feat: add branch selection protocol primitives"
```

### Task 2: Remove lobby-time skill choice and restore starter-blue defaults

**Files:**
- Modify: `modules/common/src/edu/hitsz/common/protocol/dto/CreateRoomPayload.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/dto/JoinRoomPayload.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/dto/LobbyConfigPayload.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/json/CreateRoomPayloadJsonMapper.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/json/JoinRoomPayloadJsonMapper.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/json/LobbyConfigPayloadJsonMapper.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/LauncherSelectionModel.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/LauncherPanel.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/SocketClientSession.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/Game.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/HeroController.java`
- Modify: `modules/server/src/edu/hitsz/server/LocalAuthorityServer.java`
- Modify: `modules/server/src/edu/hitsz/server/RoomRegistry.java`
- Modify: `modules/server/src/edu/hitsz/server/RoomRuntime.java`
- Modify: `modules/server/src/edu/hitsz/server/PlayerRuntimeState.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/LauncherNoSkillSelectionTest.java`
- Test: `modules/server/test/edu/hitsz/server/StarterBlueDefaultStateTest.java`

**Step 1: Write the failing tests**

```java
public final class LauncherNoSkillSelectionTest {
    public static void main(String[] args) {
        LauncherSelectionModel model = new LauncherSelectionModel();
        assert model.getDifficulty() != null;
        assert model.getSelectedSkill() == null;
    }
}
```

```java
public final class StarterBlueDefaultStateTest {
    public static void main(String[] args) {
        PlayerRuntimeState state = new PlayerRuntimeState("p1");
        assert state.getAircraftBranch() == AircraftBranch.STARTER_BLUE;
        assert state.getSelectedSkill() == null;
        assert !state.isBranchUnlocked();
    }
}
```

**Step 2: Run tests to verify they fail**

Run:

```bash
javac -encoding UTF-8 $(find modules/common/src modules/server/src modules/client-desktop/src modules/server/test modules/client-desktop/test -name '*.java') -d /tmp/branch-plan-task2
java -ea -cp /tmp/branch-plan-task2 edu.hitsz.client.LauncherNoSkillSelectionTest
java -ea -cp /tmp/branch-plan-task2 edu.hitsz.server.StarterBlueDefaultStateTest
```

Expected:
- failures because skill selection is still required and starter-blue state does not exist yet

**Step 3: Write the minimal implementation**

- Remove `selectedSkill` from create/join/lobby payload flow
- Remove skill buttons from launcher
- Make `STARTER_BLUE` the only initial player branch
- Make starter branch have no active skill
- Prevent `Space` from attempting to cast before branch unlock

**Step 4: Run tests to verify they pass**

Run the same commands as Step 2.

Expected:
- both tests PASS

**Step 5: Commit**

```bash
git add \
  modules/common/src/edu/hitsz/common/protocol/dto/CreateRoomPayload.java \
  modules/common/src/edu/hitsz/common/protocol/dto/JoinRoomPayload.java \
  modules/common/src/edu/hitsz/common/protocol/dto/LobbyConfigPayload.java \
  modules/common/src/edu/hitsz/common/protocol/json/CreateRoomPayloadJsonMapper.java \
  modules/common/src/edu/hitsz/common/protocol/json/JoinRoomPayloadJsonMapper.java \
  modules/common/src/edu/hitsz/common/protocol/json/LobbyConfigPayloadJsonMapper.java \
  modules/client-desktop/src/edu/hitsz/client/LauncherSelectionModel.java \
  modules/client-desktop/src/edu/hitsz/client/LauncherPanel.java \
  modules/client-desktop/src/edu/hitsz/client/SocketClientSession.java \
  modules/client-desktop/src/edu/hitsz/client/Game.java \
  modules/client-desktop/src/edu/hitsz/client/HeroController.java \
  modules/server/src/edu/hitsz/server/LocalAuthorityServer.java \
  modules/server/src/edu/hitsz/server/RoomRegistry.java \
  modules/server/src/edu/hitsz/server/RoomRuntime.java \
  modules/server/src/edu/hitsz/server/PlayerRuntimeState.java \
  modules/client-desktop/test/edu/hitsz/client/LauncherNoSkillSelectionTest.java \
  modules/server/test/edu/hitsz/server/StarterBlueDefaultStateTest.java
git commit -m "refactor: remove lobby skill selection and default to starter plane"
```

### Task 3: Replace teleport movement with authoritative target-follow movement

**Files:**
- Modify: `modules/server/src/edu/hitsz/server/GameplayBalance.java`
- Modify: `modules/server/src/edu/hitsz/server/PlayerRuntimeState.java`
- Modify: `modules/server/src/edu/hitsz/server/RoomRuntime.java`
- Modify: `modules/server/src/edu/hitsz/server/ServerWorldState.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/Game.java`
- Test: `modules/server/test/edu/hitsz/server/TargetFollowMovementTest.java`
- Test: `modules/server/test/edu/hitsz/server/StarterMovementDoesNotTeleportTest.java`

**Step 1: Write the failing movement tests**

```java
public final class StarterMovementDoesNotTeleportTest {
    public static void main(String[] args) {
        PlayerRuntimeState state = new PlayerRuntimeState("p1");
        state.resetForNewRound(200, 700);
        state.setMoveTarget(400, 500);
        state.tickMovement();
        assert state.getX() != 400;
        assert state.getY() != 500;
    }
}
```

```java
public final class TargetFollowMovementTest {
    public static void main(String[] args) {
        PlayerRuntimeState state = new PlayerRuntimeState("p1");
        state.resetForNewRound(200, 700);
        state.setMoveTarget(240, 700);
        for (int i = 0; i < 20; i++) {
            state.tickMovement();
        }
        assert Math.abs(state.getX() - 240) <= GameplayBalance.PLAYER_STOP_RADIUS;
    }
}
```

**Step 2: Run tests to verify they fail**

Run:

```bash
javac -encoding UTF-8 $(find modules/common/src modules/server/src modules/server/test -name '*.java') -d /tmp/branch-plan-task3
java -ea -cp /tmp/branch-plan-task3 edu.hitsz.server.StarterMovementDoesNotTeleportTest
java -ea -cp /tmp/branch-plan-task3 edu.hitsz.server.TargetFollowMovementTest
```

Expected:
- failures because movement is still direct `setPosition(x, y)`

**Step 3: Write the minimal movement implementation**

- Add target-point state to `PlayerRuntimeState`
- Add movement tuning in `GameplayBalance`:
  - starter speed
  - stop radius
  - per-branch speed overrides
- Change `RoomRuntime.handleMove(...)` to update target only
- Advance position during `ServerWorldState` tick

**Step 4: Run tests to verify they pass**

Run the same commands as Step 2.

Expected:
- both tests PASS

**Step 5: Commit**

```bash
git add \
  modules/server/src/edu/hitsz/server/GameplayBalance.java \
  modules/server/src/edu/hitsz/server/PlayerRuntimeState.java \
  modules/server/src/edu/hitsz/server/RoomRuntime.java \
  modules/server/src/edu/hitsz/server/ServerWorldState.java \
  modules/client-desktop/src/edu/hitsz/client/Game.java \
  modules/server/test/edu/hitsz/server/TargetFollowMovementTest.java \
  modules/server/test/edu/hitsz/server/StarterMovementDoesNotTeleportTest.java
git commit -m "feat: replace teleport movement with target-follow motion"
```

### Task 4: Add first-boss branch selection phase and per-player branch locking

**Files:**
- Modify: `modules/server/src/edu/hitsz/server/ChapterProgressionState.java`
- Modify: `modules/server/src/edu/hitsz/server/ProgressionPolicy.java`
- Modify: `modules/server/src/edu/hitsz/server/PlayerRuntimeState.java`
- Modify: `modules/server/src/edu/hitsz/server/RoomRuntime.java`
- Modify: `modules/server/src/edu/hitsz/server/LocalAuthorityServer.java`
- Modify: `modules/server/src/edu/hitsz/server/WorldSnapshotFactory.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/MessageType.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/SocketClientSession.java`
- Test: `modules/server/test/edu/hitsz/server/FirstBossOpensBranchSelectionTest.java`
- Test: `modules/server/test/edu/hitsz/server/AllPlayersMustChooseBranchTest.java`
- Test: `modules/server/test/edu/hitsz/server/BranchSelectionOnlyHappensOnceTest.java`

**Step 1: Write the failing branch-flow tests**

```java
public final class FirstBossOpensBranchSelectionTest {
    public static void main(String[] args) {
        // setup room and force first boss defeat
        // assert room.getGamePhase() == GamePhase.BRANCH_SELECTION
    }
}
```

```java
public final class BranchSelectionOnlyHappensOnceTest {
    public static void main(String[] args) {
        // complete first branch selection
        // defeat next boss
        // assert phase is UPGRADE_SELECTION, not BRANCH_SELECTION
    }
}
```

**Step 2: Run tests to verify they fail**

Run:

```bash
javac -encoding UTF-8 $(find modules/common/src modules/server/src modules/server/test -name '*.java') -d /tmp/branch-plan-task4
java -ea -cp /tmp/branch-plan-task4 edu.hitsz.server.FirstBossOpensBranchSelectionTest
java -ea -cp /tmp/branch-plan-task4 edu.hitsz.server.AllPlayersMustChooseBranchTest
java -ea -cp /tmp/branch-plan-task4 edu.hitsz.server.BranchSelectionOnlyHappensOnceTest
```

Expected:
- failures because only `UPGRADE_SELECTION` exists today

**Step 3: Write the minimal implementation**

- Introduce first-boss branch gate in progression state
- Add `INPUT_BRANCH_CHOICE` handling in server and client session
- Keep `BRANCH_SELECTION` blocked until all alive players choose
- Mark branch unlocked after successful selection
- After that point, future bosses route to normal `UPGRADE_SELECTION`

**Step 4: Run tests to verify they pass**

Run the same commands as Step 2.

Expected:
- all three tests PASS

**Step 5: Commit**

```bash
git add \
  modules/server/src/edu/hitsz/server/ChapterProgressionState.java \
  modules/server/src/edu/hitsz/server/ProgressionPolicy.java \
  modules/server/src/edu/hitsz/server/PlayerRuntimeState.java \
  modules/server/src/edu/hitsz/server/RoomRuntime.java \
  modules/server/src/edu/hitsz/server/LocalAuthorityServer.java \
  modules/server/src/edu/hitsz/server/WorldSnapshotFactory.java \
  modules/client-desktop/src/edu/hitsz/client/SocketClientSession.java \
  modules/server/test/edu/hitsz/server/FirstBossOpensBranchSelectionTest.java \
  modules/server/test/edu/hitsz/server/AllPlayersMustChooseBranchTest.java \
  modules/server/test/edu/hitsz/server/BranchSelectionOnlyHappensOnceTest.java
git commit -m "feat: add first boss branch selection phase"
```

### Task 5: Update client overlays and player rendering for starter/branch state

**Files:**
- Modify: `modules/client-desktop/src/edu/hitsz/client/ClientWorldState.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/DefaultSnapshotApplier.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/Game.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/HeroController.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/ImageManager.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/ChapterVisualCatalog.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/BranchSelectionOverlayTest.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/StarterPlaneRenderSelectionTest.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/BranchPlaneRenderSelectionTest.java`

**Step 1: Write the failing client tests**

```java
public final class StarterPlaneRenderSelectionTest {
    public static void main(String[] args) {
        // snapshot with STARTER_BLUE local player
        // assert ImageManager resolves 初始飞机.png
    }
}
```

```java
public final class BranchSelectionOverlayTest {
    public static void main(String[] args) {
        // world state in BRANCH_SELECTION with 3 choices
        // assert overlay becomes visible
    }
}
```

**Step 2: Run tests to verify they fail**

Run:

```bash
javac -encoding UTF-8 $(find modules/common/src modules/client-desktop/src modules/client-desktop/test -name '*.java') -d /tmp/branch-plan-task5
java -ea -Djava.awt.headless=true -cp /tmp/branch-plan-task5 edu.hitsz.client.BranchSelectionOverlayTest
java -ea -Djava.awt.headless=true -cp /tmp/branch-plan-task5 edu.hitsz.client.StarterPlaneRenderSelectionTest
java -ea -Djava.awt.headless=true -cp /tmp/branch-plan-task5 edu.hitsz.client.BranchPlaneRenderSelectionTest
```

Expected:
- failures because branch snapshots and new asset mapping are not applied yet

**Step 3: Write the minimal client implementation**

- Render starter plane from `最终素材/初始飞机.png`
- Render branches from the three branch materials
- Show dedicated `BRANCH_SELECTION` overlay with keys `1 / 2 / 3`
- Remove pre-game skill-selection hints from HUD and launcher

**Step 4: Run tests to verify they pass**

Run the same commands as Step 2.

Expected:
- all tests PASS

**Step 5: Commit**

```bash
git add \
  modules/client-desktop/src/edu/hitsz/client/ClientWorldState.java \
  modules/client-desktop/src/edu/hitsz/client/DefaultSnapshotApplier.java \
  modules/client-desktop/src/edu/hitsz/client/Game.java \
  modules/client-desktop/src/edu/hitsz/client/HeroController.java \
  modules/client-desktop/src/edu/hitsz/client/ImageManager.java \
  modules/client-desktop/src/edu/hitsz/client/ChapterVisualCatalog.java \
  modules/client-desktop/test/edu/hitsz/client/BranchSelectionOverlayTest.java \
  modules/client-desktop/test/edu/hitsz/client/StarterPlaneRenderSelectionTest.java \
  modules/client-desktop/test/edu/hitsz/client/BranchPlaneRenderSelectionTest.java
git commit -m "feat: render starter and branch aircraft states"
```

### Task 6: Implement red-speed laser baseline

**Files:**
- Create: `modules/common/src/edu/hitsz/common/protocol/dto/LaserSnapshot.java`
- Create: `modules/server/src/edu/hitsz/server/LaserBeamState.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/dto/WorldSnapshot.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/json/WorldSnapshotJsonMapper.java`
- Modify: `modules/server/src/edu/hitsz/server/GameplayBalance.java`
- Modify: `modules/server/src/edu/hitsz/server/PlayerRuntimeState.java`
- Modify: `modules/server/src/edu/hitsz/server/ServerWorldState.java`
- Modify: `modules/server/src/edu/hitsz/server/WorldSnapshotFactory.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/ClientWorldState.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/DefaultSnapshotApplier.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/Game.java`
- Test: `modules/server/test/edu/hitsz/server/RedSpeedLaserDamageTest.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/LaserSnapshotRenderTest.java`

**Step 1: Write the failing laser tests**

```java
public final class RedSpeedLaserDamageTest {
    public static void main(String[] args) {
        // RED_SPEED player fires laser
        // assert enemy in beam path loses hp
    }
}
```

```java
public final class LaserSnapshotRenderTest {
    public static void main(String[] args) {
        // snapshot with one laser beam
        // assert client stores and exposes it for paint
    }
}
```

**Step 2: Run tests to verify they fail**

Run:

```bash
javac -encoding UTF-8 $(find modules/common/src modules/server/src modules/client-desktop/src modules/server/test modules/client-desktop/test -name '*.java') -d /tmp/branch-plan-task6
java -ea -cp /tmp/branch-plan-task6 edu.hitsz.server.RedSpeedLaserDamageTest
java -ea -Djava.awt.headless=true -cp /tmp/branch-plan-task6 edu.hitsz.client.LaserSnapshotRenderTest
```

Expected:
- failures because no laser runtime or snapshot exists yet

**Step 3: Write the minimal laser implementation**

- Add laser runtime state with:
  - owner
  - angle
  - width
  - duration
  - damage
- Attach it to `RED_SPEED`
- Render as a client beam overlay

**Step 4: Run tests to verify they pass**

Run the same commands as Step 2.

Expected:
- both tests PASS

**Step 5: Commit**

```bash
git add \
  modules/common/src/edu/hitsz/common/protocol/dto/LaserSnapshot.java \
  modules/common/src/edu/hitsz/common/protocol/dto/WorldSnapshot.java \
  modules/common/src/edu/hitsz/common/protocol/json/WorldSnapshotJsonMapper.java \
  modules/server/src/edu/hitsz/server/LaserBeamState.java \
  modules/server/src/edu/hitsz/server/GameplayBalance.java \
  modules/server/src/edu/hitsz/server/PlayerRuntimeState.java \
  modules/server/src/edu/hitsz/server/ServerWorldState.java \
  modules/server/src/edu/hitsz/server/WorldSnapshotFactory.java \
  modules/client-desktop/src/edu/hitsz/client/ClientWorldState.java \
  modules/client-desktop/src/edu/hitsz/client/DefaultSnapshotApplier.java \
  modules/client-desktop/src/edu/hitsz/client/Game.java \
  modules/server/test/edu/hitsz/server/RedSpeedLaserDamageTest.java \
  modules/client-desktop/test/edu/hitsz/client/LaserSnapshotRenderTest.java
git commit -m "feat: add red speed laser weapon"
```

### Task 7: Implement green scatter and black heavy airburst baselines

**Files:**
- Create: `modules/common/src/edu/hitsz/common/protocol/dto/ExplosionSnapshot.java`
- Create: `modules/server/src/edu/hitsz/server/AirburstProjectileState.java`
- Modify: `modules/server/src/edu/hitsz/server/GameplayBalance.java`
- Modify: `modules/server/src/edu/hitsz/server/PlayerRuntimeState.java`
- Modify: `modules/server/src/edu/hitsz/server/ServerPlayerAircraft.java`
- Modify: `modules/server/src/edu/hitsz/server/ServerWorldState.java`
- Modify: `modules/server/src/edu/hitsz/server/WorldSnapshotFactory.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/dto/WorldSnapshot.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/json/WorldSnapshotJsonMapper.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/ClientWorldState.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/DefaultSnapshotApplier.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/Game.java`
- Test: `modules/server/test/edu/hitsz/server/GreenDefenseScatterPatternTest.java`
- Test: `modules/server/test/edu/hitsz/server/BlackHeavyAirburstRadiusDamageTest.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/AirburstExplosionRenderTest.java`

**Step 1: Write the failing weapon tests**

```java
public final class GreenDefenseScatterPatternTest {
    public static void main(String[] args) {
        // GREEN_DEFENSE fire should spawn spread bullets
        // assert bullet count and x-speed variance > 0
    }
}
```

```java
public final class BlackHeavyAirburstRadiusDamageTest {
    public static void main(String[] args) {
        // BLACK_HEAVY projectile reaches target/range and explodes
        // assert multiple nearby enemies lose hp
    }
}
```

**Step 2: Run tests to verify they fail**

Run:

```bash
javac -encoding UTF-8 $(find modules/common/src modules/server/src modules/client-desktop/src modules/server/test modules/client-desktop/test -name '*.java') -d /tmp/branch-plan-task7
java -ea -cp /tmp/branch-plan-task7 edu.hitsz.server.GreenDefenseScatterPatternTest
java -ea -cp /tmp/branch-plan-task7 edu.hitsz.server.BlackHeavyAirburstRadiusDamageTest
java -ea -Djava.awt.headless=true -cp /tmp/branch-plan-task7 edu.hitsz.client.AirburstExplosionRenderTest
```

Expected:
- failures because branch-specific scatter and airburst do not exist yet

**Step 3: Write the minimal implementation**

- Make `GREEN_DEFENSE` fire spread bullets
- Make `BLACK_HEAVY` fire projectiles that auto-burst at target point or max range
- Emit explosion snapshots for rendering

**Step 4: Run tests to verify they pass**

Run the same commands as Step 2.

Expected:
- all tests PASS

**Step 5: Commit**

```bash
git add \
  modules/common/src/edu/hitsz/common/protocol/dto/ExplosionSnapshot.java \
  modules/common/src/edu/hitsz/common/protocol/dto/WorldSnapshot.java \
  modules/common/src/edu/hitsz/common/protocol/json/WorldSnapshotJsonMapper.java \
  modules/server/src/edu/hitsz/server/AirburstProjectileState.java \
  modules/server/src/edu/hitsz/server/GameplayBalance.java \
  modules/server/src/edu/hitsz/server/PlayerRuntimeState.java \
  modules/server/src/edu/hitsz/server/ServerPlayerAircraft.java \
  modules/server/src/edu/hitsz/server/ServerWorldState.java \
  modules/server/src/edu/hitsz/server/WorldSnapshotFactory.java \
  modules/client-desktop/src/edu/hitsz/client/ClientWorldState.java \
  modules/client-desktop/src/edu/hitsz/client/DefaultSnapshotApplier.java \
  modules/client-desktop/src/edu/hitsz/client/Game.java \
  modules/server/test/edu/hitsz/server/GreenDefenseScatterPatternTest.java \
  modules/server/test/edu/hitsz/server/BlackHeavyAirburstRadiusDamageTest.java \
  modules/client-desktop/test/edu/hitsz/client/AirburstExplosionRenderTest.java
git commit -m "feat: add green scatter and black airburst weapons"
```

### Task 8: Replace generic post-boss upgrades with branch-specific upgrade pools

**Files:**
- Create: `modules/common/src/edu/hitsz/common/BranchUpgradeChoice.java`
- Modify: `modules/server/src/edu/hitsz/server/PlayerRuntimeState.java`
- Modify: `modules/server/src/edu/hitsz/server/GameplayBalance.java`
- Modify: `modules/server/src/edu/hitsz/server/ServerWorldState.java`
- Modify: `modules/server/src/edu/hitsz/server/WorldSnapshotFactory.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/dto/PlayerSnapshot.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/dto/UpgradeChoicePayload.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/json/WorldSnapshotJsonMapper.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/ClientWorldState.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/DefaultSnapshotApplier.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/Game.java`
- Test: `modules/server/test/edu/hitsz/server/RedBranchUpgradePoolTest.java`
- Test: `modules/server/test/edu/hitsz/server/GreenBranchUpgradePoolTest.java`
- Test: `modules/server/test/edu/hitsz/server/BlackBranchUpgradePoolTest.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/BranchUpgradeOverlayTest.java`

**Step 1: Write the failing upgrade-pool tests**

```java
public final class RedBranchUpgradePoolTest {
    public static void main(String[] args) {
        // RED_SPEED should not receive heavy-only or defense-only choices
    }
}
```

```java
public final class BlackBranchUpgradePoolTest {
    public static void main(String[] args) {
        // BLACK_HEAVY should receive airburst-specific upgrade choices
    }
}
```

**Step 2: Run tests to verify they fail**

Run:

```bash
javac -encoding UTF-8 $(find modules/common/src modules/server/src modules/client-desktop/src modules/server/test modules/client-desktop/test -name '*.java') -d /tmp/branch-plan-task8
java -ea -cp /tmp/branch-plan-task8 edu.hitsz.server.RedBranchUpgradePoolTest
java -ea -cp /tmp/branch-plan-task8 edu.hitsz.server.GreenBranchUpgradePoolTest
java -ea -cp /tmp/branch-plan-task8 edu.hitsz.server.BlackBranchUpgradePoolTest
java -ea -Djava.awt.headless=true -cp /tmp/branch-plan-task8 edu.hitsz.client.BranchUpgradeOverlayTest
```

Expected:
- failures because upgrades are still generic today

**Step 3: Write the minimal implementation**

- Introduce branch-specific upgrade enum or value set
- Generate upgrade choices by current branch
- Apply upgrades only to the relevant branch weapon/stats
- Update client overlay text to reflect branch-local choices

**Step 4: Run tests to verify they pass**

Run the same commands as Step 2.

Expected:
- all tests PASS

**Step 5: Commit**

```bash
git add \
  modules/common/src/edu/hitsz/common/BranchUpgradeChoice.java \
  modules/server/src/edu/hitsz/server/PlayerRuntimeState.java \
  modules/server/src/edu/hitsz/server/GameplayBalance.java \
  modules/server/src/edu/hitsz/server/ServerWorldState.java \
  modules/server/src/edu/hitsz/server/WorldSnapshotFactory.java \
  modules/common/src/edu/hitsz/common/protocol/dto/PlayerSnapshot.java \
  modules/common/src/edu/hitsz/common/protocol/dto/UpgradeChoicePayload.java \
  modules/common/src/edu/hitsz/common/protocol/json/WorldSnapshotJsonMapper.java \
  modules/client-desktop/src/edu/hitsz/client/ClientWorldState.java \
  modules/client-desktop/src/edu/hitsz/client/DefaultSnapshotApplier.java \
  modules/client-desktop/src/edu/hitsz/client/Game.java \
  modules/server/test/edu/hitsz/server/RedBranchUpgradePoolTest.java \
  modules/server/test/edu/hitsz/server/GreenBranchUpgradePoolTest.java \
  modules/server/test/edu/hitsz/server/BlackBranchUpgradePoolTest.java \
  modules/client-desktop/test/edu/hitsz/client/BranchUpgradeOverlayTest.java
git commit -m "feat: add branch-specific post-boss upgrades"
```

### Task 9: Update runtime assets, docs, and end-to-end coverage

**Files:**
- Modify: `modules/client-desktop/src/edu/hitsz/client/ImageManager.java`
- Modify: `modules/server/src/edu/hitsz/server/ServerImageManager.java`
- Modify: `scripts/build_runtime_with_assets.sh`
- Modify: `README.md`
- Modify: `PLAN.md`
- Test: `modules/client-desktop/test/edu/hitsz/e2e/StarterToBranchFlowSmokeTest.java`
- Test: `modules/client-desktop/test/edu/hitsz/e2e/BranchWeaponMultiplayerSmokeTest.java`

**Step 1: Write the failing smoke tests**

```java
public final class StarterToBranchFlowSmokeTest {
    public static void main(String[] args) {
        // room starts with starter blue
        // first boss defeat opens branch selection
        // choose branch and continue battle
    }
}
```

```java
public final class BranchWeaponMultiplayerSmokeTest {
    public static void main(String[] args) {
        // two clients choose different branches
        // both branch states remain isolated and visible
    }
}
```

**Step 2: Run tests to verify they fail**

Run:

```bash
javac -encoding UTF-8 $(find src modules/common/src modules/server/src modules/client-desktop/src modules/server/test modules/client-desktop/test test -name '*.java') -d /tmp/branch-plan-task9
java -ea -cp /tmp/branch-plan-task9 edu.hitsz.e2e.StarterToBranchFlowSmokeTest
java -ea -cp /tmp/branch-plan-task9 edu.hitsz.e2e.BranchWeaponMultiplayerSmokeTest
```

Expected:
- failures because branch flow and new assets are not fully wired

**Step 3: Write the minimal implementation**

- Wire final asset folder names into runtime build script
- Update docs to describe:
  - starter plane
  - first-boss branch choice
  - branch weapons
  - non-teleport movement
- Ensure runtime bundle includes all final materials

**Step 4: Run full verification**

Run:

```bash
javac -encoding UTF-8 $(find src modules/common/src modules/server/src modules/client-desktop/src modules/server/test modules/client-desktop/test test -name '*.java') -d /tmp/branch-plan-final
./scripts/build_runtime_with_assets.sh /tmp/aircraftwar-branch-runtime
java -ea -cp /tmp/branch-plan-final edu.hitsz.e2e.StarterToBranchFlowSmokeTest
java -ea -cp /tmp/branch-plan-final edu.hitsz.e2e.BranchWeaponMultiplayerSmokeTest
java -ea -cp /tmp/branch-plan-final edu.hitsz.FeatureRegressionTest
```

Expected:
- all PASS

**Step 5: Commit**

```bash
git add \
  modules/client-desktop/src/edu/hitsz/client/ImageManager.java \
  modules/server/src/edu/hitsz/server/ServerImageManager.java \
  scripts/build_runtime_with_assets.sh \
  README.md \
  PLAN.md \
  modules/client-desktop/test/edu/hitsz/e2e/StarterToBranchFlowSmokeTest.java \
  modules/client-desktop/test/edu/hitsz/e2e/BranchWeaponMultiplayerSmokeTest.java
git commit -m "docs: finalize branch aircraft runtime flow"
```

