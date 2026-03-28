package edu.hitsz.client;

public class GameAudioStateMachine {

    private boolean previousGameStarted;
    private boolean previousBossActive;
    private int previousLocalHp = -1;
    private int previousExplosionCount;

    public AudioSnapshotDecision onSnapshot(boolean gameStarted,
                                            boolean bossActive,
                                            int localHp,
                                            int explosionCount) {
        String loopTrack = gameStarted ? (bossActive ? "bgm_boss.wav" : "bgm.wav") : null;
        boolean playExplosion = explosionCount > previousExplosionCount;
        boolean playGameOver = previousLocalHp > 0 && localHp <= 0;

        previousGameStarted = gameStarted;
        previousBossActive = bossActive;
        previousLocalHp = localHp;
        previousExplosionCount = explosionCount;

        return new AudioSnapshotDecision(loopTrack, playExplosion, playGameOver);
    }
}
