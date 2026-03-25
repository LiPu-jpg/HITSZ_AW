package edu.hitsz.application.server;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SessionRegistry {

    private final Map<String, PlayerSession> sessions = new LinkedHashMap<>();

    public PlayerSession create(String sessionId, String playerId) {
        PlayerSession session = new PlayerSession(sessionId, playerId);
        sessions.put(sessionId, session);
        return session;
    }

    public PlayerSession find(String sessionId) {
        return sessions.get(sessionId);
    }

    public void remove(String sessionId) {
        sessions.remove(sessionId);
    }

    public Collection<PlayerSession> allSessions() {
        return Collections.unmodifiableCollection(sessions.values());
    }
}
