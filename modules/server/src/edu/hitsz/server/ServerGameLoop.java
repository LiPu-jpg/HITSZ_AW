package edu.hitsz.server;

import edu.hitsz.common.protocol.dto.WorldSnapshot;

public class ServerGameLoop {

    private final ServerWorldState worldState;
    private final WorldSnapshotFactory snapshotFactory;

    public ServerGameLoop(ServerWorldState worldState) {
        this(worldState, new WorldSnapshotFactory());
    }

    public ServerGameLoop(ServerWorldState worldState, WorldSnapshotFactory snapshotFactory) {
        this.worldState = worldState;
        this.snapshotFactory = snapshotFactory;
    }

    public void stepOnce() {
        stepOnce(System.currentTimeMillis());
    }

    public void stepOnce(long nowMillis) {
        worldState.advanceTick();
        worldState.stepWorld(nowMillis);
    }

    public WorldSnapshot buildSnapshot() {
        return buildSnapshot(System.currentTimeMillis());
    }

    public WorldSnapshot buildSnapshot(long nowMillis) {
        return snapshotFactory.create(worldState, nowMillis);
    }
}
