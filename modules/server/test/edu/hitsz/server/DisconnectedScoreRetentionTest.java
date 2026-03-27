package edu.hitsz.server;

import edu.hitsz.common.Difficulty;

public class DisconnectedScoreRetentionTest {

    public static void main(String[] args) {
        ServerWorldState worldState = new ServerWorldState();
        worldState.setDifficulty(Difficulty.NORMAL);
        worldState.startBattle();

        PlayerSession session = worldState.getSessionRegistry().create("session-local", "player-local");
        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 0));
        session.markDisconnected(100L);

        worldState.syncProgressionState(200L);

        assert worldState.getTotalScore() >= ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 0)
                : "Disconnected sessions retained by the room should still count toward progression state";
    }
}
