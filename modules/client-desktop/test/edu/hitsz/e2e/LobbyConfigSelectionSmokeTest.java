package edu.hitsz.e2e;

import edu.hitsz.common.Difficulty;
import edu.hitsz.common.protocol.MessageType;
import edu.hitsz.common.protocol.ProtocolMessage;
import edu.hitsz.common.protocol.dto.LobbyConfigPayload;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.JsonMessageCodec;
import edu.hitsz.common.protocol.json.LobbyConfigPayloadJsonMapper;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;
import edu.hitsz.common.protocol.socket.SocketClientTransport;
import edu.hitsz.server.LocalAuthorityServer;

import java.util.concurrent.atomic.AtomicReference;

public class LobbyConfigSelectionSmokeTest {

    public static void main(String[] args) throws Exception {
        LocalAuthorityServer server = new LocalAuthorityServer(0);
        server.start();

        JsonMessageCodec codec = new JsonMessageCodec();
        LobbyConfigPayloadJsonMapper lobbyConfigMapper = new LobbyConfigPayloadJsonMapper();
        WorldSnapshotJsonMapper snapshotMapper = new WorldSnapshotJsonMapper();

        AtomicReference<WorldSnapshot> localSnapshot = new AtomicReference<>();
        AtomicReference<WorldSnapshot> remoteSnapshot = new AtomicReference<>();

        SocketClientTransport localClient = new SocketClientTransport("127.0.0.1", server.getPort(), codec);
        SocketClientTransport remoteClient = new SocketClientTransport("127.0.0.1", server.getPort(), codec);
        localClient.setListener(message -> captureSnapshot(message, snapshotMapper, localSnapshot));
        remoteClient.setListener(message -> captureSnapshot(message, snapshotMapper, remoteSnapshot));

        localClient.start();
        remoteClient.start();

        localClient.send(RoomTestSupport.createRoomMessage("session-local", 1L, Difficulty.NORMAL.name()));
        RoomTestSupport.waitUntil(() -> localSnapshot.get() != null, 5000L);
        String roomCode = localSnapshot.get().getRoomCode();
        remoteClient.send(RoomTestSupport.joinRoomMessage("session-2", 1L, roomCode));

        RoomTestSupport.waitUntil(() ->
                        localSnapshot.get() != null
                                && remoteSnapshot.get() != null
                                && localSnapshot.get().getPlayerSnapshots().size() == 2
                                && remoteSnapshot.get().getPlayerSnapshots().size() == 2,
                5000L
        );

        localClient.send(new ProtocolMessage(
                MessageType.INPUT_LOBBY_CONFIG,
                "session-local",
                2L,
                1100L,
                lobbyConfigMapper.toJson(new LobbyConfigPayload(Difficulty.HARD.name()))
        ));

        RoomTestSupport.waitUntil(() ->
                        Difficulty.HARD.name().equals(localSnapshot.get().getDifficulty())
                                && Difficulty.HARD.name().equals(remoteSnapshot.get().getDifficulty())
                                && RoomTestSupport.findPlayer(localSnapshot.get(), "session-local").getSelectedSkill() == null,
                5000L
        );

        remoteClient.send(new ProtocolMessage(
                MessageType.INPUT_LOBBY_CONFIG,
                "session-2",
                3L,
                1200L,
                lobbyConfigMapper.toJson(new LobbyConfigPayload(Difficulty.EASY.name()))
        ));

        Thread.sleep(150L);
        assert Difficulty.HARD.name().equals(localSnapshot.get().getDifficulty())
                : "Non-host should not be able to overwrite room difficulty";
        assert RoomTestSupport.findPlayer(localSnapshot.get(), "session-2").getSelectedSkill() == null
                : "Lobby skill selection should no longer change per-player skill state";

        remoteClient.stop();
        localClient.stop();
        server.stop();
    }

    private static void captureSnapshot(ProtocolMessage message, WorldSnapshotJsonMapper snapshotMapper, AtomicReference<WorldSnapshot> target) {
        RoomTestSupport.captureSnapshot(message, snapshotMapper, target);
    }
}
