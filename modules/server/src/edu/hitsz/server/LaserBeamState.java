package edu.hitsz.server;

public class LaserBeamState {

    private final String ownerSessionId;
    private final int originX;
    private final int originY;
    private final double angle;
    private final int width;
    private final int length;
    private int durationTicks;
    private final int damage;

    public LaserBeamState(String ownerSessionId,
                          int originX,
                          int originY,
                          double angle,
                          int width,
                          int length,
                          int durationTicks,
                          int damage) {
        this.ownerSessionId = ownerSessionId;
        this.originX = originX;
        this.originY = originY;
        this.angle = angle;
        this.width = width;
        this.length = length;
        this.durationTicks = durationTicks;
        this.damage = damage;
    }

    public String getOwnerSessionId() {
        return ownerSessionId;
    }

    public int getOriginX() {
        return originX;
    }

    public int getOriginY() {
        return originY;
    }

    public double getAngle() {
        return angle;
    }

    public int getWidth() {
        return width;
    }

    public int getLength() {
        return length;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public int getDamage() {
        return damage;
    }

    public int getEndX() {
        return originX + (int) Math.round(Math.cos(angle) * length);
    }

    public int getEndY() {
        return originY + (int) Math.round(Math.sin(angle) * length);
    }

    public void advanceLifetime() {
        durationTicks--;
    }

    public boolean isExpired() {
        return durationTicks <= 0;
    }
}
