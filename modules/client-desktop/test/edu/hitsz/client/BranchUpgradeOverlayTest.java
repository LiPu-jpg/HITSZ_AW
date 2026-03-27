package edu.hitsz.client;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.GamePhase;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BranchUpgradeOverlayTest {

    public static void main(String[] args) throws Exception {
        showsBranchLocalUpgradeLabelsAndSelectionRouting();
    }

    private static void showsBranchLocalUpgradeLabelsAndSelectionRouting() throws Exception {
        Game game = new Game();
        game.setLocalSessionId("session-local");
        RecordingPublisher publisher = new RecordingPublisher();
        game.attachCommandPublisher(publisher);

        Class<?> branchUpgradeChoiceClass = Class.forName("edu.hitsz.common.BranchUpgradeChoice");
        Object laserDamage = Enum.valueOf((Class) branchUpgradeChoiceClass, "LASER_DAMAGE");
        Object laserWidth = Enum.valueOf((Class) branchUpgradeChoiceClass, "LASER_WIDTH");
        Object laserDuration = Enum.valueOf((Class) branchUpgradeChoiceClass, "LASER_DURATION");
        Object moveSpeed = Enum.valueOf((Class) branchUpgradeChoiceClass, "MOVE_SPEED");

        List<Object> choices = new ArrayList<>();
        choices.add(laserDamage);
        choices.add(laserWidth);
        choices.add(laserDuration);
        choices.add(moveSpeed);

        WorldSnapshot snapshot = new WorldSnapshot(1L);
        snapshot.setGameStarted(true);
        snapshot.setGamePhase(GamePhase.UPGRADE_SELECTION);
        snapshot.setChapterTransitionFlash(false);
        snapshot.addPlayerSnapshot(new PlayerSnapshot(
                "session-local",
                "player-local",
                200,
                300,
                900,
                66,
                false,
                3,
                "FREEZE",
                0L,
                1000,
                (List) choices,
                null,
                AircraftBranch.RED_SPEED,
                java.util.Collections.emptyList(),
                true
        ));
        game.applyWorldSnapshot(snapshot);

        assert game.isUpgradeSelectionVisible() : "Upgrade overlay should show during upgrade selection";

        Method labelMethod = Game.class.getDeclaredMethod("upgradeChoiceLabel", branchUpgradeChoiceClass);
        labelMethod.setAccessible(true);
        assert "Laser damage".equals(labelMethod.invoke(game, laserDamage))
                : "Branch upgrade overlay should render branch-local labels";
        assert "Laser width".equals(labelMethod.invoke(game, laserWidth))
                : "Branch upgrade overlay should render branch-local labels";

        game.handleLocalUpgradeChoiceByIndex(0);
        assert "LASER_DAMAGE".equals(publisher.lastUpgradeChoice)
                : "Upgrade selection should publish branch-local upgrade names";
    }

    private static final class RecordingPublisher implements ClientCommandPublisher {
        private String lastUpgradeChoice;

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
        }
    }
}
