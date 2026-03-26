# Scene Size Upgrade Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Decouple entity size from image assets, add chapter-driven scene progression, and introduce per-player upgrade selection after each boss kill.

**Architecture:** Keep the authoritative server responsible for logic size, progression state, boss transitions, and upgrade application. Keep the desktop client responsible for scaled rendering, chapter-specific visuals, and upgrade UI. Extend shared protocol DTOs so the client can render chapter and phase state without inventing local rules.

**Tech Stack:** Java, Swing, custom JSON protocol, authoritative server snapshots, multi-module project (`modules/common`, `modules/server`, `modules/client-desktop`)

---

### Task 1: Add shared progression enums and snapshot fields

**Files:**
- Create: `modules/common/src/edu/hitsz/common/GamePhase.java`
- Create: `modules/common/src/edu/hitsz/common/ChapterId.java`
- Create: `modules/common/src/edu/hitsz/common/UpgradeChoice.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/dto/WorldSnapshot.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/dto/PlayerSnapshot.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/json/WorldSnapshotJsonMapper.java`
- Test: `test/edu/hitsz/protocol/ChapterSceneProtocolTest.java`

**Step 1: Write the failing test**

Create `test/edu/hitsz/protocol/ChapterSceneProtocolTest.java` with one test method that:
- builds a `WorldSnapshot`
- sets `gamePhase = UPGRADE_SELECTION`
- sets `chapterId = CH2`
- adds a `PlayerSnapshot` carrying available upgrade choices
- round-trips JSON
- asserts all new fields survive

**Step 2: Run test to verify it fails**

Run:

```bash
javac -encoding UTF-8 $(find src modules/common/src modules/server/src modules/client-desktop/src modules/server/test modules/client-desktop/test test -name '*.java') -d /tmp/scene-plan-task1
java -ea -cp /tmp/scene-plan-task1 edu.hitsz.protocol.ChapterSceneProtocolTest
```

Expected: FAIL because the enums or snapshot fields do not exist yet.

**Step 3: Write minimal implementation**

- Add the three enums in `modules/common/src/edu/hitsz/common/`
- Extend `WorldSnapshot` with:
  - `gamePhase`
  - `chapterId`
  - `chapterTransitionFlash`
- Extend `PlayerSnapshot` with:
  - `maxHp`
  - `availableUpgradeChoices`
  - `selectedUpgradeChoice`
- Update `WorldSnapshotJsonMapper` to serialize and deserialize the new fields

**Step 4: Run test to verify it passes**

Run the same commands from Step 2.

Expected: PASS

**Step 5: Commit**

```bash
git add test/edu/hitsz/protocol/ChapterSceneProtocolTest.java \
  modules/common/src/edu/hitsz/common/GamePhase.java \
  modules/common/src/edu/hitsz/common/ChapterId.java \
  modules/common/src/edu/hitsz/common/UpgradeChoice.java \
  modules/common/src/edu/hitsz/common/protocol/dto/WorldSnapshot.java \
  modules/common/src/edu/hitsz/common/protocol/dto/PlayerSnapshot.java \
  modules/common/src/edu/hitsz/common/protocol/json/WorldSnapshotJsonMapper.java
git commit -m "feat: add chapter and upgrade snapshot fields"
```

### Task 2: Introduce explicit entity sizing instead of image-derived size

**Files:**
- Create: `modules/common/src/edu/hitsz/common/EntitySizing.java`
- Modify: `modules/server/src/edu/hitsz/server/basic/AbstractFlyingObject.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/basic/AbstractFlyingObject.java`
- Modify: `modules/server/src/edu/hitsz/server/ServerPlayerAircraft.java`
- Modify: `modules/server/src/edu/hitsz/server/ServerHeroBullet.java`
- Modify: `modules/server/src/edu/hitsz/server/aircraft/*.java`
- Modify: `modules/server/src/edu/hitsz/server/basic/*Supply.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/aircraft/*.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/basic/*Supply.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/bullet/*.java`
- Test: `modules/server/test/edu/hitsz/server/EntitySizingTest.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/EntitySizingRenderTest.java`

**Step 1: Write the failing tests**

- `EntitySizingTest.java` should assert that:
  - a `MobEnemy` collision width/height come from `EntitySizing`
  - changing image dimensions does not affect collision width/height
- `EntitySizingRenderTest.java` should assert that:
  - client render width/height come from `EntitySizing`
  - entities no longer need image dimensions to compute logical bounds

