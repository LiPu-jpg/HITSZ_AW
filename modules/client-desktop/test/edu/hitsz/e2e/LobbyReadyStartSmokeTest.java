package edu.hitsz.e2e;

import edu.hitsz.common.protocol.MessageType;
import edu.hitsz.common.protocol.ProtocolMessage;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.JsonMessageCodec;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;
import edu.hitsz.common.protocol.socket.SocketClientTransport;
import edu.hitsz.server.LocalAuthorityServer;

import java.util.concurrent.atomic.AtomicReference;

public class LobbyReadyStartSmokeTest {

    public static void main(String[] args) throws Exception {
        LocalAuthorityServer server = new LocalAuthorityServer(0);
        server.start();

        JsonMessageCodec codec = new JsonMessageCodec();
        WorldSnapshotJsonMapper snapshotMapper = new WorldSnapshotJsonMapper();

        AtomicReference<WorldSnapshot> localSnapshot = new AtomicReference<>();
        AtomicReference<WorldSnapshot> remoteSnapshot = new AtomicReference<>();

        SocketClientTransport localClient = new SocketClientTransport("127.0.0.1", server.getPort(), codec);
        SocketClientTransport remoteClient = new SocketClientTransport("127.0.0.1", server.getPort(), codec);
        localClient.setListener(message -> captureSnapshot(message, snapshotMapper, localSnapshot));
        remoteClient.setListener(message -> captureSnapshot(message, snapshotMapper, remoteSnapshot));

        localClient.start();
        remoteClient.start();

        localClient.send(RoomTestSupport.createRoomMessage("session-local", 1L, "NORMAL"));
        RoomTestSupport.waitUntil(() -> localSnapshot.get() != null, 8000L);
        String roomCode = localSnapshot.get().getRoomCode();
        remoteClient.send(RoomTestSupport.joinRoomMessage("session-2", 1L, roomCode));

        RoomTestSupport.waitUntil(() ->
                        localSnapshot.get() != null
                                && remoteSnapshot.get() != null
                                && localSnapshot.get().getConnectedPlayerCount() == 2
                                && remoteSnapshot.get().getConnectedPlayerCount() == 2,
                8000L
        );

        assert !localSnapshot.get().isGameStarted() : "Game should remain in lobby before players are ready";
        assert localSnapshot.get().getConnectedPlayerCount() == 2 : "Lobby snapshot should report two connected players";
        assert localSnapshot.get().getReadyPlayerCount() == 0 : "No player should be ready right after joining";

        localClient.send(RoomTestSupport.readyMessage("session-local", 2L, true));

        RoomTestSupport.waitUntil(() -> localSnapshot.get().getReadyPlayerCount() == 1, 8000L);
        assert !localSnapshot.get().isGameStarted() : "Game should not start until all connected players are ready";

        remoteClient.send(RoomTestSupport.readyMessage("session-2", 2L, true));
        Thread.sleep(150L);
        assert !localSnapshot.get().isGameStarted() : "Ready alone should not auto-start the room anymore";

        localClient.send(RoomTestSupport.startGameMessage("session-local", 3L));
        RoomTestSupport.waitUntil(() -> localSnapshot.get().isGameStarted(), 8000L);

        remoteClient.stop();
        localClient.stop();
        server.stop();
    }

    private static void captureSnapshot(ProtocolMessage message, WorldSnapshotJsonMapper snapshotMapper, AtomicReference<WorldSnapshot> target) {
        RoomTestSupport.captureSnapshot(message, snapshotMapper, target);
    }
}
