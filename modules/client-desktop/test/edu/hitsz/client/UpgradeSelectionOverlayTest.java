package edu.hitsz.client;

import edu.hitsz.common.GamePhase;
import edu.hitsz.common.UpgradeChoice;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

import java.util.Arrays;

public class UpgradeSelectionOverlayTest {

    public static void main(String[] args) {
        Game game = new Game();
        game.setLocalSessionId("session-local");

        WorldSnapshot selectionSnapshot = new WorldSnapshot(1L);
        selectionSnapshot.setGameStarted(true);
        selectionSnapshot.setGamePhase(GamePhase.UPGRADE_SELECTION);
        selectionSnapshot.setChapterTransitionFlash(false);
        selectionSnapshot.addPlayerSnapshot(new PlayerSnapshot(
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
                Arrays.asList(UpgradeChoice.FIRE_RATE, UpgradeChoice.BULLET_POWER),
                null
        ));
        game.applyWorldSnapshot(selectionSnapshot);

        assert game.isUpgradeSelectionVisible() : "Upgrade overlay should show during upgrade selection";

        WorldSnapshot flashingSnapshot = new WorldSnapshot(2L);
        flashingSnapshot.setGameStarted(true);
        flashingSnapshot.setGamePhase(GamePhase.UPGRADE_SELECTION);
        flashingSnapshot.setChapterTransitionFlash(true);
        flashingSnapshot.addPlayerSnapshot(new PlayerSnapshot("session-local", "player-local", 200, 300, 900, 66));
        game.applyWorldSnapshot(flashingSnapshot);
        assert !game.isUpgradeSelectionVisible() : "Upgrade overlay should stay hidden during white-flash transition";

        WorldSnapshot battleSnapshot = new WorldSnapshot(3L);
        battleSnapshot.setGameStarted(true);
        battleSnapshot.setGamePhase(GamePhase.BATTLE);
        battleSnapshot.addPlayerSnapshot(new PlayerSnapshot("session-local", "player-local", 200, 300, 900, 66));
        game.applyWorldSnapshot(battleSnapshot);
        assert !game.isUpgradeSelectionVisible() : "Upgrade overlay should hide outside upgrade selection";
    }
}
