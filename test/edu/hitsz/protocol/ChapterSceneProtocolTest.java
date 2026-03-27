package edu.hitsz.protocol;

import edu.hitsz.common.ChapterId;
import edu.hitsz.common.GamePhase;
import edu.hitsz.common.BranchUpgradeChoice;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

public class ChapterSceneProtocolTest {

    public static void main(String[] args) {
        typedProtocolContractIsExposed();
        nullUpgradeChoicesNormalizeToEmptyList();
        nullEnumInputsNormalizeToDefaults();
        legacySnapshotDefaultsMissingUpgradeFields();
    }

    private static void typedProtocolContractIsExposed() {
        assert returnType(WorldSnapshot.class, "getGamePhase") == GamePhase.class
                : "WorldSnapshot gamePhase should be typed as GamePhase";
        assert returnType(WorldSnapshot.class, "getChapterId") == ChapterId.class
                : "WorldSnapshot chapterId should be typed as ChapterId";
        assert returnType(PlayerSnapshot.class, "getAvailableUpgradeChoices") == java.util.List.class
                : "PlayerSnapshot upgrade choices should be a List";
        assert returnType(PlayerSnapshot.class, "getSelectedUpgradeChoice") == BranchUpgradeChoice.class
                : "PlayerSnapshot selected upgrade choice should be typed as BranchUpgradeChoice";

        WorldSnapshot snapshot = new WorldSnapshot(12L);
        snapshot.setGameStarted(true);
        snapshot.setGamePhase(GamePhase.UPGRADE_SELECTION);
        snapshot.setChapterId(ChapterId.CH2);
        snapshot.setChapterTransitionFlash(true);
        snapshot.addPlayerSnapshot(playerSnapshotWithTypedChoices(
                "session-local",
                "player-local",
                200,
                300,
                900,
                66,
                false,
                3,
                "FREEZE",
                1500L,
                1200,
                Arrays.asList(BranchUpgradeChoice.LASER_DAMAGE, BranchUpgradeChoice.LASER_WIDTH),
                BranchUpgradeChoice.LASER_DAMAGE
        ));

        WorldSnapshot decoded = roundTrip(snapshot);

        assert GamePhase.UPGRADE_SELECTION == decoded.getGamePhase()
                : "WorldSnapshot should serialize game phase";
        assert ChapterId.CH2 == decoded.getChapterId()
                : "WorldSnapshot should serialize chapter id";
        assert decoded.isChapterTransitionFlash()
                : "WorldSnapshot should serialize chapter transition flash";
        PlayerSnapshot playerSnapshot = decoded.getPlayerSnapshots().get(0);
        assert playerSnapshot.getMaxHp() == 1200
                : "PlayerSnapshot should serialize max hp";
        assert playerSnapshot.getAvailableUpgradeChoices().size() == 2
                : "PlayerSnapshot should serialize available upgrade choices";
        assert playerSnapshot.getAvailableUpgradeChoices().contains(BranchUpgradeChoice.LASER_DAMAGE)
                : "PlayerSnapshot should serialize available upgrade choices";
        assert BranchUpgradeChoice.LASER_DAMAGE == playerSnapshot.getSelectedUpgradeChoice()
                : "PlayerSnapshot should serialize selected upgrade choice";
    }

    private static void nullUpgradeChoicesNormalizeToEmptyList() {
        PlayerSnapshot snapshot = playerSnapshotWithTypedChoices(
                "session-local",
                "player-local",
                200,
                300,
                900,
                66,
                false,
                3,
                "FREEZE",
                0L,
                1000,
                null,
                null
        );

        assert snapshot.getAvailableUpgradeChoices().isEmpty()
                : "Null availableUpgradeChoices should normalize to empty list";
        try {
            snapshot.getAvailableUpgradeChoices().add(BranchUpgradeChoice.LASER_DAMAGE);
            throw new AssertionError("Normalized availableUpgradeChoices should be immutable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    private static void nullEnumInputsNormalizeToDefaults() {
        WorldSnapshot snapshot = new WorldSnapshot(21L);
        snapshot.setGamePhase(null);
        snapshot.setChapterId(null);

        WorldSnapshot decoded = roundTrip(snapshot);

        assert GamePhase.LOBBY == decoded.getGamePhase()
                : "Null game phase should normalize to the default";
        assert ChapterId.CH1 == decoded.getChapterId()
                : "Null chapter id should normalize to the default";
    }

    private static void legacySnapshotDefaultsMissingUpgradeFields() {
        String legacyJson = "{"
                + "\"tick\":9,"
                + "\"gameStarted\":false,"
                + "\"readyPlayerCount\":1,"
                + "\"connectedPlayerCount\":1,"
                + "\"difficulty\":\"NORMAL\","
                + "\"roomCode\":\"654321\","
                + "\"hostSessionId\":\"session-host\","
                + "\"totalScore\":90,"
                + "\"bossActive\":false,"
                + "\"nextBossScoreThreshold\":240,"
                + "\"players\":[{"
                + "\"sessionId\":\"session-local\","
                + "\"playerId\":\"player-local\","
                + "\"x\":200,"
                + "\"y\":300,"
                + "\"hp\":620,"
                + "\"score\":40,"
                + "\"ready\":false,"
                + "\"level\":2,"
                + "\"selectedSkill\":\"FREEZE\""
                + "}],"
                + "\"enemies\":[],"
                + "\"heroBullets\":[],"
                + "\"enemyBullets\":[],"
                + "\"items\":[]"
                + "}";

        WorldSnapshot decoded = new WorldSnapshotJsonMapper().fromJson(legacyJson);

        assert GamePhase.LOBBY == decoded.getGamePhase()
                : "Legacy snapshot should default missing phase";
        assert ChapterId.CH1 == decoded.getChapterId()
                : "Legacy snapshot should default missing chapter";
        PlayerSnapshot playerSnapshot = decoded.getPlayerSnapshots().get(0);
        assert playerSnapshot.getMaxHp() == 1000
                : "Legacy snapshot should default missing maxHp to the historical cap";
        assert playerSnapshot.getAvailableUpgradeChoices().isEmpty()
                : "Legacy snapshot should default missing upgrade choices to empty";
        assert playerSnapshot.getSelectedUpgradeChoice() == null
                : "Legacy snapshot should default missing selected upgrade choice to null";
    }

    private static WorldSnapshot roundTrip(WorldSnapshot snapshot) {
        WorldSnapshotJsonMapper mapper = new WorldSnapshotJsonMapper();
        return mapper.fromJson(mapper.toJson(snapshot));
    }

    private static PlayerSnapshot playerSnapshotWithTypedChoices(
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
            java.util.List<BranchUpgradeChoice> availableUpgradeChoices,
            BranchUpgradeChoice selectedUpgradeChoice
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
                    java.util.List.class,
                    BranchUpgradeChoice.class
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
                    selectedUpgradeChoice
            );
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("PlayerSnapshot should support typed upgrade choices", e);
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
