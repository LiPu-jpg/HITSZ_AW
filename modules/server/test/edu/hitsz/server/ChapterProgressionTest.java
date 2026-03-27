package edu.hitsz.server;

import edu.hitsz.common.ChapterId;
import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;
import edu.hitsz.server.aircraft.BossEnemy;

public class ChapterProgressionTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        assert roomRuntime.getChapterId() == ChapterId.CH1 : "Room progression should start at CH1";
        assert roomRuntime.getGamePhase() == GamePhase.LOBBY : "New room should start in lobby phase";

        ServerWorldState worldState = new ServerWorldState();
        worldState.setDifficulty(Difficulty.NORMAL);
        worldState.startBattle();
        PlayerSession session = worldState.getSessionRegistry().create("session-local", "player-local");
        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 0));

        worldState.syncProgressionState();

        assert worldState.getChapterId() == ChapterId.CH1
                : "Server chapter progression should start at CH1";
        assert worldState.isBossActive() : "Boss should spawn when chapter boss threshold is reached";
        assert worldState.getEnemyAircrafts().size() == 1 : "Boss spawn should replace other enemies";
        assert worldState.getEnemyAircrafts().get(0) instanceof BossEnemy : "Spawned enemy should be a boss";

        worldState.getChapterProgressionState().advanceToNextChapter();
        assert worldState.getChapterId() == ChapterId.CH2
                : "Chapter progression state should be able to advance to the next configured chapter";
    }
}
