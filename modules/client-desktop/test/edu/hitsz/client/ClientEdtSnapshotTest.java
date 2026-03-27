package edu.hitsz.client;

import edu.hitsz.client.Game;
import edu.hitsz.client.SocketClientSession;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.server.LocalAuthorityServer;

import javax.swing.SwingUtilities;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientEdtSnapshotTest {

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
        });

        LocalAuthorityServer server = new LocalAuthorityServer(0);
        server.start();

        EdtAwareGame game = new EdtAwareGame();
        SocketClientSession session = new SocketClientSession(
                "127.0.0.1",
                server.getPort(),
                server.getLocalSessionId(),
                game
        );
        game.attachCommandPublisher(session);
        session.start();
        session.publishCreateRoom("NORMAL", "FREEZE");

        boolean applied = game.awaitSnapshot(3000L);

        session.stop();
        server.stop();

        assert applied : "Client should receive at least one world snapshot";
        assert game.wasAppliedOnEdt() : "World snapshots should be applied on Swing EDT";
    }

    private static class EdtAwareGame extends Game {
        private final CountDownLatch latch = new CountDownLatch(1);
        private volatile boolean appliedOnEdt;

        @Override
        public void applyWorldSnapshot(WorldSnapshot snapshot) {
            appliedOnEdt = SwingUtilities.isEventDispatchThread();
            super.applyWorldSnapshot(snapshot);
            latch.countDown();
        }

        boolean awaitSnapshot(long timeoutMillis) throws InterruptedException {
            return latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        }

        boolean wasAppliedOnEdt() {
            return appliedOnEdt;
        }
    }
}
