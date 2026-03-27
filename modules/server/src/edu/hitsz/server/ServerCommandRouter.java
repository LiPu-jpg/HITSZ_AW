package edu.hitsz.server;

import edu.hitsz.server.command.MoveCommand;
import edu.hitsz.server.command.SkillCommand;

public class ServerCommandRouter {

    private final SessionRegistry sessionRegistry;

    public ServerCommandRouter(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public PlayerSession route(MoveCommand command) {
        return validate(command.getSessionId());
    }

    public PlayerSession route(SkillCommand command) {
        return validate(command.getSessionId());
    }

    public PlayerSession validate(String sessionId) {
        PlayerSession session = sessionRegistry.find(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Unknown session: " + sessionId);
        }
        return session;
    }
}
