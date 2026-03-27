package edu.hitsz.e2e;

import edu.hitsz.common.protocol.MessageType;
import edu.hitsz.common.protocol.ProtocolMessage;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.JsonMessageCodec;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;
import edu.hitsz.common.protocol.socket.SocketClientTransport;
import edu.hitsz.server.LocalAuthorityServer;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

public class RoomIsolationSmokeTest {

    public static void main(String[] args) throws Exception {
        LocalAuthorityServer server = new LocalAuthorityServer(0);
        server.start();

        JsonMessageCodec codec = new JsonMessageCodec();
        WorldSnapshotJsonMapper snapshotMapper = new WorldSnapshotJsonMapper();
        AtomicReference<WorldSnapshot> hostOneSnapshot = new AtomicReference<>();
        AtomicReference<WorldSnapshot> guestSnapshot = new AtomicReference<>();
        AtomicReference<WorldSnapshot> hostTwoSnapshot = new AtomicReference<>();

        SocketClientTransport hostOne = new SocketClientTransport("127.0.0.1", server.getPort(), codec);
        SocketClientTransport guest = new SocketClientTransport("127.0.0.1", server.getPort(), codec);
        SocketClientTransport hostTwo = new SocketClientTransport("127.0.0.1", server.getPort(), codec);
        hostOne.setListener(message -> captureSnapshot(message, snapshotMapper, hostOneSnapshot));
        guest.setListener(message -> captureSnapshot(message, snapshotMapper, guestSnapshot));
        hostTwo.setListener(message -> captureSnapshot(message, snapshotMapper, hostTwoSnapshot));

        hostOne.start();
        guest.start();
        hostTwo.start();

        hostOne.send(message("CREATE_ROOM", "session-host-1", 1L, "{\"difficulty\":\"HARD\"}"));
        hostTwo.send(message("CREATE_ROOM", "session-host-2", 1L, "{\"difficulty\":\"EASY\"}"));

        waitUntil(() -> hostOneSnapshot.get() != null && hostTwoSnapshot.get() != null, 3000L);

        String roomCode = invokeString(hostOneSnapshot.get(), "getRoomCode");
        guest.send(message("JOIN_ROOM", "session-guest", 1L,
                "{\"roomCode\":\"" + roomCode + "\"}"));

        waitUntil(() -> containsPlayer(hostOneSnapshot.get(), "session-guest")
                && containsPlayer(guestSnapshot.get(), "session-host-1")
                && !containsPlayer(hostOneSnapshot.get(), "session-host-2")
                && !containsPlayer(hostTwoSnapshot.get(), "session-guest"), 3000L);

        assert roomCode.equals(invokeString(guestSnapshot.get(), "getRoomCode"))
                : "Joiner should enter the same room";
        assert hostOneSnapshot.get().getConnectedPlayerCount() == 2
                : "Room A should contain exactly two connected players";
        assert hostTwoSnapshot.get().getConnectedPlayerCount() == 1
                : "Room B should remain isolated";

        hostTwo.stop();
        guest.stop();
        hostOne.stop();
        server.stop();
    }

    private static ProtocolMessage message(String typeName, String sessionId, long sequence, String payload) {
        return new ProtocolMessage(
                MessageType.valueOf(typeName),
                sessionId,
                sequence,
                System.currentTimeMillis(),
                payload
        );
    }

    private static void captureSnapshot(
            ProtocolMessage message,
            WorldSnapshotJsonMapper snapshotMapper,
            AtomicReference<WorldSnapshot> target
    ) {
        if (message.getMessageType() != MessageType.WORLD_SNAPSHOT || message.getPayload() == null) {
            return;
        }
        target.set(snapshotMapper.fromJson(message.getPayload()));
    }

    private static boolean containsPlayer(WorldSnapshot snapshot, String sessionId) {
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

    private static String invokeString(Object target, String methodName) throws Exception {
        Method method = target.getClass().getMethod(methodName);
        return (String) method.invoke(target);
    }

    private static void waitUntil(Check check, long timeoutMillis) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < deadline) {
            if (check.ok()) {
                return;
            }
            Thread.sleep(30L);
        }
        throw new AssertionError("Condition not satisfied within " + timeoutMillis + "ms");
    }

    private interface Check {
        boolean ok();
    }
}
