package edu.hitsz.server;

import edu.hitsz.common.Difficulty;
import edu.hitsz.common.protocol.MessageType;
import edu.hitsz.common.protocol.ProtocolMessage;
import edu.hitsz.common.protocol.dto.CreateRoomPayload;
import edu.hitsz.common.protocol.dto.BranchChoicePayload;
import edu.hitsz.common.protocol.dto.InputMovePayload;
import edu.hitsz.common.protocol.dto.InputSkillPayload;
import edu.hitsz.common.protocol.dto.JoinRoomPayload;
import edu.hitsz.common.protocol.dto.LobbyConfigPayload;
import edu.hitsz.common.protocol.dto.ReadyPayload;
import edu.hitsz.common.protocol.dto.UpgradeChoicePayload;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.CreateRoomPayloadJsonMapper;
import edu.hitsz.common.protocol.json.BranchChoicePayloadJsonMapper;
import edu.hitsz.common.protocol.json.InputMovePayloadJsonMapper;
import edu.hitsz.common.protocol.json.InputSkillPayloadJsonMapper;
import edu.hitsz.common.protocol.json.JoinRoomPayloadJsonMapper;
import edu.hitsz.common.protocol.json.JsonMessageCodec;
import edu.hitsz.common.protocol.json.LobbyConfigPayloadJsonMapper;
import edu.hitsz.common.protocol.json.ReadyPayloadJsonMapper;
import edu.hitsz.common.protocol.json.UpgradeChoicePayloadJsonMapper;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;
import edu.hitsz.common.protocol.socket.ServerConnectionListener;
import edu.hitsz.common.protocol.socket.SocketServerTransport;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

public class LocalAuthorityServer {

    private static final String LOCAL_SESSION_ID = "session-local";
    private static final String LOCAL_PLAYER_ID = "player-local";
    private static final long DISCONNECTED_SESSION_RETENTION_MILLIS = 10_000L;

    private final RoomRegistry roomRegistry;
    private final SocketServerTransport transport;
    private final InputMovePayloadJsonMapper movePayloadJsonMapper;
    private final InputSkillPayloadJsonMapper skillPayloadJsonMapper;
    private final CreateRoomPayloadJsonMapper createRoomPayloadJsonMapper;
    private final JoinRoomPayloadJsonMapper joinRoomPayloadJsonMapper;
    private final LobbyConfigPayloadJsonMapper lobbyConfigPayloadJsonMapper;
    private final ReadyPayloadJsonMapper readyPayloadJsonMapper;
    private final UpgradeChoicePayloadJsonMapper upgradeChoicePayloadJsonMapper;
    private final BranchChoicePayloadJsonMapper branchChoicePayloadJsonMapper;
    private final WorldSnapshotJsonMapper snapshotJsonMapper;
    private final Timer timer;
    private final AtomicLong serverSequence;

    public LocalAuthorityServer(int port) {
        this("0.0.0.0", port, 128);
    }

    public LocalAuthorityServer(String bindHost, int port, int backlog) {
        this.roomRegistry = new RoomRegistry();
        this.transport = new SocketServerTransport(bindHost, port, backlog, new JsonMessageCodec());
        this.movePayloadJsonMapper = new InputMovePayloadJsonMapper();
        this.skillPayloadJsonMapper = new InputSkillPayloadJsonMapper();
        this.createRoomPayloadJsonMapper = new CreateRoomPayloadJsonMapper();
        this.joinRoomPayloadJsonMapper = new JoinRoomPayloadJsonMapper();
        this.lobbyConfigPayloadJsonMapper = new LobbyConfigPayloadJsonMapper();
        this.readyPayloadJsonMapper = new ReadyPayloadJsonMapper();
        this.upgradeChoicePayloadJsonMapper = new UpgradeChoicePayloadJsonMapper();
        this.branchChoicePayloadJsonMapper = new BranchChoicePayloadJsonMapper();
        this.snapshotJsonMapper = new WorldSnapshotJsonMapper();
        this.timer = new Timer("local-authority-server", true);
        this.serverSequence = new AtomicLong();
        transport.setListener(this::handleMessage);
        transport.setConnectionListener(new LifecycleConnectionListener());
    }

