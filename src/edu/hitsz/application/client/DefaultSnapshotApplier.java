package edu.hitsz.application.client;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.aircraft.OtherPlayer;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.basic.AbstractItem;
import edu.hitsz.basic.BloodSupply;
import edu.hitsz.basic.BombSupply;
import edu.hitsz.basic.FirePlusSupply;
import edu.hitsz.basic.FireSupply;
import edu.hitsz.basic.FreezeSupply;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.application.protocol.dto.BulletSnapshot;
import edu.hitsz.application.protocol.dto.EnemySnapshot;
import edu.hitsz.application.protocol.dto.ItemSnapshot;
import edu.hitsz.application.protocol.dto.PlayerSnapshot;
import edu.hitsz.application.protocol.dto.WorldSnapshot;

public class DefaultSnapshotApplier implements SnapshotApplier {

    @Override
    public void apply(WorldSnapshot snapshot, ClientWorldState state) {
        state.getPlayerAircrafts().clear();
        state.getEnemyAircrafts().clear();
        state.getHeroBullets().clear();
        state.getEnemyBullets().clear();
        state.getItems().clear();
        state.setLocalScore(0);

        for (PlayerSnapshot playerSnapshot : snapshot.getPlayerSnapshots()) {
            if (playerSnapshot.isLocalPlayer()) {
                HeroAircraft heroAircraft = HeroAircraft.getSingleton();
                heroAircraft.setLocation(playerSnapshot.getX(), playerSnapshot.getY());
                heroAircraft.setHp(playerSnapshot.getHp());
                state.getPlayerAircrafts().add(heroAircraft);
                state.setLocalScore(playerSnapshot.getScore());
                continue;
            }
            state.getPlayerAircrafts().add(new OtherPlayer(
                    playerSnapshot.getPlayerId(),
                    playerSnapshot.getX(),
                    playerSnapshot.getY(),
                    0,
                    0,
                    playerSnapshot.getHp()
            ));
        }

        for (EnemySnapshot enemySnapshot : snapshot.getEnemySnapshots()) {
            state.getEnemyAircrafts().add(createEnemy(enemySnapshot));
        }
        for (BulletSnapshot bulletSnapshot : snapshot.getHeroBulletSnapshots()) {
            state.getHeroBullets().add(createBullet(bulletSnapshot));
        }
        for (BulletSnapshot bulletSnapshot : snapshot.getEnemyBulletSnapshots()) {
            state.getEnemyBullets().add(createBullet(bulletSnapshot));
        }
        for (ItemSnapshot itemSnapshot : snapshot.getItemSnapshots()) {
            state.getItems().add(createItem(itemSnapshot));
        }
    }

    private AbstractAircraft createEnemy(EnemySnapshot enemySnapshot) {
        String type = enemySnapshot.getType();
        if (type.endsWith("EliteEnemy")) {
            return new edu.hitsz.aircraft.EliteEnemy(enemySnapshot.getX(), enemySnapshot.getY(), 0, 0, enemySnapshot.getHp());
        }
        if (type.endsWith("ElitePlusEnemy")) {
            return new edu.hitsz.aircraft.ElitePlusEnemy(enemySnapshot.getX(), enemySnapshot.getY(), 0, 0, enemySnapshot.getHp());
        }
        if (type.endsWith("AceEnemy")) {
            return new edu.hitsz.aircraft.AceEnemy(enemySnapshot.getX(), enemySnapshot.getY(), 0, 0, enemySnapshot.getHp());
        }
        if (type.endsWith("BossEnemy")) {
            return new edu.hitsz.aircraft.BossEnemy(enemySnapshot.getX(), enemySnapshot.getY(), 0, 0, enemySnapshot.getHp());
        }
        return new MobEnemy(enemySnapshot.getX(), enemySnapshot.getY(), 0, 0, enemySnapshot.getHp());
    }

    private BaseBullet createBullet(BulletSnapshot bulletSnapshot) {
        if ("ENEMY".equals(bulletSnapshot.getType())) {
            return new EnemyBullet(bulletSnapshot.getX(), bulletSnapshot.getY(), 0, 0, 0);
        }
        return new HeroBullet(bulletSnapshot.getX(), bulletSnapshot.getY(), 0, 0, 0);
    }

    private AbstractItem createItem(ItemSnapshot itemSnapshot) {
        switch (itemSnapshot.getType()) {
            case "BLOOD":
                return new BloodSupply(itemSnapshot.getX(), itemSnapshot.getY(), 0, 0);
            case "FIRE":
                return new FireSupply(itemSnapshot.getX(), itemSnapshot.getY(), 0, 0);
            case "FIRE_PLUS":
                return new FirePlusSupply(itemSnapshot.getX(), itemSnapshot.getY(), 0, 0);
            case "BOMB":
                return new BombSupply(itemSnapshot.getX(), itemSnapshot.getY(), 0, 0);
            case "FREEZE":
                return new FreezeSupply(itemSnapshot.getX(), itemSnapshot.getY(), 0, 0);
            default:
                return new BloodSupply(itemSnapshot.getX(), itemSnapshot.getY(), 0, 0);
        }
    }
}
