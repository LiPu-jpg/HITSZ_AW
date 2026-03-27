package edu.hitsz.client;

import edu.hitsz.client.aircraft.HeroAircraft;
import edu.hitsz.common.protocol.SnapshotTypes;
import edu.hitsz.common.protocol.dto.BulletSnapshot;
import edu.hitsz.common.protocol.dto.EnemySnapshot;
import edu.hitsz.common.protocol.dto.ItemSnapshot;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

public class UnknownSnapshotTypeFailFastTest {

    public static void main(String[] args) {
        ClientWorldState state = new ClientWorldState();
        WorldSnapshot baseline = new WorldSnapshot(0L);
        baseline.addPlayerSnapshot(new PlayerSnapshot("session-local", "player-local", 200, 300, 900, 66));
        baseline.addEnemySnapshot(new EnemySnapshot(SnapshotTypes.Enemy.MOB, 100, 120, 30));
        baseline.addHeroBulletSnapshot(new BulletSnapshot(SnapshotTypes.Bullet.HERO, 120, 220));
        baseline.addItemSnapshot(new ItemSnapshot(SnapshotTypes.Item.BLOOD, 160, 260));
        new DefaultSnapshotApplier().apply(baseline, state, "session-local");
        HeroAircraft.getSingleton().setLocation(200, 300);
        HeroAircraft.getSingleton().setHp(900);

        WorldSnapshot enemySnapshot = new WorldSnapshot(1L);
        enemySnapshot.addEnemySnapshot(new EnemySnapshot("UNKNOWN_ENEMY", 100, 120, 30));
        expectIllegalArgument(() -> new DefaultSnapshotApplier().apply(enemySnapshot, state, "session-local"));
        assert state.getEnemyAircrafts().size() == 1 : "Client state should stay unchanged after enemy type decode failure";
        assert HeroAircraft.getSingleton().getHp() == 900 : "Local hero singleton should stay unchanged after enemy type decode failure";

        WorldSnapshot itemSnapshot = new WorldSnapshot(2L);
        itemSnapshot.addItemSnapshot(new ItemSnapshot("UNKNOWN_ITEM", 160, 260));
        expectIllegalArgument(() -> new DefaultSnapshotApplier().apply(itemSnapshot, state, "session-local"));
        assert state.getItems().size() == 1 : "Client state should stay unchanged after item type decode failure";

        WorldSnapshot bulletSnapshot = new WorldSnapshot(3L);
        bulletSnapshot.addHeroBulletSnapshot(new BulletSnapshot("UNKNOWN_BULLET", 120, 220));
        expectIllegalArgument(() -> new DefaultSnapshotApplier().apply(bulletSnapshot, state, "session-local"));
        assert state.getHeroBullets().size() == 1 : "Client state should stay unchanged after bullet type decode failure";
    }

    private static void expectIllegalArgument(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Unknown snapshot types should fail fast instead of silently mapping to the wrong object");
    }
}
