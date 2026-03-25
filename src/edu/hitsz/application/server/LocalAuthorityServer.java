package edu.hitsz.application.server;

import edu.hitsz.application.Main;
import edu.hitsz.application.protocol.MessageType;
import edu.hitsz.application.protocol.ProtocolMessage;
import edu.hitsz.application.protocol.dto.InputMovePayload;
import edu.hitsz.application.protocol.dto.InputSkillPayload;
import edu.hitsz.application.protocol.dto.WorldSnapshot;
import edu.hitsz.application.protocol.json.InputMovePayloadJsonMapper;
import edu.hitsz.application.protocol.json.InputSkillPayloadJsonMapper;
import edu.hitsz.application.protocol.json.JsonMessageCodec;
import edu.hitsz.application.protocol.json.WorldSnapshotJsonMapper;
import edu.hitsz.application.protocol.socket.SocketServerTransport;
import edu.hitsz.application.server.command.MoveCommand;
import edu.hitsz.application.server.command.SkillCommand;
import edu.hitsz.application.server.skill.DefaultServerSkillResolver;
import edu.hitsz.application.server.skill.SkillScalingConfig;
import edu.hitsz.application.server.skill.SkillType;

public class LocalAuthorityServer {

    private static final String LOCAL_SESSION_ID = "session-local";
    private static final String LOCAL_PLAYER_ID = "player-local";
    private static final String REMOTE_SESSION_ID = "session-2";
    private static final String REMOTE_PLAYER_ID = "player-2";

    private final ServerWorldState worldState;
    private final ServerGameLoop gameLoop;
    private final ServerCommandRouter commandRouter;
    private final DefaultServerSkillResolver skillResolver;
    private final SocketServerTransport transport;
    private final InputMovePayloadJsonMapper movePayloadJsonMapper;
    private final InputSkillPayloadJsonMapper skillPayloadJsonMapper;
    private final WorldSnapshotJsonMapper snapshotJsonMapper;

    public LocalAuthorityServer(int port) {
        this.worldState = new ServerWorldState();
        this.gameLoop = new ServerGameLoop(worldState);
        this.commandRouter = new ServerCommandRouter(worldState.getSessionRegistry());
        this.skillResolver = new DefaultServerSkillResolver(SkillScalingConfig.defaultConfig());
        this.transport = new SocketServerTransport(port, new JsonMessageCodec());
        this.movePayloadJsonMapper = new InputMovePayloadJsonMapper();
        this.skillPayloadJsonMapper = new InputSkillPayloadJsonMapper();
        this.snapshotJsonMapper = new WorldSnapshotJsonMapper();
        seedDemoPlayers();
        transport.setListener(this::handleMessage);
    }

    public void start() {
        transport.start();
    }

    public void stop() {
        transport.stop();
    }

    public int getPort() {
        return transport.getPort();
    }

    public String getLocalSessionId() {
        return LOCAL_SESSION_ID;
    }

    private void seedDemoPlayers() {
        PlayerSession localSession = worldState.getSessionRegistry().create(LOCAL_SESSION_ID, LOCAL_PLAYER_ID);
        localSession.getPlayerState().setPosition(
                Main.WINDOW_WIDTH / 2,
                Main.WINDOW_HEIGHT - 80
        );

        PlayerSession remoteSession = worldState.getSessionRegistry().create(REMOTE_SESSION_ID, REMOTE_PLAYER_ID);
        remoteSession.getPlayerState().setPosition(
                Main.WINDOW_WIDTH / 2 - 80,
                Main.WINDOW_HEIGHT - 160
        );
    }

    private void handleMessage(ProtocolMessage message) {
        switch (message.getMessageType()) {
            case HELLO:
                sendSnapshot(message.getSessionId(), message.getSequence());
                break;
            case INPUT_MOVE:
                handleMove(message);
                break;
            case INPUT_SKILL:
                handleSkill(message);
                break;
            default:
                break;
        }
    }

    private void handleMove(ProtocolMessage message) {
        InputMovePayload payload = movePayloadJsonMapper.fromJson(message.getPayload());
        PlayerSession session = commandRouter.route(new MoveCommand(
                message.getSessionId(),
                payload.getX(),
                payload.getY(),
                message.getSequence(),
                message.getTimestamp()
        ));
        session.getPlayerState().setPosition(payload.getX(), payload.getY());
        gameLoop.stepOnce(message.getTimestamp());
        sendSnapshot(message.getSessionId(), message.getSequence());
    }

    private void handleSkill(ProtocolMessage message) {
        InputSkillPayload payload = skillPayloadJsonMapper.fromJson(message.getPayload());
        PlayerSession session = commandRouter.route(new SkillCommand(
                message.getSessionId(),
                payload.getSkillType(),
                message.getSequence(),
                message.getTimestamp()
        ));
        skillResolver.applySkill(
                SkillType.valueOf(payload.getSkillType()),
                session,
                worldState,
                message.getTimestamp()
        );
        gameLoop.stepOnce(message.getTimestamp());
        sendSnapshot(message.getSessionId(), message.getSequence());
    }

    private void sendSnapshot(String sessionId, long sequence) {
        WorldSnapshot snapshot = gameLoop.buildSnapshotForSession(sessionId);
        transport.send(new ProtocolMessage(
                MessageType.WORLD_SNAPSHOT,
                sessionId,
                sequence,
                System.currentTimeMillis(),
                snapshotJsonMapper.toJson(snapshot)
        ));
    }
}
