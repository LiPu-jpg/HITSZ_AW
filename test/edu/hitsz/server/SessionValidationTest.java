package edu.hitsz.server;

import edu.hitsz.application.server.PlayerSession;
import edu.hitsz.application.server.SessionRegistry;

public class SessionValidationTest {

    public static void main(String[] args) {
        SessionRegistry registry = new SessionRegistry();
        PlayerSession session = registry.create("session-A", "player-A");

        assert registry.find("session-A") == session;
        assert registry.find("session-B") == null;
    }
}
