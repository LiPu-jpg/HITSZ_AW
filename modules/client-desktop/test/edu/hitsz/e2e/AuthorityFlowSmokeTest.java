package edu.hitsz.e2e;

import edu.hitsz.client.aircraft.HeroAircraft;
import edu.hitsz.client.aircraft.OtherPlayer;
import edu.hitsz.client.ClientWorldState;
import edu.hitsz.client.DefaultSnapshotApplier;
import edu.hitsz.common.protocol.MessageType;
import edu.hitsz.common.protocol.ProtocolMessage;
import edu.hitsz.common.protocol.json.InputMovePayloadJsonMapper;
import edu.hitsz.common.protocol.json.JsonMessageCodec;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;
import edu.hitsz.common.protocol.socket.SocketClientTransport;
import edu.hitsz.common.protocol.socket.SocketServerTransport;
import edu.hitsz.server.PlayerSession;
import edu.hitsz.server.ServerCommandRouter;
import edu.hitsz.server.ServerGameLoop;
import edu.hitsz.server.ServerWorldState;
import edu.hitsz.common.protocol.dto.InputMovePayload;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AuthorityFlowSmokeTest {

    public static void main(String[] args) throws Exception {
        JsonMessageCodec codec = new JsonMessageCodec();
        InputMovePayloadJsonMapper moveMapper = new InputMovePayloadJsonMapper();
        WorldSnapshotJsonMapper snapshotMapper = new WorldSnapshotJsonMapper();

        ServerWorldState worldState = new ServerWorldState();
        PlayerSession localSession = worldState.getSessionRegistry().create("session-local", "player-local");
        localSession.getPlayerState().setPosition(10, 20);
        PlayerSession remoteSession = worldState.getSessionRegistry().create("session-2", "player-2");
        remoteSession.getPlayerState().setPosition(50, 60);

        ServerGameLoop serverGameLoop = new ServerGameLoop(worldState);
        ServerCommandRouter router = new ServerCommandRouter(worldState.getSessionRegistry());

        SocketServerTransport serverTransport = new SocketServerTransport(0, codec);
        serverTransport.start();

        CountDownLatch latch = new CountDownLatch(1);
        ClientWorldState clientWorldState = new ClientWorldState();
        DefaultSnapshotApplier applier = new DefaultSnapshotApplier();

        SocketClientTransport clientTransport = new SocketClientTransport("127.0.0.1", serverTransport.getPort(), codec);

        serverTransport.setListener(message -> {
            if (message.getMessageType() != MessageType.INPUT_MOVE) {
                return;
            }
            InputMovePayload movePayload = moveMapper.fromJson(message.getPayload());
            PlayerSession session = router.validate(message.getSessionId());
            session.getPlayerState().setPosition(movePayload.getX(), movePayload.getY());
            serverGameLoop.stepOnce(2000L);
            WorldSnapshot snapshot = serverGameLoop.buildSnapshot();
            serverTransport.send(new ProtocolMessage(
                    MessageType.WORLD_SNAPSHOT,
                    "broadcast",
                    1L,
                    2000L,
                    snapshotMapper.toJson(snapshot)
            ));
        });

        clientTransport.setListener(message -> {
            if (message.getMessageType() != MessageType.WORLD_SNAPSHOT) {
                return;
            }
            WorldSnapshot snapshot = snapshotMapper.fromJson(message.getPayload());
            applier.apply(snapshot, clientWorldState, "session-local");
            latch.countDown();
        });

        clientTransport.start();
        clientTransport.send(new ProtocolMessage(
                MessageType.INPUT_MOVE,
                "session-local",
                1L,
                1000L,
                moveMapper.toJson(new InputMovePayload(240, 650))
        ));

        boolean updated = latch.await(3, TimeUnit.SECONDS);

        clientTransport.stop();
        serverTransport.stop();

        assert updated : "Client should receive snapshot from local authority server";
        assert clientWorldState.getPlayerAircrafts().size() == 2 : "Snapshot should contain local and remote players";
        assert containsHero(clientWorldState) : "Local player should be mapped to HeroAircraft singleton";
        assert containsOtherPlayer(clientWorldState) : "Remote player should be mapped to OtherPlayer";
        assert HeroAircraft.getSingleton().getLocationX() == 240 : "Hero x should follow server snapshot";
        assert HeroAircraft.getSingleton().getLocationY() == 650 : "Hero y should follow server snapshot";
    }

    private static boolean containsHero(ClientWorldState state) {
        for (edu.hitsz.client.aircraft.AbstractAircraft aircraft : state.getPlayerAircrafts()) {
            if (aircraft == HeroAircraft.getSingleton()) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsOtherPlayer(ClientWorldState state) {
        for (edu.hitsz.client.aircraft.AbstractAircraft aircraft : state.getPlayerAircrafts()) {
            if (aircraft instanceof OtherPlayer) {
                return true;
            }
        }
        return false;
    }
}
