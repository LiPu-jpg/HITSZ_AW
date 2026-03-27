package edu.hitsz.server;

import edu.hitsz.server.aircraft.MobEnemy;
import edu.hitsz.server.PlayerSession;
import edu.hitsz.server.ServerWorldState;
import edu.hitsz.server.WorldSnapshotFactory;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.server.basic.BloodSupply;
import edu.hitsz.server.bullet.EnemyBullet;
import edu.hitsz.server.bullet.HeroBullet;

public class ServerSnapshotContentTest {

    public static void main(String[] args) {
        ServerWorldState worldState = new ServerWorldState();
        PlayerSession session = worldState.getSessionRegistry().create("session-local", "player-local");
        session.getPlayerState().setPosition(200, 300);
        session.getPlayerState().setScore(77);

        worldState.getEnemyAircrafts().add(new MobEnemy(100, 120, 0, 10, 30));
        worldState.getHeroBullets().add(new HeroBullet(120, 220, 0, -5, 30));
        worldState.getEnemyBullets().add(new EnemyBullet(140, 240, 0, 5, 20));
        worldState.getItems().add(new BloodSupply(160, 260, 0, 4));

        WorldSnapshot snapshot = new WorldSnapshotFactory().create(worldState);

        assert snapshot.getPlayerSnapshots().size() == 1 : "Player snapshot should exist";
        assert snapshot.getPlayerSnapshots().get(0).getScore() == 77 : "Player score should be serialized";
        assert snapshot.getEnemySnapshots().size() == 1 : "Enemy snapshot should exist";
        assert snapshot.getHeroBulletSnapshots().size() == 1 : "Hero bullet snapshot should exist";
        assert snapshot.getEnemyBulletSnapshots().size() == 1 : "Enemy bullet snapshot should exist";
        assert snapshot.getItemSnapshots().size() == 1 : "Item snapshot should exist";
    }
}
