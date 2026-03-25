package edu.hitsz.application.server;

import edu.hitsz.application.protocol.dto.PlayerSnapshot;
import edu.hitsz.application.protocol.dto.WorldSnapshot;

public class WorldSnapshotFactory {

    public WorldSnapshot create(ServerWorldState worldState) {
        return createForSession(worldState, null);
    }

    public WorldSnapshot createForSession(ServerWorldState worldState, String receiverSessionId) {
        WorldSnapshot snapshot = new WorldSnapshot(worldState.getTick());
        for (PlayerSession session : worldState.getPlayerSessions()) {
            PlayerRuntimeState playerState = session.getPlayerState();
            snapshot.addPlayerSnapshot(new PlayerSnapshot(
                    session.getSessionId(),
                    session.getPlayerId(),
                    session.getSessionId().equals(receiverSessionId),
                    playerState.getX(),
                    playerState.getY(),
                    playerState.getHp()
            ));
        }
        return snapshot;
    }
}
