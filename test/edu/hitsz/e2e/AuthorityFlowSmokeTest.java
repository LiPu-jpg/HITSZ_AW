package edu.hitsz.e2e;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.OtherPlayer;
import edu.hitsz.application.client.ClientWorldState;
import edu.hitsz.application.client.DefaultSnapshotApplier;
import edu.hitsz.application.protocol.MessageType;
import edu.hitsz.application.protocol.ProtocolMessage;
import edu.hitsz.application.protocol.json.InputMovePayloadJsonMapper;
import edu.hitsz.application.protocol.json.JsonMessageCodec;
import edu.hitsz.application.protocol.json.WorldSnapshotJsonMapper;
import edu.hitsz.application.protocol.socket.SocketClientTransport;
import edu.hitsz.application.protocol.socket.SocketServerTransport;
import edu.hitsz.application.server.PlayerSession;
import edu.hitsz.application.server.ServerCommandRouter;
import edu.hitsz.application.server.ServerGameLoop;
import edu.hitsz.application.server.ServerWorldState;
import edu.hitsz.application.protocol.dto.InputMovePayload;
import edu.hitsz.application.protocol.dto.WorldSnapshot;

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
            WorldSnapshot snapshot = serverGameLoop.buildSnapshotForSession(message.getSessionId());
            serverTransport.send(new ProtocolMessage(
                    MessageType.WORLD_SNAPSHOT,
                    message.getSessionId(),
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
            applier.apply(snapshot, clientWorldState);
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
        assert clientWorldState.getPlayerAircrafts().get(0) == HeroAircraft.getSingleton()
                : "Local player should be mapped to HeroAircraft singleton";
        assert clientWorldState.getPlayerAircrafts().get(1) instanceof OtherPlayer
                : "Remote player should be mapped to OtherPlayer";
        assert HeroAircraft.getSingleton().getLocationX() == 240 : "Hero x should follow server snapshot";
        assert HeroAircraft.getSingleton().getLocationY() == 650 : "Hero y should follow server snapshot";
    }
}
