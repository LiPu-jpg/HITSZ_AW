package edu.hitsz.server;

import edu.hitsz.common.Difficulty;
import edu.hitsz.server.aircraft.EliteEnemy;
import edu.hitsz.server.basic.BombSupply;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

public class BombSupplyDropDuringPickupTest {

    public static void main(String[] args) throws Exception {
        bombPickupDoesNotConcurrentModifyItemList();
    }

    private static void bombPickupDoesNotConcurrentModifyItemList() throws Exception {
        ServerWorldState worldState = new ServerWorldState();
        worldState.setDifficulty(Difficulty.NORMAL);

        PlayerSession session = worldState.getSessionRegistry().create("session-test", "player-test");
        session.getPlayerState().setPosition(100, 600);

        worldState.getItems().add(new BombSupply(100, 600, 0, 0));
        worldState.getEnemyAircrafts().add(new EliteEnemy(120, 200, 0, 0, 1));

        setFixedRandom(worldState, new Random() {
            @Override
            public double nextDouble() {
                return 0.0;
            }
        });

        worldState.stepWorld(System.currentTimeMillis());

        List<?> items = worldState.getItems();
        assert !items.isEmpty()
                : "Bomb pickup should be able to generate new dropped supplies without breaking the tick";
    }

    private static void setFixedRandom(ServerWorldState worldState, Random random) throws Exception {
        Field randomField = ServerWorldState.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(worldState, random);
    }
}
