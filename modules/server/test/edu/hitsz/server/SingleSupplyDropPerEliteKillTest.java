package edu.hitsz.server;

import edu.hitsz.server.aircraft.EliteEnemy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

public class SingleSupplyDropPerEliteKillTest {

    public static void main(String[] args) throws Exception {
        ServerWorldState worldState = new ServerWorldState();
        setDeterministicDropRandom(worldState);

        worldState.getSessionRegistry().create("session-local", "player-local");
        worldState.getEnemyAircrafts().add(new EliteEnemy(200, 100, 0, 0, 1));
        worldState.getHeroBullets().add(new ServerHeroBullet("session-local", 200, 100, 0, 0, 10));

        Method crashCheckAction = ServerWorldState.class.getDeclaredMethod("crashCheckAction", long.class);
        crashCheckAction.setAccessible(true);
        crashCheckAction.invoke(worldState, 0L);

        assert worldState.getItems().size() == 1
                : "Destroying one elite enemy should produce at most one dropped supply";
    }

    private static void setDeterministicDropRandom(ServerWorldState worldState) throws Exception {
        Field randomField = ServerWorldState.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(worldState, new Random() {
            @Override
            public double nextDouble() {
                return 0.0;
            }
        });
    }
}
