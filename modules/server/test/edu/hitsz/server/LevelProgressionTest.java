package edu.hitsz.server;

public class LevelProgressionTest {

    public static void main(String[] args) {
        PlayerRuntimeState player = new PlayerRuntimeState("player-local");
        ProgressionPolicy policy = ProgressionPolicy.defaultPolicy();

        player.addScore(160);
        player.syncProgression(policy);

        assert player.getLevel() == 3 : "160 round score should promote the player to level 3";
        assert player.getAircraft().shoot("session-local").size() >= 2
                : "Level-up should increase the player's aircraft firepower";
    }
}
