package edu.hitsz.server;

import edu.hitsz.common.Difficulty;
import edu.hitsz.server.aircraft.BossEnemy;

public class BossTriggerTest {

    public static void main(String[] args) {
        ServerWorldState worldState = new ServerWorldState();
        worldState.setDifficulty(Difficulty.NORMAL);
        worldState.startBattle();

        PlayerSession session = worldState.getSessionRegistry().create("session-local", "player-local");
        session.getPlayerState().setScore(220);

        worldState.syncProgressionState();

        assert worldState.isBossActive() : "Boss phase should start once total score reaches the threshold";
        assert worldState.getEnemyAircrafts().size() == 1 : "Boss phase should spawn exactly one boss";
        assert worldState.getEnemyAircrafts().get(0) instanceof BossEnemy : "The spawned enemy should be a BossEnemy";
        assert worldState.getNextBossScoreThreshold() > 220 : "Next boss threshold should advance after the boss spawns";
    }
}
