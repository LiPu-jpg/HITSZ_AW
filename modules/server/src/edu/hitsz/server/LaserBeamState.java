package edu.hitsz.server;

public class LaserBeamState {

    private final String ownerSessionId;
    private final int originX;
    private final int originY;
    private final double angle;
    private final int width;
    private final int length;
    private int durationTicks;
    private final int initialDurationTicks;
    private final int damage;
    private final String style;
    private final String nextPhaseStyle;
    private final int nextPhaseDurationTicks;
    private final int nextPhaseDamage;

    public LaserBeamState(String ownerSessionId,
                          int originX,
                          int originY,
                          double angle,
                          int width,
                          int length,
                          int durationTicks,
                          int damage) {
        this(ownerSessionId, originX, originY, angle, width, length, durationTicks, damage, "PLAYER_RED_SPEED", null, 0, 0);
    }

    public LaserBeamState(String ownerSessionId,
                          int originX,
                          int originY,
                          double angle,
                          int width,
                          int length,
                          int durationTicks,
                          int damage,
                          String style,
                          String nextPhaseStyle,
                          int nextPhaseDurationTicks,
                          int nextPhaseDamage) {
        this.ownerSessionId = ownerSessionId;
        this.originX = originX;
        this.originY = originY;
        this.angle = angle;
        this.width = width;
        this.length = length;
        this.durationTicks = durationTicks;
        this.initialDurationTicks = durationTicks;
        this.damage = damage;
        this.style = style;
        this.nextPhaseStyle = nextPhaseStyle;
        this.nextPhaseDurationTicks = nextPhaseDurationTicks;
        this.nextPhaseDamage = nextPhaseDamage;
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
        if ("BOSS_WARNING".equals(style) && initialDurationTicks > 0) {
            return Math.max(0.0, Math.min(1.0, 1.0 - (double) durationTicks / (double) initialDurationTicks));
        }
        return 1.0;
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

    public boolean isHostileBossLaser() {
        return "BOSS_WARNING".equals(style) || "BOSS_FIRING".equals(style);
    }

    public LaserBeamState buildNextPhase() {
        if (nextPhaseStyle == null) {
            return null;
        }
        return new LaserBeamState(
                ownerSessionId,
                originX,
                originY,
                angle,
                width,
                length,
                nextPhaseDurationTicks,
                nextPhaseDamage,
                nextPhaseStyle,
                null,
                0,
                0
        );
    }
}
