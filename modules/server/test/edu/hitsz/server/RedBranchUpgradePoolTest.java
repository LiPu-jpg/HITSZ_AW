package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;

import java.util.Arrays;
import java.util.List;

public class RedBranchUpgradePoolTest {

    public static void main(String[] args) {
        PlayerRuntimeState playerState = new PlayerRuntimeState("player-local");
        playerState.applyBranchChoice(AircraftBranch.RED_SPEED);
        playerState.openUpgradeSelection();

        assert choiceNames(playerState.getAvailableUpgradeChoices()).equals(Arrays.asList(
                "LASER_DAMAGE",
                "LASER_WIDTH",
                "LASER_DURATION",
                "MOVE_SPEED"
        )) : "RED_SPEED should receive only red-local upgrade choices";
    }

    private static List<String> choiceNames(List<?> choices) {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (Object choice : choices) {
            names.add(choice.toString());
        }
        return names;
    }
}
