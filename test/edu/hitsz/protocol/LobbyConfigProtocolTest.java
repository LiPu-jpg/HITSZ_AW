package edu.hitsz.protocol;

import edu.hitsz.common.Difficulty;
import edu.hitsz.common.protocol.dto.LobbyConfigPayload;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.LobbyConfigPayloadJsonMapper;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;

import java.lang.reflect.Method;

public class LobbyConfigProtocolTest {

    public static void main(String[] args) {
        lobbyConfigPayloadRoundTrips();
        lobbyConfigPayloadDoesNotExposeSelectedSkill();
        progressionFieldsRoundTripInWorldSnapshot();
    }

    private static void lobbyConfigPayloadRoundTrips() {
        LobbyConfigPayloadJsonMapper mapper = new LobbyConfigPayloadJsonMapper();
        LobbyConfigPayload original = new LobbyConfigPayload(Difficulty.HARD.name(), "BOMB");

        String json = mapper.toJson(original);
        LobbyConfigPayload decoded = mapper.fromJson(json);

        assert Difficulty.HARD.name().equals(decoded.getDifficulty()) : "Difficulty should survive JSON mapping";
        assert !json.contains("selectedSkill") : "Lobby config JSON should not contain a selected skill field";
    }

    private static void lobbyConfigPayloadDoesNotExposeSelectedSkill() {
        assert returnType(LobbyConfigPayload.class, "getSelectedSkill") == null
                : "Lobby config payload should no longer expose selectedSkill";
    }

    private static void progressionFieldsRoundTripInWorldSnapshot() {
        WorldSnapshot snapshot = new WorldSnapshot(8L);
        snapshot.setGameStarted(true);
        snapshot.setDifficulty(Difficulty.NORMAL.name());
        snapshot.setTotalScore(180);
        snapshot.setBossActive(true);
        snapshot.setNextBossScoreThreshold(320);

        WorldSnapshotJsonMapper mapper = new WorldSnapshotJsonMapper();
        WorldSnapshot decoded = mapper.fromJson(mapper.toJson(snapshot));

        assert Difficulty.NORMAL.name().equals(decoded.getDifficulty()) : "Snapshot should carry room difficulty";
        assert decoded.getTotalScore() == 180 : "Snapshot should carry total room score";
        assert decoded.isBossActive() : "Snapshot should carry boss-active state";
        assert decoded.getNextBossScoreThreshold() == 320 : "Snapshot should carry next boss threshold";
    }

    private static Class<?> returnType(Class<?> type, String methodName) {
        try {
            Method method = type.getMethod(methodName);
            return method.getReturnType();
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
