package edu.hitsz.server;

import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GameConstants;
import edu.hitsz.common.ChapterId;
import edu.hitsz.common.GamePhase;
import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.BranchUpgradeChoice;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.server.command.MoveCommand;
import edu.hitsz.server.command.SkillCommand;
import edu.hitsz.server.skill.DefaultServerSkillResolver;
import edu.hitsz.server.skill.SkillScalingConfig;
import edu.hitsz.server.skill.SkillType;

import java.util.LinkedList;
import java.util.List;

public class RoomRuntime {

    private final String roomCode;
    private final ServerWorldState worldState;
    private final ServerGameLoop gameLoop;
    private final ServerCommandRouter commandRouter;
    private final DefaultServerSkillResolver skillResolver;
    private String hostSessionId;
    private boolean gameStarted;

    public RoomRuntime(String roomCode, String hostSessionId, Difficulty difficulty) {
        this.roomCode = roomCode;
        this.hostSessionId = hostSessionId;
        this.worldState = new ServerWorldState();
        this.gameLoop = new ServerGameLoop(worldState);
        this.commandRouter = new ServerCommandRouter(worldState.getSessionRegistry());
        this.skillResolver = new DefaultServerSkillResolver(SkillScalingConfig.defaultConfig());
        this.worldState.setDifficulty(difficulty);
    }

    public synchronized String getRoomCode() {
        return roomCode;
    }

    public synchronized String getHostSessionId() {
        return hostSessionId;
    }

    public synchronized boolean isGameStarted() {
        return gameStarted;
    }

    public synchronized Difficulty getDifficulty() {
        return worldState.getDifficulty();
    }

    public synchronized ChapterProgressionState getChapterProgressionState() {
        return worldState.getChapterProgressionState();
    }

    public synchronized ChapterId getChapterId() {
        return worldState.getChapterId();
    }

    public synchronized GamePhase getGamePhase() {
        return worldState.getGamePhase();
    }

    public synchronized PlayerSession findSession(String sessionId) {
        return worldState.getSessionRegistry().find(sessionId);
    }

    public synchronized boolean containsSession(String sessionId) {
        return findSession(sessionId) != null;
    }

    public synchronized void addOrReconnectPlayer(String sessionId, String playerId, long nowMillis) {
        PlayerSession session = worldState.getSessionRegistry().find(sessionId);
        if (gameStarted && session == null) {
            return;
        }
        if (session == null) {
            session = worldState.getSessionRegistry().create(sessionId, playerId);
            resetSessionForLobby(session);
        } else if (!gameStarted && shouldResetForLobby(session)) {
            resetSessionForLobby(session);
        }
        session.markConnected(nowMillis);
        session.markSeen(nowMillis);
        session.setReady(false);
    }

    public synchronized void removeSession(String sessionId) {
        worldState.getSessionRegistry().remove(sessionId);
        if (sessionId.equals(hostSessionId)) {
            reassignHostIfNecessary();
        }
    }

    public synchronized void markDisconnected(String sessionId, long nowMillis) {
        PlayerSession session = findSession(sessionId);
        if (session == null) {
            return;
        }
        session.markDisconnected(nowMillis);
        if (!gameStarted) {
            session.setReady(false);
        }
    }

    public synchronized void updateLobbyConfig(String sessionId, Difficulty difficulty) {
        if (gameStarted) {
            return;
        }
        PlayerSession session = commandRouter.validate(sessionId);
        if (difficulty != null && hostSessionId.equals(sessionId)) {
            boolean difficultyChanged = worldState.getDifficulty() != difficulty;
            worldState.setDifficulty(difficulty);
            if (difficultyChanged) {
                clearAllReadyStates();
            }
        }
    }

    public synchronized void updateReady(String sessionId, boolean ready) {
        commandRouter.validate(sessionId).setReady(ready);
    }

    public synchronized void startRoundIfHost(String sessionId) {
        if (gameStarted || !hostSessionId.equals(sessionId)) {
            return;
        }
        if (!allConnectedPlayersReady()) {
            return;
        }
        worldState.resetRoundState();
        worldState.startBattle();
        for (PlayerSession session : worldState.getSessionRegistry().allSessions()) {
            if (session.isConnected()) {
                resetSessionForRound(session);
            } else {
                markAsSpectatingForRound(session);
            }
        }
        gameStarted = true;
    }