**Step 2: Run tests to verify they fail**

Run:

```bash
javac -encoding UTF-8 $(find src modules/common/src modules/server/src modules/client-desktop/src modules/server/test modules/client-desktop/test test -name '*.java') -d /tmp/scene-plan-task2
java -ea -cp /tmp/scene-plan-task2 edu.hitsz.server.EntitySizingTest
java -ea -cp /tmp/scene-plan-task2 edu.hitsz.client.EntitySizingRenderTest
```

Expected: FAIL because size still falls back to image width and height.

**Step 3: Write minimal implementation**

- Add `EntitySizing` constants for:
  - hero
  - mob
  - elite
  - elitePlus
  - ace
  - boss
  - hero bullet
  - enemy bullet
  - blood / fire / firePlus / bomb / freeze item
- Remove image-size fallback from both `AbstractFlyingObject` base classes
- Set `width` and `height` explicitly in constructors for each concrete entity

**Step 4: Run tests to verify they pass**

Run the same commands from Step 2.

Expected: PASS

**Step 5: Commit**

```bash
git add modules/common/src/edu/hitsz/common/EntitySizing.java \
  modules/server/src/edu/hitsz/server/basic/AbstractFlyingObject.java \
  modules/client-desktop/src/edu/hitsz/client/basic/AbstractFlyingObject.java \
  modules/server/src/edu/hitsz/server/ServerPlayerAircraft.java \
  modules/server/src/edu/hitsz/server/ServerHeroBullet.java \
  modules/server/src/edu/hitsz/server/aircraft \
  modules/server/src/edu/hitsz/server/basic \
  modules/client-desktop/src/edu/hitsz/client/aircraft \
  modules/client-desktop/src/edu/hitsz/client/basic \
  modules/client-desktop/src/edu/hitsz/client/bullet \
  modules/server/test/edu/hitsz/server/EntitySizingTest.java \
  modules/client-desktop/test/edu/hitsz/client/EntitySizingRenderTest.java
git commit -m "feat: decouple entity size from image assets"
```

### Task 3: Make client rendering use configured render size

**Files:**
- Modify: `modules/client-desktop/src/edu/hitsz/client/Game.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/aircraft/HeroAircraft.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/ImageManager.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/HeroSpawnSizingTest.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/ScaledDrawLayoutTest.java`

**Step 1: Write the failing tests**

- `HeroSpawnSizingTest.java` should assert hero initial Y depends on configured render height, not image height.
- `ScaledDrawLayoutTest.java` should assert `Game` computes draw rectangle from object `width/height`, not `image.getWidth()/image.getHeight()`.

**Step 2: Run tests to verify they fail**

Run:

```bash
javac -encoding UTF-8 $(find src modules/common/src modules/server/src modules/client-desktop/src modules/server/test modules/client-desktop/test test -name '*.java') -d /tmp/scene-plan-task3
java -ea -cp /tmp/scene-plan-task3 edu.hitsz.client.HeroSpawnSizingTest
java -ea -cp /tmp/scene-plan-task3 edu.hitsz.client.ScaledDrawLayoutTest
```

Expected: FAIL because render placement still depends on raw image size.

**Step 3: Write minimal implementation**

- Update `HeroAircraft.getSingleton()` spawn Y to use configured hero render height
- Update `Game.paintImageWithPositionRevised(...)` to call `drawImage(image, x, y, width, height, null)`
- Keep backgrounds using full-window stretch

**Step 4: Run tests to verify they pass**

Run the same commands from Step 2.

Expected: PASS

**Step 5: Commit**

```bash
git add modules/client-desktop/src/edu/hitsz/client/Game.java \
  modules/client-desktop/src/edu/hitsz/client/aircraft/HeroAircraft.java \
  modules/client-desktop/src/edu/hitsz/client/ImageManager.java \
  modules/client-desktop/test/edu/hitsz/client/HeroSpawnSizingTest.java \
  modules/client-desktop/test/edu/hitsz/client/ScaledDrawLayoutTest.java
git commit -m "feat: scale client rendering from entity sizing"
```

### Task 4: Add chapter catalog and server-side chapter progression state

