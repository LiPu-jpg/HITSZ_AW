package edu.hitsz.protocol;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.UpgradeChoice;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BranchSnapshotProtocolTest {

    public static void main(String[] args) {
        typedBranchFieldsAreExposed();
        branchStateRoundTripsInWorldSnapshot();
        legacySnapshotsDefaultMissingBranchFields();
    }

    private static void typedBranchFieldsAreExposed() {
        assert returnType(PlayerSnapshot.class, "getAircraftBranch") == AircraftBranch.class
                : "PlayerSnapshot should expose aircraft branch";
        assert returnType(PlayerSnapshot.class, "getAvailableBranchChoices") == List.class
                : "PlayerSnapshot should expose branch choices as a List";
        assert returnType(PlayerSnapshot.class, "isBranchUnlocked") == boolean.class
                : "PlayerSnapshot should expose branch unlocked state";
        assert returnType(WorldSnapshot.class, "isFirstBossBranchSelection") == boolean.class
                : "WorldSnapshot should expose first boss branch selection";
    }

    private static void branchStateRoundTripsInWorldSnapshot() {
        WorldSnapshot snapshot = new WorldSnapshot(99L);
        snapshot.setFirstBossBranchSelection(true);
        snapshot.addPlayerSnapshot(playerSnapshotWithBranchState(
                "session-branch",
                "player-branch",
                300,
                400,
                850,
                77,
                true,
                4,
                "BOMB",
                1800L,
                1400,
                Arrays.asList(UpgradeChoice.FIRE_RATE),
                UpgradeChoice.FIRE_RATE,
                AircraftBranch.GREEN_DEFENSE,
                Arrays.asList(AircraftBranch.STARTER_BLUE, AircraftBranch.GREEN_DEFENSE),
                true
        ));

        WorldSnapshot decoded = roundTrip(snapshot);

        assert decoded.isFirstBossBranchSelection()
                : "WorldSnapshot should serialize firstBossBranchSelection";
        PlayerSnapshot playerSnapshot = decoded.getPlayerSnapshots().get(0);
        assert AircraftBranch.GREEN_DEFENSE == playerSnapshot.getAircraftBranch()
                : "PlayerSnapshot should serialize aircraft branch";
        assert playerSnapshot.getAvailableBranchChoices().size() == 2
                : "PlayerSnapshot should serialize available branch choices";
        assert playerSnapshot.getAvailableBranchChoices().contains(AircraftBranch.STARTER_BLUE)
                : "PlayerSnapshot should serialize available branch choices";
        assert playerSnapshot.isBranchUnlocked()
                : "PlayerSnapshot should serialize branch unlocked state";
    }

    private static void legacySnapshotsDefaultMissingBranchFields() {
        String legacyJson = "{"
                + "\"tick\":12,"
                + "\"gameStarted\":false,"
                + "\"readyPlayerCount\":1,"
                + "\"connectedPlayerCount\":1,"
                + "\"difficulty\":\"NORMAL\","
                + "\"roomCode\":\"654321\","
                + "\"hostSessionId\":\"session-host\","
                + "\"totalScore\":40,"
                + "\"bossActive\":false,"
                + "\"nextBossScoreThreshold\":240,"
                + "\"players\":[{"
                + "\"sessionId\":\"session-branch\","
                + "\"playerId\":\"player-branch\","
                + "\"x\":300,"
                + "\"y\":400,"
                + "\"hp\":850,"
                + "\"score\":77,"
                + "\"ready\":true,"
                + "\"level\":4,"
                + "\"selectedSkill\":\"BOMB\","
                + "\"skillCooldownRemainingMillis\":1800,"
                + "\"maxHp\":1400,"
                + "\"availableUpgradeChoices\":[\"FIRE_RATE\"],"
                + "\"selectedUpgradeChoice\":\"FIRE_RATE\""
                + "}],"
                + "\"enemies\":[],"
                + "\"heroBullets\":[],"
                + "\"enemyBullets\":[],"
                + "\"items\":[]"
                + "}";

        WorldSnapshot decoded = new WorldSnapshotJsonMapper().fromJson(legacyJson);

        assert !decoded.isFirstBossBranchSelection()
                : "Legacy snapshots should default missing firstBossBranchSelection to false";
        PlayerSnapshot playerSnapshot = decoded.getPlayerSnapshots().get(0);
        assert playerSnapshot.getAircraftBranch() == null
                : "Legacy snapshots should default missing aircraftBranch to null";
        assert playerSnapshot.getAvailableBranchChoices().isEmpty()
                : "Legacy snapshots should default missing availableBranchChoices to empty";
        assert !playerSnapshot.isBranchUnlocked()
                : "Legacy snapshots should default missing branchUnlocked to false";
    }

    private static WorldSnapshot roundTrip(WorldSnapshot snapshot) {
        WorldSnapshotJsonMapper mapper = new WorldSnapshotJsonMapper();
        return mapper.fromJson(mapper.toJson(snapshot));
    }

    private static PlayerSnapshot playerSnapshotWithBranchState(
            String sessionId,
            String playerId,
            int x,
            int y,
            int hp,
            int score,
            boolean ready,
            int level,
            String selectedSkill,
            long cooldownRemainingMillis,
            int maxHp,
            List<UpgradeChoice> availableUpgradeChoices,
            UpgradeChoice selectedUpgradeChoice,
            AircraftBranch aircraftBranch,
            List<AircraftBranch> availableBranchChoices,
            boolean branchUnlocked
    ) {
        try {
            Constructor<PlayerSnapshot> constructor = PlayerSnapshot.class.getConstructor(
                    String.class,
                    String.class,
                    int.class,
                    int.class,
                    int.class,
                    int.class,
                    boolean.class,
                    int.class,
                    String.class,
                    long.class,
                    int.class,
                    List.class,
                    UpgradeChoice.class,
                    AircraftBranch.class,
                    List.class,
                    boolean.class
            );
            return constructor.newInstance(
                    sessionId,
                    playerId,
                    x,
                    y,
                    hp,
                    score,
                    ready,
                    level,
                    selectedSkill,
                    cooldownRemainingMillis,
                    maxHp,
                    availableUpgradeChoices,
                    selectedUpgradeChoice,
                    aircraftBranch,
                    availableBranchChoices,
                    branchUnlocked
            );
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("PlayerSnapshot should support branch state serialization", e);
        }
    }

    private static Class<?> returnType(Class<?> type, String methodName) {
        try {
            Method method = type.getMethod(methodName);
            return method.getReturnType();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Missing method: " + type.getName() + "." + methodName, e);
        }
    }
}
