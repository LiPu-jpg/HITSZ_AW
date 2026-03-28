package edu.hitsz.client;

public class GameAudioStateMachine {

    private boolean previousGameStarted;
    private boolean previousBossActive;
    private int previousLocalHp = -1;
    private int previousExplosionCount;
    private int previousBulletHitCount;
    private int previousSupplyPickupCount;

    public AudioSnapshotDecision onSnapshot(boolean gameStarted,
                                            boolean bossActive,
                                            int localHp,
                                            int explosionCount) {
        return onSnapshot(gameStarted, bossActive, localHp, explosionCount, 0, 0);
    }

    public AudioSnapshotDecision onSnapshot(boolean gameStarted,
                                            boolean bossActive,
                                            int localHp,
                                            int explosionCount,
                                            int bulletHitCount,
                                            int supplyPickupCount) {
        String loopTrack = gameStarted ? (bossActive ? "bgm_boss.wav" : "bgm.wav") : null;
        boolean playExplosion = explosionCount > previousExplosionCount;
        boolean playBulletHit = bulletHitCount > previousBulletHitCount;
        boolean playSupplyPickup = supplyPickupCount > previousSupplyPickupCount;
        boolean playGameOver = previousLocalHp > 0 && localHp <= 0;

        previousGameStarted = gameStarted;
        previousBossActive = bossActive;
        previousLocalHp = localHp;
        previousExplosionCount = explosionCount;
        previousBulletHitCount = bulletHitCount;
        previousSupplyPickupCount = supplyPickupCount;

        return new AudioSnapshotDecision(loopTrack, playExplosion, playBulletHit, playSupplyPickup, playGameOver);
    }
}
