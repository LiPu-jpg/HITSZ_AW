package edu.hitsz.e2e;

import edu.hitsz.common.protocol.MessageType;
import edu.hitsz.common.protocol.ProtocolMessage;
import edu.hitsz.common.protocol.dto.ReadyPayload;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.JsonMessageCodec;
import edu.hitsz.common.protocol.json.ReadyPayloadJsonMapper;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;
import edu.hitsz.common.protocol.socket.SocketClientTransport;
import edu.hitsz.server.LocalAuthorityServer;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

public class HostStartOnlySmokeTest {

    public static void main(String[] args) throws Exception {
        LocalAuthorityServer server = new LocalAuthorityServer(0);
        server.start();

        JsonMessageCodec codec = new JsonMessageCodec();
        ReadyPayloadJsonMapper readyMapper = new ReadyPayloadJsonMapper();
        WorldSnapshotJsonMapper snapshotMapper = new WorldSnapshotJsonMapper();
        AtomicReference<WorldSnapshot> hostSnapshot = new AtomicReference<>();
        AtomicReference<WorldSnapshot> guestSnapshot = new AtomicReference<>();

        SocketClientTransport host = new SocketClientTransport("127.0.0.1", server.getPort(), codec);
        SocketClientTransport guest = new SocketClientTransport("127.0.0.1", server.getPort(), codec);
        host.setListener(message -> captureSnapshot(message, snapshotMapper, hostSnapshot));
        guest.setListener(message -> captureSnapshot(message, snapshotMapper, guestSnapshot));
        host.start();
        guest.start();

        host.send(message("CREATE_ROOM", "session-host", 1L, "{\"difficulty\":\"NORMAL\"}"));
        waitUntil(() -> hostSnapshot.get() != null, 3000L);
        String roomCode = invokeString(hostSnapshot.get(), "getRoomCode");

        guest.send(message("JOIN_ROOM", "session-guest", 1L,
                "{\"roomCode\":\"" + roomCode + "\"}"));
        waitUntil(() -> guestSnapshot.get() != null && guestSnapshot.get().getConnectedPlayerCount() == 2, 3000L);

        host.send(new ProtocolMessage(
                MessageType.INPUT_READY,
                "session-host",
                2L,
                System.currentTimeMillis(),
                readyMapper.toJson(new ReadyPayload(true))
        ));
        guest.send(new ProtocolMessage(
                MessageType.INPUT_READY,
                "session-guest",
                2L,
                System.currentTimeMillis(),
                readyMapper.toJson(new ReadyPayload(true))
        ));
        waitUntil(() -> hostSnapshot.get().getReadyPlayerCount() == 2, 3000L);

        guest.send(message("START_GAME", "session-guest", 3L, "{}"));
        Thread.sleep(150L);
        assert !hostSnapshot.get().isGameStarted() : "Guest should not be able to start the room";

        host.send(message("START_GAME", "session-host", 4L, "{}"));
        waitUntil(() -> hostSnapshot.get().isGameStarted() && guestSnapshot.get().isGameStarted(), 3000L);

        guest.stop();
        host.stop();
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