**Files:**
- Create: `modules/server/src/edu/hitsz/server/ChapterCatalog.java`
- Create: `modules/server/src/edu/hitsz/server/ChapterProgressionState.java`
- Modify: `modules/server/src/edu/hitsz/server/GameplayBalance.java`
- Modify: `modules/server/src/edu/hitsz/server/ProgressionPolicy.java`
- Modify: `modules/server/src/edu/hitsz/server/ServerWorldState.java`
- Modify: `modules/server/src/edu/hitsz/server/RoomRuntime.java`
- Test: `modules/server/test/edu/hitsz/server/ChapterProgressionTest.java`
- Test: `modules/server/test/edu/hitsz/server/BossToUpgradePhaseTest.java`

**Step 1: Write the failing tests**

- `ChapterProgressionTest.java` should assert:
  - room starts in `CH1`
  - reaching chapter boss threshold spawns chapter boss
- `BossToUpgradePhaseTest.java` should assert:
  - boss defeat moves phase to `UPGRADE_SELECTION`
  - battle does not resume immediately

**Step 2: Run tests to verify they fail**

Run:

```bash
javac -encoding UTF-8 $(find src modules/common/src modules/server/src modules/client-desktop/src modules/server/test modules/client-desktop/test test -name '*.java') -d /tmp/scene-plan-task4
java -ea -cp /tmp/scene-plan-task4 edu.hitsz.server.ChapterProgressionTest
java -ea -cp /tmp/scene-plan-task4 edu.hitsz.server.BossToUpgradePhaseTest
```

Expected: FAIL because server has no chapter state or upgrade-selection phase yet.

**Step 3: Write minimal implementation**

- Add `ChapterCatalog` entries for `CH1`, `CH2`, `CH3`
- Add `ChapterProgressionState` with:
  - `chapterId`
  - `gamePhase`
  - `bossStage`
  - `flashUntilMillis`
- Move boss threshold and next chapter decisions through this state
- Pause combat progression while in `UPGRADE_SELECTION`

**Step 4: Run tests to verify they pass**

Run the same commands from Step 2.

Expected: PASS

**Step 5: Commit**

```bash
git add modules/server/src/edu/hitsz/server/ChapterCatalog.java \
  modules/server/src/edu/hitsz/server/ChapterProgressionState.java \
  modules/server/src/edu/hitsz/server/GameplayBalance.java \
  modules/server/src/edu/hitsz/server/ProgressionPolicy.java \
  modules/server/src/edu/hitsz/server/ServerWorldState.java \
  modules/server/src/edu/hitsz/server/RoomRuntime.java \
  modules/server/test/edu/hitsz/server/ChapterProgressionTest.java \
  modules/server/test/edu/hitsz/server/BossToUpgradePhaseTest.java
git commit -m "feat: add chapter-driven server progression"
```

### Task 5: Sync chapter and phase state into snapshots and client world state

**Files:**
- Modify: `modules/server/src/edu/hitsz/server/WorldSnapshotFactory.java`
- Modify: `modules/server/src/edu/hitsz/server/ServerGameLoop.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/ClientWorldState.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/DefaultSnapshotApplier.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/Game.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/ChapterSnapshotApplyTest.java`

**Step 1: Write the failing test**

`ChapterSnapshotApplyTest.java` should assert that:
- chapter id enters `ClientWorldState`
- phase enters `ClientWorldState`
- boss-transition flash flag enters `ClientWorldState`

**Step 2: Run test to verify it fails**

Run:

```bash
javac -encoding UTF-8 $(find src modules/common/src modules/server/src modules/client-desktop/src modules/server/test modules/client-desktop/test test -name '*.java') -d /tmp/scene-plan-task5
java -ea -cp /tmp/scene-plan-task5 edu.hitsz.client.ChapterSnapshotApplyTest
```

Expected: FAIL because client world state does not yet track chapter or phase.

**Step 3: Write minimal implementation**

- Populate chapter/phase/flash fields in `WorldSnapshotFactory`
- Store them in `ClientWorldState`
- Apply them in `DefaultSnapshotApplier`
- Reflect them in `Game`

**Step 4: Run test to verify it passes**

Run the same commands from Step 2.

Expected: PASS

**Step 5: Commit**

```bash
git add modules/server/src/edu/hitsz/server/WorldSnapshotFactory.java \
  modules/server/src/edu/hitsz/server/ServerGameLoop.java \
  modules/client-desktop/src/edu/hitsz/client/ClientWorldState.java \
  modules/client-desktop/src/edu/hitsz/client/DefaultSnapshotApplier.java \
  modules/client-desktop/src/edu/hitsz/client/Game.java \
  modules/client-desktop/test/edu/hitsz/client/ChapterSnapshotApplyTest.java
git commit -m "feat: sync chapter state to client snapshots"
```

