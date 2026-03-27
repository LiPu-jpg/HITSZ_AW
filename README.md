# AircraftWar

## Current Layout

- `modules/common`: shared protocol, DTO, JSON codec, socket transport, shared constants
- `modules/server`: authoritative server runtime and `edu.hitsz.server.ServerMain`
- `modules/client-desktop`: Swing desktop client and `edu.hitsz.client.ClientMain`
- `src/edu/hitsz/application/Main.java`: compatibility wrapper that delegates to the new dev bootstrap

The old shared entity tree under `src/edu/hitsz/{aircraft,bullet,basic}` has been fully retired. Client render entities now live in `modules/client-desktop`, and server authority entities now live in `modules/server`.

## Build

```bash
javac -encoding UTF-8 $(find src modules/common/src modules/server/src modules/client-desktop/src modules/server/test modules/client-desktop/test test -name '*.java') -d /tmp/aircraftwar-build
```

Runtime build with bundled images:

```bash
./scripts/build_runtime_with_assets.sh /tmp/aircraftwar-runtime-with-assets
```

Latest local runtime bundle refreshed during Task 9:

```bash
./scripts/build_runtime_with_assets.sh /tmp/aircraftwar-branch-runtime
```

## Run

Standalone server:

```bash
java -cp /tmp/aircraftwar-build edu.hitsz.server.ServerMain
```

Standalone desktop client:

```bash
java -cp /tmp/aircraftwar-build edu.hitsz.client.ClientMain 127.0.0.1 20123 session-local
```

Client startup flow:

- first enter the launcher page
- choose `Create Room` or `Join Room`
- room host chooses room difficulty when creating a room
- joiners manually input room code
- click `Enter Room`
- then use the in-room ready/start flow

Round and chapter flow:

- room lobby -> all connected players ready -> host starts round
- battle begins in `CH1` with the same `STARTER_BLUE` aircraft for every player
- when the room reaches the current boss threshold, boss battle starts
- boss defeat enters a short white-flash transition
- after the first boss flash ends, each alive player gets an individual branch selection
- after later boss flashes end, each alive player gets an individual branch-local upgrade selection
- after all required upgrade choices are submitted, the room advances to the next chapter
- after the final chapter boss and upgrade resolution, the room returns to lobby

After the client enters a room lobby:

- `Enter`: toggle ready
- `S`: host starts the room after everyone is ready

During battle:

- drag the mouse to update the authoritative movement target
- branch weapons are fired by the server cadence after branch unlock

During first branch selection:

- `1 / 2 / 3`: choose `RED_SPEED / GREEN_DEFENSE / BLACK_HEAVY`

During upgrade selection:

- `1 / 2 / 3 / 4`: choose the branch-local upgrade shown in the on-screen list

Upgrade selection notes:

- the upgrade overlay is shown only in `UPGRADE_SELECTION`
- the white-flash transition must finish before the server accepts upgrade submissions
- upgrade choice order follows the server snapshot, not a client-local hard-coded mapping

Notes:

- room difficulty is room-level and chosen by the host
- the round starts only when all connected players are ready and the host sends start
- difficulty affects battle pacing and progression tempo; chapter visuals are driven by `chapterId`
- all players start as `STARTER_BLUE` and only use the starter baseline weapon before the first branch unlock
- the first boss unlocks aircraft branches instead of numeric upgrades
- later boss upgrades are branch-specific:
  - `RED_SPEED`: laser damage / width / duration / move speed
  - `GREEN_DEFENSE`: spread count / width / bullet damage / max HP
  - `BLACK_HEAVY`: airburst damage / radius / range / max HP
- current branch weapon baselines are:
  - `RED_SPEED`: forward laser sweep
  - `GREEN_DEFENSE`: spread-shot pattern
  - `BLACK_HEAVY`: visible explosive shell -> target-point or max-range airburst
- enemy attack baselines now vary by chapter:
  - `CH1`: original patterns
  - `CH2`: denser elite and boss spreads
  - `CH3`: explosive-shell elite and boss volleys
- the older `FREEZE / BOMB / SHIELD` server skill system still exists in code, but it is not part of the current branch-aircraft runtime flow

Background mapping:

- launcher: `bg.jpg`
- `CH1`: `bg2.jpg`
- `CH2`: `bg3.jpg`
- `CH3`: `bg4.jpg`
- boss encounter: `bg5.jpg`

Chapter visuals:

- background selection is chapter-based
- enemy visuals are chapter-tinted variants of the current enemy tier
- difficulty no longer directly selects the battle background

Local dev mode that starts a local server and then opens the desktop client:

```bash
java -cp /tmp/aircraftwar-build edu.hitsz.client.DevMain
```

Legacy compatibility entrypoint:

```bash
java -cp /tmp/aircraftwar-build edu.hitsz.application.Main
```
