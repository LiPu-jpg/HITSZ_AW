package edu.hitsz.server;

public class PlayerInitialShootReadinessTest {

    public static void main(String[] args) {
        PlayerRuntimeState state = new PlayerRuntimeState("player-local");
        assert state.shouldShootAtTick(0L)
                : "Fresh player state should be allowed to fire immediately";

        state.markShotAtTick(5L);
        state.resetForNewRound(200, 600);
        assert state.shouldShootAtTick(0L)
                : "Round reset should restore immediate firing readiness";
    }
}
