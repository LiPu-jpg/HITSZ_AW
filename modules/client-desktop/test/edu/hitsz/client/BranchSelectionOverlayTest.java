package edu.hitsz.client;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.GamePhase;
import edu.hitsz.common.UpgradeChoice;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.Arrays;

public class BranchSelectionOverlayTest {

    public static void main(String[] args) throws Exception {
        showsOverlayAndRoutesNumberKeysToBranchChoices();
        preservesUpgradeSelectionNumberRoutingOutsideBranchOverlay();
        removesOutdatedLobbySkillHint();
    }

    private static void showsOverlayAndRoutesNumberKeysToBranchChoices() {
        Game game = new Game();
        game.setLocalSessionId("session-local");
        RecordingPublisher publisher = new RecordingPublisher();
        game.attachCommandPublisher(publisher);

        WorldSnapshot branchSnapshot = new WorldSnapshot(1L);
        branchSnapshot.setGameStarted(true);
        branchSnapshot.setGamePhase(GamePhase.BRANCH_SELECTION);
        branchSnapshot.addPlayerSnapshot(new PlayerSnapshot(
                "session-local",
                "player-local",
                200,
                300,
                900,
                66,
                false,
                1,
                null,
                0L,
                1000,
                java.util.Collections.emptyList(),
                null,
                AircraftBranch.STARTER_BLUE,
                Arrays.asList(AircraftBranch.RED_SPEED, AircraftBranch.GREEN_DEFENSE, AircraftBranch.BLACK_HEAVY),
                false
        ));
        game.applyWorldSnapshot(branchSnapshot);

        assert game.isBranchSelectionVisible() : "Branch overlay should show during branch selection";
        assert !game.isUpgradeSelectionVisible() : "Upgrade overlay should stay hidden during branch selection";

        pressKey(game, KeyEvent.VK_1);
        assert "RED_SPEED".equals(publisher.lastBranchChoice)
                : "Key 1 should publish the first branch choice";
        pressKey(game, KeyEvent.VK_2);
        assert "GREEN_DEFENSE".equals(publisher.lastBranchChoice)
                : "Key 2 should publish the second branch choice";
        pressKey(game, KeyEvent.VK_3);
        assert "BLACK_HEAVY".equals(publisher.lastBranchChoice)
                : "Key 3 should publish the third branch choice";
    }

    private static void preservesUpgradeSelectionNumberRoutingOutsideBranchOverlay() {
        Game game = new Game();
        game.setLocalSessionId("session-local");
        RecordingPublisher publisher = new RecordingPublisher();
        game.attachCommandPublisher(publisher);

        WorldSnapshot upgradeSnapshot = new WorldSnapshot(2L);
        upgradeSnapshot.setGameStarted(true);
        upgradeSnapshot.setGamePhase(GamePhase.UPGRADE_SELECTION);
        upgradeSnapshot.addPlayerSnapshot(new PlayerSnapshot(
                "session-local",
                "player-local",
                200,
                300,
                900,
                66,
                false,
                2,
                "FREEZE",
                0L,
                1000,
                Arrays.asList(UpgradeChoice.FIRE_RATE, UpgradeChoice.BULLET_POWER, UpgradeChoice.LIGHT_TRACKING),
                null,
                AircraftBranch.GREEN_DEFENSE,
                java.util.Collections.emptyList(),
                true
        ));
        game.applyWorldSnapshot(upgradeSnapshot);

        assert !game.isBranchSelectionVisible() : "Branch overlay should hide outside branch selection";
        assert game.isUpgradeSelectionVisible() : "Upgrade overlay should still work";

        pressKey(game, KeyEvent.VK_2);
        assert "BULLET_POWER".equals(publisher.lastUpgradeChoice)
                : "Number keys should keep upgrade routing when branch overlay is inactive";
    }

    private static void removesOutdatedLobbySkillHint() throws Exception {
        Game game = new Game();
        game.setLocalSessionId("session-local");

        WorldSnapshot lobbySnapshot = new WorldSnapshot(3L);
        lobbySnapshot.setGameStarted(false);
        lobbySnapshot.setReadyPlayerCount(1);
        lobbySnapshot.setConnectedPlayerCount(2);
        lobbySnapshot.setDifficulty("NORMAL");
        lobbySnapshot.setRoomCode("123456");
        lobbySnapshot.addPlayerSnapshot(new PlayerSnapshot(
                "session-local",
                "player-local",
                200,
                300,
                900,
                66,
                false,
                1,
                null,
                0L,
                1000,
                java.util.Collections.emptyList(),
                null,
                AircraftBranch.STARTER_BLUE,
                Arrays.asList(AircraftBranch.RED_SPEED, AircraftBranch.GREEN_DEFENSE, AircraftBranch.BLACK_HEAVY),
                false
        ));
        game.applyWorldSnapshot(lobbySnapshot);

        String[] lines = buildLobbyOverlayLines(game);
        for (String line : lines) {
            assert !line.contains("Skill") : "Lobby overlay should not mention pre-game skill selection: " + line;
        }
    }

    private static void pressKey(Game game, int keyCode) {
        KeyEvent event = new KeyEvent(game, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, KeyEvent.CHAR_UNDEFINED);
        for (java.awt.event.KeyListener listener : game.getKeyListeners()) {
            listener.keyPressed(event);
        }
    }

    private static String[] buildLobbyOverlayLines(Game game) throws Exception {
        Method method = Game.class.getDeclaredMethod("buildLobbyOverlayLines");
        method.setAccessible(true);
        return (String[]) method.invoke(game);
    }

    private static final class RecordingPublisher implements ClientCommandPublisher {
        private String lastUpgradeChoice;
        private String lastBranchChoice;

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void publishCreateRoom(String difficulty) {
        }

        @Override
        public void publishJoinRoom(String roomCode) {
        }

        @Override
        public void publishStartGame() {
        }

        @Override
        public void publishMove(int x, int y) {
        }

        @Override
        public void publishSkill(String skillType) {
        }

        @Override
        public void publishReady(boolean ready) {
        }

        @Override
        public void publishLobbyConfig(String difficulty) {
        }

        @Override
        public void publishUpgradeChoice(String choice) {
            lastUpgradeChoice = choice;
        }

        @Override
        public void publishBranchChoice(String branch) {
            lastBranchChoice = branch;
        }
    }
}
