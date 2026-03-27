package edu.hitsz.client;

import edu.hitsz.common.protocol.MessageType;
import edu.hitsz.common.protocol.ProtocolMessage;
import edu.hitsz.common.protocol.dto.CreateRoomPayload;
import edu.hitsz.common.protocol.dto.InputMovePayload;
import edu.hitsz.common.protocol.dto.InputSkillPayload;
import edu.hitsz.common.protocol.dto.JoinRoomPayload;
import edu.hitsz.common.protocol.dto.LobbyConfigPayload;
import edu.hitsz.common.protocol.dto.ReadyPayload;
import edu.hitsz.common.protocol.dto.UpgradeChoicePayload;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.CreateRoomPayloadJsonMapper;
import edu.hitsz.common.protocol.json.InputMovePayloadJsonMapper;
import edu.hitsz.common.protocol.json.InputSkillPayloadJsonMapper;
import edu.hitsz.common.protocol.json.JoinRoomPayloadJsonMapper;
import edu.hitsz.common.protocol.json.JsonMessageCodec;
import edu.hitsz.common.protocol.json.LobbyConfigPayloadJsonMapper;
import edu.hitsz.common.protocol.json.ReadyPayloadJsonMapper;
import edu.hitsz.common.protocol.json.UpgradeChoicePayloadJsonMapper;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;
import edu.hitsz.common.protocol.socket.SocketClientTransport;

import javax.swing.SwingUtilities;
import java.util.concurrent.atomic.AtomicLong;

public class SocketClientSession implements ClientCommandPublisher {

    private final String sessionId;
    private final Game game;
    private final SocketClientTransport transport;
    private final AtomicLong sequence;
    private final CreateRoomPayloadJsonMapper createRoomPayloadJsonMapper;
    private final JoinRoomPayloadJsonMapper joinRoomPayloadJsonMapper;
    private final InputMovePayloadJsonMapper movePayloadJsonMapper;
    private final InputSkillPayloadJsonMapper skillPayloadJsonMapper;
    private final LobbyConfigPayloadJsonMapper lobbyConfigPayloadJsonMapper;
    private final ReadyPayloadJsonMapper readyPayloadJsonMapper;
    private final UpgradeChoicePayloadJsonMapper upgradeChoicePayloadJsonMapper;
    private final WorldSnapshotJsonMapper worldSnapshotJsonMapper;

    public SocketClientSession(String host, int port, String sessionId, Game game) {
        this.sessionId = sessionId;
        this.game = game;
        this.game.setLocalSessionId(sessionId);
        this.transport = new SocketClientTransport(host, port, new JsonMessageCodec());
        this.sequence = new AtomicLong();
        this.createRoomPayloadJsonMapper = new CreateRoomPayloadJsonMapper();
        this.joinRoomPayloadJsonMapper = new JoinRoomPayloadJsonMapper();
        this.movePayloadJsonMapper = new InputMovePayloadJsonMapper();
        this.skillPayloadJsonMapper = new InputSkillPayloadJsonMapper();
        this.lobbyConfigPayloadJsonMapper = new LobbyConfigPayloadJsonMapper();
        this.readyPayloadJsonMapper = new ReadyPayloadJsonMapper();
        this.upgradeChoicePayloadJsonMapper = new UpgradeChoicePayloadJsonMapper();
        this.worldSnapshotJsonMapper = new WorldSnapshotJsonMapper();
    }

    @Override
    public void start() {
        transport.setListener(message -> {
            if (message.getMessageType() != MessageType.WORLD_SNAPSHOT || message.getPayload() == null) {
                return;
            }
            WorldSnapshot snapshot = worldSnapshotJsonMapper.fromJson(message.getPayload());
            SwingUtilities.invokeLater(() -> game.applyWorldSnapshot(snapshot));
        });
        transport.start();
        transport.send(new ProtocolMessage(
                MessageType.HELLO,
                sessionId,
                nextSequence(),
                System.currentTimeMillis(),
                "{}"
        ));
    }

    @Override
    public void stop() {
        transport.stop();
    }

    @Override
    public void publishCreateRoom(String difficulty, String selectedSkill) {
        publishCreateRoom(difficulty);
    }

    @Override
    public void publishCreateRoom(String difficulty) {
        transport.send(new ProtocolMessage(
                MessageType.CREATE_ROOM,
                sessionId,
                nextSequence(),
                System.currentTimeMillis(),
                createRoomPayloadJsonMapper.toJson(new CreateRoomPayload(difficulty))
        ));
    }

    @Override
    public void publishJoinRoom(String roomCode, String selectedSkill) {
        publishJoinRoom(roomCode);
    }

    @Override
    public void publishJoinRoom(String roomCode) {
        transport.send(new ProtocolMessage(
                MessageType.JOIN_ROOM,
                sessionId,
                nextSequence(),
                System.currentTimeMillis(),
                joinRoomPayloadJsonMapper.toJson(new JoinRoomPayload(roomCode))
        ));
    }

    @Override
    public void publishStartGame() {
        transport.send(new ProtocolMessage(
                MessageType.START_GAME,
                sessionId,
                nextSequence(),
                System.currentTimeMillis(),
                "{}"
        ));
    }

    @Override
    public void publishMove(int x, int y) {
        transport.send(new ProtocolMessage(
                MessageType.INPUT_MOVE,
                sessionId,
                nextSequence(),
                System.currentTimeMillis(),
                movePayloadJsonMapper.toJson(new InputMovePayload(x, y))
        ));
    }

    @Override
    public void publishSkill(String skillType) {
        transport.send(new ProtocolMessage(
                MessageType.INPUT_SKILL,
                sessionId,
                nextSequence(),
                System.currentTimeMillis(),
                skillPayloadJsonMapper.toJson(new InputSkillPayload(skillType))
        ));
    }

    @Override
    public void publishReady(boolean ready) {
        transport.send(new ProtocolMessage(
                MessageType.INPUT_READY,
                sessionId,
                nextSequence(),
                System.currentTimeMillis(),
                readyPayloadJsonMapper.toJson(new ReadyPayload(ready))
        ));
    }

    @Override
    public void publishLobbyConfig(String difficulty, String selectedSkill) {
        publishLobbyConfig(difficulty);
    }

    @Override
    public void publishLobbyConfig(String difficulty) {
        transport.send(new ProtocolMessage(
                MessageType.INPUT_LOBBY_CONFIG,
                sessionId,
                nextSequence(),
                System.currentTimeMillis(),
                lobbyConfigPayloadJsonMapper.toJson(new LobbyConfigPayload(difficulty))
        ));
    }

    @Override
    public void publishUpgradeChoice(String choice) {
        transport.send(new ProtocolMessage(
                MessageType.INPUT_UPGRADE_CHOICE,
                sessionId,
                nextSequence(),
                System.currentTimeMillis(),
                upgradeChoicePayloadJsonMapper.toJson(new UpgradeChoicePayload(choice))
        ));
    }

    private long nextSequence() {
        return sequence.incrementAndGet();
    }
}
