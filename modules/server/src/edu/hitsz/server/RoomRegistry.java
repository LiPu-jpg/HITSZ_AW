package edu.hitsz.server;

import edu.hitsz.common.Difficulty;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class RoomRegistry {

    private final Map<String, RoomRuntime> rooms = new LinkedHashMap<>();
    private final Map<String, String> sessionToRoomCode = new LinkedHashMap<>();
    private final Random random = new Random();

    public synchronized RoomRuntime createRoom(
            String sessionId,
            String playerId,
            Difficulty difficulty,
            long nowMillis
    ) {
        leaveCurrentRoom(sessionId);
        String roomCode = nextRoomCode();
        RoomRuntime room = new RoomRuntime(roomCode, sessionId, difficulty);
        rooms.put(roomCode, room);
        sessionToRoomCode.put(sessionId, roomCode);
        room.addOrReconnectPlayer(sessionId, playerId, nowMillis);
        return room;
    }

    public synchronized RoomRuntime joinRoom(
            String sessionId,
            String playerId,
            String roomCode,
            long nowMillis
    ) {
        RoomRuntime room = rooms.get(roomCode);
        if (room == null || room.isGameStarted()) {
            return null;
        }
        leaveCurrentRoom(sessionId);
        sessionToRoomCode.put(sessionId, roomCode);
        room.addOrReconnectPlayer(sessionId, playerId, nowMillis);
        return room;
    }

    public synchronized RoomRuntime findByRoomCode(String roomCode) {
        return rooms.get(roomCode);
    }

    public synchronized RoomRuntime findBySession(String sessionId) {
        String roomCode = sessionToRoomCode.get(sessionId);
        if (roomCode == null) {
            return null;
        }
        RoomRuntime room = rooms.get(roomCode);
        if (room == null || !room.containsSession(sessionId)) {
            sessionToRoomCode.remove(sessionId);
            return null;
        }
        return room;
    }

    public synchronized Collection<RoomRuntime> allRooms() {
        return Collections.unmodifiableCollection(new LinkedList<>(rooms.values()));
    }

    public synchronized void removeEmptyRooms() {
        Collection<String> emptyRoomCodes = new LinkedList<>();
        for (RoomRuntime room : rooms.values()) {
            if (room.isEmpty()) {
                emptyRoomCodes.add(room.getRoomCode());
            }
        }
        for (String roomCode : emptyRoomCodes) {
            rooms.remove(roomCode);
            sessionToRoomCode.entrySet().removeIf(entry -> roomCode.equals(entry.getValue()));
        }
    }

    private synchronized void leaveCurrentRoom(String sessionId) {
        String roomCode = sessionToRoomCode.remove(sessionId);
        if (roomCode == null) {
            return;
        }
        RoomRuntime room = rooms.get(roomCode);
        if (room == null) {
            return;
        }
        room.removeSession(sessionId);
        if (room.isEmpty()) {
            rooms.remove(roomCode);
        }
    }

    private synchronized String nextRoomCode() {
        String roomCode;
        do {
            roomCode = String.valueOf(100000 + random.nextInt(900000));
        } while (rooms.containsKey(roomCode));
        return roomCode;
    }
}
