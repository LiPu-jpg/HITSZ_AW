package edu.hitsz.application.server;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.application.protocol.dto.PlayerSnapshot;
import edu.hitsz.application.protocol.dto.EnemySnapshot;
import edu.hitsz.application.protocol.dto.BulletSnapshot;
import edu.hitsz.application.protocol.dto.ItemSnapshot;
import edu.hitsz.application.protocol.dto.WorldSnapshot;
import edu.hitsz.basic.AbstractItem;
import edu.hitsz.basic.BloodSupply;
import edu.hitsz.basic.BombSupply;
import edu.hitsz.basic.FirePlusSupply;
import edu.hitsz.basic.FireSupply;
import edu.hitsz.basic.FreezeSupply;
import edu.hitsz.bullet.BaseBullet;

public class WorldSnapshotFactory {

    public WorldSnapshot create(ServerWorldState worldState) {
        return createForSession(worldState, null);
    }

    public WorldSnapshot createForSession(ServerWorldState worldState, String receiverSessionId) {
        WorldSnapshot snapshot = new WorldSnapshot(worldState.getTick());
        for (PlayerSession session : worldState.getPlayerSessions()) {
            PlayerRuntimeState playerState = session.getPlayerState();
            snapshot.addPlayerSnapshot(new PlayerSnapshot(
                    session.getSessionId(),
                    session.getPlayerId(),
                    session.getSessionId().equals(receiverSessionId),
                    playerState.getX(),
                    playerState.getY(),
                    playerState.getHp(),
                    playerState.getScore()
            ));
        }
        for (AbstractAircraft enemyAircraft : worldState.getEnemyAircrafts()) {
            snapshot.addEnemySnapshot(new EnemySnapshot(
                    enemyAircraft.getClass().getName(),
                    enemyAircraft.getLocationX(),
                    enemyAircraft.getLocationY(),
                    enemyAircraft.getHp()
            ));
        }
        for (BaseBullet bullet : worldState.getHeroBullets()) {
            snapshot.addHeroBulletSnapshot(new BulletSnapshot(
                    "HERO",
                    bullet.getLocationX(),
                    bullet.getLocationY()
            ));
        }
        for (BaseBullet bullet : worldState.getEnemyBullets()) {
            snapshot.addEnemyBulletSnapshot(new BulletSnapshot(
                    "ENEMY",
                    bullet.getLocationX(),
                    bullet.getLocationY()
            ));
        }
        for (AbstractItem item : worldState.getItems()) {
            snapshot.addItemSnapshot(new ItemSnapshot(
                    itemTypeOf(item),
                    item.getLocationX(),
                    item.getLocationY()
            ));
        }
        return snapshot;
    }

    private String itemTypeOf(AbstractItem item) {
        if (item instanceof BloodSupply) {
            return "BLOOD";
        }
        if (item instanceof FireSupply) {
            return "FIRE";
        }
        if (item instanceof FirePlusSupply) {
            return "FIRE_PLUS";
        }
        if (item instanceof BombSupply) {
            return "BOMB";
        }
        if (item instanceof FreezeSupply) {
            return "FREEZE";
        }
        return item.getClass().getSimpleName().toUpperCase();
    }
}
