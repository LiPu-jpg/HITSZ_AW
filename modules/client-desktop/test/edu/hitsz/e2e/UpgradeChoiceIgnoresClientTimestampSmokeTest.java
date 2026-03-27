package edu.hitsz.e2e;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.BranchUpgradeChoice;
import edu.hitsz.common.GamePhase;
import edu.hitsz.common.protocol.json.JsonMessageCodec;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;
import edu.hitsz.common.protocol.socket.SocketClientTransport;
import edu.hitsz.server.LocalAuthorityServer;
import edu.hitsz.server.PlayerSession;
import edu.hitsz.server.ProgressionPolicy;
import edu.hitsz.server.RoomRegistry;
import edu.hitsz.server.RoomRuntime;
import edu.hitsz.server.ServerWorldState;

import java.util.concurrent.atomic.AtomicReference;

public class UpgradeChoiceIgnoresClientTimestampSmokeTest {

    public static void main(String[] args) throws Exception {
        LocalAuthorityServer server = new LocalAuthorityServer(0);
        server.start();

        JsonMessageCodec codec = new JsonMessageCodec();
        WorldSnapshotJsonMapper snapshotMapper = new WorldSnapshotJsonMapper();
        AtomicReference<edu.hitsz.common.protocol.dto.WorldSnapshot> snapshotRef = new AtomicReference<>();

        SocketClientTransport client = new SocketClientTransport("127.0.0.1", server.getPort(), codec);
        client.setListener(message -> RoomTestSupport.captureSnapshot(message, snapshotMapper, snapshotRef));
        client.start();

        client.send(RoomTestSupport.createRoomMessage("session-local", 1L, "NORMAL"));
        RoomTestSupport.waitUntil(() -> snapshotRef.get() != null, 3000L);
        client.send(RoomTestSupport.readyMessage("session-local", 2L, true));
        client.send(RoomTestSupport.startGameMessage("session-local", 3L));
        RoomTestSupport.waitUntil(() -> snapshotRef.get() != null && snapshotRef.get().isGameStarted(), 3000L);

        RoomRuntime roomRuntime = extractRoomRegistry(server).findBySession("session-local");
        ServerWorldState worldState = extractWorldState(roomRuntime);
        PlayerSession session = roomRuntime.findSession("session-local");
        long nowMillis = System.currentTimeMillis();
        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(edu.hitsz.common.Difficulty.NORMAL, 0));
        worldState.syncProgressionState(nowMillis);
        worldState.getEnemyAircrafts().clear();
        worldState.syncProgressionState(nowMillis + 100L);

        RoomTestSupport.waitUntil(() ->
                        snapshotRef.get() != null && snapshotRef.get().getGamePhase() == GamePhase.BRANCH_SELECTION,
                3000L
        );
        client.send(RoomTestSupport.branchChoiceMessage("session-local", 4L, AircraftBranch.RED_SPEED.name()));
        roomRuntime.tick(nowMillis + 120L, 10_000L);

        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(edu.hitsz.common.Difficulty.NORMAL, 1));
        worldState.syncProgressionState(nowMillis + 200L);
        worldState.getEnemyAircrafts().clear();
        worldState.syncProgressionState(nowMillis + 300L);

        RoomTestSupport.waitUntil(() ->
                        snapshotRef.get() != null && snapshotRef.get().getGamePhase() == GamePhase.UPGRADE_SELECTION,
                3000L
        );

        client.send(RoomTestSupport.upgradeChoiceMessage(
                "session-local",
                5L,
                BranchUpgradeChoice.LASER_DAMAGE.name(),
                System.currentTimeMillis() + 60_000L
        ));
        Thread.sleep(150L);

        assert session.getPlayerState().getSelectedUpgradeChoice() == null
                : "Server should not accept an upgrade early just because the client supplied a future timestamp";

        client.stop();
        server.stop();
    }

    private static RoomRegistry extractRoomRegistry(LocalAuthorityServer server) {
        try {
            java.lang.reflect.Field field = LocalAuthorityServer.class.getDeclaredField("roomRegistry");
            field.setAccessible(true);
            return (RoomRegistry) field.get(server);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("LocalAuthorityServer should retain a RoomRegistry", e);
        }
    }

    private static ServerWorldState extractWorldState(RoomRuntime roomRuntime) {
        try {
            java.lang.reflect.Field field = RoomRuntime.class.getDeclaredField("worldState");
            field.setAccessible(true);
            return (ServerWorldState) field.get(roomRuntime);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("RoomRuntime should retain a ServerWorldState", e);
        }
    }
}
