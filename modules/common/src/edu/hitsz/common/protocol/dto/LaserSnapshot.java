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
    private final String style;
    private final double chargeRatio;

    public LaserSnapshot(String ownerSessionId,
                         int originX,
                         int originY,
                         double angle,
                         int width,
                         int length,
                         int durationTicks,
                         int damage) {
        this(ownerSessionId, originX, originY, angle, width, length, durationTicks, damage, "PLAYER_RED_SPEED", 1.0);
    }

    public LaserSnapshot(String ownerSessionId,
                         int originX,
                         int originY,
                         double angle,
                         int width,
                         int length,
                         int durationTicks,
                         int damage,
                         String style,
                         double chargeRatio) {
        this.ownerSessionId = ownerSessionId;
        this.originX = originX;
        this.originY = originY;
        this.angle = angle;
        this.width = width;
        this.length = length;
        this.durationTicks = durationTicks;
        this.damage = damage;
        this.style = style;
        this.chargeRatio = chargeRatio;
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

    public String getStyle() {
        return style;
    }

    public double getChargeRatio() {
        return chargeRatio;
    }
}
