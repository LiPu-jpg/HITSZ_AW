package edu.hitsz.client;

import edu.hitsz.client.aircraft.HeroAircraft;
import edu.hitsz.client.Game;
import edu.hitsz.client.ClientCommandPublisher;
import edu.hitsz.common.protocol.SnapshotTypes;
import edu.hitsz.common.protocol.dto.EnemySnapshot;
import edu.hitsz.common.protocol.dto.ItemSnapshot;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.client.basic.AbstractItem;
import edu.hitsz.client.basic.BloodSupply;
import edu.hitsz.client.basic.BombSupply;
import edu.hitsz.client.basic.FirePlusSupply;
import edu.hitsz.client.basic.FireSupply;
import edu.hitsz.client.basic.FreezeSupply;

import java.lang.reflect.Method;
import java.util.Arrays;

public class GameClientBoundaryTest {

    public static void main(String[] args) {
        ignoresLocalMovementWithoutPublisher();
        publishesMoveAndSkillInsteadOfMutatingLocalState();
        publishesUpgradeChoiceBySnapshotOrder();
        appliesSnapshotIntoRenderableState();
        heroAircraftDoesNotShootLocally();
        itemHierarchyDoesNotExposeLocalActivationHook();
    }

    private static void ignoresLocalMovementWithoutPublisher() {
        Game game = new Game();
        HeroAircraft hero = HeroAircraft.getSingleton();
        hero.setLocation(256, 700);

        game.handleLocalHeroInput(123, 456);

        assert hero.getLocationX() == 256 : "Client-only game should not locally move hero without server publisher";
        assert hero.getLocationY() == 700 : "Client-only game should not locally move hero without server publisher";
    }

    private static void publishesMoveAndSkillInsteadOfMutatingLocalState() {
        Game game = new Game();
        HeroAircraft hero = HeroAircraft.getSingleton();
        hero.setLocation(256, 700);
        RecordingPublisher publisher = new RecordingPublisher();
        game.attachCommandPublisher(publisher);

        game.handleLocalHeroInput(300, 520);
        game.handleLocalSkillInput("BOMB");

        assert publisher.movePublished : "Move input should be published to server";
        assert publisher.lastMoveX == 300 : "Move X should be forwarded unchanged";
        assert publisher.lastMoveY == 520 : "Move Y should be forwarded unchanged";
        assert "BOMB".equals(publisher.lastSkillType) : "Skill input should be published to server";
        assert hero.getLocationX() == 256 : "Published move should not mutate hero immediately on client";
        assert hero.getLocationY() == 700 : "Published move should not mutate hero immediately on client";
    }

    private static void publishesUpgradeChoiceBySnapshotOrder() {
        Game game = new Game();
        game.setLocalSessionId("session-local");
        RecordingPublisher publisher = new RecordingPublisher();
        game.attachCommandPublisher(publisher);

        WorldSnapshot snapshot = new WorldSnapshot(1L);
        snapshot.setGameStarted(true);
        snapshot.setGamePhase(edu.hitsz.common.GamePhase.UPGRADE_SELECTION);
        snapshot.setChapterTransitionFlash(false);
        snapshot.addPlayerSnapshot(new PlayerSnapshot(
                "session-local",
                "player-local",
                300,
                520,
                900,
                42,
                false,
                2,
                "FREEZE",
                0L,
                1000,
                Arrays.asList(edu.hitsz.common.UpgradeChoice.LIGHT_TRACKING, edu.hitsz.common.UpgradeChoice.BULLET_POWER),
                null
        ));

        game.applyWorldSnapshot(snapshot);
        game.handleLocalUpgradeChoiceByIndex(0);

        assert "LIGHT_TRACKING".equals(publisher.lastUpgradeChoice)
                : "Upgrade input should follow the server-provided choice order";
    }

    private static void appliesSnapshotIntoRenderableState() {
        Game game = new Game();
        game.setLocalSessionId("session-local");
        WorldSnapshot snapshot = new WorldSnapshot(3L);
        snapshot.addPlayerSnapshot(new PlayerSnapshot("session-local", "player-local", 300, 520, 880, 42));
        snapshot.addEnemySnapshot(new EnemySnapshot(SnapshotTypes.Enemy.MOB, 100, 120, 30));
        snapshot.addItemSnapshot(new ItemSnapshot(SnapshotTypes.Item.BOMB, 160, 260));

        game.applyWorldSnapshot(snapshot);

        assert HeroAircraft.getSingleton().getLocationX() == 300 : "Snapshot should drive local hero X";
        assert HeroAircraft.getSingleton().getLocationY() == 520 : "Snapshot should drive local hero Y";
        assert game.getLocalHp() == 880 : "HUD HP should come from synchronized local state";
        assert game.getPlayerAircrafts().size() == 1 : "Renderable player list should come from snapshot";
        assert game.getEnemyAircrafts().size() == 1 : "Renderable enemy list should come from snapshot";
        assert game.getItems().size() == 1 : "Renderable item list should come from snapshot";
    }

    private static void heroAircraftDoesNotShootLocally() {
        HeroAircraft hero = HeroAircraft.getSingleton();
        assert hero.shoot().isEmpty() : "Client HeroAircraft should not create local bullets";
    }

    private static void itemHierarchyDoesNotExposeLocalActivationHook() {
        assert !declaresMethod(AbstractItem.class, "activate") : "AbstractItem should not expose local activate hook";
        assert !declaresMethod(BloodSupply.class, "activate") : "BloodSupply should not expose local activate hook";
        assert !declaresMethod(FireSupply.class, "activate") : "FireSupply should not expose local activate hook";
        assert !declaresMethod(FirePlusSupply.class, "activate") : "FirePlusSupply should not expose local activate hook";
        assert !declaresMethod(BombSupply.class, "activate") : "BombSupply should not expose local activate hook";
        assert !declaresMethod(FreezeSupply.class, "activate") : "FreezeSupply should not expose local activate hook";
    }

    private static boolean declaresMethod(Class<?> type, String methodName) {
        for (Method method : type.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    private static class RecordingPublisher implements ClientCommandPublisher {
        private boolean movePublished;
        private int lastMoveX;
        private int lastMoveY;
        private String lastSkillType;
        private String createDifficulty;
        private String joinRoomCode;
        private boolean startPublished;
        private boolean lastReady;
        private String lastDifficulty;
        private String lastUpgradeChoice;

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void publishCreateRoom(String difficulty) {
            createDifficulty = difficulty;
        }

        @Override
        public void publishJoinRoom(String roomCode) {
            joinRoomCode = roomCode;
        }

        @Override
        public void publishStartGame() {
            startPublished = true;
        }

        @Override
        public void publishMove(int x, int y) {
            movePublished = true;
            lastMoveX = x;
            lastMoveY = y;
        }

        @Override
        public void publishSkill(String skillType) {
            lastSkillType = skillType;
        }

        @Override
        public void publishReady(boolean ready) {
            lastReady = ready;
        }

        @Override
        public void publishLobbyConfig(String difficulty) {
            lastDifficulty = difficulty;
        }

        @Override
        public void publishUpgradeChoice(String choice) {
            lastUpgradeChoice = choice;
        }
    }
}
