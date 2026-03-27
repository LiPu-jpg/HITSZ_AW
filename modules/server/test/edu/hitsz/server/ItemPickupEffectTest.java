package edu.hitsz.server;

import edu.hitsz.server.aircraft.MobEnemy;
import edu.hitsz.server.PlayerSession;
import edu.hitsz.server.ServerWorldState;
import edu.hitsz.server.basic.BombSupply;
import edu.hitsz.server.basic.FirePlusSupply;
import edu.hitsz.server.basic.FireSupply;
import edu.hitsz.server.basic.FreezeSupply;
import edu.hitsz.server.bullet.BaseBullet;

import java.util.List;

public class ItemPickupEffectTest {

    public static void main(String[] args) {
        bombPickupDamagesAllEnemies();
        freezePickupAppliesWorldFreeze();
        firePickupImprovesHeroFirepower();
        firePlusPickupIsStrongerThanFirePickup();
    }

    private static void bombPickupDamagesAllEnemies() {
        ServerWorldState worldState = new ServerWorldState();
        PlayerSession session = worldState.getSessionRegistry().create("session-A", "player-A");
        session.getPlayerState().setPosition(200, 500);

        MobEnemy enemyA = new MobEnemy(100, 100, 0, 10, 120);
        MobEnemy enemyB = new MobEnemy(200, 100, 0, 10, 120);
        worldState.getEnemyAircrafts().add(enemyA);
        worldState.getEnemyAircrafts().add(enemyB);
        worldState.getItems().add(new BombSupply(200, 500, 0, 0));

        worldState.stepWorld(1000L);

        assert enemyA.getHp() < 120 : "Bomb pickup should damage all enemies";
        assert enemyB.getHp() < 120 : "Bomb pickup should damage all enemies";
    }

    private static void freezePickupAppliesWorldFreeze() {
        ServerWorldState worldState = new ServerWorldState();
        PlayerSession session = worldState.getSessionRegistry().create("session-B", "player-B");
        session.getPlayerState().setPosition(200, 500);

        MobEnemy enemy = new MobEnemy(100, 100, 0, 10, 120);
        worldState.getEnemyAircrafts().add(enemy);
        worldState.getItems().add(new FreezeSupply(200, 500, 0, 0));

        worldState.stepWorld(1000L);
        int yAfterPickupTick = enemy.getLocationY();
        worldState.stepEnemies(1500L);

        assert enemy.getLocationY() == yAfterPickupTick : "Freeze pickup should stop enemy movement during effect";
    }

    private static void firePickupImprovesHeroFirepower() {
        ServerWorldState worldState = new ServerWorldState();
        PlayerSession session = worldState.getSessionRegistry().create("session-C", "player-C");
        session.getPlayerState().setPosition(200, 500);

        List<BaseBullet> beforePickup = session.getPlayerState().getAircraft().shoot(session.getSessionId());
        worldState.getItems().add(new FireSupply(200, 500, 0, 0));
        worldState.stepWorld(1000L);
        List<BaseBullet> afterPickup = session.getPlayerState().getAircraft().shoot(session.getSessionId());

        assert afterPickup.size() > beforePickup.size()
                || afterPickup.get(0).getPower() > beforePickup.get(0).getPower()
                : "Fire pickup should improve hero firepower";
    }

    private static void firePlusPickupIsStrongerThanFirePickup() {
        ServerWorldState fireWorld = new ServerWorldState();
        PlayerSession fireSession = fireWorld.getSessionRegistry().create("session-D", "player-D");
        fireSession.getPlayerState().setPosition(200, 500);
        fireWorld.getItems().add(new FireSupply(200, 500, 0, 0));
        fireWorld.stepWorld(1000L);
        List<BaseBullet> fireBullets = fireSession.getPlayerState().getAircraft().shoot(fireSession.getSessionId());

        ServerWorldState firePlusWorld = new ServerWorldState();
        PlayerSession firePlusSession = firePlusWorld.getSessionRegistry().create("session-E", "player-E");
        firePlusSession.getPlayerState().setPosition(200, 500);
        firePlusWorld.getItems().add(new FirePlusSupply(200, 500, 0, 0));
        firePlusWorld.stepWorld(1000L);
        List<BaseBullet> firePlusBullets = firePlusSession.getPlayerState().getAircraft().shoot(firePlusSession.getSessionId());

        assert firePlusBullets.size() > fireBullets.size()
                || firePlusBullets.get(0).getPower() > fireBullets.get(0).getPower()
                : "FirePlus pickup should be stronger than Fire pickup";
    }
}
