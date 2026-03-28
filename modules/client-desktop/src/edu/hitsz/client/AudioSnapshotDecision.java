package edu.hitsz.client;

public class AudioSnapshotDecision {

    private final String loopTrack;
    private final boolean playExplosion;
    private final boolean playBulletHit;
    private final boolean playSupplyPickup;
    private final boolean playGameOver;

    public AudioSnapshotDecision(String loopTrack,
                                 boolean playExplosion,
                                 boolean playBulletHit,
                                 boolean playSupplyPickup,
                                 boolean playGameOver) {
        this.loopTrack = loopTrack;
        this.playExplosion = playExplosion;
        this.playBulletHit = playBulletHit;
        this.playSupplyPickup = playSupplyPickup;
        this.playGameOver = playGameOver;
    }

    public String getLoopTrack() {
        return loopTrack;
    }

    public boolean shouldPlayExplosion() {
        return playExplosion;
    }

    public boolean shouldPlayBulletHit() {
        return playBulletHit;
    }

    public boolean shouldPlaySupplyPickup() {
        return playSupplyPickup;
    }

    public boolean shouldPlayGameOver() {
        return playGameOver;
    }
}