    public void start() {
        transport.start();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long nowMillis = System.currentTimeMillis();
                for (RoomRuntime room : roomRegistry.allRooms()) {
                    room.tick(nowMillis, DISCONNECTED_SESSION_RETENTION_MILLIS);
                    sendSnapshot(room, serverSequence.incrementAndGet(), nowMillis);
                }
                roomRegistry.removeEmptyRooms();
            }
        }, 0, GameplayBalance.WORLD_TICK_INTERVAL_MILLIS);
    }

    public void stop() {
        timer.cancel();
        transport.stop();
    }

    public int getPort() {
        return transport.getPort();
    }

    public String getBindHost() {
        return transport.getBindHost();
    }

    public String getLocalSessionId() {
        return LOCAL_SESSION_ID;
    }

    private void handleMessage(ProtocolMessage message) {
        switch (message.getMessageType()) {
            case HELLO:
                handleHello(message);
                break;
            case CREATE_ROOM:
                handleCreateRoom(message);
                break;
            case JOIN_ROOM:
                handleJoinRoom(message);
                break;
            case START_GAME:
                handleStartGame(message);
                break;
            case INPUT_MOVE:
                handleMove(message);
                break;
            case INPUT_SKILL:
                handleSkill(message);
                break;
            case INPUT_READY:
                handleReady(message);
                break;
            case INPUT_LOBBY_CONFIG:
                handleLobbyConfig(message);
                break;
            case INPUT_UPGRADE_CHOICE:
                handleUpgradeChoice(message);
                break;
            case INPUT_BRANCH_CHOICE:
                handleBranchChoice(message);
                break;
            default:
                break;
        }
    }

    private void handleHello(ProtocolMessage message) {
        RoomRuntime room = roomRegistry.findBySession(message.getSessionId());
        if (room == null) {
            return;
        }
        room.addOrReconnectPlayer(message.getSessionId(), derivePlayerId(message.getSessionId()), message.getTimestamp());
        sendSnapshot(room, message.getSequence(), System.currentTimeMillis());
    }

    private void handleCreateRoom(ProtocolMessage message) {
        CreateRoomPayload payload = createRoomPayloadJsonMapper.fromJson(message.getPayload());
        RoomRuntime room = roomRegistry.createRoom(
                message.getSessionId(),
                derivePlayerId(message.getSessionId()),
                Difficulty.valueOf(payload.getDifficulty()),
                message.getTimestamp()
        );
        sendSnapshot(room, message.getSequence(), System.currentTimeMillis());
    }

    private void handleJoinRoom(ProtocolMessage message) {
        JoinRoomPayload payload = joinRoomPayloadJsonMapper.fromJson(message.getPayload());
        RoomRuntime room = roomRegistry.joinRoom(
                message.getSessionId(),
                derivePlayerId(message.getSessionId()),
                payload.getRoomCode(),
                message.getTimestamp()
        );
        if (room != null) {
            sendSnapshot(room, message.getSequence(), System.currentTimeMillis());
        }
    }

    private void handleStartGame(ProtocolMessage message) {
        RoomRuntime room = touchRoom(message);
        if (room == null) {
            return;
        }
        room.startRoundIfHost(message.getSessionId());
        sendSnapshot(room, message.getSequence(), System.currentTimeMillis());
    }

    private void handleMove(ProtocolMessage message) {
        RoomRuntime room = touchRoom(message);
        if (room == null) {
            return;
        }
        InputMovePayload payload = movePayloadJsonMapper.fromJson(message.getPayload());
        room.handleMove(message.getSessionId(), payload.getX(), payload.getY(), message.getSequence(), message.getTimestamp());
        sendSnapshot(room, message.getSequence(), System.currentTimeMillis());
    }

    private void handleSkill(ProtocolMessage message) {
        RoomRuntime room = touchRoom(message);
        if (room == null) {
            return;
        }
        InputSkillPayload payload = skillPayloadJsonMapper.fromJson(message.getPayload());
        room.handleSkill(message.getSessionId(), payload.getSkillType(), message.getSequence(), message.getTimestamp());
        sendSnapshot(room, message.getSequence(), System.currentTimeMillis());
    }

    private void handleReady(ProtocolMessage message) {
        RoomRuntime room = touchRoom(message);
        if (room == null) {
            return;
        }
        ReadyPayload payload = readyPayloadJsonMapper.fromJson(message.getPayload());
        room.updateReady(message.getSessionId(), payload.isReady());
        sendSnapshot(room, message.getSequence(), System.currentTimeMillis());
    }

    private void handleLobbyConfig(ProtocolMessage message) {
        RoomRuntime room = touchRoom(message);
        if (room == null) {
            return;
        }
        LobbyConfigPayload payload = lobbyConfigPayloadJsonMapper.fromJson(message.getPayload());
        Difficulty difficulty = payload.getDifficulty() == null ? null : Difficulty.valueOf(payload.getDifficulty());
        room.updateLobbyConfig(message.getSessionId(), difficulty);
        sendSnapshot(room, message.getSequence(), System.currentTimeMillis());
    }

    private void handleUpgradeChoice(ProtocolMessage message) {
        RoomRuntime room = touchRoom(message);
        if (room == null) {
            return;
        }
        UpgradeChoicePayload payload = upgradeChoicePayloadJsonMapper.fromJson(message.getPayload());
        long nowMillis = System.currentTimeMillis();
        room.handleUpgradeChoice(message.getSessionId(), payload.getChoice(), message.getSequence(), nowMillis);
        sendSnapshot(room, message.getSequence(), nowMillis);
    }

    private void handleBranchChoice(ProtocolMessage message) {
        RoomRuntime room = touchRoom(message);
        if (room == null) {
            return;
        }
        BranchChoicePayload payload = branchChoicePayloadJsonMapper.fromJson(message.getPayload());
        long nowMillis = System.currentTimeMillis();
        room.handleBranchChoice(message.getSessionId(), payload.getBranch(), message.getSequence(), nowMillis);
        sendSnapshot(room, message.getSequence(), nowMillis);
    }

    private RoomRuntime touchRoom(ProtocolMessage message) {
        RoomRuntime room = roomRegistry.findBySession(message.getSessionId());
        if (room == null) {
            return null;
        }
        PlayerSession session = room.findSession(message.getSessionId());
        if (session != null) {
            session.markConnected(message.getTimestamp());
            session.markSeen(message.getTimestamp());
        }
        return room;
    }

    private void sendSnapshot(RoomRuntime room, long sequence, long nowMillis) {
        if (room == null || room.connectedSessionIds().isEmpty()) {
            return;
        }
        WorldSnapshot snapshot = room.buildSnapshot(nowMillis);
        transport.sendToSessions(new ProtocolMessage(
                MessageType.WORLD_SNAPSHOT,
                "broadcast",
                sequence,
                System.currentTimeMillis(),
                snapshotJsonMapper.toJson(snapshot)
        ), room.connectedSessionIds());
    }

    private String derivePlayerId(String sessionId) {
        if (LOCAL_SESSION_ID.equals(sessionId)) {
            return LOCAL_PLAYER_ID;
        }
        if (sessionId.startsWith("session-")) {
            return "player-" + sessionId.substring("session-".length());
        }
        return "player-" + sessionId;
    }

    private final class LifecycleConnectionListener implements ServerConnectionListener {
        @Override
        public void onClientDisconnected(String sessionId) {
            RoomRuntime room = roomRegistry.findBySession(sessionId);
            if (room != null) {
                room.markDisconnected(sessionId, System.currentTimeMillis());
            }
        }
    }
}