    public synchronized void handleMove(String sessionId, int x, int y, long sequence, long timestamp) {
        if (!gameStarted || worldState.getGamePhase() != GamePhase.BATTLE) {
            return;
        }
        PlayerSession session = commandRouter.route(new MoveCommand(sessionId, x, y, sequence, timestamp));
        session.getPlayerState().setTargetPosition(x, y);
    }

    public synchronized void handleSkill(String sessionId, String skillType, long sequence, long timestamp) {
        if (!gameStarted || worldState.getGamePhase() != GamePhase.BATTLE) {
            return;
        }
        PlayerSession session = commandRouter.route(new SkillCommand(sessionId, skillType, sequence, timestamp));
        if (!session.getPlayerState().isBranchUnlocked()
                || session.getPlayerState().getSelectedSkill() == null) {
            return;
        }
        try {
            skillResolver.applySkill(
                    SkillType.valueOf(session.getPlayerState().getSelectedSkill()),
                    session,
                    worldState,
                    timestamp
            );
        } catch (IllegalArgumentException ignored) {
            // Ignore unsupported skill values until branch selection is implemented.
        }
    }

    public synchronized void handleUpgradeChoice(String sessionId, String choice, long sequence, long timestamp) {
        if (!gameStarted || worldState.getGamePhase() != GamePhase.UPGRADE_SELECTION) {
            return;
        }
        if (timestamp < worldState.getFlashUntilMillis()) {
            return;
        }
        PlayerSession session = commandRouter.validate(sessionId);
        if (session.getPlayerState().getAircraft().notValid()) {
            return;
        }
        try {
            session.getPlayerState().applyUpgradeChoice(BranchUpgradeChoice.valueOf(choice));
        } catch (IllegalArgumentException ignored) {
            // Ignore invalid upgrade choices from the client.
        }
    }

    public synchronized void handleBranchChoice(String sessionId, String branch, long sequence, long timestamp) {
        if (!gameStarted || worldState.getGamePhase() != GamePhase.BRANCH_SELECTION) {
            return;
        }
        PlayerSession session = commandRouter.validate(sessionId);
        if (session.getPlayerState().getAircraft().notValid()) {
            return;
        }
        try {
            session.getPlayerState().applyBranchChoice(AircraftBranch.valueOf(branch));
        } catch (IllegalArgumentException ignored) {
            // Ignore invalid branch choices from the client.
        }
    }

    public synchronized void tick(long nowMillis, long disconnectedSessionRetentionMillis) {
        worldState.getSessionRegistry().removeExpiredDisconnectedSessions(nowMillis, disconnectedSessionRetentionMillis);
        reassignHostIfNecessary();
        if (gameStarted) {
            if (worldState.getGamePhase() == GamePhase.UPGRADE_SELECTION
                    || worldState.getGamePhase() == GamePhase.BRANCH_SELECTION) {
                if (worldState.getSessionRegistry().allSessions().isEmpty()) {
                    returnToLobby();
                    return;
                }
                if (worldState.getGamePhase() == GamePhase.UPGRADE_SELECTION
                        && nowMillis < worldState.getFlashUntilMillis()) {
                    return;
                }
                if (!selectionRequirementsSatisfied()) {
                    return;
                }
                if (!worldState.advanceAfterBossSelection()) {
                    returnToLobby();
                    return;
                }
                return;
            }
            if (connectedPlayerCount() == 0) {
                if (worldState.getSessionRegistry().allSessions().isEmpty()) {
                    returnToLobby();
                }
                return;
            }
            if (!worldState.hasAnyConnectedAlivePlayer()) {
                if (worldState.hasAnyDisconnectedAlivePlayer()) {
                    return;
                }
                returnToLobby();
                return;
            }
            gameLoop.stepOnce(nowMillis);
            if (!worldState.hasAnyConnectedAlivePlayer()) {
                if (worldState.hasAnyDisconnectedAlivePlayer()) {
                    return;
                }
                returnToLobby();
            }
        }
    }

    private boolean allAlivePlayersSelectedUpgrade() {
        for (PlayerSession session : worldState.getSessionRegistry().allSessions()) {
            if (session.getPlayerState().getAircraft().notValid()) {
                continue;
            }
            if (session.getPlayerState().hasPendingUpgradeChoice()) {
                return false;
            }
        }
        return true;
    }

    private boolean allAlivePlayersSelectedBranch() {
        for (PlayerSession session : worldState.getSessionRegistry().allSessions()) {
            if (session.getPlayerState().getAircraft().notValid()) {
                continue;
            }
            if (session.getPlayerState().hasPendingBranchChoice()) {
                return false;
            }
        }
        return true;
    }

