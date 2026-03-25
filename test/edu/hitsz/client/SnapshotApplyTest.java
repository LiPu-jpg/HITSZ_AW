package edu.hitsz.client;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.OtherPlayer;
import edu.hitsz.application.client.ClientWorldState;
import edu.hitsz.application.client.DefaultSnapshotApplier;
import edu.hitsz.application.protocol.dto.PlayerSnapshot;
import edu.hitsz.application.protocol.dto.WorldSnapshot;

public class SnapshotApplyTest {

    public static void main(String[] args) {
        ClientWorldState state = new ClientWorldState();
        assert state.getPlayerAircrafts().isEmpty();

        WorldSnapshot snapshot = new WorldSnapshot(5L);
        snapshot.addPlayerSnapshot(new PlayerSnapshot("session-local", "player-local", true, 200, 300, 900));
        snapshot.addPlayerSnapshot(new PlayerSnapshot("session-2", "player-2", false, 260, 340, 800));

        new DefaultSnapshotApplier().apply(snapshot, state);

        assert state.getPlayerAircrafts().size() == 2 : "Snapshot should create local and remote players";
        assert state.getPlayerAircrafts().get(0) == HeroAircraft.getSingleton()
                : "Local player should use HeroAircraft singleton";
        assert state.getPlayerAircrafts().get(1) instanceof OtherPlayer
                : "Remote player should become OtherPlayer";
    }
}
