package edu.hitsz.client;

import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

public class DeadPlayerVisibilityTest {

    public static void main(String[] args) {
        Game game = new Game();
        game.setLocalSessionId("session-local");

        WorldSnapshot snapshot = new WorldSnapshot(1L);
        snapshot.addPlayerSnapshot(new PlayerSnapshot("session-local", "player-local", 300, 520, 0, 42));
        snapshot.addPlayerSnapshot(new PlayerSnapshot("session-2", "player-2", 120, 240, 0, 20));

        game.applyWorldSnapshot(snapshot);

        assert game.getLocalHp() == 0 : "Local HP should still reflect the synchronized dead state";
        assert game.getPlayerAircrafts().isEmpty()
                : "Dead local and remote players should not remain in the renderable aircraft list";
    }
}
