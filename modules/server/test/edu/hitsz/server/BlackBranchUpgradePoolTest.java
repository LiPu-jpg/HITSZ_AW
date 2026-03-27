package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;

import java.util.Arrays;
import java.util.List;

public class BlackBranchUpgradePoolTest {

    public static void main(String[] args) {
        PlayerRuntimeState playerState = new PlayerRuntimeState("player-local");
        playerState.applyBranchChoice(AircraftBranch.BLACK_HEAVY);
        playerState.openUpgradeSelection();

        assert choiceNames(playerState.getAvailableUpgradeChoices()).equals(Arrays.asList(
                "AIRBURST_DAMAGE",
                "AIRBURST_RADIUS",
                "AIRBURST_RANGE",
                "MAX_HP"
        )) : "BLACK_HEAVY should receive only black-local upgrade choices";
    }

    private static List<String> choiceNames(List<?> choices) {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (Object choice : choices) {
            names.add(choice.toString());
        }
        return names;
    }
}
