package edu.hitsz.client;

import edu.hitsz.common.ChapterId;
import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;
import edu.hitsz.server.PlayerSession;
import edu.hitsz.server.ProgressionPolicy;
import edu.hitsz.server.RoomRuntime;
import edu.hitsz.server.WorldSnapshotFactory;

public class ChapterSnapshotPipelineTest {

    public static void main(String[] args) {
        long nowMillis = System.currentTimeMillis();
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "session-local", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("session-local", "player-local", nowMillis);
        roomRuntime.findSession("session-local").getPlayerState().setSelectedSkill("FREEZE");
        roomRuntime.updateReady("session-local", true);
        roomRuntime.startRoundIfHost("session-local");

        PlayerSession session = roomRuntime.findSession("session-local");
        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 0));
        extractWorldState(roomRuntime).syncProgressionState(nowMillis);
        extractWorldState(roomRuntime).getEnemyAircrafts().clear();
        extractWorldState(roomRuntime).syncProgressionState(nowMillis + 100L);

        WorldSnapshot roomSnapshot = roomRuntime.buildSnapshot();
        WorldSnapshot decodedSnapshot = new WorldSnapshotJsonMapper().fromJson(new WorldSnapshotJsonMapper().toJson(roomSnapshot));
        ClientWorldState clientWorldState = new ClientWorldState();
        new DefaultSnapshotApplier().apply(decodedSnapshot, clientWorldState, "session-local");

        assert clientWorldState.getChapterId() == ChapterId.CH1
                : "Server->JSON->client pipeline should preserve chapter";
        assert clientWorldState.getGamePhase() == GamePhase.UPGRADE_SELECTION
                : "Server->JSON->client pipeline should preserve phase";

        WorldSnapshot deterministicFlashSnapshot = new WorldSnapshotFactory().create(
                extractWorldState(roomRuntime),
                roomRuntime.getChapterProgressionState().getFlashUntilMillis() - 1L
        );
        ClientWorldState flashState = new ClientWorldState();
        new DefaultSnapshotApplier().apply(
                new WorldSnapshotJsonMapper().fromJson(new WorldSnapshotJsonMapper().toJson(deterministicFlashSnapshot)),
                flashState,
                "session-local"
        );
        assert flashState.isChapterTransitionFlash()
                : "Server->JSON->client pipeline should preserve transition flash state";
    }

    private static edu.hitsz.server.ServerWorldState extractWorldState(RoomRuntime roomRuntime) {
        try {
            java.lang.reflect.Field field = RoomRuntime.class.getDeclaredField("worldState");
            field.setAccessible(true);
            return (edu.hitsz.server.ServerWorldState) field.get(roomRuntime);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("RoomRuntime should retain a ServerWorldState", e);
        }
    }
}
