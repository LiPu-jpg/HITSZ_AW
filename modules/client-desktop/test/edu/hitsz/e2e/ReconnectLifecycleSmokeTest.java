package edu.hitsz.e2e;

import edu.hitsz.common.protocol.ProtocolMessage;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.InputMovePayload;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.InputMovePayloadJsonMapper;
import edu.hitsz.common.protocol.json.JsonMessageCodec;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;
import edu.hitsz.common.protocol.socket.SocketClientTransport;
import edu.hitsz.server.LocalAuthorityServer;

import java.util.concurrent.atomic.AtomicReference;

public class ReconnectLifecycleSmokeTest {

    public static void main(String[] args) throws Exception {
        LocalAuthorityServer server = new LocalAuthorityServer(0);
        server.start();

        JsonMessageCodec codec = new JsonMessageCodec();
        InputMovePayloadJsonMapper moveMapper = new InputMovePayloadJsonMapper();
        WorldSnapshotJsonMapper snapshotMapper = new WorldSnapshotJsonMapper();
        AtomicReference<WorldSnapshot> localSnapshot = new AtomicReference<>();

        SocketClientTransport localClient = new SocketClientTransport("127.0.0.1", server.getPort(), codec);
        SocketClientTransport remoteClient = new SocketClientTransport("127.0.0.1", server.getPort(), codec);
        localClient.setListener(message -> captureSnapshot(message, snapshotMapper, localSnapshot));
        remoteClient.setListener(message -> {
        });

        localClient.start();
        remoteClient.start();

        localClient.send(RoomTestSupport.createRoomMessage("session-local", 1L, "NORMAL"));
        RoomTestSupport.waitUntil(() -> localSnapshot.get() != null, 3000L);
        String roomCode = localSnapshot.get().getRoomCode();
        remoteClient.send(RoomTestSupport.joinRoomMessage("session-2", 1L, roomCode));

        RoomTestSupport.waitUntil(() -> RoomTestSupport.containsPlayer(localSnapshot.get(), "session-2"), 3000L);
        PlayerSnapshot lobbyRemote = RoomTestSupport.findPlayer(localSnapshot.get(), "session-2");

        remoteClient.send(new ProtocolMessage(
                edu.hitsz.common.protocol.MessageType.INPUT_MOVE,
                "session-2",
                2L,
                1100L,
                moveMapper.toJson(new InputMovePayload(420, 560))
        ));

        RoomTestSupport.waitUntil(() ->
                        RoomTestSupport.findPlayer(localSnapshot.get(), "session-2").getX() == lobbyRemote.getX()
                                && RoomTestSupport.findPlayer(localSnapshot.get(), "session-2").getY() == lobbyRemote.getY(),
                3000L
        );

        remoteClient.stop();

        RoomTestSupport.waitUntil(() -> !RoomTestSupport.containsPlayer(localSnapshot.get(), "session-2"), 3000L);

        SocketClientTransport reconnectedRemote = new SocketClientTransport("127.0.0.1", server.getPort(), codec);
        reconnectedRemote.setListener(message -> {
        });
        reconnectedRemote.start();
        reconnectedRemote.send(new ProtocolMessage(
                edu.hitsz.common.protocol.MessageType.HELLO,
                "session-2",
                3L,
                1200L,
                "{}"
        ));

        RoomTestSupport.waitUntil(() ->
                        RoomTestSupport.containsPlayer(localSnapshot.get(), "session-2")
                                && RoomTestSupport.findPlayer(localSnapshot.get(), "session-2").getSelectedSkill() == null
                                && !RoomTestSupport.findPlayer(localSnapshot.get(), "session-2").isBranchUnlocked()
                                && RoomTestSupport.findPlayer(localSnapshot.get(), "session-2").getX() == lobbyRemote.getX()
                                && RoomTestSupport.findPlayer(localSnapshot.get(), "session-2").getY() == lobbyRemote.getY(),
                3000L
        );

        reconnectedRemote.stop();
        localClient.stop();
        server.stop();
    }

    private static void captureSnapshot(ProtocolMessage message, WorldSnapshotJsonMapper snapshotMapper, AtomicReference<WorldSnapshot> target) {
        RoomTestSupport.captureSnapshot(message, snapshotMapper, target);
    }
}
