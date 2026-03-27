package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

import java.util.List;

public class FirstBossOpensBranchSelectionTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "player-local", 0L);
        roomRuntime.updateReady("host-session", true);
        roomRuntime.startRoundIfHost("host-session");

        PlayerSession session = roomRuntime.findSession("host-session");
        ServerWorldState worldState = extractWorldState(roomRuntime);
        long nowMillis = 1_000L;

        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 0));
        worldState.syncProgressionState(nowMillis);
        assert worldState.isBossActive() : "Precondition failed: first boss should spawn before defeat";

        worldState.getEnemyAircrafts().clear();
        worldState.syncProgressionState(nowMillis + 100L);

        assert roomRuntime.getGamePhase() == GamePhase.BRANCH_SELECTION
                : "First boss defeat should open branch selection instead of upgrade selection";

        WorldSnapshot snapshot = roomRuntime.buildSnapshot(nowMillis + 100L);
        assert snapshot.isFirstBossBranchSelection()
                : "Snapshot should flag the first-boss branch-selection phase";

        List<PlayerSnapshot> players = snapshot.getPlayerSnapshots();
        assert players.size() == 1 : "Snapshot should include the alive player";
        assert players.get(0).getAvailableBranchChoices().contains(AircraftBranch.RED_SPEED)
                : "Alive players should receive branch options when branch selection opens";
        assert players.get(0).getAvailableBranchChoices().contains(AircraftBranch.GREEN_DEFENSE)
                : "Alive players should receive all configured branch options";
        assert players.get(0).getAvailableBranchChoices().contains(AircraftBranch.BLACK_HEAVY)
                : "Alive players should receive all configured branch options";
    }

    private static ServerWorldState extractWorldState(RoomRuntime roomRuntime) {
        try {
            java.lang.reflect.Field field = RoomRuntime.class.getDeclaredField("worldState");
            field.setAccessible(true);
            return (ServerWorldState) field.get(roomRuntime);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("RoomRuntime should retain a ServerWorldState", e);
        }
    }
}
