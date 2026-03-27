package edu.hitsz.e2e;

import edu.hitsz.common.protocol.MessageType;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.JsonMessageCodec;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;
import edu.hitsz.common.protocol.socket.SocketClientTransport;
import edu.hitsz.server.LocalAuthorityServer;

import java.util.concurrent.atomic.AtomicReference;

public class DelayedJoinStartsFreshTest {

    public static void main(String[] args) throws Exception {
        LocalAuthorityServer server = new LocalAuthorityServer(0);
        server.start();

        Thread.sleep(1200L);

        JsonMessageCodec codec = new JsonMessageCodec();
        WorldSnapshotJsonMapper snapshotMapper = new WorldSnapshotJsonMapper();
        AtomicReference<WorldSnapshot> snapshotRef = new AtomicReference<>();

        SocketClientTransport client = new SocketClientTransport("127.0.0.1", server.getPort(), codec);
        client.setListener(message -> {
            if (message.getMessageType() != MessageType.WORLD_SNAPSHOT || message.getPayload() == null) {
                return;
            }
            snapshotRef.set(snapshotMapper.fromJson(message.getPayload()));
        });

        client.start();
        client.send(RoomTestSupport.createRoomMessage(server.getLocalSessionId(), 1L, "NORMAL"));

        RoomTestSupport.waitUntil(() -> snapshotRef.get() != null, 3000L);

        WorldSnapshot snapshot = snapshotRef.get();

        client.stop();
        server.stop();

        assert snapshot.getPlayerSnapshots().size() == 1 : "First join should create exactly one player";
        assert snapshot.getPlayerSnapshots().get(0).getHp() == 1000 : "Player should join with full HP";
        assert snapshot.getEnemySnapshots().isEmpty() : "World should not spawn enemies before the first player joins";
        assert snapshot.getEnemyBulletSnapshots().isEmpty() : "World should not accumulate enemy bullets before the first player joins";
    }

}
