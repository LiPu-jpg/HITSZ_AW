package edu.hitsz.server;

import edu.hitsz.common.protocol.dto.ExplosionSnapshot;

public class AirburstProjectileState {

    private final String ownerSessionId;
    private final int originX;
    private final int originY;
    private final int burstX;
    private final int burstY;
    private final int projectileSpeed;
    private final int burstRadius;
    private final int damage;
    private int currentX;
    private int currentY;
    private boolean readyToBurst;

    public AirburstProjectileState(String ownerSessionId,
                                   int originX,
                                   int originY,
                                   int targetX,
                                   int targetY,
                                    int maxRange,
                                   int projectileSpeed,
                                   int burstRadius,
                                   int damage) {
        this.ownerSessionId = ownerSessionId;
        this.originX = originX;
        this.originY = originY;
        double distance = Math.hypot(targetX - originX, targetY - originY);
        if (distance > maxRange) {
            double ratio = maxRange / distance;
            this.burstX = originX + (int) Math.round((targetX - originX) * ratio);
            this.burstY = originY + (int) Math.round((targetY - originY) * ratio);
        } else {
            this.burstX = targetX;
            this.burstY = targetY;
        }
        this.projectileSpeed = Math.max(1, projectileSpeed);
        this.burstRadius = burstRadius;
        this.damage = damage;
        this.currentX = originX;
        this.currentY = originY;
        this.readyToBurst = false;
    }

    public String getOwnerSessionId() {
        return ownerSessionId;
    }

    public int getDamage() {
        return damage;
    }

    public int getBurstRadius() {
        return burstRadius;
    }

    public int getCurrentX() {
        return currentX;
    }

    public int getCurrentY() {
        return currentY;
    }

    public void advance() {
        if (readyToBurst) {
            return;
        }
        int deltaX = burstX - currentX;
        int deltaY = burstY - currentY;
        double remaining = Math.hypot(deltaX, deltaY);
        if (remaining <= projectileSpeed) {
            currentX = burstX;
            currentY = burstY;
            readyToBurst = true;
            return;
        }
        double ratio = projectileSpeed / remaining;
        currentX += (int) Math.round(deltaX * ratio);
        currentY += (int) Math.round(deltaY * ratio);
    }

    public boolean isReadyToBurst() {
        return readyToBurst;
    }

    public ExplosionSnapshot resolveBurst() {
        return new ExplosionSnapshot(burstX, burstY, burstRadius, GameplayBalance.BLACK_HEAVY_EXPLOSION_DURATION_TICKS);
    }
}
