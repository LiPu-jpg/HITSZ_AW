package edu.hitsz.common.protocol.dto;

public class LaserSnapshot {

    private final String ownerSessionId;
    private final int originX;
    private final int originY;
    private final double angle;
    private final int width;
    private final int length;
    private final int durationTicks;
    private final int damage;

    public LaserSnapshot(String ownerSessionId,
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
}
