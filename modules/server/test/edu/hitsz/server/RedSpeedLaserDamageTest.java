package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.server.aircraft.BossEnemy;
import edu.hitsz.server.aircraft.EliteEnemy;

import java.lang.reflect.Method;
import java.util.Collections;

public class RedSpeedLaserDamageTest {

    public static void main(String[] args) throws Exception {
        laserDamagesEnemyInsideBeamPath();
        laserDamagesWideEnemyWhenBeamOverlapsBodyEdge();
    }

    private static void laserDamagesEnemyInsideBeamPath() throws Exception {
        ServerWorldState worldState = new ServerWorldState();
        PlayerSession session = worldState.getSessionRegistry().create("session-local", "player-local");
        PlayerRuntimeState playerState = session.getPlayerState();
        playerState.resetForNewRound(200, 700);
        playerState.openBranchSelection(Collections.singletonList(AircraftBranch.RED_SPEED));
        playerState.applyBranchChoice(AircraftBranch.RED_SPEED);

        EliteEnemy enemy = new EliteEnemy(200, 560, 0, 0, 120);
        worldState.getEnemyAircrafts().add(enemy);

        Method shootAction = ServerWorldState.class.getDeclaredMethod("shootAction", long.class);
        shootAction.setAccessible(true);
        shootAction.invoke(worldState, 0L);

        assert worldState.getHeroBullets().isEmpty()
                : "RED_SPEED normal fire should create a laser instead of hero bullets";
        assert worldState.getActiveLasers().size() == 1
                : "RED_SPEED normal fire should register one active laser";

        Method crashCheckAction = ServerWorldState.class.getDeclaredMethod("crashCheckAction", long.class);
        crashCheckAction.setAccessible(true);
        crashCheckAction.invoke(worldState, 0L);

        LaserBeamState laser = worldState.getActiveLasers().get(0);
        assert enemy.getHp() == 120 - laser.getDamage()
                : "Enemy inside the beam should lose authoritative laser damage";
    }

    private static void laserDamagesWideEnemyWhenBeamOverlapsBodyEdge() throws Exception {
        ServerWorldState worldState = new ServerWorldState();
        PlayerSession session = worldState.getSessionRegistry().create("session-local", "player-local");
        PlayerRuntimeState playerState = session.getPlayerState();
        playerState.resetForNewRound(200, 700);
        playerState.openBranchSelection(Collections.singletonList(AircraftBranch.RED_SPEED));
        playerState.applyBranchChoice(AircraftBranch.RED_SPEED);

        BossEnemy boss = new BossEnemy(340, 560, 0, 0, 240);
        worldState.getEnemyAircrafts().add(boss);

        Method shootAction = ServerWorldState.class.getDeclaredMethod("shootAction", long.class);
        shootAction.setAccessible(true);
        shootAction.invoke(worldState, 0L);

        Method crashCheckAction = ServerWorldState.class.getDeclaredMethod("crashCheckAction", long.class);
        crashCheckAction.setAccessible(true);
        crashCheckAction.invoke(worldState, 0L);

        LaserBeamState laser = worldState.getActiveLasers().get(0);
        assert boss.getHp() == 240 - laser.getDamage()
                : "Laser should damage a wide enemy when the beam overlaps its body edge";
    }
}
