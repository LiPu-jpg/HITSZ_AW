package edu.hitsz.application.client;

import edu.hitsz.application.Game;
import edu.hitsz.application.protocol.MessageType;
import edu.hitsz.application.protocol.ProtocolMessage;
import edu.hitsz.application.protocol.dto.InputMovePayload;
import edu.hitsz.application.protocol.dto.InputSkillPayload;
import edu.hitsz.application.protocol.dto.WorldSnapshot;
import edu.hitsz.application.protocol.json.InputMovePayloadJsonMapper;
import edu.hitsz.application.protocol.json.InputSkillPayloadJsonMapper;
import edu.hitsz.application.protocol.json.JsonMessageCodec;
import edu.hitsz.application.protocol.json.WorldSnapshotJsonMapper;
import edu.hitsz.application.protocol.socket.SocketClientTransport;

import java.util.concurrent.atomic.AtomicLong;

public class SocketClientSession implements ClientCommandPublisher {

    private final String sessionId;
    private final Game game;
    private final SocketClientTransport transport;
    private final AtomicLong sequence;
    private final InputMovePayloadJsonMapper movePayloadJsonMapper;
    private final InputSkillPayloadJsonMapper skillPayloadJsonMapper;
    private final WorldSnapshotJsonMapper worldSnapshotJsonMapper;

    public SocketClientSession(String host, int port, String sessionId, Game game) {
        this.sessionId = sessionId;
        this.game = game;
        this.transport = new SocketClientTransport(host, port, new JsonMessageCodec());
        this.sequence = new AtomicLong();
        this.movePayloadJsonMapper = new InputMovePayloadJsonMapper();
        this.skillPayloadJsonMapper = new InputSkillPayloadJsonMapper();
        this.worldSnapshotJsonMapper = new WorldSnapshotJsonMapper();
    }

    @Override
    public void start() {
        transport.setListener(message -> {
            if (message.getMessageType() != MessageType.WORLD_SNAPSHOT || message.getPayload() == null) {
                return;
            }
            WorldSnapshot snapshot = worldSnapshotJsonMapper.fromJson(message.getPayload());
            game.applyWorldSnapshot(snapshot);
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

    private long nextSequence() {
        return sequence.incrementAndGet();
    }
}
