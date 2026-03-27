package edu.hitsz.client;

import edu.hitsz.client.aircraft.AbstractAircraft;
import edu.hitsz.client.aircraft.HeroAircraft;
import edu.hitsz.client.aircraft.OtherPlayer;
import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.ChapterId;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

import java.util.Arrays;

public class BranchPlaneRenderSelectionTest {

    public static void main(String[] args) {
        ClientWorldState state = new ClientWorldState();
        WorldSnapshot snapshot = new WorldSnapshot(2L);
        snapshot.addPlayerSnapshot(new PlayerSnapshot(
                "session-local",
                "player-local",
                220,
                320,
                900,
                88,
                false,
                2,
                "BOMB",
                0L,
                1000,
                java.util.Collections.emptyList(),
                null,
                AircraftBranch.GREEN_DEFENSE,
                Arrays.asList(AircraftBranch.RED_SPEED, AircraftBranch.GREEN_DEFENSE, AircraftBranch.BLACK_HEAVY),
                true
        ));
        snapshot.addPlayerSnapshot(new PlayerSnapshot(
                "session-remote",
                "player-remote",
                300,
                360,
                870,
                44,
                false,
                2,
                "FREEZE",
                0L,
                1000,
                java.util.Collections.emptyList(),
                null,
                AircraftBranch.RED_SPEED,
                java.util.Collections.emptyList(),
                true
        ));

        new DefaultSnapshotApplier().apply(snapshot, state, "session-local");

        assert state.getLocalAircraftBranch() == AircraftBranch.GREEN_DEFENSE
                : "Local branch should be synchronized into client world state";
        assert state.getLocalAvailableBranchChoices().equals(Arrays.asList(
                AircraftBranch.RED_SPEED,
                AircraftBranch.GREEN_DEFENSE,
                AircraftBranch.BLACK_HEAVY
        )) : "Branch choices should be synchronized into client world state";
        assert HeroAircraft.getSingleton().getAircraftBranch() == AircraftBranch.GREEN_DEFENSE
                : "Hero aircraft should update to the snapshot branch";
        assert ImageManager.get(HeroAircraft.getSingleton(), ChapterId.CH1) == ImageManager.GREEN_DEFENSE_IMAGE
                : "Defense branch should render with the defense branch image";

        AbstractAircraft aircraft = state.getPlayerAircrafts().get(0);
        assert aircraft instanceof OtherPlayer : "Remote player should render as OtherPlayer";
        OtherPlayer otherPlayer = (OtherPlayer) aircraft;
        assert otherPlayer.getAircraftBranch() == AircraftBranch.RED_SPEED
                : "Other players should carry their branch identity";
        assert ImageManager.get(otherPlayer, ChapterId.CH1) == ImageManager.RED_SPEED_IMAGE
                : "Remote speed branch should render with the speed branch image";
    }
}
