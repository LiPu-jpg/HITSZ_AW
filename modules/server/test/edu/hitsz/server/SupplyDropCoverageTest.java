package edu.hitsz.server;

import edu.hitsz.server.aircraft.AbstractAircraft;
import edu.hitsz.server.aircraft.EliteEnemy;
import edu.hitsz.server.ServerWorldState;
import edu.hitsz.server.basic.AbstractItem;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class SupplyDropCoverageTest {

    public static void main(String[] args) throws Exception {
        ServerWorldState worldState = new ServerWorldState();
        EliteEnemy eliteEnemy = new EliteEnemy(200, 100, 0, 8, 60);

        Method generateSupply = ServerWorldState.class.getDeclaredMethod(
                "generateSupply",
                AbstractAircraft.class
        );
        generateSupply.setAccessible(true);

        for (int i = 0; i < 4000; i++) {
            generateSupply.invoke(worldState, eliteEnemy);
        }

        Set<String> itemTypes = new HashSet<>();
        for (AbstractItem item : worldState.getItems()) {
            itemTypes.add(item.getClass().getSimpleName());
        }

        assert itemTypes.contains("BloodSupply") : "Elite drop table should include BloodSupply";
        assert itemTypes.contains("FireSupply") : "Elite drop table should include FireSupply";
        assert itemTypes.contains("FirePlusSupply") : "Elite drop table should include FirePlusSupply";
        assert itemTypes.contains("BombSupply") : "Elite drop table should include BombSupply";
        assert itemTypes.contains("FreezeSupply") : "Elite drop table should include FreezeSupply";
    }
}
