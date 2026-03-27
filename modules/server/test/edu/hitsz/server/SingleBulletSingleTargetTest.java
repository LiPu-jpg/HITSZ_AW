package edu.hitsz.server;

import edu.hitsz.server.aircraft.EliteEnemy;

import java.lang.reflect.Method;

public class SingleBulletSingleTargetTest {

    public static void main(String[] args) throws Exception {
        ServerWorldState worldState = new ServerWorldState();
        PlayerSession session = worldState.getSessionRegistry().create("session-local", "player-local");
        worldState.getEnemyAircrafts().add(new EliteEnemy(200, 100, 0, 0, 10));
        worldState.getEnemyAircrafts().add(new EliteEnemy(200, 100, 0, 0, 10));
        worldState.getHeroBullets().add(new ServerHeroBullet("session-local", 200, 100, 0, 0, 10));

        Method crashCheckAction = ServerWorldState.class.getDeclaredMethod("crashCheckAction", long.class);
        crashCheckAction.setAccessible(true);
        crashCheckAction.invoke(worldState, 0L);

        int destroyedCount = 0;
        for (edu.hitsz.server.aircraft.AbstractAircraft enemyAircraft : worldState.getEnemyAircrafts()) {
            if (enemyAircraft.notValid()) {
                destroyedCount++;
            }
        }

        assert destroyedCount == 1 : "One hero bullet should destroy at most one overlapping enemy";
        assert session.getPlayerState().getScore() == GameplayBalance.ELITE_ENEMY_SCORE
                : "One bullet hit should award score for only one enemy";
    }
}
