package edu.hitsz.client;

public class SocketClientSessionFailureSafetyTest {

    public static void main(String[] args) {
        Game game = new Game();
        SocketClientSession session = new SocketClientSession("127.0.0.1", 29999, "session-local", game);

        boolean failedToStart = false;
        try {
            session.start();
        } catch (IllegalStateException expected) {
            failedToStart = true;
        }

        assert failedToStart : "Precondition failed: test requires an unreachable port";

        session.publishMove(100, 200);
        session.publishReady(true);
        session.publishSkill("FREEZE");
        session.publishUpgradeChoice("LASER_DAMAGE");
        session.publishBranchChoice("RED_SPEED");
    }
}
