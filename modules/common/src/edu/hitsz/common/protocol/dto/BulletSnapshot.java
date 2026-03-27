package edu.hitsz.common.protocol.dto;

public class BulletSnapshot {

    private final String type;
    private final int x;
    private final int y;

    public BulletSnapshot(String type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public String getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