    private boolean selectionRequirementsSatisfied() {
        if (worldState.getGamePhase() == GamePhase.BRANCH_SELECTION) {
            return allAlivePlayersSelectedBranch();
        }
        return allAlivePlayersSelectedUpgrade();
    }

    public synchronized WorldSnapshot buildSnapshot() {
        return buildSnapshot(System.currentTimeMillis());
    }

    public synchronized WorldSnapshot buildSnapshot(long nowMillis) {
        WorldSnapshot snapshot = gameLoop.buildSnapshot(nowMillis);
        snapshot.setRoomCode(roomCode);
        snapshot.setHostSessionId(hostSessionId);
        snapshot.setGameStarted(gameStarted);
        snapshot.setConnectedPlayerCount(connectedPlayerCount());
        snapshot.setReadyPlayerCount(readyPlayerCount());
        snapshot.setDifficulty(worldState.getDifficulty().name());
        return snapshot;
    }

    public synchronized List<String> connectedSessionIds() {
        List<String> sessionIds = new LinkedList<>();
        for (PlayerSession session : worldState.getPlayerSessions()) {
            sessionIds.add(session.getSessionId());
        }
        return sessionIds;
    }

    public synchronized boolean isEmpty() {
        return worldState.getSessionRegistry().allSessions().isEmpty();
    }

    public synchronized int connectedPlayerCount() {
        return worldState.getPlayerSessions().size();
    }

    private boolean allConnectedPlayersReady() {
        int connectedCount = connectedPlayerCount();
        return connectedCount > 0 && readyPlayerCount() == connectedCount;
    }

    private int readyPlayerCount() {
        int readyCount = 0;
        for (PlayerSession session : worldState.getPlayerSessions()) {
            if (session.isReady()) {
                readyCount++;
            }
        }
        return readyCount;
    }

    private void clearAllReadyStates() {
        for (PlayerSession session : worldState.getPlayerSessions()) {
            session.setReady(false);
        }
    }

    private boolean shouldResetForLobby(PlayerSession session) {
        return session.getPlayerState().getAircraft().notValid()
                || session.getPlayerState().getAircraftBranch() != edu.hitsz.common.AircraftBranch.STARTER_BLUE
                || session.getPlayerState().isBranchUnlocked()
                || session.getPlayerState().getSelectedSkill() != null
                || session.getPlayerState().getScore() > 0
                || session.isReady();
    }

    private void resetSessionForRound(PlayerSession session) {
        int playerIndex = playerIndexOf(session.getSessionId());
        session.getPlayerState().resetForNewRound(
                spawnXForPlayerIndex(playerIndex),
                GameConstants.WINDOW_HEIGHT - 80
        );
        session.setReady(false);
    }

    private void resetSessionForLobby(PlayerSession session) {
        resetSessionForRound(session);
    }

    private void markAsSpectatingForRound(PlayerSession session) {
        session.getPlayerState().setHp(0);
        session.getPlayerState().setScore(0);
        session.setReady(false);
    }

    private int playerIndexOf(String sessionId) {
        int index = 0;
        for (PlayerSession session : worldState.getSessionRegistry().allSessions()) {
            if (session.getSessionId().equals(sessionId)) {
                return index;
            }
            index++;
        }
        return connectedPlayerCount();
    }

    private int spawnXForPlayerIndex(int playerIndex) {
        int baseX = GameConstants.WINDOW_WIDTH / 2 + playerIndex * 80;
        return Math.min(GameConstants.WINDOW_WIDTH - 60, baseX);
    }

    private void returnToLobby() {
        worldState.resetRoundState();
        for (PlayerSession session : worldState.getSessionRegistry().allSessions()) {
            resetSessionForLobby(session);
        }
        gameStarted = false;
    }

    private void reassignHostIfNecessary() {
        PlayerSession currentHost = hostSessionId == null
                ? null
                : worldState.getSessionRegistry().find(hostSessionId);
        if (currentHost != null && currentHost.isConnected()) {
            return;
        }
        for (PlayerSession session : worldState.getSessionRegistry().allSessions()) {
            if (session.isConnected()) {
                hostSessionId = session.getSessionId();
                return;
            }
        }
        hostSessionId = null;
        for (PlayerSession session : worldState.getSessionRegistry().allSessions()) {
            hostSessionId = session.getSessionId();
            break;
        }
    }
}
