package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

import java.util.Collections;

public class BlackHeavyExplosionDurationPersistenceTest {

    public static void main(String[] args) {
        explosionSnapshotPersistsForItsConfiguredDuration();
    }

    private static void explosionSnapshotPersistsForItsConfiguredDuration() {
        ServerWorldState worldState = new ServerWorldState();
        PlayerSession session = worldState.getSessionRegistry().create("session-local", "player-local");
        PlayerRuntimeState playerState = session.getPlayerState();
        playerState.resetForNewRound(200, 700);
        playerState.openBranchSelection(Collections.singletonList(AircraftBranch.BLACK_HEAVY));
        playerState.applyBranchChoice(AircraftBranch.BLACK_HEAVY);
        worldState.getEnemyAircrafts().add(new edu.hitsz.server.aircraft.EliteEnemy(200, 620, 0, 0, 120));

        WorldSnapshotFactory snapshotFactory = new WorldSnapshotFactory();

        int ticksToExplosion = 0;
        while (explosionCount(snapshotFactory.create(worldState)) == 0 && ticksToExplosion < 12) {
            worldState.advanceTick();
            worldState.stepWorld((long) ticksToExplosion * GameplayBalance.WORLD_TICK_INTERVAL_MILLIS);
            ticksToExplosion++;
        }

        assert explosionCount(snapshotFactory.create(worldState)) == 1
                : "Burst snapshot should appear after the projectile reaches its target";
        assert remainingDuration(snapshotFactory.create(worldState)) == GameplayBalance.BLACK_HEAVY_EXPLOSION_DURATION_TICKS
                : "Burst should expose the configured remaining duration on spawn";

        worldState.advanceTick();
        worldState.stepWorld((long) ticksToExplosion * GameplayBalance.WORLD_TICK_INTERVAL_MILLIS);
        assert explosionCount(snapshotFactory.create(worldState)) == 1
                : "Burst snapshot should persist on the next tick";
        assert remainingDuration(snapshotFactory.create(worldState)) == GameplayBalance.BLACK_HEAVY_EXPLOSION_DURATION_TICKS - 1
                : "Remaining burst duration should count down each tick";

        worldState.advanceTick();
        worldState.stepWorld((long) (ticksToExplosion + 1) * GameplayBalance.WORLD_TICK_INTERVAL_MILLIS);
        assert explosionCount(snapshotFactory.create(worldState)) == 1
                : "Burst snapshot should still persist before expiry";
        assert remainingDuration(snapshotFactory.create(worldState)) == 1
                : "Burst should still report one tick remaining just before expiry";

        worldState.advanceTick();
        worldState.stepWorld((long) (ticksToExplosion + 2) * GameplayBalance.WORLD_TICK_INTERVAL_MILLIS);
        assert explosionCount(snapshotFactory.create(worldState)) == 0
                : "Burst snapshot should disappear after the configured duration";
    }

    private static int explosionCount(WorldSnapshot snapshot) {
        return snapshot.getExplosionSnapshots().size();
    }

    private static int remainingDuration(WorldSnapshot snapshot) {
        return snapshot.getExplosionSnapshots().get(0).getDurationTicks();
    }
}
