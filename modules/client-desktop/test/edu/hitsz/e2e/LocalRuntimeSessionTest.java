package edu.hitsz.e2e;

import edu.hitsz.client.aircraft.HeroAircraft;
import edu.hitsz.client.Game;
import edu.hitsz.client.SocketClientSession;
import edu.hitsz.server.LocalAuthorityServer;

public class LocalRuntimeSessionTest {

    public static void main(String[] args) throws Exception {
        LocalAuthorityServer server = new LocalAuthorityServer(0);
        server.start();

        Game game = new Game();
        SocketClientSession session = new SocketClientSession(
                "127.0.0.1",
                server.getPort(),
                server.getLocalSessionId(),
                game
        );
        game.attachCommandPublisher(session);
        session.start();
        session.publishCreateRoom("NORMAL", "FREEZE");

        waitUntil(() -> game.getPlayerAircrafts().size() == 1, 3000L);
        game.handleLocalReadyToggle();
        session.publishStartGame();
        waitUntil(() -> !game.getEnemyAircrafts().isEmpty(), 3000L);

        game.handleLocalHeroInput(300, 520);

        waitUntil(() ->
                        HeroAircraft.getSingleton().getLocationX() == 300
                                && HeroAircraft.getSingleton().getLocationY() == 520,
                3000L
        );

        session.stop();
        server.stop();

        assert game.getPlayerAircrafts().size() == 1 : "Single-player runtime should still go through server";
        assert !game.getEnemyAircrafts().isEmpty() : "Server should drive enemy generation via snapshots";
        assert HeroAircraft.getSingleton().getLocationX() == 300 : "Move command should round-trip via local server";
        assert HeroAircraft.getSingleton().getLocationY() == 520 : "Move command should round-trip via local server";
    }

    private static void waitUntil(Check check, long timeoutMillis) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < deadline) {
            if (check.ok()) {
                return;
            }
            Thread.sleep(30L);
        }
        throw new AssertionError("Condition not satisfied within " + timeoutMillis + "ms");
    }

    private interface Check {
        boolean ok();
    }
}