### Task 6: Drive backgrounds and themed enemy visuals by chapter

**Files:**
- Modify: `modules/client-desktop/src/edu/hitsz/client/ImageManager.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/Game.java`
- Create: `modules/client-desktop/src/edu/hitsz/client/ChapterVisualCatalog.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/ChapterBackgroundSelectionTest.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/ChapterEnemyThemeSelectionTest.java`

**Step 1: Write the failing tests**

- `ChapterBackgroundSelectionTest.java` should assert that `CH1/CH2/CH3` map to different backgrounds.
- `ChapterEnemyThemeSelectionTest.java` should assert that the same enemy archetype can resolve chapter-specific image variants.

**Step 2: Run tests to verify they fail**

Run:

```bash
javac -encoding UTF-8 $(find src modules/common/src modules/server/src modules/client-desktop/src modules/server/test modules/client-desktop/test test -name '*.java') -d /tmp/scene-plan-task6
java -ea -cp /tmp/scene-plan-task6 edu.hitsz.client.ChapterBackgroundSelectionTest
java -ea -cp /tmp/scene-plan-task6 edu.hitsz.client.ChapterEnemyThemeSelectionTest
```

Expected: FAIL because visuals only depend on difficulty and bossActive.

**Step 3: Write minimal implementation**

- Add a chapter visual catalog on the client
- Make `ImageManager` resolve background and enemy/Boss textures by chapter
- Keep current `difficulty` field only for HUD and balance messaging, not visual scene selection

**Step 4: Run tests to verify they pass**

Run the same commands from Step 2.

Expected: PASS

**Step 5: Commit**

```bash
git add modules/client-desktop/src/edu/hitsz/client/ImageManager.java \
  modules/client-desktop/src/edu/hitsz/client/Game.java \
  modules/client-desktop/src/edu/hitsz/client/ChapterVisualCatalog.java \
  modules/client-desktop/test/edu/hitsz/client/ChapterBackgroundSelectionTest.java \
  modules/client-desktop/test/edu/hitsz/client/ChapterEnemyThemeSelectionTest.java
git commit -m "feat: add chapter-based scene visuals"
```

### Task 7: Add per-player upgrade selection after boss defeat

**Files:**
- Create: `modules/common/src/edu/hitsz/common/protocol/dto/UpgradeChoicePayload.java`
- Create: `modules/common/src/edu/hitsz/common/protocol/json/UpgradeChoicePayloadJsonMapper.java`
- Modify: `modules/common/src/edu/hitsz/common/protocol/MessageType.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/ClientCommandPublisher.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/SocketClientSession.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/HeroController.java`
- Modify: `modules/client-desktop/src/edu/hitsz/client/Game.java`
- Modify: `modules/server/src/edu/hitsz/server/LocalAuthorityServer.java`
- Modify: `modules/server/src/edu/hitsz/server/RoomRuntime.java`
- Modify: `modules/server/src/edu/hitsz/server/PlayerRuntimeState.java`
- Test: `modules/server/test/edu/hitsz/server/UpgradeChoiceApplicationTest.java`
- Test: `modules/client-desktop/test/edu/hitsz/client/UpgradeSelectionOverlayTest.java`
- Test: `modules/client-desktop/test/edu/hitsz/e2e/BossUpgradeSceneSmokeTest.java`

**Step 1: Write the failing tests**

- `UpgradeChoiceApplicationTest.java` should assert that:
  - boss defeat opens selection
  - player can submit one choice
  - choice modifies the player build
- `UpgradeSelectionOverlayTest.java` should assert that client shows overlay only during `UPGRADE_SELECTION`
- `BossUpgradeSceneSmokeTest.java` should assert end-to-end chapter progression through the upgrade phase

**Step 2: Run tests to verify they fail**

Run:

```bash
javac -encoding UTF-8 $(find src modules/common/src modules/server/src modules/client-desktop/src modules/server/test modules/client-desktop/test test -name '*.java') -d /tmp/scene-plan-task7
java -ea -cp /tmp/scene-plan-task7 edu.hitsz.server.UpgradeChoiceApplicationTest
java -ea -cp /tmp/scene-plan-task7 edu.hitsz.client.UpgradeSelectionOverlayTest
java -ea -cp /tmp/scene-plan-task7 edu.hitsz.e2e.BossUpgradeSceneSmokeTest
```

Expected: FAIL because no upgrade message type or overlay exists yet.

**Step 3: Write minimal implementation**

