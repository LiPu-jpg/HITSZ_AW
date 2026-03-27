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

Latest local runtime bundle refreshed during Task 8:

```bash
./scripts/build_runtime_with_assets.sh /tmp/aircraftwar-scene-runtime
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
- room host chooses difficulty and skill when creating a room
- joiners manually input room code and choose their own skill
- click `Enter Room`
- then use the in-room ready/start flow

Round and chapter flow:

- room lobby -> all connected players ready -> host starts round
- battle begins in `CH1`
- when the room reaches the current boss threshold, boss battle starts
- boss defeat enters a short white-flash transition
- after the flash ends, each alive player gets an individual upgrade selection
- after all required upgrade choices are submitted, the room advances to the next chapter
- after the final chapter boss and upgrade resolution, the room returns to lobby

After the client enters a room lobby:

- `1 / 2 / 3`: select `FREEZE / BOMB / SHIELD`
- `Enter`: toggle ready
- `S`: host starts the room after everyone is ready

During battle:

- `Space`: cast the selected skill

During upgrade selection:

- `1 / 2 / 3 / 4`: choose the upgrade shown in the on-screen list

Upgrade selection notes:

- the upgrade overlay is shown only in `UPGRADE_SELECTION`
- the white-flash transition must finish before the server accepts upgrade submissions
- upgrade choice order follows the server snapshot, not a client-local hard-coded mapping

Notes:

- room difficulty is room-level and chosen by the host
- non-host players can only change their own selected skill
- the round starts only when all connected players are ready and the host sends start
- difficulty affects battle pacing and progression tempo; chapter visuals are driven by `chapterId`

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
