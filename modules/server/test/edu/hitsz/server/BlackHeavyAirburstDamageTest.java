package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.server.aircraft.EliteEnemy;

import java.lang.reflect.Method;
import java.util.Collections;

public class BlackHeavyAirburstDamageTest {

    public static void main(String[] args) throws Exception {
        blackHeavyNormalFireBurstsAndDamagesEnemiesNearTargetPoint();
    }

    private static void blackHeavyNormalFireBurstsAndDamagesEnemiesNearTargetPoint() throws Exception {
        ServerWorldState worldState = new ServerWorldState();
        PlayerSession session = worldState.getSessionRegistry().create("session-local", "player-local");
        PlayerRuntimeState playerState = session.getPlayerState();
        playerState.resetForNewRound(200, 700);
        playerState.openBranchSelection(Collections.singletonList(AircraftBranch.BLACK_HEAVY));
        playerState.applyBranchChoice(AircraftBranch.BLACK_HEAVY);

        EliteEnemy nearTargetEnemy = new EliteEnemy(200, 560, 0, 0, 120);
        EliteEnemy farEnemy = new EliteEnemy(360, 560, 0, 0, 120);
        worldState.getEnemyAircrafts().add(nearTargetEnemy);
        worldState.getEnemyAircrafts().add(farEnemy);

        Method shootAction = ServerWorldState.class.getDeclaredMethod("shootAction", long.class);
        shootAction.setAccessible(true);
        shootAction.invoke(worldState, 0L);

        assert worldState.getHeroBullets().isEmpty()
                : "BLACK_HEAVY normal fire should not use standard hero bullets";

        Method burstAction = ServerWorldState.class.getDeclaredMethod("burstAction", long.class);
        burstAction.setAccessible(true);
        burstAction.invoke(worldState, 0L);

        assert nearTargetEnemy.getHp() < 120
                : "BLACK_HEAVY burst should damage enemies near the target point";
        assert farEnemy.getHp() == 120
                : "BLACK_HEAVY burst should not damage enemies outside the burst radius";
    }
}
