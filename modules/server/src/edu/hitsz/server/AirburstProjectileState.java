package edu.hitsz.server;

import edu.hitsz.common.protocol.dto.ExplosionSnapshot;

public class AirburstProjectileState {

    private final String ownerSessionId;
    private final int originX;
    private final int originY;
    private final int targetX;
    private final int targetY;
    private final int maxRange;
    private final int burstRadius;
    private final int damage;

    public AirburstProjectileState(String ownerSessionId,
                                   int originX,
                                   int originY,
                                   int targetX,
                                   int targetY,
                                   int maxRange,
                                   int burstRadius,
                                   int damage) {
        this.ownerSessionId = ownerSessionId;
        this.originX = originX;
        this.originY = originY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.maxRange = maxRange;
        this.burstRadius = burstRadius;
        this.damage = damage;
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

    public ExplosionSnapshot resolveBurst() {
        int burstX = targetX;
        int burstY = targetY;
        double distance = Math.hypot(targetX - originX, targetY - originY);
        if (distance > maxRange) {
            double ratio = maxRange / distance;
            burstX = originX + (int) Math.round((targetX - originX) * ratio);
            burstY = originY + (int) Math.round((targetY - originY) * ratio);
        }
        return new ExplosionSnapshot(burstX, burstY, burstRadius, GameplayBalance.BLACK_HEAVY_EXPLOSION_DURATION_TICKS);
    }
}
