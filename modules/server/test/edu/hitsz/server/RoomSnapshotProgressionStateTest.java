package edu.hitsz.server;

import edu.hitsz.common.ChapterId;
import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

import java.lang.reflect.Field;

public class RoomSnapshotProgressionStateTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "player-local", 0L);
        roomRuntime.findSession("host-session").getPlayerState().setSelectedSkill("FREEZE");

        WorldSnapshot lobbySnapshot = roomRuntime.buildSnapshot();
        assert lobbySnapshot.getChapterId() == ChapterId.CH1 : "Lobby snapshot should expose the current chapter";
        assert lobbySnapshot.getGamePhase() == GamePhase.LOBBY : "Lobby snapshot should expose the current phase";

        roomRuntime.updateReady("host-session", true);
        roomRuntime.startRoundIfHost("host-session");

        ServerWorldState worldState = extractWorldState(roomRuntime);
        PlayerSession session = roomRuntime.findSession("host-session");
        long nowMillis = System.currentTimeMillis();
        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 0));

        worldState.syncProgressionState(nowMillis);
        worldState.getEnemyAircrafts().clear();
        worldState.syncProgressionState(nowMillis + 100L);

        WorldSnapshot upgradeSnapshot = roomRuntime.buildSnapshot();
        assert upgradeSnapshot.getChapterId() == ChapterId.CH1
                : "Snapshot should preserve the current chapter while waiting for upgrade selection";
        assert upgradeSnapshot.getGamePhase() == GamePhase.UPGRADE_SELECTION
                : "Snapshot should expose the current game phase";
        assert upgradeSnapshot.isChapterTransitionFlash()
                : "Snapshot should expose an active chapter transition flash window";
    }

    private static ServerWorldState extractWorldState(RoomRuntime roomRuntime) {
        try {
            Field field = RoomRuntime.class.getDeclaredField("worldState");
            field.setAccessible(true);
            return (ServerWorldState) field.get(roomRuntime);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("RoomRuntime should retain a ServerWorldState", e);
        }
    }
}
