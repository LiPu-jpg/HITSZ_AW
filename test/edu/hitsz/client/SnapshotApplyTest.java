package edu.hitsz.client;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.aircraft.OtherPlayer;
import edu.hitsz.application.client.ClientWorldState;
import edu.hitsz.application.client.DefaultSnapshotApplier;
import edu.hitsz.application.protocol.dto.BulletSnapshot;
import edu.hitsz.application.protocol.dto.EnemySnapshot;
import edu.hitsz.application.protocol.dto.ItemSnapshot;
import edu.hitsz.application.protocol.dto.PlayerSnapshot;
import edu.hitsz.application.protocol.dto.WorldSnapshot;

public class SnapshotApplyTest {

    public static void main(String[] args) {
        ClientWorldState state = new ClientWorldState();
        assert state.getPlayerAircrafts().isEmpty();

        WorldSnapshot snapshot = new WorldSnapshot(5L);
        snapshot.addPlayerSnapshot(new PlayerSnapshot("session-local", "player-local", true, 200, 300, 900, 66));
        snapshot.addPlayerSnapshot(new PlayerSnapshot("session-2", "player-2", false, 260, 340, 800, 10));
        snapshot.addEnemySnapshot(new EnemySnapshot(MobEnemy.class.getName(), 100, 120, 30));
        snapshot.addHeroBulletSnapshot(new BulletSnapshot("HERO", 120, 220));
        snapshot.addEnemyBulletSnapshot(new BulletSnapshot("ENEMY", 140, 240));
        snapshot.addItemSnapshot(new ItemSnapshot("BLOOD", 160, 260));

        new DefaultSnapshotApplier().apply(snapshot, state);

        assert state.getPlayerAircrafts().size() == 2 : "Snapshot should create local and remote players";
        assert state.getPlayerAircrafts().get(0) == HeroAircraft.getSingleton()
                : "Local player should use HeroAircraft singleton";
        assert state.getPlayerAircrafts().get(1) instanceof OtherPlayer
                : "Remote player should become OtherPlayer";
        assert state.getEnemyAircrafts().size() == 1 : "Enemy snapshot should create enemy render object";
        assert state.getHeroBullets().size() == 1 : "Hero bullet snapshot should create render object";
        assert state.getEnemyBullets().size() == 1 : "Enemy bullet snapshot should create render object";
        assert state.getItems().size() == 1 : "Item snapshot should create render object";
        assert state.getLocalScore() == 66 : "Local score should come from local player snapshot";
    }
}
