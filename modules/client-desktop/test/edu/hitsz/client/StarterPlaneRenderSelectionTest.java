package edu.hitsz.client;

import edu.hitsz.client.aircraft.HeroAircraft;
import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.ChapterId;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

public class StarterPlaneRenderSelectionTest {

    public static void main(String[] args) {
        Game game = new Game();
        game.setLocalSessionId("session-local");

        WorldSnapshot snapshot = new WorldSnapshot(1L);
        snapshot.addPlayerSnapshot(new PlayerSnapshot(
                "session-local",
                "player-local",
                200,
                300,
                900,
                66,
                false,
                1,
                null,
                0L,
                1000,
                java.util.Collections.emptyList(),
                null,
                AircraftBranch.STARTER_BLUE,
                java.util.Collections.emptyList(),
                false
        ));

        game.applyWorldSnapshot(snapshot);

        assert game.getLocalAircraftBranch() == AircraftBranch.STARTER_BLUE
                : "Local starter branch should come from the snapshot";
        assert HeroAircraft.getSingleton().getAircraftBranch() == AircraftBranch.STARTER_BLUE
                : "Hero aircraft should carry starter branch identity";
        assert ImageManager.get(HeroAircraft.getSingleton(), ChapterId.CH1) == ImageManager.STARTER_BLUE_IMAGE
                : "Starter branch should render with the starter plane image";
    }
}
