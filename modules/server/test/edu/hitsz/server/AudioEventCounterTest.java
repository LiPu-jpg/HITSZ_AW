package edu.hitsz.server;

import edu.hitsz.server.aircraft.EliteEnemy;
import edu.hitsz.server.basic.BloodSupply;
import edu.hitsz.server.bullet.BaseBullet;

import java.lang.reflect.Method;
import java.util.List;

public class AudioEventCounterTest {

    public static void main(String[] args) throws Exception {
        bulletHitCounterIncrementsOnHeroBulletCollision();
        supplyPickupCounterIncrementsOnItemPickup();
    }

    private static void bulletHitCounterIncrementsOnHeroBulletCollision() throws Exception {
        ServerWorldState worldState = new ServerWorldState();
        PlayerSession session = worldState.getSessionRegistry().create("session-local", "player-local");
        session.getPlayerState().resetForNewRound(200, 700);

        EliteEnemy enemy = new EliteEnemy(200, 698, 0, 0, 120);
        worldState.getEnemyAircrafts().add(enemy);

        List<BaseBullet> bullets = session.getPlayerState().getAircraft().shoot(session.getSessionId());
        worldState.getHeroBullets().addAll(bullets);

        Method crashCheckAction = ServerWorldState.class.getDeclaredMethod("crashCheckAction", long.class);
        crashCheckAction.setAccessible(true);
        crashCheckAction.invoke(worldState, 0L);

        assert worldState.getBulletHitAudioCount() > 0
                : "Hero bullet collision should increase the authoritative bullet-hit audio counter";
    }

    private static void supplyPickupCounterIncrementsOnItemPickup() throws Exception {
        ServerWorldState worldState = new ServerWorldState();
        PlayerSession session = worldState.getSessionRegistry().create("session-local", "player-local");
        session.getPlayerState().setPosition(200, 500);
        worldState.getItems().add(new BloodSupply(200, 500, 0, 0));

        Method crashCheckAction = ServerWorldState.class.getDeclaredMethod("crashCheckAction", long.class);
        crashCheckAction.setAccessible(true);
        crashCheckAction.invoke(worldState, 0L);

        assert worldState.getSupplyPickupAudioCount() > 0
                : "Picking up a supply should increase the authoritative supply audio counter";
    }
}
