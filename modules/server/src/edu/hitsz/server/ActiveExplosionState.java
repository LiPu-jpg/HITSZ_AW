package edu.hitsz.server;

import edu.hitsz.common.protocol.dto.ExplosionSnapshot;

public class ActiveExplosionState {

    private final int x;
    private final int y;
    private final int radius;
    private int remainingTicks;

    public ActiveExplosionState(ExplosionSnapshot snapshot) {
        this.x = snapshot.getX();
        this.y = snapshot.getY();
        this.radius = snapshot.getRadius();
        this.remainingTicks = snapshot.getDurationTicks();
    }

    public ExplosionSnapshot toSnapshot() {
        return new ExplosionSnapshot(x, y, radius, remainingTicks);
    }

    public void advanceLifetime() {
        remainingTicks--;
    }

    public boolean isExpired() {
        return remainingTicks <= 0;
    }
}
