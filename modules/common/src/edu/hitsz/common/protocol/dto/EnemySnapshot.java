package edu.hitsz.common.protocol.dto;

public class EnemySnapshot {

    private final String type;
    private final int x;
    private final int y;
    private final int hp;

    public EnemySnapshot(String type, int x, int y, int hp) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.hp = hp;
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

    public int getHp() {
        return hp;
    }
}
