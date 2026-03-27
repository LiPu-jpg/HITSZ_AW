package edu.hitsz.e2e;

import edu.hitsz.common.protocol.MessageType;
import edu.hitsz.common.protocol.ProtocolMessage;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.ReadyPayload;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.ReadyPayloadJsonMapper;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;

import java.util.concurrent.atomic.AtomicReference;

final class RoomTestSupport {

    private static final ReadyPayloadJsonMapper READY_MAPPER = new ReadyPayloadJsonMapper();

    private RoomTestSupport() {
    }

    static ProtocolMessage createRoomMessage(String sessionId, long sequence, String difficulty) {
        return new ProtocolMessage(
                MessageType.CREATE_ROOM,
                sessionId,
                sequence,
                System.currentTimeMillis(),
                "{\"difficulty\":\"" + difficulty + "\"}"
        );
    }

    static ProtocolMessage joinRoomMessage(String sessionId, long sequence, String roomCode) {
        return new ProtocolMessage(
                MessageType.JOIN_ROOM,
                sessionId,
                sequence,
                System.currentTimeMillis(),
                "{\"roomCode\":\"" + roomCode + "\"}"
        );
    }

    static ProtocolMessage startGameMessage(String sessionId, long sequence) {
        return new ProtocolMessage(
                MessageType.START_GAME,
                sessionId,
                sequence,
                System.currentTimeMillis(),
                "{}"
        );
    }

    static ProtocolMessage readyMessage(String sessionId, long sequence, boolean ready) {
        return new ProtocolMessage(
                MessageType.INPUT_READY,
                sessionId,
                sequence,
                System.currentTimeMillis(),
                READY_MAPPER.toJson(new ReadyPayload(ready))
        );
    }

    static ProtocolMessage upgradeChoiceMessage(String sessionId, long sequence, String choice) {
        return upgradeChoiceMessage(sessionId, sequence, choice, System.currentTimeMillis());
    }

    static ProtocolMessage upgradeChoiceMessage(String sessionId, long sequence, String choice, long timestamp) {
        return new ProtocolMessage(
                MessageType.INPUT_UPGRADE_CHOICE,
                sessionId,
                sequence,
                timestamp,
                "{\"choice\":\"" + choice + "\"}"
        );
    }

    static void captureSnapshot(
            ProtocolMessage message,
            WorldSnapshotJsonMapper snapshotMapper,
            AtomicReference<WorldSnapshot> target
    ) {
        if (message.getMessageType() != MessageType.WORLD_SNAPSHOT || message.getPayload() == null) {
            return;
        }
        target.set(snapshotMapper.fromJson(message.getPayload()));
    }

    static boolean containsPlayer(WorldSnapshot snapshot, String sessionId) {
        if (snapshot == null) {
            return false;
        }
        for (PlayerSnapshot playerSnapshot : snapshot.getPlayerSnapshots()) {
            if (sessionId.equals(playerSnapshot.getSessionId())) {
                return true;
            }
        }
        return false;
    }

    static boolean hasPosition(WorldSnapshot snapshot, String sessionId, int x, int y) {
        if (snapshot == null) {
            return false;
        }
        for (PlayerSnapshot playerSnapshot : snapshot.getPlayerSnapshots()) {
            if (sessionId.equals(playerSnapshot.getSessionId())) {
                return playerSnapshot.getX() == x && playerSnapshot.getY() == y;
            }
        }
        return false;
    }

    static PlayerSnapshot findPlayer(WorldSnapshot snapshot, String sessionId) {
        if (snapshot == null) {
            throw new AssertionError("Snapshot was null");
        }
        for (PlayerSnapshot playerSnapshot : snapshot.getPlayerSnapshots()) {
            if (sessionId.equals(playerSnapshot.getSessionId())) {
                return playerSnapshot;
            }
        }
        throw new AssertionError("Player snapshot not found for " + sessionId);
    }

    static void waitUntil(Check check, long timeoutMillis) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < deadline) {
            if (check.ok()) {
                return;
            }
            Thread.sleep(30L);
        }
        throw new AssertionError("Condition not satisfied within " + timeoutMillis + "ms");
    }

    interface Check {
        boolean ok();
    }
}
