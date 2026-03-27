package edu.hitsz.client;

import edu.hitsz.common.ChapterId;
import edu.hitsz.common.GamePhase;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

public class ChapterSnapshotApplyTest {

    public static void main(String[] args) {
        ClientWorldState state = new ClientWorldState();
        WorldSnapshot snapshot = new WorldSnapshot(9L);
        snapshot.setChapterId(ChapterId.CH2);
        snapshot.setGamePhase(GamePhase.UPGRADE_SELECTION);
        snapshot.setChapterTransitionFlash(true);

        new DefaultSnapshotApplier().apply(snapshot, state, "session-local");

        assert state.getChapterId() == ChapterId.CH2 : "Client world state should store snapshot chapter";
        assert state.getGamePhase() == GamePhase.UPGRADE_SELECTION : "Client world state should store snapshot phase";
        assert state.isChapterTransitionFlash() : "Client world state should store chapter transition flash state";

        Game game = new Game();
        game.setLocalSessionId("session-local");
        game.applyWorldSnapshot(snapshot);

        assert game.getChapterId() == ChapterId.CH2 : "Game should reflect snapshot chapter";
        assert game.getGamePhase() == GamePhase.UPGRADE_SELECTION : "Game should reflect snapshot phase";
        assert game.isChapterTransitionFlash() : "Game should reflect snapshot flash state";
    }
}
