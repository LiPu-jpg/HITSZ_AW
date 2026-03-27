package edu.hitsz.server;

import edu.hitsz.server.aircraft.AceEnemy;
import edu.hitsz.server.aircraft.BossEnemy;
import edu.hitsz.server.aircraft.EliteEnemy;
import edu.hitsz.server.aircraft.ElitePlusEnemy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

public class EliteDropOnlyForBaseEliteTest {

    public static void main(String[] args) throws Exception {
        ServerWorldState worldState = new ServerWorldState();
        setDeterministicDropRandom(worldState);

        Method generateSupply = ServerWorldState.class.getDeclaredMethod("generateSupply", edu.hitsz.server.aircraft.AbstractAircraft.class);
        generateSupply.setAccessible(true);

        generateSupply.invoke(worldState, new EliteEnemy(100, 100, 0, 0, 1));
        int afterElite = worldState.getItems().size();
        generateSupply.invoke(worldState, new ElitePlusEnemy(100, 100, 0, 0, 1));
        generateSupply.invoke(worldState, new AceEnemy(100, 100, 0, 0, 1));
        generateSupply.invoke(worldState, new BossEnemy(100, 100, 0, 0, 1));

        assert afterElite == 1 : "Base elite should still be able to drop a supply";
        assert worldState.getItems().size() == afterElite
                : "Elite-family subtypes should not implicitly reuse the base elite drop table";
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
