package edu.hitsz.client;

import edu.hitsz.client.aircraft.HeroAircraft;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

public class GameLocalAliveStateTest {

    public static void main(String[] args) {
        HeroAircraft.getSingleton().setHp(888);

        Game game = new Game();
        game.setLocalSessionId("session-local");

        WorldSnapshot snapshot = new WorldSnapshot(1L);
        snapshot.addPlayerSnapshot(new PlayerSnapshot("session-other", "player-other", 200, 300, 900, 66));
        game.applyWorldSnapshot(snapshot);

        assert game.getLocalHp() == 0 : "Game local HP should reset when local player is absent from the snapshot";
        assert !game.isLocalPlayerAlive() : "Overlay/game-state alive checks should follow synchronized local HP";
        assert HeroAircraft.getSingleton().getHp() == 888 : "Hero singleton value should not define local alive state by itself";
    }
}
