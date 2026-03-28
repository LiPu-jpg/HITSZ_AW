package edu.hitsz.server;

public class PlayerMoveSpeedBalanceTest {

    public static void main(String[] args) {
        assert GameplayBalance.PLAYER_BASE_MOVE_SPEED == 16
                : "Player base move speed should be doubled to 16";
    }
}
