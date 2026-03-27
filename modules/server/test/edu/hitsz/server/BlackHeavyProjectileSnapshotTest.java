package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.protocol.SnapshotTypes;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.server.aircraft.EliteEnemy;

import java.lang.reflect.Method;
import java.util.Collections;

public class BlackHeavyProjectileSnapshotTest {

    public static void main(String[] args) throws Exception {
        blackHeavyShotProducesVisibleExplosiveProjectileBeforeBurst();
    }

    private static void blackHeavyShotProducesVisibleExplosiveProjectileBeforeBurst() throws Exception {
        ServerWorldState worldState = new ServerWorldState();
        PlayerSession session = worldState.getSessionRegistry().create("session-local", "player-local");
        PlayerRuntimeState playerState = session.getPlayerState();
        playerState.resetForNewRound(200, 700);
        playerState.openBranchSelection(Collections.singletonList(AircraftBranch.BLACK_HEAVY));
        playerState.applyBranchChoice(AircraftBranch.BLACK_HEAVY);
        worldState.getEnemyAircrafts().add(new EliteEnemy(200, 620, 0, 0, 120));

        Method shootAction = ServerWorldState.class.getDeclaredMethod("shootAction", long.class);
        shootAction.setAccessible(true);
        shootAction.invoke(worldState, 0L);

        WorldSnapshot snapshot = new WorldSnapshotFactory().create(worldState, 0L);
        assert snapshot.getHeroBulletSnapshots().size() == 1
                : "BLACK_HEAVY should expose an in-flight projectile snapshot before the burst resolves";
        assert SnapshotTypes.Bullet.HERO_EXPLOSIVE.equals(snapshot.getHeroBulletSnapshots().get(0).getType())
                : "BLACK_HEAVY in-flight projectile should use the explosive hero bullet snapshot type";
        assert snapshot.getExplosionSnapshots().isEmpty()
                : "An airburst should not resolve on the same frame that the projectile is spawned";
    }
}
