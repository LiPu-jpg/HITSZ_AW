package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.server.basic.AbstractFlyingObject;
import edu.hitsz.server.bullet.BaseBullet;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class GreenDefenseSpreadFireTest {

    public static void main(String[] args) throws Exception {
        greenDefenseNormalFireSpawnsSpreadBullets();
    }

    private static void greenDefenseNormalFireSpawnsSpreadBullets() throws Exception {
        ServerWorldState worldState = new ServerWorldState();
        PlayerSession session = worldState.getSessionRegistry().create("session-local", "player-local");
        PlayerRuntimeState playerState = session.getPlayerState();
        playerState.resetForNewRound(200, 700);
        playerState.openBranchSelection(Collections.singletonList(AircraftBranch.GREEN_DEFENSE));
        playerState.applyBranchChoice(AircraftBranch.GREEN_DEFENSE);

        Method shootAction = ServerWorldState.class.getDeclaredMethod("shootAction", long.class);
        shootAction.setAccessible(true);
        shootAction.invoke(worldState, 0L);

        List<BaseBullet> heroBullets = worldState.getHeroBullets();
        assert heroBullets.size() > 1 : "GREEN_DEFENSE normal fire should spawn spread bullets";

        int firstSpeedX = speedXOf(heroBullets.get(0));
        boolean hasSpeedVariance = false;
        for (BaseBullet bullet : heroBullets) {
            if (speedXOf(bullet) != firstSpeedX) {
                hasSpeedVariance = true;
                break;
            }
        }

        assert hasSpeedVariance : "GREEN_DEFENSE spread bullets should have x-speed variance";
    }

    private static int speedXOf(BaseBullet bullet) throws Exception {
        Field field = AbstractFlyingObject.class.getDeclaredField("speedX");
        field.setAccessible(true);
        return field.getInt(bullet);
    }
}
