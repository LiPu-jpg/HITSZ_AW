package edu.hitsz.protocol;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.GamePhase;
import edu.hitsz.common.protocol.MessageType;
import edu.hitsz.common.protocol.dto.BranchChoicePayload;
import edu.hitsz.common.protocol.json.BranchChoicePayloadJsonMapper;

public class BranchChoiceProtocolTest {

    public static void main(String[] args) {
        enumAndMessageTypesExposeBranchSelection();
        payloadRoundTripsBranchChoice();
    }

    private static void enumAndMessageTypesExposeBranchSelection() {
        assert AircraftBranch.STARTER_BLUE.name().equals("STARTER_BLUE")
                : "AircraftBranch should expose STARTER_BLUE";
        assert AircraftBranch.RED_SPEED.name().equals("RED_SPEED")
                : "AircraftBranch should expose RED_SPEED";
        assert AircraftBranch.GREEN_DEFENSE.name().equals("GREEN_DEFENSE")
                : "AircraftBranch should expose GREEN_DEFENSE";
        assert AircraftBranch.BLACK_HEAVY.name().equals("BLACK_HEAVY")
                : "AircraftBranch should expose BLACK_HEAVY";
        assert MessageType.INPUT_BRANCH_CHOICE == MessageType.valueOf("INPUT_BRANCH_CHOICE")
                : "MessageType should expose INPUT_BRANCH_CHOICE";
        assert GamePhase.BRANCH_SELECTION == GamePhase.valueOf("BRANCH_SELECTION")
                : "GamePhase should expose BRANCH_SELECTION";
    }

    private static void payloadRoundTripsBranchChoice() {
        BranchChoicePayloadJsonMapper mapper = new BranchChoicePayloadJsonMapper();
        BranchChoicePayload original = new BranchChoicePayload(AircraftBranch.RED_SPEED.name());

        String json = mapper.toJson(original);
        BranchChoicePayload decoded = mapper.fromJson(json);

        assert AircraftBranch.RED_SPEED.name().equals(decoded.getBranch())
                : "Branch choice should survive JSON mapping";
    }
}
