package edu.hitsz.e2e;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.Game;
import edu.hitsz.application.client.SocketClientSession;
import edu.hitsz.application.server.LocalAuthorityServer;

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

        waitUntil(() -> game.getPlayerAircrafts().size() == 2, 3000L);

        game.handleLocalHeroInput(300, 520);

        waitUntil(() ->
                        HeroAircraft.getSingleton().getLocationX() == 300
                                && HeroAircraft.getSingleton().getLocationY() == 520,
                3000L
        );

        session.stop();
        server.stop();

        assert game.getPlayerAircrafts().size() == 2 : "Initial snapshot should contain local and remote players";
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
