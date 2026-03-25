package edu.hitsz.application.protocol.dto;

public class InputMovePayload {

    private final int x;
    private final int y;

    public InputMovePayload(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
