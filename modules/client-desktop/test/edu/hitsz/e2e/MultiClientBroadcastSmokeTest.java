package edu.hitsz.e2e;

import edu.hitsz.client.ClientWorldState;
import edu.hitsz.client.DefaultSnapshotApplier;
import edu.hitsz.client.aircraft.HeroAircraft;
import edu.hitsz.client.aircraft.OtherPlayer;
import edu.hitsz.common.protocol.ProtocolMessage;
import edu.hitsz.common.protocol.dto.InputMovePayload;
import edu.hitsz.common.protocol.dto.ReadyPayload;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.InputMovePayloadJsonMapper;
import edu.hitsz.common.protocol.json.JsonMessageCodec;
import edu.hitsz.common.protocol.json.ReadyPayloadJsonMapper;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;
import edu.hitsz.common.protocol.socket.SocketClientTransport;
import edu.hitsz.server.LocalAuthorityServer;

import java.util.concurrent.atomic.AtomicReference;

public class MultiClientBroadcastSmokeTest {

    public static void main(String[] args) throws Exception {
        LocalAuthorityServer server = new LocalAuthorityServer(0);
        server.start();

        JsonMessageCodec codec = new JsonMessageCodec();
        InputMovePayloadJsonMapper moveMapper = new InputMovePayloadJsonMapper();
        ReadyPayloadJsonMapper readyMapper = new ReadyPayloadJsonMapper();
        WorldSnapshotJsonMapper snapshotMapper = new WorldSnapshotJsonMapper();
        AtomicReference<WorldSnapshot> snapshotForLocalClient = new AtomicReference<>();
        AtomicReference<WorldSnapshot> snapshotForRemoteClient = new AtomicReference<>();

        SocketClientTransport localClient = new SocketClientTransport("127.0.0.1", server.getPort(), codec);
        SocketClientTransport remoteClient = new SocketClientTransport("127.0.0.1", server.getPort(), codec);

        localClient.setListener(message -> captureSnapshot(message, snapshotMapper, snapshotForLocalClient));
        remoteClient.setListener(message -> captureSnapshot(message, snapshotMapper, snapshotForRemoteClient));

        localClient.start();
        remoteClient.start();

        localClient.send(RoomTestSupport.createRoomMessage("session-local", 1L, "NORMAL"));
        RoomTestSupport.waitUntil(() -> snapshotForLocalClient.get() != null, 3000L);
        String roomCode = snapshotForLocalClient.get().getRoomCode();
        remoteClient.send(RoomTestSupport.joinRoomMessage("session-2", 1L, roomCode));

        RoomTestSupport.waitUntil(() -> RoomTestSupport.containsPlayer(snapshotForLocalClient.get(), "session-local")
                && RoomTestSupport.containsPlayer(snapshotForLocalClient.get(), "session-2")
                && RoomTestSupport.containsPlayer(snapshotForRemoteClient.get(), "session-local")
                && RoomTestSupport.containsPlayer(snapshotForRemoteClient.get(), "session-2"), 3000L);

        localClient.send(RoomTestSupport.readyMessage("session-local", 2L, true));
        remoteClient.send(RoomTestSupport.readyMessage("session-2", 2L, true));
        localClient.send(RoomTestSupport.startGameMessage("session-local", 3L));

        RoomTestSupport.waitUntil(() -> snapshotForLocalClient.get() != null && snapshotForLocalClient.get().isGameStarted()
                && snapshotForRemoteClient.get() != null && snapshotForRemoteClient.get().isGameStarted(), 3000L);

        localClient.send(new ProtocolMessage(
                edu.hitsz.common.protocol.MessageType.INPUT_MOVE,
                "session-local",
                4L,
                1100L,
                moveMapper.toJson(new InputMovePayload(300, 520))
        ));
        remoteClient.send(new ProtocolMessage(
                edu.hitsz.common.protocol.MessageType.INPUT_MOVE,
                "session-2",
                4L,
                1200L,
                moveMapper.toJson(new InputMovePayload(420, 560))
        ));

        RoomTestSupport.waitUntil(() -> RoomTestSupport.hasPosition(snapshotForLocalClient.get(), "session-local", 300, 520)
                && RoomTestSupport.hasPosition(snapshotForLocalClient.get(), "session-2", 420, 560)
                && RoomTestSupport.hasPosition(snapshotForRemoteClient.get(), "session-local", 300, 520)
                && RoomTestSupport.hasPosition(snapshotForRemoteClient.get(), "session-2", 420, 560), 3000L);

        ClientWorldState localState = new ClientWorldState();
        ClientWorldState remoteState = new ClientWorldState();
        DefaultSnapshotApplier applier = new DefaultSnapshotApplier();
        applier.apply(snapshotForLocalClient.get(), localState, "session-local");
        applier.apply(snapshotForRemoteClient.get(), remoteState, "session-2");

        remoteClient.stop();
        localClient.stop();
        server.stop();

        assert localState.getPlayerAircrafts().size() == 2 : "Broadcast snapshot should include both players for local client";
        assert containsHero(localState) : "Local client should recognize its own session as HeroAircraft";
        assert containsOtherPlayer(localState) : "Local client should render the teammate as OtherPlayer";

        assert remoteState.getPlayerAircrafts().size() == 2 : "Broadcast snapshot should include both players for remote client";
        assert containsHero(remoteState) : "Remote client should recognize its own session from the shared snapshot";
        assert containsOtherPlayer(remoteState) : "Remote client should render the teammate as OtherPlayer";
    }

    private static void captureSnapshot(ProtocolMessage message, WorldSnapshotJsonMapper snapshotMapper, AtomicReference<WorldSnapshot> target) {
        RoomTestSupport.captureSnapshot(message, snapshotMapper, target);
    }

    private static boolean containsHero(ClientWorldState state) {
        return state.getPlayerAircrafts().stream().anyMatch(aircraft -> aircraft == HeroAircraft.getSingleton());
    }

    private static boolean containsOtherPlayer(ClientWorldState state) {
        return state.getPlayerAircrafts().stream().anyMatch(aircraft -> aircraft instanceof OtherPlayer);
    }

}
