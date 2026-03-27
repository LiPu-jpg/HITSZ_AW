package edu.hitsz.common.protocol.dto;

public class ExplosionSnapshot {

    private final int x;
    private final int y;
    private final int radius;
    private final int durationTicks;

    public ExplosionSnapshot(int x, int y, int radius, int durationTicks) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.durationTicks = durationTicks;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRadius() {
        return radius;
    }

    public int getDurationTicks() {
        return durationTicks;
    }
}
