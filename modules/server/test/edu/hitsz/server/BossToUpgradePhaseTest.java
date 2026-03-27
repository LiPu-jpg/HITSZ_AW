package edu.hitsz.server;

import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;

public class BossToUpgradePhaseTest {

    public static void main(String[] args) {
        ServerWorldState worldState = new ServerWorldState();
        worldState.setDifficulty(Difficulty.NORMAL);
        worldState.startBattle();
        PlayerSession session = worldState.getSessionRegistry().create("session-local", "player-local");
        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 0));

        worldState.syncProgressionState();
        assert worldState.isBossActive() : "Precondition failed: boss should be active before defeat";

        worldState.getEnemyAircrafts().clear();
        worldState.syncProgressionState();

        assert worldState.getChapterProgressionState().getGamePhase() == GamePhase.UPGRADE_SELECTION
                : "Boss defeat should move the server into upgrade selection";
        assert worldState.getChapterProgressionState().getFlashUntilMillis() > 0L
                : "Boss defeat should start a chapter transition flash window";

        int enemiesBeforeNextTick = worldState.getEnemyAircrafts().size();
        worldState.stepWorld(2_000L);

        assert worldState.getChapterProgressionState().getGamePhase() == GamePhase.UPGRADE_SELECTION
                : "Upgrade selection should not auto-return to battle on the next tick";
        assert worldState.getEnemyAircrafts().size() == enemiesBeforeNextTick
                : "No combat should resume while the server is waiting for upgrade choice";
    }
}
