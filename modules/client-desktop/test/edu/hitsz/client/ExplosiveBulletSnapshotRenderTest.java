package edu.hitsz.client;

import edu.hitsz.client.bullet.ExplosiveEnemyBullet;
import edu.hitsz.client.bullet.ExplosiveHeroBullet;
import edu.hitsz.common.protocol.SnapshotTypes;
import edu.hitsz.common.protocol.dto.BulletSnapshot;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

public class ExplosiveBulletSnapshotRenderTest {

    public static void main(String[] args) {
        explosiveBulletSnapshotsResolveToExplosiveRenderObjects();
    }

    private static void explosiveBulletSnapshotsResolveToExplosiveRenderObjects() {
        WorldSnapshot snapshot = new WorldSnapshot(11L);
        snapshot.addPlayerSnapshot(new PlayerSnapshot(
                "session-local",
                "player-local",
                200,
                320,
                900,
                12,
                false,
                1,
                null
        ));
        snapshot.addHeroBulletSnapshot(new BulletSnapshot(SnapshotTypes.Bullet.HERO_EXPLOSIVE, 180, 260));
        snapshot.addEnemyBulletSnapshot(new BulletSnapshot(SnapshotTypes.Bullet.ENEMY_EXPLOSIVE, 220, 280));

        ClientWorldState state = new ClientWorldState();
        new DefaultSnapshotApplier().apply(snapshot, state, "session-local");

        assert state.getHeroBullets().size() == 1 : "Explosive hero bullet snapshot should be applied";
        assert state.getEnemyBullets().size() == 1 : "Explosive enemy bullet snapshot should be applied";
        assert state.getHeroBullets().get(0) instanceof ExplosiveHeroBullet
                : "Explosive hero bullet snapshots should render as ExplosiveHeroBullet";
        assert state.getEnemyBullets().get(0) instanceof ExplosiveEnemyBullet
                : "Explosive enemy bullet snapshots should render as ExplosiveEnemyBullet";
        assert ImageManager.get(state.getHeroBullets().get(0)) == ImageManager.EXPLOSIVE_HERO_BULLET_IMAGE
                : "Explosive hero bullets should use the explosive friendly bullet art";
        assert ImageManager.get(state.getEnemyBullets().get(0)) == ImageManager.EXPLOSIVE_ENEMY_BULLET_IMAGE
                : "Explosive enemy bullets should use the explosive hostile bullet art";
    }
}