- Add `INPUT_UPGRADE_CHOICE`
- Add payload and JSON mapper
- Add per-player pending choice fields
- Pause world progression while waiting for player choices
- Resume next chapter only when all alive/connected players have chosen
- Add white-flash transition flag before opening the overlay

**Step 4: Run tests to verify they pass**

Run the same commands from Step 2.

Expected: PASS

**Step 5: Commit**

```bash
git add modules/common/src/edu/hitsz/common/protocol/dto/UpgradeChoicePayload.java \
  modules/common/src/edu/hitsz/common/protocol/json/UpgradeChoicePayloadJsonMapper.java \
  modules/common/src/edu/hitsz/common/protocol/MessageType.java \
  modules/client-desktop/src/edu/hitsz/client/ClientCommandPublisher.java \
  modules/client-desktop/src/edu/hitsz/client/SocketClientSession.java \
  modules/client-desktop/src/edu/hitsz/client/HeroController.java \
  modules/client-desktop/src/edu/hitsz/client/Game.java \
  modules/server/src/edu/hitsz/server/LocalAuthorityServer.java \
  modules/server/src/edu/hitsz/server/RoomRuntime.java \
  modules/server/src/edu/hitsz/server/PlayerRuntimeState.java \
  modules/server/test/edu/hitsz/server/UpgradeChoiceApplicationTest.java \
  modules/client-desktop/test/edu/hitsz/client/UpgradeSelectionOverlayTest.java \
  modules/client-desktop/test/edu/hitsz/e2e/BossUpgradeSceneSmokeTest.java
git commit -m "feat: add per-player boss upgrade selection"
```

### Task 8: Run full regression and refresh local runtime build

**Files:**
- Modify: `README.md`
- Modify: `PLAN.md`
- Modify: `scripts/build_runtime_with_assets.sh` if new assets or chapter images are added

**Step 1: Run focused test groups**

Run:

```bash
javac -encoding UTF-8 $(find src modules/common/src modules/server/src modules/client-desktop/src modules/server/test modules/client-desktop/test test -name '*.java') -d /tmp/scene-plan-final
```

Then run:

```bash
java -ea -cp /tmp/scene-plan-final edu.hitsz.protocol.ChapterSceneProtocolTest
java -ea -cp /tmp/scene-plan-final edu.hitsz.server.EntitySizingTest
java -ea -cp /tmp/scene-plan-final edu.hitsz.client.EntitySizingRenderTest
java -ea -cp /tmp/scene-plan-final edu.hitsz.server.ChapterProgressionTest
java -ea -cp /tmp/scene-plan-final edu.hitsz.server.BossToUpgradePhaseTest
java -ea -cp /tmp/scene-plan-final edu.hitsz.client.ChapterSnapshotApplyTest
java -ea -cp /tmp/scene-plan-final edu.hitsz.client.ChapterBackgroundSelectionTest
java -ea -cp /tmp/scene-plan-final edu.hitsz.server.UpgradeChoiceApplicationTest
java -ea -cp /tmp/scene-plan-final edu.hitsz.e2e.BossUpgradeSceneSmokeTest
```

**Step 2: Run main regression suite**

Run:

```bash
for cls in \
  edu.hitsz.protocol.ProtocolEnvelopeTest \
  edu.hitsz.protocol.JsonCodecSmokeTest \
  edu.hitsz.protocol.RoomSnapshotProtocolTest \
  edu.hitsz.client.SnapshotApplyTest \
  edu.hitsz.client.GameClientBoundaryTest \
  edu.hitsz.e2e.MultiClientBroadcastSmokeTest \
  edu.hitsz.e2e.RoomIsolationSmokeTest \
  edu.hitsz.server.FreezeBombShieldTest \
  edu.hitsz.server.SkillCooldownEnforcementTest \
  edu.hitsz.server.ServerWorldLoopTest \
  edu.hitsz.FeatureRegressionTest
do
  java -ea -cp /tmp/scene-plan-final $cls
done
```

Expected: PASS

**Step 3: Refresh runtime build**

Run:

```bash
./scripts/build_runtime_with_assets.sh /tmp/aircraftwar-scene-runtime
```

**Step 4: Update docs**

- Update `README.md` with:
  - chapter flow
  - upgrade controls
  - scene change explanation
- Update `PLAN.md` current status

**Step 5: Commit**

```bash
git add README.md PLAN.md scripts/build_runtime_with_assets.sh
git commit -m "docs: update chapter and upgrade runtime docs"
```
