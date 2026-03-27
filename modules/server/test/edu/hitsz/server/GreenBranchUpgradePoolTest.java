package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;

import java.util.Arrays;
import java.util.List;

public class GreenBranchUpgradePoolTest {

    public static void main(String[] args) {
        PlayerRuntimeState playerState = new PlayerRuntimeState("player-local");
        playerState.applyBranchChoice(AircraftBranch.GREEN_DEFENSE);
        playerState.openUpgradeSelection();

        assert choiceNames(playerState.getAvailableUpgradeChoices()).equals(Arrays.asList(
                "SPREAD_COUNT",
                "SPREAD_WIDTH",
                "BULLET_DAMAGE",
                "MAX_HP"
        )) : "GREEN_DEFENSE should receive only green-local upgrade choices";
    }

    private static List<String> choiceNames(List<?> choices) {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (Object choice : choices) {
            names.add(choice.toString());
        }
        return names;
    }
}
